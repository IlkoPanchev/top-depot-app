package warehouse.orders;

import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import warehouse.addresses.model.AddressEntity;
import warehouse.addresses.model.AddressServiceModel;
import warehouse.categories.model.CategoryEntity;
import warehouse.customers.model.CustomerEntity;
import warehouse.customers.model.CustomerServiceModel;
import warehouse.customers.service.CustomerService;
import warehouse.items.model.ItemAddServiceModel;
import warehouse.items.model.ItemEntity;
import warehouse.items.model.ItemViewServiceModel;
import warehouse.orderline.model.OrderLineAddServiceModel;
import warehouse.orderline.model.OrderLineEntity;
import warehouse.orderline.model.OrderLineViewServiceModel;
import warehouse.orderline.service.OrderLineService;
import warehouse.orders.model.OrderAddServiceModel;
import warehouse.orders.model.OrderEntity;
import warehouse.orders.model.OrderViewServiceModel;
import warehouse.orders.repository.OrderRepository;
import warehouse.orders.service.OrderService;
import warehouse.orders.service.impl.OrderServiceImpl;
import warehouse.suppliers.model.SupplierEntity;
import warehouse.suppliers.model.SupplierServiceModel;
import warehouse.suppliers.service.impl.SupplierServiceImpl;
import warehouse.utils.file.FileIOUtil;
import warehouse.utils.file.impl.FileIOUtilImpl;
import warehouse.utils.time.TimeBordersConvertor;
import warehouse.utils.validation.ValidationUtil;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceUnitTests {

    private OrderService orderServiceToTest;
    private OrderEntity orderEntity;
    private OrderAddServiceModel orderAddServiceModel;
    private OrderAddServiceModel existingOrderAddServiceModel;
    private Pageable pageable;


    @Mock
    OrderRepository mockOrderRepository;
    @Mock
    TimeBordersConvertor mockTimeBordersConvertor;
    @Mock
    OrderLineService mockOrderLineService;
    @Mock
    CustomerService mockCustomerService;
    @Mock
    ValidationUtil mockValidationUtil;

    @BeforeEach
    public void SetUp() {

        this.orderServiceToTest = new OrderServiceImpl(mockOrderRepository,
                new ModelMapper(),
                mockTimeBordersConvertor,
                new Gson(),
                new FileIOUtilImpl(),
                mockOrderLineService,
                mockCustomerService,
                mockValidationUtil);
        this.orderEntity = this.createExistingOrderEntity();
        this.orderAddServiceModel = this.createOrderAddServiceModel();
        this.pageable = this.initPageable();
        this.existingOrderAddServiceModel = this.createExistingOrderAddServiceModel();
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testAddMethodWithValidServiceModel() throws Exception {

        when(mockOrderRepository.saveAndFlush(any(OrderEntity.class))).thenReturn(orderEntity);
        when(mockValidationUtil.isValid(orderAddServiceModel)).thenReturn(true);

        orderServiceToTest.addOrder(orderAddServiceModel);

        ArgumentCaptor<OrderEntity> argument = ArgumentCaptor.forClass(OrderEntity.class);
        Mockito.verify(mockOrderRepository, times(1)).saveAndFlush(argument.capture());
        OrderEntity newOrderActual = argument.getValue();

        Assertions.assertEquals(orderAddServiceModel.getTotal(), newOrderActual.getTotal());

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testAddMethodThrowsConstraintViolationException() throws Exception {

        when(mockValidationUtil.isValid(orderAddServiceModel)).thenReturn(false);

        Assertions.assertThrows(ConstraintViolationException.class, () -> {
            orderServiceToTest.addOrder(orderAddServiceModel);
        });
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testFindAllPageableMethod() {

        Page<OrderEntity> orderEntities = new PageImpl<>(List.of(orderEntity), pageable, 1L);

        when(mockOrderRepository.findAllByDeletedFalse(any(Pageable.class))).thenReturn(orderEntities);

        Page<OrderViewServiceModel> orderViewServiceModels = this.orderServiceToTest.findAllPageable(pageable);

        Assertions.assertEquals(orderViewServiceModels.getTotalElements(), 1L);
        Assertions.assertEquals(orderViewServiceModels.getTotalPages(), 1);

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testSearchMethod(){

        Page<OrderEntity> orderEntities = new PageImpl<>(List.of(orderEntity), pageable, 1L);

        when(mockOrderRepository.search(any(String.class), any(Pageable.class))).thenReturn(orderEntities);

        Page<OrderViewServiceModel> orderViewServiceModels = this.orderServiceToTest.search("search", pageable);

        Assertions.assertEquals(orderViewServiceModels.getTotalElements(), 1L);
        Assertions.assertEquals(orderViewServiceModels.getTotalPages(), 1);


    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testFindAllPageableOrderByUpdatedMethod() {

        Page<OrderEntity> orderEntities = new PageImpl<>(List.of(orderEntity), pageable, 1L);

        when(mockOrderRepository.findAllOrderByUpdatedOnDesc(any(Pageable.class))).thenReturn(orderEntities);

        List<OrderViewServiceModel> orderViewServiceModels = orderServiceToTest.findAllPageableOrderByUpdated();

        Assertions.assertEquals(orderViewServiceModels.size(), 1L);
        Assertions.assertEquals(orderViewServiceModels.get(0).getTotal(), new BigDecimal("100"));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testFindAllPageableCompletedOrderByUpdatedMethod() {

        Page<OrderEntity> orderEntities = new PageImpl<>(List.of(orderEntity), pageable, 1L);

        when(mockOrderRepository.findAllCompletedOrdersByUpdatedOnDesc(any(Pageable.class))).thenReturn(orderEntities);

        List<OrderViewServiceModel> orderViewServiceModels = orderServiceToTest.findAllPageableCompletedOrderByUpdated();

        Assertions.assertEquals(orderViewServiceModels.size(), 1L);
        Assertions.assertEquals(orderViewServiceModels.get(0).getTotal(), new BigDecimal("100"));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testFindAllPageableOrderByCreatedMethod() {

        Page<OrderEntity> orderEntities = new PageImpl<>(List.of(orderEntity), pageable, 1L);

        when(mockOrderRepository.findAllOrdersByCreatedOnDesc(any(Pageable.class))).thenReturn(orderEntities);

        List<OrderViewServiceModel> orderViewServiceModels = orderServiceToTest.findAllPageableOrderByCreated();

        Assertions.assertEquals(orderViewServiceModels.size(), 1L);
        Assertions.assertEquals(orderViewServiceModels.get(0).getTotal(), new BigDecimal("100"));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testFindByIdMethod() {

        when(mockOrderRepository.findById(any(Long.class))).thenReturn(Optional.of(orderEntity));

        OrderViewServiceModel orderViewServiceModel = this.orderServiceToTest.findById(1L);

        Assertions.assertEquals(orderViewServiceModel.getId(), 1L);
        Assertions.assertEquals(orderViewServiceModel.getTotal(), new BigDecimal("100"));


    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testFindByIdMethodThrowsEntityNotFoundException() {

        when(mockOrderRepository.findById(any(Long.class))).thenThrow(EntityNotFoundException.class);

        Assertions.assertThrows(EntityNotFoundException.class, () -> this.orderServiceToTest.findById(5L));
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testEditOrderMethodWithValidServiceModel() throws Exception {


        orderAddServiceModel.setTotal(new BigDecimal("1000"));

        when(mockValidationUtil.isValid(existingOrderAddServiceModel)).thenReturn(true);
        when(mockOrderRepository.saveAndFlush(any(OrderEntity.class)))
                .thenReturn(orderEntity);

        orderServiceToTest.editOrder(existingOrderAddServiceModel);

        ArgumentCaptor<OrderEntity> argument = ArgumentCaptor.forClass(OrderEntity.class);
        Mockito.verify(mockOrderRepository, times(1)).saveAndFlush(argument.capture());
        OrderEntity newOrderActual = argument.getValue();

        Assertions.assertEquals(existingOrderAddServiceModel.getId(), newOrderActual.getId());
        Assertions.assertEquals(existingOrderAddServiceModel.getTotal(), newOrderActual.getTotal());


    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testEditOrderMethodThrowsConstraintViolationException() throws Exception {

        orderAddServiceModel.setTotal(new BigDecimal("1000"));

        when(mockValidationUtil.isValid(existingOrderAddServiceModel)).thenReturn(false);

        Assertions.assertThrows(ConstraintViolationException.class, () -> this.orderServiceToTest.editOrder(existingOrderAddServiceModel));



    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testCompleteOrderMethodWithValidServiceModel() throws Exception {

        when(mockValidationUtil.isValid(existingOrderAddServiceModel)).thenReturn(true);
        when(mockOrderRepository.saveAndFlush(any(OrderEntity.class)))
                .thenReturn(orderEntity);

        orderServiceToTest.completeOrder(existingOrderAddServiceModel);

        ArgumentCaptor<OrderEntity> argument = ArgumentCaptor.forClass(OrderEntity.class);
        Mockito.verify(mockOrderRepository, times(1)).saveAndFlush(argument.capture());
        OrderEntity newOrderActual = argument.getValue();

        Assertions.assertEquals(existingOrderAddServiceModel.getId(), newOrderActual.getId());
        Assertions.assertTrue(newOrderActual.isClosed());


    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testCompleteOrderMethodThrowsConstraintViolationException() throws Exception {

        when(mockValidationUtil.isValid(existingOrderAddServiceModel)).thenReturn(false);

        Assertions.assertThrows(ConstraintViolationException.class, () -> this.orderServiceToTest.completeOrder(existingOrderAddServiceModel));


    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testIncompleteOrderMethodWithValidServiceModel() throws Exception {

        orderAddServiceModel.setClosed(true);

        when(mockValidationUtil.isValid(existingOrderAddServiceModel)).thenReturn(true);
        when(mockOrderRepository.saveAndFlush(any(OrderEntity.class)))
                .thenReturn(orderEntity);

        orderServiceToTest.incompleteOrder(existingOrderAddServiceModel);

        ArgumentCaptor<OrderEntity> argument = ArgumentCaptor.forClass(OrderEntity.class);
        Mockito.verify(mockOrderRepository, times(1)).saveAndFlush(argument.capture());
        OrderEntity newOrderActual = argument.getValue();

        Assertions.assertEquals(existingOrderAddServiceModel.getId(), newOrderActual.getId());
        Assertions.assertFalse(newOrderActual.isClosed());


    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testIncompleteOrderMethodThrowsConstraintViolationException() throws Exception {

        when(mockValidationUtil.isValid(existingOrderAddServiceModel)).thenReturn(false);

        Assertions.assertThrows(ConstraintViolationException.class, () -> this.orderServiceToTest.incompleteOrder(existingOrderAddServiceModel));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testArchiveOrderMethodWithValidServiceModel() throws Exception {

        when(mockValidationUtil.isValid(existingOrderAddServiceModel)).thenReturn(true);
        when(mockOrderRepository.saveAndFlush(any(OrderEntity.class)))
                .thenReturn(orderEntity);

        orderServiceToTest.archiveOrder(existingOrderAddServiceModel);

        ArgumentCaptor<OrderEntity> argument = ArgumentCaptor.forClass(OrderEntity.class);
        Mockito.verify(mockOrderRepository, times(1)).saveAndFlush(argument.capture());
        OrderEntity newOrderActual = argument.getValue();

        Assertions.assertEquals(existingOrderAddServiceModel.getId(), newOrderActual.getId());
        Assertions.assertTrue(newOrderActual.isArchives());


    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testArchiveOrderMethodThrowsConstraintViolationException() throws Exception {

        when(mockValidationUtil.isValid(existingOrderAddServiceModel)).thenReturn(false);

        Assertions.assertThrows(ConstraintViolationException.class, () -> this.orderServiceToTest.archiveOrder(existingOrderAddServiceModel));

    }


    private OrderAddServiceModel createExistingOrderAddServiceModel() {

        OrderAddServiceModel orderAddServiceModel = this.createOrderAddServiceModel();
        orderAddServiceModel.setId(1L);
        return orderAddServiceModel;
    }

    private OrderViewServiceModel createOrderViewServiceModel() {

        OrderViewServiceModel orderViewServiceModel = new OrderViewServiceModel();
        orderViewServiceModel.setId(1L);
        Set<OrderLineViewServiceModel> orderLineViewServiceModels = new HashSet<>();
        orderLineViewServiceModels.add(this.createOrderLineViewServiceModel());
        orderViewServiceModel.setOrderLineEntities(orderLineViewServiceModels);
        orderViewServiceModel.setCustomer(this.createCustomerServiceModel());
        orderViewServiceModel.setTotal(new BigDecimal("100"));

        return orderViewServiceModel;
    }

    private OrderLineViewServiceModel createOrderLineViewServiceModel() {

        OrderLineViewServiceModel orderLineViewServiceModel = new OrderLineViewServiceModel();
        orderLineViewServiceModel.setItem(this.createItemViewServiceModel());
        orderLineViewServiceModel.setQuantity(1);
        orderLineViewServiceModel.setSubtotal(new BigDecimal("100"));

        return orderLineViewServiceModel;

    }

    private ItemViewServiceModel createItemViewServiceModel() {

        ItemViewServiceModel itemViewServiceModel = new ItemViewServiceModel();
        itemViewServiceModel.setId(1L);
        itemViewServiceModel.setName("item_name");
        itemViewServiceModel.setDescription("item_description");
        itemViewServiceModel.setLocation("item_location");
        itemViewServiceModel.setImg("item_img");
        itemViewServiceModel.setPrice(new BigDecimal("100"));

        itemViewServiceModel.setCategory("category_name");
        itemViewServiceModel.setSupplier("supplier_name");

        return itemViewServiceModel;
    }


    private OrderAddServiceModel createOrderAddServiceModel() {

        OrderAddServiceModel orderAddServiceModel = new OrderAddServiceModel();
        Set<OrderLineAddServiceModel> orderLineAddServiceModels = new HashSet<>();
        orderLineAddServiceModels.add(this.createOrderLineAddServiceModel());
        orderAddServiceModel.setOrderLineEntities(orderLineAddServiceModels);
        orderAddServiceModel.setCustomer(this.createCustomerServiceModel());
        orderAddServiceModel.setTotal(new BigDecimal("100"));

        return orderAddServiceModel;
    }

    private OrderLineAddServiceModel createOrderLineAddServiceModel() {

        OrderLineAddServiceModel orderLineAddServiceModel = new OrderLineAddServiceModel();
        orderLineAddServiceModel.setItem(this.createItemAddServiceModel());
        orderLineAddServiceModel.setQuantity(1);
        orderLineAddServiceModel.setSubtotal(new BigDecimal("100"));

        return orderLineAddServiceModel;
    }

    private ItemAddServiceModel createItemAddServiceModel() {

        ItemAddServiceModel itemAddServiceModel = new ItemAddServiceModel();
        itemAddServiceModel.setId(1L);
        itemAddServiceModel.setName("item_name");
        itemAddServiceModel.setDescription("item_description");
        itemAddServiceModel.setLocation("item_location");
        MockMultipartFile img = this.createMultipartFile();
        itemAddServiceModel.setImg(img);
        itemAddServiceModel.setPrice(new BigDecimal("100"));

        itemAddServiceModel.setCategory("category_name");
        itemAddServiceModel.setSupplier("supplier_name");

        return itemAddServiceModel;
    }

    private MockMultipartFile createMultipartFile(){
        MockMultipartFile file
                = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        return file;
    }


    private CustomerServiceModel createCustomerServiceModel() {

        CustomerServiceModel customerServiceModel = new CustomerServiceModel();
        customerServiceModel.setCompanyName("company_name");
        customerServiceModel.setPersonName("person_name");
        customerServiceModel.setEmail("customer@mail.bg");

        AddressServiceModel addressServiceModel = new AddressServiceModel();
        addressServiceModel.setRegion("Sofia city");
        addressServiceModel.setCity("Sofia");
        addressServiceModel.setStreet("Tintyava 15");
        addressServiceModel.setPhone("02111222");

        customerServiceModel.setAddress(addressServiceModel);

        return customerServiceModel;
    }

    private OrderEntity createExistingOrderEntity() {

        OrderEntity orderEntity = new OrderEntity();

        orderEntity.setId(1L);
        Set<OrderLineEntity> orderLineEntities = new HashSet<>();
        orderLineEntities.add(this.createOrderLineEntity());
        orderEntity.setOrderLineEntities(orderLineEntities);
        orderEntity.setCustomer(this.createExistingCustomerEntity());
        orderEntity.setTotal(new BigDecimal("100"));

        return orderEntity;
    }

    private OrderLineEntity createOrderLineEntity() {

        OrderLineEntity orderLineEntity = new OrderLineEntity();

        orderLineEntity.setItem(this.createExistingItemEntity());
        orderLineEntity.setQuantity(1);
        orderLineEntity.setSubtotal(new BigDecimal("100"));

        return orderLineEntity;
    }

    private ItemEntity createItemEntity() {

        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setName("item_name");
        itemEntity.setDescription("item_description");
        itemEntity.setLocation("item_location");
        itemEntity.setImg("item_img");
        itemEntity.setPrice(new BigDecimal("100"));

        CategoryEntity categoryEntity = this.createExistingCategoryEntity();
        itemEntity.setCategory(categoryEntity);
        SupplierEntity supplierEntity = this.createExistingSupplierEntity();
        itemEntity.setSupplier(supplierEntity);

        return itemEntity;
    }

    private ItemEntity createExistingItemEntity() {

        ItemEntity itemEntity = this.createItemEntity();
        itemEntity.setId(1L);

        return itemEntity;
    }

    private SupplierEntity createExistingSupplierEntity() {

        SupplierEntity supplierEntity = new SupplierEntity();
        supplierEntity.setId(1L);
        supplierEntity.setName("supplier_name");
        supplierEntity.setEmail("email@email.com");

        AddressEntity addressEntity = this.createExistingAddressEntity();
        supplierEntity.setAddressEntity(addressEntity);

        return supplierEntity;
    }

    private AddressEntity createExistingAddressEntity() {

        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setId(1L);
        addressEntity.setRegion("region");
        addressEntity.setCity("city");
        addressEntity.setStreet("street");
        addressEntity.setPhone("+359 02 111 222");

        return addressEntity;
    }

    private CategoryEntity createExistingCategoryEntity() {

        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setId(1L);
        categoryEntity.setName("category_name");
        categoryEntity.setDescription("category_description");

        return categoryEntity;
    }

    private CustomerEntity createCustomerEntity() {

        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setCompanyName("company_name");
        customerEntity.setPersonName("person_name");
        customerEntity.setEmail("customer@mail.bg");
        customerEntity.setBlocked(false);

        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setRegion("Sofia city");
        addressEntity.setCity("Sofia");
        addressEntity.setStreet("Tintyava 15");
        addressEntity.setPhone("02111222");

        customerEntity.setAddressEntity(addressEntity);
        return customerEntity;
    }

    private CustomerEntity createExistingCustomerEntity(){

        CustomerEntity customerEntity = this.createCustomerEntity();
        customerEntity.setId(1L);
        customerEntity.getAddressEntity().setId(1L);

        return customerEntity;

    }

    private Pageable initPageable() {

        String option = "name";
        Sort sort = Sort.by(option).ascending();
        int page = 0;
        int pageSize = 1;

        return PageRequest.of(page, pageSize, sort);
    }


}
