package warehouse.orders;

import org.junit.jupiter.api.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import warehouse.addresses.service.AddressService;
import warehouse.customers.model.CustomerServiceModel;
import warehouse.customers.model.CustomerViewBindingModel;
import warehouse.customers.service.CustomerService;
import warehouse.items.model.ItemViewBindingModel;
import warehouse.items.model.ItemViewServiceModel;
import warehouse.items.service.ItemService;
import warehouse.orders.model.OrderViewBindingModel;
import warehouse.orders.model.OrderViewServiceModel;
import warehouse.orders.orderdata.OrderData;
import warehouse.orders.orderdata.service.OrderDataManager;
import warehouse.orders.service.OrderService;
import warehouse.suppliers.service.SupplierService;

import java.math.BigDecimal;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class OrderControllerIntegrationTests {

    private OrderData orderData;
    private OrderData editOrderData;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private OrderService orderService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    ItemService itemService;

    @BeforeEach
    public void setUp(){
        this.orderData = this.createTestOrderData();
        this.editOrderData = this.createTestEditOrderData();
    }



    @Test
    @Order(1)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testGetAllPageableMethodWithDefaultRequestParamsContainsCorrectAttributesAndView() throws Exception {

        mockMvc.perform(get("/orders/all/pageable")).
                andExpect(status().isOk()).
                andExpect(model().attributeExists("orders")).
                andExpect(model().attributeExists("result")).
                andExpect(model().attributeExists("selectedPageSize")).
                andExpect(model().attributeExists("pageSizes")).
                andExpect(model().attributeExists("pager")).
                andExpect(model().attributeExists("selectedSortOption")).
                andExpect(model().attributeExists("sortOptions")).
                andExpect(model().attributeExists("sortDirection")).
                andExpect(model().attribute("sortDirection", equalTo("desc"))).
                andExpect(model().attributeExists("reversedSortDirection")).
                andExpect(model().attributeExists("path")).
                andExpect(view().name("orders/order-all"));

    }


    @Test
    @Order(2)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testOrderAddMethodGetReturnsCorrectView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/orders/open").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/order-add"));
        ;
    }

    @Test
    @Order(3)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testOrderAddItemMethodGetContainsCorrectAttributesAndView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/orders/addItem").param("id", "1")).
                andExpect(status().isOk()).
                andExpect(model().attributeExists("itemViewBindingModel")).
                andExpect(view().name("orders/order-add-item"));
        ;
    }

    @Test
    @Order(4)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testOrderAddItemConfirmMethodPostReturnsCorrectView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                post("/orders/addItem").
                with(csrf()).
                contentType(MediaType.APPLICATION_FORM_URLENCODED).
                param("id", "1").
                param("quantity", "1").
                sessionAttr("orderData", orderData)).
                andExpect(status().isOk()).
                andExpect(view().name("orders/order-add"));
        ;
    }

    @Test
    @Order(5)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testOrderAddItemCancelMethodGetReturnsCorrectView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                get("/orders/addItem/cancel").sessionAttr("orderData",  orderData)).
                andExpect(status().isOk()).
                andExpect(view().name("orders/order-add"));
        ;
    }

    @Test
    @Order(6)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testUpdateOrderDataMethodGetReturnsCorrectView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                get("/orders/orderData/update").
                param("id", "1").
                param("quantity", "1").
                sessionAttr("orderData",  orderData)).
                andExpect(status().isOk()).
                andExpect(view().name("fragments/draft-order-order-lines-table :: draftOrderOrderLinesTable"));
        ;
    }

    @Test
    @Order(7)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testRemoveOrderDataMethodPostReturnsCorrectView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                post("/orders/orderData/remove").
                with(csrf()).
                contentType(MediaType.APPLICATION_FORM_URLENCODED).
                param("removeId", "1").
                sessionAttr("orderData",  orderData)).
                andExpect(status().isOk()).
                andExpect(view().name("orders/order-add"));
        ;
    }

    @Test
    @Order(8)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testCancelOrderMethodGetReturnsCorrectView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                get("/orders/cancel")).
                andExpect(status().is3xxRedirection()).
                andExpect(view().name("redirect:/orders/all/pageable"));
        ;
    }

    @Test
    @Order(9)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testSaveOrderMethodIntegrationTests() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                get("/orders/save").sessionAttr("orderData", orderData)).
                andExpect(status().is3xxRedirection()).
                andExpect(view().name("redirect:/orders/all/pageable"));

        OrderViewServiceModel orderViewServiceModel = this.orderService.findById(7L);
        Assertions.assertEquals("Company_Name_1", orderViewServiceModel.getCustomer().getCompanyName());
        Assertions.assertEquals(1, orderViewServiceModel.getOrderLineEntities().size());
        Assertions.assertEquals(new BigDecimal("100.00"), orderViewServiceModel.getTotal());

    }

    @Test
    @Order(10)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testSaveDraftOrderMethodGetReturnsCorrectView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                get("/orders/save/draft")).
                andExpect(status().is3xxRedirection()).
                andExpect(view().name("redirect:/orders/all/pageable"));

    }

    @Test
    @Order(11)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testOpenDraftOrderMethodGetReturnsCorrectView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                get("/orders/open/draft").sessionAttr("orderData", orderData)).
                andExpect(status().isOk()).
                andExpect(view().name("orders/order-add"));

    }


    @Test
    @Order(12)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testEditOrderMethodGetReturnsCorrectView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                get("/orders/edit").param("id", "1")).
                andExpect(status().isOk()).
                andExpect(view().name("orders/order-edit"));

    }

    @Test
    @Order(13)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testEditOrderUpdateMethodGetReturnsCorrectView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                get("/orders/editOrder/update").
                param("id", "1").
                param("quantity", "1")).
                andExpect(status().isOk()).
                andExpect(view().name("fragments/order-edit-order-lines-table :: editOrderOrderLinesTable"));

    }

    @Test
    @Order(14)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testEditOrderRemoveMethodPostReturnsCorrectView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                post("/orders/editOrder/remove").
                with(csrf()).
                param("removeId", "1").sessionAttr("editOrderData", editOrderData)).
                andExpect(status().isOk()).
                andExpect(view().name("orders/order-edit"));

    }

    @Test
    @Order(15)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testEditOrderAddItemMethodGetReturnsCorrectView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                get("/orders/editOrder/addItem").
                param("id", "1")).
                andExpect(status().isOk()).
                andExpect(model().attributeExists("itemViewBindingModel")).
                andExpect(view().name("orders/order-add-item-edit-order"));

    }

    @Test
    @Order(16)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testEditOrderAddItemConfirmMethodPostReturnsCorrectView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                post("/orders/editOrder/addItem").
                with(csrf()).
                param("id", "1").
                param("quantity", "1").sessionAttr("editOrderData", editOrderData)).
                andExpect(status().isOk()).
                andExpect(view().name("orders/order-edit"));

    }

    @Test
    @Order(17)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testEditOrderAddItemCancelMethodGetReturnsCorrectView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                get("/orders/editOrder/addItem/cancel").sessionAttr("editOrderData", editOrderData)).
                andExpect(status().isOk()).
                andExpect(view().name("orders/order-edit"));

    }

    @Test
    @Order(18)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testEditOrderSaveMethodGetReturnsCorrectView() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                get("/orders/edit/save").sessionAttr("editOrderData", editOrderData)).
                andExpect(status().is3xxRedirection()).
                andExpect(view().name("redirect:/orders/all/pageable"));


    }

    @Test
    @Order(19)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testEditOrderCompleteMethod() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                get("/orders/edit/complete").sessionAttr("editOrderData", editOrderData)).
                andExpect(status().is3xxRedirection()).
                andExpect(view().name("redirect:/orders/all/pageable"));

       OrderViewServiceModel orderViewServiceModel =  this.orderService.findById(2L);
       Assertions.assertTrue(orderViewServiceModel.isClosed());

    }

    @Test
    @Order(20)
    @WithMockUser(username = "manager", roles = {"USER", "MANAGER"})
    public void testEditOrderCancelMethod() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                get("/orders/edit/cancel").sessionAttr("editOrderData", editOrderData)).
                andExpect(status().is3xxRedirection()).
                andExpect(view().name("redirect:/orders/all/pageable"));


    }

    @Test
    @Order(21)
    @WithMockUser(username = "user")
    public void testOrderCompleteOpenMethod() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                get("/orders/complete/open").param("id", "1")
                .sessionAttr("editOrderData", editOrderData)).
                andExpect(status().isOk()).
                andExpect(view().name("orders/order-complete-open"));


    }

    @Test
    @Order(22)
    @WithMockUser(username = "user")
    public void testOrderArchiveOpenMethod() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                get("/orders/archive/open").param("id", "1")
                .sessionAttr("editOrderData", editOrderData)).
                andExpect(status().isOk()).
                andExpect(view().name("orders/order-archive-open"));


    }

    @Test
    @Order(23)
    @WithMockUser(username = "user")
    public void testOrderCompleteMethod() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                get("/orders/complete").sessionAttr("editOrderData", editOrderData)).
                andExpect(status().is3xxRedirection()).
                andExpect(view().name("redirect:/orders/all/pageable"));


    }

    @Test
    @Order(24)
    @WithMockUser(username = "user")
    public void testOrderIncompleteMethod() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                get("/orders/incomplete").sessionAttr("editOrderData", editOrderData)).
                andExpect(status().is3xxRedirection()).
                andExpect(view().name("redirect:/orders/all/pageable"));


    }

    @Test
    @Order(25)
    @WithMockUser(username = "user")
    public void testOrderArchiveMethod() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                get("/orders/archive").sessionAttr("editOrderData", editOrderData)).
                andExpect(status().is3xxRedirection()).
                andExpect(view().name("redirect:/orders/all/pageable"));


    }

    @Test
    @Order(26)
    @WithMockUser(username = "user")
    public void testCompleteCancelMethod() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                get("/orders/complete/cancel").sessionAttr("editOrderData", editOrderData)).
                andExpect(status().is3xxRedirection()).
                andExpect(view().name("redirect:/orders/all/pageable"));


    }

    @Test
    @Order(27)
    @WithMockUser(username = "user")
    public void testArchiveViewMethod() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                get("/orders/archive/view").param("id", "1")).
                andExpect(status().isOk()).
                andExpect(model().attributeExists("orderViewBindingModel")).
                andExpect(view().name("orders/order-archive-view"));


    }

    @Test
    @Order(28)
    @WithMockUser(username = "user")
    public void testArchiveBackMethod() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.
                get("/orders/archive/back").sessionAttr("editOrderData", editOrderData)).
                andExpect(status().is3xxRedirection()).
                andExpect(view().name("redirect:/orders/all/pageable"));


    }

    private OrderData createTestEditOrderData() {

        OrderViewServiceModel orderViewServiceModel = this.orderService.findById(2L);

        OrderViewBindingModel orderViewBindingModel = this.modelMapper.map(orderViewServiceModel, OrderViewBindingModel.class);
        OrderData orderData = this.modelMapper.map(orderViewBindingModel, OrderData.class);

        return orderData;
    }


    private OrderData createTestOrderData() {

        OrderData orderData = new OrderData();
        orderData.setCustomer(this.getTestCustomerViewBindingModel(1L));
        orderData.addOrderLine(this.getTestOrderLine(1L), 1);
        orderData.getTotalAmount();

        return orderData;
    }

    private ItemViewBindingModel getTestOrderLine(long id) {

        ItemViewServiceModel itemViewServiceModel = this.itemService.findById(id);
        ItemViewBindingModel itemViewBindingModel = this.modelMapper.map(itemViewServiceModel, ItemViewBindingModel.class);

        return itemViewBindingModel;
    }

    private CustomerViewBindingModel getTestCustomerViewBindingModel(Long id) {

        CustomerServiceModel customerServiceModel = this.customerService.findById(id);
        CustomerViewBindingModel customerViewBindingModel = this.modelMapper.map(customerServiceModel, CustomerViewBindingModel.class);

        return customerViewBindingModel;
    }

}
