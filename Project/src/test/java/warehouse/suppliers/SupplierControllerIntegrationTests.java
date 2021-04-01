package warehouse.suppliers;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import warehouse.addresses.model.AddressAddBindingModel;
import warehouse.addresses.model.AddressServiceModel;
import warehouse.suppliers.model.SupplierAddBindingModel;
import warehouse.suppliers.model.SupplierEntity;
import warehouse.suppliers.model.SupplierServiceModel;
import warehouse.suppliers.service.SupplierService;


import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class SupplierControllerIntegrationTests {

    private SupplierAddBindingModel supplierAddBindingModel;
    private AddressAddBindingModel addressAddBindingModel;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SupplierService supplierService;


    @BeforeEach
    public void setUp() {
        this.supplierAddBindingModel = this.createSupplierAddBindingModel();
        this.addressAddBindingModel = this.createAddressAddBindingModel();
    }


    @Test
    @Order(1)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testSupplierAddMethodGetContainsCorrectAttributesAndView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/suppliers/add"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("supplierAddBindingModel"))
                .andExpect(model().attributeExists("addressAddBindingModel"))
                .andExpect(model().attributeExists("supplierExists"))
                .andExpect(view().name("suppliers/supplier-add"));
        ;
    }


    @Test
    @Order(2)
    @WithMockUser(username = "user_1")
    public void testSupplierAddMethodGetAccessDeniedForNormalUser() throws Exception {
        mockMvc.perform(get("/suppliers/add")).
                andExpect(status().isForbidden());
    }


    @Test
    @Order(3)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testSupplierAddMethodPost() throws Exception {

        mockMvc.perform(
                post("/suppliers/add").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("supplierAddBindingModel", supplierAddBindingModel).
                        flashAttr("addressAddBindingModel", addressAddBindingModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/suppliers/all/pageable"));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("manager_1", "mmm"));

        SupplierServiceModel supplierServiceModel = this.supplierService.findById(7L);

        Assertions.assertEquals("Test_supplier_1", supplierServiceModel.getName());


    }


    @Test
    @Order(4)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testSupplierAddMethodPostReturnsCorrectViewWhenSupplierBindingErrorPresent() throws Exception {

        supplierAddBindingModel.setName("T");

        mockMvc.perform(
                post("/suppliers/add").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("supplierAddBindingModel", supplierAddBindingModel).
                        flashAttr("addressAddBindingModel", addressAddBindingModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:add"));
    }


    @Test
    @Order(5)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testSupplierAddMethodPostReturnsCorrectViewWhenAddressBindingErrorPresent() throws Exception {

        addressAddBindingModel.setRegion("T");

        mockMvc.perform(
                post("/suppliers/add").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("supplierAddBindingModel", supplierAddBindingModel).
                        flashAttr("addressAddBindingModel", addressAddBindingModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:add"));
    }

    @Test
    @Order(6)
    @WithMockUser(username = "user_1")
    public void testSupplierAddMethodPostAccessDeniedForNormalUser() throws Exception {

        mockMvc.perform(post("/suppliers/add")).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(7)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testGetAllMethodWithDefaultRequestParamsContainsCorrectAttributesAndView() throws Exception {

        mockMvc.perform(get("/suppliers/all/pageable")).
                andExpect(status().isOk()).
                andExpect(model().attributeExists("suppliers")).
                andExpect(model().attributeExists("result")).
                andExpect(model().attributeExists("selectedPageSize")).
                andExpect(model().attributeExists("pageSizes")).
                andExpect(model().attributeExists("pager")).
                andExpect(model().attributeExists("selectedSortOption")).
                andExpect(model().attributeExists("sortOptions")).
                andExpect(model().attributeExists("sortDirection")).
                andExpect(model().attribute("sortDirection", equalTo("asc"))).
                andExpect(model().attributeExists("reversedSortDirection")).
                andExpect(model().attributeExists("path")).
                andExpect(view().name("suppliers/supplier-all"));

    }


    @Test
    @Order(8)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testSupplierEditMethodGetContainsCorrectAttributesAndView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/suppliers/edit").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("supplierAddBindingModel"))
                .andExpect(model().attributeExists("addressAddBindingModel"))
                .andExpect(view().name("suppliers/supplier-edit"));
        ;
    }

    @Test
    @Order(9)
    @WithMockUser(username = "user_1")
    public void testSupplierEditMethodGetAccessDeniedForNormalUser() throws Exception {
        mockMvc.perform(get("/suppliers/add")).
                andExpect(status().isForbidden());
    }


    @Test
    @Order(10)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testSupplierEditMethodPost() throws Exception {


        supplierAddBindingModel.setId(1L);
        supplierAddBindingModel.setName("Supplier_1");
        supplierAddBindingModel.setEmail("supplier@mail.bg_edit");

        addressAddBindingModel.setId(1L);

        mockMvc.perform(
                patch("/suppliers/edit").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("supplierAddBindingModel", supplierAddBindingModel).
                        flashAttr("addressAddBindingModel", addressAddBindingModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/suppliers/all/pageable"));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("manager_1", "mmm"));

        SupplierServiceModel supplierServiceModel = this.supplierService.findById(1L);

        Assertions.assertEquals("supplier@mail.bg_edit", supplierServiceModel.getEmail());

    }





    @Test
    @Order(11)
    @WithMockUser(username = "user_1")
    public void testSupplierEditMethodPostAccessDeniedForNormalUser() throws Exception {
        mockMvc.perform(post("/suppliers/edit")).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(12)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testSupplierBlockMethodPostReturnsCorrectView() throws Exception {

        mockMvc.perform(
                post("/suppliers/block").
                        with(csrf()).
                        param("blockId", "1")).
                andExpect(redirectedUrl("/suppliers/all/pageable"));
    }

    @Test
    @Order(13)
    @WithMockUser(username = "user_1")
    public void testSupplierBlockMethodAccessDeniedForNormalUser() throws Exception {

        mockMvc.perform(
                post("/suppliers/block").
                        with(csrf()).
                        param("blockId", "1")).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(14)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testSupplierUnblockMethodPostReturnsCorrectView() throws Exception {

        mockMvc.perform(
                post("/suppliers/unblock").
                        with(csrf()).
                        param("unblockId", "1")).
                andExpect(redirectedUrl("/suppliers/all/pageable"));
    }

    @Test
    @Order(15)
    @WithMockUser(username = "user_1")
    public void testSupplierUnblockMethodAccessDeniedForNormalUser() throws Exception {

        mockMvc.perform(
                post("/suppliers/unblock").
                        with(csrf()).
                        param("unblockId", "1")).
                andExpect(status().isForbidden());
    }


    private SupplierAddBindingModel createSupplierAddBindingModel() {
        SupplierAddBindingModel supplierAddBindingModel = new SupplierAddBindingModel();
        supplierAddBindingModel.setName("Test_supplier_1");
        supplierAddBindingModel.setEmail("supplier@mail.bg");
        return supplierAddBindingModel;
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
