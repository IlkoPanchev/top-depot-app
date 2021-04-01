package warehouse.users;

import org.aspectj.lang.annotation.Before;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import warehouse.addresses.model.AddressAddBindingModel;
import warehouse.departments.model.DepartmentEntity;
import warehouse.departments.model.DepartmentName;
import warehouse.departments.model.DepartmentServiceModel;
import warehouse.suppliers.model.SupplierAddBindingModel;
import warehouse.suppliers.model.SupplierEntity;
import warehouse.suppliers.model.SupplierServiceModel;
import warehouse.suppliers.service.SupplierService;
import warehouse.users.model.*;
import warehouse.users.service.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class UserControllerIntegrationTests {

    private UserRegisterBindingModel userRegisterBindingModel;


    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;

    @BeforeEach
    public void setUp() {
        this.userRegisterBindingModel = this.createUserRegisterBindingModel();
    }

    @Test
    @Order(1)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER", "ADMIN"})
    public void testUserRegisterMethodGetContainsCorrectAttributesAndView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/users/register"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userRegisterBindingModel"))
                .andExpect(model().attributeExists("confirmPasswordCorrect"))
                .andExpect(model().attributeExists("userExists"))
                .andExpect(view().name("users/user-register"));
        ;
    }

    @Test
    @Order(2)
    @WithMockUser(username = "user_1", roles = {"USER", "MANAGER"})
    public void testUserRegisterMethodGetAccessDeniedIfNotAdmin() throws Exception {

        mockMvc.perform(get("/users/register")).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER", "ADMIN"})
    public void testUserRegisterConfirmMethodPost() throws Exception {


        mockMvc.perform(
                post("/users/register").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("userRegisterBindingModel", userRegisterBindingModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/users/all/pageable"));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "aaa"));

        UserServiceModel userServiceModel = this.userService.findUserById(7L);

        Assertions.assertEquals(userRegisterBindingModel.getUsername(), userServiceModel.getUsername());

    }


    @Test
    @Order(4)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER", "ADMIN"})
    public void testUserRegisterConfirmMethodPostReturnsCorrectViewWhenUserBindingErrorPresent() throws Exception {

        userRegisterBindingModel.setUsername("u");

        mockMvc.perform(
                post("/users/register").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("userRegisterBindingModel", userRegisterBindingModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:register"));
    }

    @Test
    @Order(5)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER", "ADMIN"})
    public void testUserRegisterConfirmMethodPostReturnsCorrectViewWhenPasswordDoesntMatch() throws Exception {


        userRegisterBindingModel.setConfirmPassword("pasword");

        mockMvc.perform(
                post("/users/register").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("userRegisterBindingModel", userRegisterBindingModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:register"));
    }

    @Test
    @Order(6)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER", "ADMIN"})
    public void testUserRegisterConfirmMethodPostReturnsCorrectViewWhenUsernameExists() throws Exception {


        userRegisterBindingModel.setUsername("admin");

        mockMvc.perform(
                post("/users/register").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("userRegisterBindingModel", userRegisterBindingModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:register"));
    }

    @Test
    @Order(7)
    @WithMockUser(username = "user_1", roles = {"USER", "MANAGER"})
    public void testUserRegisterConfirmMethodPostAccessDeniedIfNotAdmin() throws Exception {

        mockMvc.perform(get("/users/register")).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(8)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER", "ADMIN"})
    public void testGetAllMethodWithDefaultRequestParamsContainsCorrectAttributesAndView() throws Exception {

        mockMvc.perform(get("/users/all/pageable")).
                andExpect(status().isOk()).
                andExpect(model().attributeExists("users")).
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
                andExpect(view().name("users/user-all"));

    }

    @Test
    @Order(9)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER", "ADMIN"})
    public void testUserEditMethodGetContainsCorrectAttributesAndView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/users/edit").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userRegisterBindingModel"))
                .andExpect(model().attributeExists("confirmPasswordCorrect"))
                .andExpect(view().name("users/user-edit"));
        ;
    }

    @Test
    @Order(10)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testUserEditMethodGetAccessDeniedIfNotAdmin() throws Exception {
        mockMvc.perform(get("/users/edit").param("id", "1")).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(11)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER", "ADMIN"})
    public void testUserEditMethodPost() throws Exception {

        userRegisterBindingModel.setId(1L);
        userRegisterBindingModel.setUsername("admin");
        userRegisterBindingModel.setEmail("test_email_2@email.comedit");

        mockMvc.perform(
                patch("/users/edit").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("userRegisterBindingModel", userRegisterBindingModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/users/all/pageable"));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "aaa"));

        UserServiceModel userServiceModel = this.userService.findUserById(1L);

        Assertions.assertEquals("test_email_2@email.comedit", userServiceModel.getEmail());

    }

    @Test
    @Order(12)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER", "ADMIN"})
    public void testUserEditMethodPostReturnsCorrectViewWhenBindingErrorPresent() throws Exception {

        userRegisterBindingModel.setId(1L);
        userRegisterBindingModel.setUsername("u");

        mockMvc.perform(
                patch("/users/edit").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("userRegisterBindingModel", userRegisterBindingModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:edit"));

    }


    @Test
    @Order(13)
    @WithMockUser(username = "manager", password = "mmm", roles = {"USER", "MANAGER"})
    public void testSupplierEditMethodPostAccessDeniedIfNotAdmin() throws Exception {
        mockMvc.perform(post("/users/edit")).
                andExpect(status().isForbidden());
    }


    @Test
    @Order(14)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testUserBlockMethodAccessDeniedIfNotAdmin() throws Exception {

        mockMvc.perform(
                post("/users/block").
                        with(csrf()).
                        param("blockId", "1")).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(15)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER", "ADMIN"})
    public void testUserBlockMethodPost() throws Exception {

        mockMvc.perform(
                post("/users/block").
                        with(csrf()).
                        param("blockId", "2"));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "aaa"));

        UserServiceModel userServiceModel = this.userService.findUserById(2L);

        Assertions.assertFalse(userServiceModel.isEnabled());
    }


    @Test
    @Order(16)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testUserUnblockMethodAccessDeniedIfNotAdmin() throws Exception {

        mockMvc.perform(
                post("/users/unblock").
                        with(csrf()).
                        param("unblockId", "2")).
                andExpect(status().isForbidden());
    }

    @Test
    @Order(17)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER", "ADMIN"})
    public void testUserUnblockMethodPost() throws Exception {

        mockMvc.perform(
                post("/users/unblock").
                        with(csrf()).
                        param("unblockId", "2"));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "aaa"));

        UserServiceModel userServiceModel = this.userService.findUserById(2L);

        Assertions.assertTrue(userServiceModel.isEnabled());
    }

    @Test
    @Order(18)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER", "ADMIN"})
    public void testUserAddRoleMethodGetContainsCorrectAttributesAndView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/users/roles/addRole").param("id", "6"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("roleAddBindingModel"))

                .andExpect(view().name("users/user-role-add"));
        ;
    }

    @Test
    @Order(19)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER", "ADMIN"})
    public void testUserAddRoleConfirmMethodPostContainsCorrectAttributesAndView() throws Exception {

        RoleAddBindingModel roleAddBindingModel = new RoleAddBindingModel();
        roleAddBindingModel.setRole("ROLE_USER");
        roleAddBindingModel.setUserId(6L);

        mockMvc.perform(
                post("/users/roles/addRole").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("roleAddBindingModel", roleAddBindingModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/users/all/pageable"));
        ;
    }


    @Test
    @Order(20)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER", "ADMIN"})
    public void testUserAddRoleConfirmMethodPostServiceIntegration() throws Exception {

        RoleAddBindingModel roleAddBindingModel = new RoleAddBindingModel();
        roleAddBindingModel.setRole("ROLE_MANAGER");
        roleAddBindingModel.setUserId(6L);

        mockMvc.perform(
                post("/users/roles/addRole").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("roleAddBindingModel", roleAddBindingModel));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "aaa"));

        UserEntity userEntity = this.userService.findUserByUsername("user_3").orElse(null);
        List<String> roles = userEntity.getRoles().stream().map(RoleEntity::getRole).collect(Collectors.toList());
        Assertions.assertTrue(roles.contains("ROLE_MANAGER"));
    }

    @Test
    @Order(21)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER", "ADMIN"})
    public void testUserRemoveRoleMethodGetContainsCorrectAttributesAndView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/users/roles/remove").param("id", "6"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("roleAddBindingModel"))

                .andExpect(view().name("users/user-role-remove"));
        ;
    }

    @Test
    @Order(22)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER", "ADMIN"})
    public void testUserRemoveRoleConfirmMethodPostContainsCorrectAttributesAndView() throws Exception {

        RoleAddBindingModel roleAddBindingModel = new RoleAddBindingModel();
        roleAddBindingModel.setRole("ROLE_USER");
        roleAddBindingModel.setUserId(6L);

        mockMvc.perform(
                post("/users/roles/remove").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("roleAddBindingModel", roleAddBindingModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/users/all/pageable"));
        ;
    }

    @Test
    @Order(23)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER", "ADMIN"})
    public void testUserRemoveRoleConfirmMethodPostServiceIntegration() throws Exception {

        RoleAddBindingModel roleAddBindingModel = new RoleAddBindingModel();
        roleAddBindingModel.setRole("ROLE_MANAGER");
        roleAddBindingModel.setUserId(6L);

        mockMvc.perform(
                post("/users/roles/remove").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("roleAddBindingModel", roleAddBindingModel));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "aaa"));

        UserEntity userEntity = this.userService.findUserByUsername("user_3").orElse(null);
        List<String> roles = userEntity.getRoles().stream().map(RoleEntity::getRole).collect(Collectors.toList());
        Assertions.assertTrue(roles.isEmpty());
    }

    @Test
    @Order(24)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER", "ADMIN"})
    public void testUserEditProfileMethodGetContainsCorrectAttributesAndView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/users/profile").param("name", "admin"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userRegisterBindingModel"))
                .andExpect(model().attributeExists("confirmPasswordCorrect"))
                .andExpect(view().name("users/user-profile-edit"));
        ;
    }

    @Test
    @Order(25)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER", "ADMIN"})
    public void testUserProfileEditConfirmMethodPost() throws Exception {

        userRegisterBindingModel.setId(1L);
        userRegisterBindingModel.setUsername("admin");
        userRegisterBindingModel.setEmail("test_email_3@email.com");

        mockMvc.perform(
                post("/users/profile").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("userRegisterBindingModel", userRegisterBindingModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "aaa"));

        UserServiceModel userServiceModel = this.userService.findUserById(1L);

        Assertions.assertEquals("test_email_3@email.com", userServiceModel.getEmail());
    }

    @Test
    @Order(26)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER", "ADMIN"})
    public void testUserProfileEditConfirmMethodPostContainsCorrectAttributesAndViewWhenBindingErrorPresent() throws Exception {

        userRegisterBindingModel.setId(1L);
        userRegisterBindingModel.setPassword("p");

        mockMvc.perform(
                post("/users/profile").
                        with(csrf()).
                        contentType(MediaType.APPLICATION_FORM_URLENCODED).
                        flashAttr("userRegisterBindingModel", userRegisterBindingModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:profile"));
        ;
    }



    private UserRegisterBindingModel createUserRegisterBindingModel() {

        UserRegisterBindingModel userRegisterBindingModel = new UserRegisterBindingModel();
        userRegisterBindingModel.setUsername("username");
        userRegisterBindingModel.setPassword("password");
        userRegisterBindingModel.setConfirmPassword("password");
        userRegisterBindingModel.setDepartment(DepartmentName.DEPARTMENT_I);
        userRegisterBindingModel.setEmail("test_email_1@email.com");
        userRegisterBindingModel.setRole("ROLE_USER");

        return userRegisterBindingModel;
    }



}
