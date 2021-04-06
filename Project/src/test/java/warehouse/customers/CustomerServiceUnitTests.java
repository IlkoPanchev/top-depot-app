package warehouse.customers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import warehouse.addresses.model.AddressEntity;
import warehouse.addresses.model.AddressServiceModel;
import warehouse.addresses.service.AddressService;
import warehouse.customers.model.CustomerEntity;
import warehouse.customers.model.CustomerServiceModel;
import warehouse.customers.model.CustomerTurnoverViewModel;
import warehouse.customers.repository.CustomerRepository;
import warehouse.customers.service.CustomerService;
import warehouse.customers.service.impl.CustomerServiceImpl;
import warehouse.orders.service.OrderService;
import warehouse.suppliers.model.SupplierEntity;
import warehouse.suppliers.model.SupplierServiceModel;
import warehouse.suppliers.model.SupplierTurnoverViewModel;
import warehouse.utils.time.TimeBordersConvertor;
import warehouse.utils.validation.ValidationUtil;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceUnitTests {

    private final String CUSTOMER_NAME = "company_name";

    private CustomerService customerServiceToTest;
    private CustomerEntity customerEntity;
    private CustomerServiceModel customerServiceModel;
    private CustomerServiceModel existingCustomerServiceModel;
    private Pageable pageable;

    @Mock
    CustomerRepository mockCustomerRepository;
    @Mock
    TimeBordersConvertor mockTimeBordersConvertor;
    @Mock
    OrderService mockOrderService;
    @Mock
    AddressService mockAddressService;
    @Mock
    ValidationUtil mockValidationUtil;

    @BeforeEach
    public void SetUp(){
        this.customerServiceToTest = new CustomerServiceImpl(mockCustomerRepository,
                new ModelMapper(),
                mockTimeBordersConvertor,
                mockOrderService,
                mockAddressService,
                mockValidationUtil);

        this.customerEntity = this.createExistingCustomerEntity();
        this.customerServiceModel = this.createCustomerServiceModel();
        this.existingCustomerServiceModel = this.createExistingCustomerServiceModel();
        this.pageable = this.initPageable();
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testCustomerExistMethodReturnFalse(){

        Optional<CustomerEntity> customerEntity = Optional.empty();

        when(mockCustomerRepository.findByCompanyName(any(String.class))).thenReturn(customerEntity);

        Assertions.assertFalse(this.customerServiceToTest.customerExists(CUSTOMER_NAME));
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testCustomerExistMethodReturnTrue(){


        when(mockCustomerRepository.findByCompanyName(any(String.class))).thenReturn(Optional.of(customerEntity));

        Assertions.assertTrue(this.customerServiceToTest.customerExists(CUSTOMER_NAME));
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testAddMethodWithValidServiceModel(){

        when(mockValidationUtil.isValid(any(CustomerServiceModel.class))).thenReturn(true);
        when(mockCustomerRepository.saveAndFlush(any(CustomerEntity.class))).thenReturn(customerEntity);

        customerServiceToTest.add(customerServiceModel);

        ArgumentCaptor<CustomerEntity> argument = ArgumentCaptor.forClass(CustomerEntity.class);
        Mockito.verify(mockCustomerRepository, times(1)).saveAndFlush(argument.capture());
        CustomerEntity customerEntityActual = argument.getValue();

        Assertions.assertEquals(customerServiceModel.getCompanyName(), customerEntityActual.getCompanyName());
    }


    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testAddMethodThrowsConstraintViolationException(){

        when(mockValidationUtil.isValid(any(CustomerServiceModel.class))).thenReturn(false);

        Assertions.assertThrows(ConstraintViolationException.class, () ->  customerServiceToTest.add(customerServiceModel));
    }

    @Test@MockitoSettings(strictness = Strictness.WARN)
    public void testFindAllMethod(){

        when(mockCustomerRepository.findAll()).thenReturn(List.of(customerEntity));

        List<CustomerServiceModel> customerServiceModels = this.customerServiceToTest.findAll();

        Assertions.assertEquals(customerServiceModels.size(), 1);
        Assertions.assertEquals(customerServiceModels.get(0).getCompanyName(), CUSTOMER_NAME);

    }

    @Test@MockitoSettings(strictness = Strictness.WARN)
    public void testFindAllPageableMethod(){

        Page<CustomerEntity> customerEntities = new PageImpl<>(List.of(customerEntity), pageable, 1L);

        when(mockCustomerRepository.findAll(any(Pageable.class))).thenReturn(customerEntities);

        Page<CustomerServiceModel> customerServiceModelPage = customerServiceToTest.findAllPageable(pageable);

        Assertions.assertEquals(customerServiceModelPage.getTotalElements(), 1L);
        Assertions.assertEquals(customerServiceModelPage.getTotalPages(), 1);

    }

    @Test@MockitoSettings(strictness = Strictness.WARN)
    public void testFindAllPageableUnblockedMethod(){

        Page<CustomerEntity> customerEntities = new PageImpl<>(List.of(customerEntity), pageable, 1L);

        when(mockCustomerRepository.findAllByBlockedFalse(any(Pageable.class))).thenReturn(customerEntities);

        Page<CustomerServiceModel> customerServiceModelPage = customerServiceToTest.findAllPageableUnblocked(pageable);

        Assertions.assertEquals(customerServiceModelPage.getTotalElements(), 1L);
        Assertions.assertEquals(customerServiceModelPage.getTotalPages(), 1);

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testFindByIdMethod() {

        when(mockCustomerRepository.findById(any(Long.class))).thenReturn(Optional.of(customerEntity));

        CustomerServiceModel customerServiceModel = this.customerServiceToTest.findById(1L);

        Assertions.assertEquals(customerServiceModel.getId(), 1L);
        Assertions.assertEquals(customerServiceModel.getCompanyName(), CUSTOMER_NAME);
        Assertions.assertEquals(customerServiceModel.getAddress().getId(), 1L);

    }


    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testFindByIdMethodThrowsEntityNotFoundException() {

        when(mockCustomerRepository.findById(any(Long.class))).thenThrow(EntityNotFoundException.class);

        Assertions.assertThrows(EntityNotFoundException.class, () -> this.customerServiceToTest.findById(5L));
    }


    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testSearchMethod(){

        Page<CustomerEntity> customerEntities = new PageImpl<>(List.of(customerEntity), pageable, 1L);

        when(mockCustomerRepository.search(any(String.class), any(Pageable.class))).thenReturn(customerEntities);

        Page<CustomerServiceModel> customerServiceModelPage = customerServiceToTest.search(CUSTOMER_NAME, pageable);

        Assertions.assertEquals(customerServiceModelPage.getTotalElements(), 1L);
        Assertions.assertEquals(customerServiceModelPage.getTotalPages(), 1);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testSearchUnblockedMethod(){

        Page<CustomerEntity> customerEntities = new PageImpl<>(List.of(customerEntity), pageable, 1L);

        when(mockCustomerRepository.searchUnblocked(any(String.class), any(Pageable.class))).thenReturn(customerEntities);

        Page<CustomerServiceModel> customerServiceModelPage = customerServiceToTest.searchUnblocked(CUSTOMER_NAME, pageable);

        Assertions.assertEquals(customerServiceModelPage.getTotalElements(), 1L);
        Assertions.assertEquals(customerServiceModelPage.getTotalPages(), 1);

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testEditMethodWithValidServiceModel() throws Exception {

        when(mockCustomerRepository.findById(any(Long.class))).thenReturn(Optional.of(customerEntity));
        when(mockAddressService.addressExists(any(Long.class))).thenReturn(true);
        when(mockValidationUtil.isValid(existingCustomerServiceModel)).thenReturn(true);
        when(mockCustomerRepository.saveAndFlush(any(CustomerEntity.class)))
                .thenReturn(customerEntity);

        customerServiceToTest.edit(existingCustomerServiceModel);

        ArgumentCaptor<CustomerEntity> argument = ArgumentCaptor.forClass(CustomerEntity.class);
        Mockito.verify(mockCustomerRepository, times(1)).saveAndFlush(argument.capture());
        CustomerEntity newCustomerActual = argument.getValue();

        Assertions.assertEquals(existingCustomerServiceModel.getId(), newCustomerActual.getId());
        Assertions.assertEquals(existingCustomerServiceModel.getCompanyName(), newCustomerActual.getCompanyName());
        Assertions.assertEquals(existingCustomerServiceModel.getAddress().getId(), newCustomerActual.getAddressEntity().getId());
        Assertions.assertEquals(existingCustomerServiceModel.getAddress().getCity(), newCustomerActual.getAddressEntity().getCity());

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testEditMethodThrowsConstraintViolationException() throws Exception {

        when(mockValidationUtil.isValid(existingCustomerServiceModel)).thenReturn(false);

        Assertions.assertThrows(ConstraintViolationException.class, () -> this.customerServiceToTest.edit(existingCustomerServiceModel));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testEditMethodThrowsNotFoundEntityExceptionForCustomerEntity() throws Exception {

        when(mockValidationUtil.isValid(existingCustomerServiceModel)).thenReturn(true);
        when(mockCustomerRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> this.customerServiceToTest.edit(existingCustomerServiceModel));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testEditMethodThrowsNotFoundEntityExceptionForAddressEntity() throws Exception {

        when(mockValidationUtil.isValid(existingCustomerServiceModel)).thenReturn(true);
        when(mockCustomerRepository.findById(any(Long.class))).thenReturn(Optional.of(customerEntity));
        when(mockAddressService.addressExists(any(Long.class))).thenReturn(false);

        Assertions.assertThrows(EntityNotFoundException.class, () -> this.customerServiceToTest.edit(existingCustomerServiceModel));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testBlockMethod() {

        when(mockCustomerRepository.findById(any(Long.class))).thenReturn(Optional.of(customerEntity));

        customerServiceToTest.block(1L);

        ArgumentCaptor<CustomerEntity> argument = ArgumentCaptor.forClass(CustomerEntity.class);
        Mockito.verify(mockCustomerRepository, times(1)).saveAndFlush(argument.capture());
        CustomerEntity newCustomerActual = argument.getValue();

        Assertions.assertTrue(newCustomerActual.isBlocked());

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testBlockMethodThrowsEntityNotFoundException() {

        when(mockCustomerRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> this. customerServiceToTest.block(1L));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testUnlockMethod() {

        customerEntity.setBlocked(true);

        when(mockCustomerRepository.findById(any(Long.class))).thenReturn(Optional.of(customerEntity));

        customerServiceToTest.unblock(1L);

        ArgumentCaptor<CustomerEntity> argument = ArgumentCaptor.forClass(CustomerEntity.class);
        Mockito.verify(mockCustomerRepository, times(1)).saveAndFlush(argument.capture());
        CustomerEntity newCustomerActual = argument.getValue();

        Assertions.assertFalse(newCustomerActual.isBlocked());

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testUnlockMethodThrowsEntityNotFoundException() {

        when(mockCustomerRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> this. customerServiceToTest.unblock(1L));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testGetCustomerTurnoverMethod() {

        LocalDateTime[] timeBorders = this.getTimeBorders();
        when(mockTimeBordersConvertor
                .getTimeBordersAsLocalDateTime(any(String.class), any(String.class), any(LocalDateTime.class))).thenReturn(timeBorders);

        when(mockOrderService.getDateTimeFirstArchiveOrder()).thenReturn(LocalDateTime.now());

        List<Object[]> result = this.getResult();
        when(mockCustomerRepository.findCustomerTurnover(any(LocalDateTime.class), any(LocalDateTime.class), any(String.class), any(Pageable.class)))
                .thenReturn(result);

        List<CustomerTurnoverViewModel> customerTurnoverViewModels = this.customerServiceToTest.getCustomerTurnover("", "", "null");

        Assertions.assertEquals(CUSTOMER_NAME, customerTurnoverViewModels.get(0).getCompanyName());
        Assertions.assertEquals(BigDecimal.valueOf(1000), customerTurnoverViewModels.get(0).getTurnover());
        Assertions.assertEquals(2, customerTurnoverViewModels.get(0).getOrdersCount());
        Assertions.assertEquals(10, customerTurnoverViewModels.get(0).getOrderedItems());

    }


    private List<Object[]> getResult() {

        List<Object[]> result = new ArrayList<>();
        Object[] objects = new Object[5];
        objects[0] = "company_name";
        objects[1] = "person_name";
        objects[2] = BigDecimal.valueOf(1000);
        objects[3] = 2;
        objects[4] = 10;
        result.add(objects);
        return result;
    }

    private LocalDateTime[] getTimeBorders() {

        return new LocalDateTime[]{LocalDateTime.now(), LocalDateTime.now()};
    }


    private CustomerEntity createExistingCustomerEntity(){

        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setId(1L);
        customerEntity.setCompanyName("company_name");
        customerEntity.setPersonName("person_name");
        customerEntity.setEmail("customer@mail.bg");
        customerEntity.setBlocked(false);

        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setId(1L);
        addressEntity.setRegion("Sofia city");
        addressEntity.setCity("Sofia");
        addressEntity.setStreet("Tintyava 15");
        addressEntity.setPhone("02111222");

        customerEntity.setAddressEntity(addressEntity);

        return customerEntity;

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

    private CustomerServiceModel createExistingCustomerServiceModel(){

        CustomerServiceModel customerServiceModel = this.createCustomerServiceModel();
        customerServiceModel.setId(1L);
        customerServiceModel.getAddress().setId(1L);

        return customerServiceModel;

    }

    private Pageable initPageable() {

        String option = "name";
        Sort sort = Sort.by(option).ascending();
        int page = 0;
        int pageSize = 1;

        return PageRequest.of(page, pageSize, sort);
    }

}
