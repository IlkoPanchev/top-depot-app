package warehouse.items;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;
import warehouse.addresses.model.AddressAddBindingModel;
import warehouse.cloudinary.CloudinaryService;
import warehouse.customers.model.CustomerAddBindingModel;
import warehouse.customers.model.CustomerServiceModel;
import warehouse.customers.service.CustomerService;
import warehouse.items.model.ItemAddBindingModel;
import warehouse.items.model.ItemViewServiceModel;
import warehouse.items.service.ItemService;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static warehouse.constants.GlobalConstants.PAGE_SIZES;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class ItemControllerIntegrationTest {

    String img = "http://res.cloudinary.com/ipanchev/image/upload/v1616226988/id08tagytlyglzz84nyb.jpg";

    private ItemAddBindingModel itemAddBindingModel;

    @MockBean
    private CloudinaryService mockCloudinaryService;

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ItemService itemService;


    @BeforeEach
    public void setUp(){
        this.itemAddBindingModel = this.createItemAddBindingModel();
    }

    @Test
    @Order(1)
    @WithMockUser(username = "manager_1", password = "mmm", roles = {"USER", "MANAGER"})
    public void testGetAllMethodWithDefaultRequestParamsContainsCorrectAttributesAndView() throws Exception {

        mockMvc.perform(get("/items/all/pageable")).
                andExpect(status().isOk()).
                andExpect(model().attributeExists("items")).
                andExpect(model().attributeExists("result")).
                andExpect(model().attributeExists("selectedPageSize")).
                andExpect(model().attribute("selectedPageSize", equalTo(5))).
                andExpect(model().attributeExists("pageSizes")).
                andExpect(model().attributeExists("pager")).
                andExpect(model().attributeExists("selectedSortOption")).
                andExpect(model().attribute("selectedSortOption", equalTo("Name"))).
                andExpect(model().attributeExists("sortOptions")).
                andExpect(model().attributeExists("sortDirection")).
                andExpect(model().attribute("sortDirection", equalTo("asc"))).
                andExpect(model().attributeExists("reversedSortDirection")).
                andExpect(model().attribute("reversedSortDirection", equalTo("desc"))).
                andExpect(model().attributeExists("path")).
                andExpect(model().attribute("path", equalTo("/items/all/pageable"))).
                andExpect(view().name("items/item-all"));

    }

    @Test
    @Order(2)
    @WithMockUser(username = "manager_1", roles = {"USER", "MANAGER"})
    public void testItemAddMethodGetContainsCorrectAttributesAndView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/items/add"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("itemAddBindingModel"))
                .andExpect(model().attribute("itemAddBindingModel", hasProperty("name", is(nullValue()))))
                .andExpect(model().attributeExists("suppliers"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("itemExists"))
                .andExpect(view().name("items/item-add"));
        ;
    }

    @Test
    @Order(3)
    @WithMockUser(username = "user_1")
    public void testItemAddMethodGetAccessDeniedForNormalUser() throws Exception {

        mockMvc.perform(get("/items/add")).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(4)
    @WithMockUser(username = "manager_1", roles = {"USER", "MANAGER"})
    public void testItemAddConfirmMethodPost() throws Exception {

        when(this.mockCloudinaryService.uploadImage(any(MultipartFile.class))).thenReturn(img);

        mockMvc.perform(
                post("/items/add").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("itemAddBindingModel", itemAddBindingModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/items/all/pageable"));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("manager_1", "mmm"));

        ItemViewServiceModel itemViewServiceModel = this.itemService.findById(7L);

        Assertions.assertEquals("Test_item_1", itemViewServiceModel.getName());
    }



    @Test
    @Order(5)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testItemAddConfirmMethodPostReturnsCorrectViewWhenItemBindingErrorPresent() throws Exception {


        itemAddBindingModel.setName("T");

        mockMvc.perform(
                post("/items/add").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("itemAddBindingModel", itemAddBindingModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:add"));
    }

    @Test
    @Order(6)
    @WithMockUser(username = "manager_1", password = "mmm", roles = {"USER", "MANAGER"})
    public void testEditOrderMethodWithDefaultRequestParamsContainsCorrectAttributesAndView() throws Exception {

        mockMvc.perform(get("/items/all/pageable/editOrder")).
                andExpect(status().isOk()).
                andExpect(model().attributeExists("items")).
                andExpect(model().attributeExists("result")).
                andExpect(model().attributeExists("selectedPageSize")).
                andExpect(model().attribute("selectedPageSize", equalTo(5))).
                andExpect(model().attributeExists("pageSizes")).
                andExpect(model().attributeExists("pager")).
                andExpect(model().attributeExists("selectedSortOption")).
                andExpect(model().attribute("selectedSortOption", equalTo("Name"))).
                andExpect(model().attributeExists("sortOptions")).
                andExpect(model().attributeExists("sortDirection")).
                andExpect(model().attribute("sortDirection", equalTo("asc"))).
                andExpect(model().attributeExists("reversedSortDirection")).
                andExpect(model().attribute("reversedSortDirection", equalTo("desc"))).
                andExpect(model().attributeExists("path")).
                andExpect(model().attribute("path", equalTo("/items/all/pageable/editOrder"))).
                andExpect(view().name("items/item-all-order-edit"));

    }

    @Test
    @Order(7)
    @WithMockUser(username = "manager_1", password = "mmm", roles = {"USER", "MANAGER"})
    public void testOrderAddItemMethodWithDefaultRequestParamsContainsCorrectAttributesAndView() throws Exception {

        mockMvc.perform(get("/items/all/pageable/orderAddItem")).
                andExpect(status().isOk()).
                andExpect(model().attributeExists("items")).
                andExpect(model().attributeExists("result")).
                andExpect(model().attributeExists("selectedPageSize")).
                andExpect(model().attribute("selectedPageSize", equalTo(5))).
                andExpect(model().attributeExists("pageSizes")).
                andExpect(model().attributeExists("pager")).
                andExpect(model().attributeExists("selectedSortOption")).
                andExpect(model().attribute("selectedSortOption", equalTo("Name"))).
                andExpect(model().attributeExists("sortOptions")).
                andExpect(model().attributeExists("sortDirection")).
                andExpect(model().attribute("sortDirection", equalTo("asc"))).
                andExpect(model().attributeExists("reversedSortDirection")).
                andExpect(model().attribute("reversedSortDirection", equalTo("desc"))).
                andExpect(model().attributeExists("path")).
                andExpect(model().attribute("path", equalTo("/items/all/pageable/orderAddItem"))).
                andExpect(view().name("items/item-all-order-add-item"));

    }

    @Test
    @Order(8)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testEditMethodGetContainsCorrectAttributesAndView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/items/edit").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("itemAddBindingModel"))
                .andExpect(model().attribute("itemAddBindingModel", hasProperty("name", is("Name_1"))))
                .andExpect(model().attributeExists("suppliers"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(view().name("items/item-edit"));
        ;
    }



    @Test
    @Order(9)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testItemEditConfirmMethodPostReturnsCorrectViewWhenBindingErrorPresent() throws Exception {


        itemAddBindingModel.setId(1L);
        itemAddBindingModel.setName("T");

        mockMvc.perform(
                patch("/items/edit").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("itemAddBindingModel", itemAddBindingModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:edit"));
    }

    @Test
    @Order(10)
    @WithMockUser(username = "manager", password = "mmm", roles = {"USER", "MANAGER"})
    public void testItemEditConfirmMethod() throws Exception {

        itemAddBindingModel.setId(1L);
        itemAddBindingModel.setName("Name_1");
        itemAddBindingModel.setDescription("description_edit");
        itemAddBindingModel.setPrice(new BigDecimal(1000));

        when(this.mockCloudinaryService.uploadImage(any(MultipartFile.class))).thenReturn(img);

        mockMvc.perform(
                patch("/items/edit").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("itemAddBindingModel", itemAddBindingModel));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("manager_1", "mmm"));

        ItemViewServiceModel itemViewServiceModel = this.itemService.findById(1L);

        Assertions.assertEquals("description_edit", itemViewServiceModel.getDescription());
        Assertions.assertEquals(new BigDecimal("1000.00"), itemViewServiceModel.getPrice());

    }

    @Test
    @Order(11)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testItemBlockMethodPostReturnsCorrectView() throws Exception {

        mockMvc.perform(
                post("/items/block").
                        with(csrf()).
                        param("blockId", "1")).
                andExpect(redirectedUrl("/items/all/pageable"));


    }

    @Test
    @Order(12)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testItemBlockMethodPostServiceIntegration() throws Exception {

        mockMvc.perform(
                post("/items/block").
                        with(csrf()).
                        param("blockId", "1")).
                andExpect(redirectedUrl("/items/all/pageable"));

        ItemViewServiceModel itemViewServiceModel = this.itemService.findById(1L);

        Assertions.assertTrue(itemViewServiceModel.isBlocked());
    }

    @Test
    @Order(13)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testItemUnblockMethodPostReturnsCorrectView() throws Exception {

        mockMvc.perform(
                post("/items/unblock").
                        with(csrf()).
                        param("unblockId", "1")).
                andExpect(redirectedUrl("/items/all/pageable"));
    }

    @Test
    @Order(14)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testItemUnblockMethodPostServiceIntegration() throws Exception {

        mockMvc.perform(
                post("/items/unblock").
                        with(csrf()).
                        param("unblockId", "1"));

        ItemViewServiceModel itemViewServiceModel = this.itemService.findById(1L);

        Assertions.assertFalse(itemViewServiceModel.isBlocked());
    }



    private ItemAddBindingModel createItemAddBindingModel() {

        ItemAddBindingModel itemAddBindingModel = new ItemAddBindingModel();
        itemAddBindingModel.setName("Test_item_1");
        itemAddBindingModel.setDescription("description_1");
        itemAddBindingModel.setImg(this.createMultipartFile());
        itemAddBindingModel.setLocation("location_1");
        itemAddBindingModel.setPrice(new BigDecimal(100));
        itemAddBindingModel.setCategory("laptops");
        itemAddBindingModel.setSupplier("Supplier_1");
        return itemAddBindingModel;
    }

    private MockMultipartFile createMultipartFile(){
        MockMultipartFile file
                = new MockMultipartFile(
                "file",
                "hello.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes()
        );
        return file;
    }

}
