package warehouse.categories;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import warehouse.categories.model.CategoryAddBindingModel;
import warehouse.categories.model.CategoryServiceModel;
import warehouse.categories.service.CategoryService;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CategoryControllerIntegrationTests {

    private CategoryAddBindingModel categoryAddBindingModel;

    @Autowired
    MockMvc mockMvc;
    @Autowired
    CategoryService categoryService;

    @BeforeEach
    public void setUp(){
        this.categoryAddBindingModel = this.createCategoryAddBindingModel();
    }


    @Test
    @Order(1)
    @WithMockUser(username = "manager_1", password = "mmm", roles = {"USER", "MANAGER"})
    public void testGetAllMethodWithDefaultRequestParamsContainsCorrectAttributesAndView() throws Exception {

        mockMvc.perform(get("/categories/all/pageable")).
                andExpect(status().isOk()).
                andExpect(model().attributeExists("categories")).
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
                andExpect(model().attribute("path", equalTo("/categories/all/pageable"))).
                andExpect(view().name("categories/category-all"));
    }

    @Test
    @Order(2)
    @WithMockUser(username = "manager_1", roles = {"USER", "MANAGER"})
    public void testCategoryAddMethodGetContainsCorrectAttributesAndView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/categories/add"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("categoryAddBindingModel"))
                .andExpect(model().attribute("categoryAddBindingModel", hasProperty("name", is(nullValue()))))
                .andExpect(model().attributeExists("categoryExists"))
                .andExpect(view().name("categories/category-add"));
        ;
    }

    @Test
    @Order(3)
    @WithMockUser(username = "user_1")
    public void testCategoryAddMethodGetAccessDeniedForNormalUser() throws Exception {

        mockMvc.perform(get("/categories/add")).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(4)
    @WithMockUser(username = "manager_1", roles = {"USER", "MANAGER"})
    public void testCategoryAddMethodPost() throws Exception {


        mockMvc.perform(
                post("/categories/add").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("categoryAddBindingModel", categoryAddBindingModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/categories/all/pageable"));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("manager_1", "mmm"));

        CategoryServiceModel categoryServiceModel = this.categoryService.findById(7L);

        Assertions.assertEquals("Test_category_1", categoryServiceModel.getName());
    }


    @Test
    @Order(5)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testCategoryAddMethodPostReturnsCorrectViewWhenCategoryBindingErrorPresent() throws Exception {

        categoryAddBindingModel.setName("T");

        mockMvc.perform(
                post("/categories/add").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("categoryAddBindingModel", categoryAddBindingModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:add"));
    }


    @Test
    @Order(6)
    @WithMockUser(username = "user_1")
    public void testCategoryAddMethodPostAccessDeniedForNormalUser() throws Exception {

        mockMvc.perform(post("/categories/add")).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(7)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testCategoryEditMethodGetContainsCorrectAttributesAndView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/categories/edit").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("categoryAddBindingModel"))
                .andExpect(model().attribute("categoryAddBindingModel", hasProperty("name", is("desctop"))))
                .andExpect(view().name("categories/category-edit"));
        ;
    }

    @Test
    @Order(8)
    @WithMockUser(username = "user_1")
    public void testCategoryEditMethodGetAccessDeniedForNormalUser() throws Exception {

        mockMvc.perform(get("/categories/edit").param("id", "1")).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(9)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testCategoryEditMethodPost() throws Exception {


        categoryAddBindingModel.setId(1L);
        categoryAddBindingModel.setName("Test_category_11");

        mockMvc.perform(
                patch("/categories/edit").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("categoryAddBindingModel", categoryAddBindingModel))

                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/categories/all/pageable"));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("manager_1", "mmm"));

        CategoryServiceModel categoryServiceModel = this.categoryService.findById(1L);

        Assertions.assertEquals("Test_category_11", categoryServiceModel.getName());
    }


    @Test
    @Order(10)
    @WithMockUser(username = "user_1")
    public void testCategoryEditMethodPostAccessDeniedForNormalUser() throws Exception {

        mockMvc.perform(post("/categories/edit").param("id", "1")).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(11)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testCategoryBlockMethodPostReturnsCorrectView() throws Exception {

        mockMvc.perform(
                post("/categories/block").
                        with(csrf()).
                        param("blockId", "1")).
                andExpect(redirectedUrl("/categories/all/pageable"));
    }

    @Test
    @Order(12)
    @WithMockUser(username = "user_1")
    public void testCategoryBlockMethodAccessDeniedForNormalUser() throws Exception {

        mockMvc.perform(
                post("/categories/block").
                        with(csrf()).
                        param("blockId", "1")).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(13)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testCategoryUnblockMethodPostReturnsCorrectView() throws Exception {

        mockMvc.perform(
                post("/categories/unblock").
                        with(csrf()).
                        param("unblockId", "1")).
                andExpect(redirectedUrl("/categories/all/pageable"));
    }

    @Test
    @Order(14)
    @WithMockUser(username = "user_1")
    public void testCategoryUnblockMethodAccessDeniedForNormalUser() throws Exception {

        mockMvc.perform(
                post("/categories/unblock").
                        with(csrf()).
                        param("unblockId", "1")).
                andExpect(status().isForbidden());
    }



    private CategoryAddBindingModel createCategoryAddBindingModel() {

        CategoryAddBindingModel categoryAddBindingModel = new CategoryAddBindingModel();
        categoryAddBindingModel.setName("Test_category_1");
        categoryAddBindingModel.setDescription("description_1");

        return categoryAddBindingModel;
    }


}
