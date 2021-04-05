package warehouse.customers;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import warehouse.addresses.model.AddressAddBindingModel;
import warehouse.customers.model.CustomerAddBindingModel;
import warehouse.customers.model.CustomerEntity;
import warehouse.customers.model.CustomerServiceModel;
import warehouse.customers.service.CustomerService;
import warehouse.suppliers.model.SupplierAddBindingModel;
import warehouse.suppliers.model.SupplierEntity;
import warehouse.suppliers.model.SupplierServiceModel;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CustomerControllerIntegrationTests {

    private CustomerAddBindingModel customerAddBindingModel;
    private AddressAddBindingModel addressAddBindingModel;

    @Autowired
    MockMvc mockMvc;
    @Autowired
    CustomerService customerService;

    @BeforeEach
    public void setUp(){
        this.customerAddBindingModel = this.createCustomerAddBindingModel();
        this.addressAddBindingModel = this.createAddressAddBindingModel();
    }

    @Test
    @Order(1)
    @WithMockUser(username = "manager_1", password = "mmm", roles = {"USER", "MANAGER"})
    public void testGetAllMethodWithDefaultRequestParamsContainsCorrectAttributesAndView() throws Exception {

        mockMvc.perform(get("/customers/all/pageable")).
                andExpect(status().isOk()).
                andExpect(model().attributeExists("customers")).
                andExpect(model().attributeExists("result")).
                andExpect(model().attributeExists("selectedPageSize")).
                andExpect(model().attribute("selectedPageSize", equalTo(5))).
                andExpect(model().attributeExists("pageSizes")).
                andExpect(model().attributeExists("pager")).
                andExpect(model().attributeExists("selectedSortOption")).
                andExpect(model().attribute("selectedSortOption", equalTo("Company"))).
                andExpect(model().attributeExists("sortOptions")).
                andExpect(model().attributeExists("sortDirection")).
                andExpect(model().attribute("sortDirection", equalTo("asc"))).
                andExpect(model().attributeExists("reversedSortDirection")).
                andExpect(model().attribute("reversedSortDirection", equalTo("desc"))).
                andExpect(model().attributeExists("path")).
                andExpect(model().attribute("path", equalTo("/customers/all/pageable"))).
                andExpect(view().name("customers/customer-all"));
    }

    @Test
    @Order(2)
    @WithMockUser(username = "manager_1", password = "mmm", roles = {"USER", "MANAGER"})
    public void testGetAllNewOrderMethodWithDefaultRequestParamsContainsCorrectAttributesAndView() throws Exception {

        mockMvc.perform(get("/customers/all/newOrder")).
                andExpect(status().isOk()).
                andExpect(model().attributeExists("customers")).
                andExpect(model().attributeExists("result")).
                andExpect(model().attributeExists("selectedPageSize")).
                andExpect(model().attribute("selectedPageSize", equalTo(5))).
                andExpect(model().attributeExists("pageSizes")).
                andExpect(model().attributeExists("pager")).
                andExpect(model().attributeExists("selectedSortOption")).
                andExpect(model().attribute("selectedSortOption", equalTo("Company"))).
                andExpect(model().attributeExists("sortOptions")).
                andExpect(model().attributeExists("sortDirection")).
                andExpect(model().attribute("sortDirection", equalTo("asc"))).
                andExpect(model().attributeExists("reversedSortDirection")).
                andExpect(model().attribute("reversedSortDirection", equalTo("desc"))).
                andExpect(model().attributeExists("path")).
                andExpect(model().attribute("path", equalTo("/customers/all/newOrder"))).
                andExpect(view().name("customers/customer-all-new-order"));
    }

    @Test
    @Order(3)
    @WithMockUser(username = "manager_1", roles = {"USER", "MANAGER"})
    public void testCustomerAddMethodGetContainsCorrectAttributesAndView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/customers/add"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("customerAddBindingModel"))
                .andExpect(model().attribute("customerAddBindingModel", hasProperty("companyName", is(nullValue()))))
                .andExpect(model().attributeExists("addressAddBindingModel"))
                .andExpect(model().attribute("addressAddBindingModel", hasProperty("region", is(nullValue()))))
                .andExpect(model().attributeExists("customerExist"))
                .andExpect(view().name("customers/customer-add"));
        ;
    }

    @Test
    @Order(4)
    @WithMockUser(username = "user_1")
    public void testCustomerAddMethodGetAccessDeniedForNormalUser() throws Exception {
        mockMvc.perform(get("/customers/add")).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(5)
    @WithMockUser(username = "manager_1", roles = {"USER", "MANAGER"})
    public void testSupplierAddMethodPost() throws Exception {

        mockMvc.perform(
                post("/customers/add").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("customerAddBindingModel", customerAddBindingModel).
                        flashAttr("addressAddBindingModel", addressAddBindingModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/customers/all/pageable"));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("manager_1", "mmm"));

        CustomerServiceModel customerServiceModel = this.customerService.findById(7L);

        Assertions.assertEquals("Test_customer_1", customerServiceModel.getCompanyName());
        Assertions.assertEquals(7L, customerServiceModel.getAddress().getId());
    }



    @Test
    @Order(6)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testCustomerAddMethodPostReturnsCorrectViewWhenCustomerBindingErrorPresent() throws Exception {

        customerAddBindingModel.setCompanyName("T");

        mockMvc.perform(
                post("/customers/add").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("customerAddBindingModel", customerAddBindingModel).
                        flashAttr("addressAddBindingModel", addressAddBindingModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:add"));
    }

    @Test
    @Order(7)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testCustomerAddMethodPostReturnsCorrectViewWhenAddressBindingErrorPresent() throws Exception {

        addressAddBindingModel.setRegion("T");

        mockMvc.perform(
                post("/customers/add").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("customerAddBindingModel", customerAddBindingModel).
                        flashAttr("addressAddBindingModel", addressAddBindingModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:add"));
    }


    @Test
    @Order(8)
    @WithMockUser(username = "user_1")
    public void testCustomerAddMethodPostAccessDeniedForNormalUser() throws Exception {

        mockMvc.perform(post("/customers/add")).
                andExpect(status().isForbidden());
    }



    @Test
    @Order(9)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testCustomerEditMethodGetContainsCorrectAttributesAndView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/customers/edit").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("customerAddBindingModel"))
                .andExpect(model().attribute("customerAddBindingModel", hasProperty("companyName", is("Company_Name_1"))))
                .andExpect(model().attributeExists("addressAddBindingModel"))
                .andExpect(model().attribute("addressAddBindingModel", hasProperty("region", is("Region_1"))))
                .andExpect(view().name("customers/customer-edit"));
        ;
    }

    @Test
    @Order(10)
    @WithMockUser(username = "user_1")
    public void testCustomerEditMethodGetAccessDeniedForNormalUser() throws Exception {

        mockMvc.perform(get("/customers/edit").param("id", "1")).
                andExpect(status().isForbidden());
    }


    @Test
    @Order(11)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testCustomerEditMethodPost() throws Exception {


        customerAddBindingModel.setId(1L);
        customerAddBindingModel.setCompanyName("Test_customer_11");

        addressAddBindingModel.setId(1L);

        mockMvc.perform(
                patch("/customers/edit").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("customerAddBindingModel", customerAddBindingModel).
                        flashAttr("addressAddBindingModel", addressAddBindingModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/customers/all/pageable"));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("manager_1", "mmm"));

        CustomerServiceModel customerServiceModel = this.customerService.findById(1L);

        Assertions.assertEquals("Test_customer_11", customerServiceModel.getCompanyName());
        Assertions.assertEquals(1L, customerServiceModel.getAddress().getId());
        Assertions.assertEquals("Sofia", customerServiceModel.getAddress().getCity());
    }

    @Test
    @Order(12)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testCustomerEditMethodPostReturnsCorrectViewWhenBindingErrorPresent() throws Exception {

        customerAddBindingModel.setId(1L);
        customerAddBindingModel.setCompanyName("T");

        addressAddBindingModel.setId(1L);

        mockMvc.perform(
                patch("/customers/edit").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("customerAddBindingModel", customerAddBindingModel).
                        flashAttr("addressAddBindingModel", addressAddBindingModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:edit"));
    }


    @Test
    @Order(13)
    @WithMockUser(username = "user_1")
    public void testCustomerEditMethodPostAccessDeniedForNormalUser() throws Exception {

        mockMvc.perform(post("/customers/edit")).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(14)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testCustomerBlockMethodPostReturnsCorrectView() throws Exception {

        mockMvc.perform(
                post("/customers/block").
                        with(csrf()).
                        param("blockId", "1")).
                andExpect(redirectedUrl("/customers/all/pageable"));
    }

    @Test
    @Order(15)
    @WithMockUser(username = "user_1")
    public void testCustomerBlockMethodAccessDeniedForNormalUser() throws Exception {

        mockMvc.perform(
                post("/customers/block").
                        with(csrf()).
                        param("blockId", "1")).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(16)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testCustomerUnblockMethodPostReturnsCorrectView() throws Exception {

        mockMvc.perform(
                post("/customers/unblock").
                        with(csrf()).
                        param("unblockId", "1")).
                andExpect(redirectedUrl("/customers/all/pageable"));
    }

    @Test
    @Order(17)
    @WithMockUser(username = "user_1")
    public void testCustomerUnblockMethodAccessDeniedForNormalUser() throws Exception {

        mockMvc.perform(
                post("/customers/unblock").
                        with(csrf()).
                        param("unblockId", "1")).
                andExpect(status().isForbidden());
    }


    private CustomerAddBindingModel createCustomerAddBindingModel() {
        CustomerAddBindingModel customerAddBindingModel = new CustomerAddBindingModel();
        customerAddBindingModel.setCompanyName("Test_customer_1");
        customerAddBindingModel.setPersonName("personName");
        customerAddBindingModel.setEmail("supplier@mail.bg");
        return customerAddBindingModel;
    }

    private AddressAddBindingModel createAddressAddBindingModel() {
        AddressAddBindingModel addressAddBindingModel = new AddressAddBindingModel();
        addressAddBindingModel.setRegion("Sofia city");
        addressAddBindingModel.setCity("Sofia");
        addressAddBindingModel.setStreet("Tintyava 15");
        addressAddBindingModel.setPhone("02111222");
        return addressAddBindingModel;
    }
}
