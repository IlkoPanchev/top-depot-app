package warehouse.home;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import warehouse.customers.service.CustomerService;
import warehouse.orders.service.OrderService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HomeControllerIntegrationTests {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    OrderService orderService;

    @Test
    @Order(1)
    @WithMockUser(username = "manager_1")
    public void testHomeMethodContainsCorrectAttributesAndView() throws Exception {

        mockMvc.perform(get("/")).
                andExpect(status().isOk()).
                andExpect(model().attributeExists("lastCompletedOrders")).
                andExpect(model().attributeExists("lastCreatedOrders")).
                andExpect(model().attributeExists("lastUpdatedOrders")).
                andExpect(model().attributeExists("totalOrders")).
                andExpect(view().name("home"));
    }

    @Test
    @Order(2)
    @WithMockUser(username = "manager_1")
    public void testHomeAbsoluteMethod() throws Exception {

        mockMvc.perform(get("/home")).
                andExpect(status().isOk()).
                andExpect(model().attributeExists("lastCompletedOrders")).
                andExpect(model().attributeExists("lastCreatedOrders")).
                andExpect(model().attributeExists("lastUpdatedOrders")).
                andExpect(model().attributeExists("totalOrders")).
                andExpect(view().name("home"));
    }

    @Test
    @Order(3)
    @WithMockUser(username = "manager_1")
    public void testHomePostMethod() throws Exception {

        mockMvc.perform(post
                ("/home")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)).
                andExpect(status().is3xxRedirection()).
                andExpect(view().name("redirect:/home"));
    }
}
