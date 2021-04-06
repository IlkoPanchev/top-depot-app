package warehouse.suppliers;

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
import warehouse.orders.service.OrderService;
import warehouse.suppliers.model.SupplierEntity;
import warehouse.suppliers.model.SupplierServiceModel;
import warehouse.suppliers.model.SupplierTurnoverViewModel;
import warehouse.suppliers.repository.SupplierRepository;
import warehouse.suppliers.service.SupplierService;
import warehouse.suppliers.service.impl.SupplierServiceImpl;
import warehouse.utils.time.TimeBordersConvertor;
import warehouse.utils.validation.ValidationUtil;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class SupplierServiceImplUnitTests {

    private final String SUPPLIER_NAME = "supplier";

    private SupplierService supplierServiceToTest;
    private SupplierEntity supplierEntity;
    private SupplierServiceModel supplierServiceModel;
    private SupplierServiceModel existingSupplierServiceModel;
    private Pageable pageable;

    @Mock
    SupplierRepository mockSupplierRepository;
    @Mock
    OrderService mockOrderService;
    @Mock
    TimeBordersConvertor mockTimeBordersConvertor;
    @Mock
    AddressService mockAddressService;
    @Mock
    ValidationUtil mockValidationUtil;

    @BeforeEach
    public void SetUp() {

        this.supplierServiceToTest = new SupplierServiceImpl(mockSupplierRepository,
                new ModelMapper(),
                mockOrderService,
                mockTimeBordersConvertor,
                mockAddressService,
                mockValidationUtil);
        this.supplierEntity = this.createExistingSupplierEntity();
        this.supplierServiceModel = this.createSupplierServiceModel();
        this.existingSupplierServiceModel = this.createExistingSupplierServiceModel();
        this.pageable = this.initPageable();
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testFindAllMethod() {

        when(mockSupplierRepository.findAll()).thenReturn(List.of(supplierEntity));

        List<SupplierServiceModel> supplierServiceModels = supplierServiceToTest.findAll();

        Assertions.assertEquals(1, supplierServiceModels.size());
        Assertions.assertEquals(supplierServiceModels.get(0).getName(), supplierEntity.getName());
    }


    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testAddMethodWithValidServiceModel() throws Exception {

        when(mockSupplierRepository.saveAndFlush(any(SupplierEntity.class))).thenReturn(supplierEntity);
        when(mockValidationUtil.isValid(supplierServiceModel)).thenReturn(true);

        supplierServiceToTest.add(supplierServiceModel);

        ArgumentCaptor<SupplierEntity> argument = ArgumentCaptor.forClass(SupplierEntity.class);
        Mockito.verify(mockSupplierRepository, times(1)).saveAndFlush(argument.capture());
        SupplierEntity newSupplierActual = argument.getValue();

        Assertions.assertEquals(supplierServiceModel.getName(), newSupplierActual.getName());

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testAddMethodThrowsConstraintViolationException() throws Exception {

        when(mockValidationUtil.isValid(supplierServiceModel)).thenReturn(false);

        Assertions.assertThrows(ConstraintViolationException.class, () -> {
            supplierServiceToTest.add(supplierServiceModel);
        });
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testFindAllPageableMethod() {

        Page<SupplierEntity> supplierEntities = new PageImpl<>(List.of(supplierEntity), pageable, 1L);

        when(mockSupplierRepository.findAll(any(Pageable.class))).thenReturn(supplierEntities);

        Page<SupplierServiceModel> supplierServiceModelPage = supplierServiceToTest.findAllPageable(pageable);

        Assertions.assertEquals(supplierServiceModelPage.getTotalElements(), 1L);
        Assertions.assertEquals(supplierServiceModelPage.getTotalPages(), 1);

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testSearchMethod(){

        Page<SupplierEntity> supplierEntities = new PageImpl<>(List.of(supplierEntity), pageable, 1L);

        when(mockSupplierRepository.search(any(String.class), any(Pageable.class))).thenReturn(supplierEntities);

        Page<SupplierServiceModel> supplierServiceModelPage = supplierServiceToTest.search(SUPPLIER_NAME, pageable);

        Assertions.assertEquals(supplierServiceModelPage.getTotalElements(), 1L);
        Assertions.assertEquals(supplierServiceModelPage.getTotalPages(), 1);
        Assertions.assertEquals(SUPPLIER_NAME, supplierServiceModelPage.get().collect(Collectors.toList()).get(0).getName());

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testFindByIdMethod() {

        when(mockSupplierRepository.findById(any(Long.class))).thenReturn(Optional.of(supplierEntity));

        SupplierServiceModel supplierServiceModel = this.supplierServiceToTest.findById(1L);


        Assertions.assertEquals(supplierServiceModel.getId(), 1L);
        Assertions.assertEquals(supplierServiceModel.getName(), SUPPLIER_NAME);
        Assertions.assertEquals(supplierServiceModel.getAddress().getId(), 1L);

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testFindByIdMethodThrowsEntityNotFoundException() {

        when(mockSupplierRepository.findById(any(Long.class))).thenThrow(EntityNotFoundException.class);

        Assertions.assertThrows(EntityNotFoundException.class, () -> this.supplierServiceToTest.findById(5L));
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testSupplierExistsMethodReturnsTrue() {

        when(mockSupplierRepository.findByName(any(String.class))).thenReturn(Optional.of(supplierEntity));

        Assertions.assertTrue(this.supplierServiceToTest.supplierExists(SUPPLIER_NAME));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testSupplierExistsReturnsFalse() {

        Optional<SupplierEntity> supplierEntity = Optional.empty();

        when(mockSupplierRepository.findByName(any(String.class))).thenReturn(supplierEntity);

        Assertions.assertFalse(this.supplierServiceToTest.supplierExists(SUPPLIER_NAME));
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testEditMethodWithValidServiceModel() throws Exception {

        when(mockSupplierRepository.findById(any(Long.class))).thenReturn(Optional.of(supplierEntity));
        when(mockAddressService.addressExists(any(Long.class))).thenReturn(true);
        when(mockValidationUtil.isValid(existingSupplierServiceModel)).thenReturn(true);
        when(mockSupplierRepository.saveAndFlush(any(SupplierEntity.class)))
                .thenReturn(supplierEntity);

        supplierServiceToTest.edit(existingSupplierServiceModel);

        ArgumentCaptor<SupplierEntity> argument = ArgumentCaptor.forClass(SupplierEntity.class);
        Mockito.verify(mockSupplierRepository, times(1)).saveAndFlush(argument.capture());
        SupplierEntity newSupplierActual = argument.getValue();

        Assertions.assertEquals(existingSupplierServiceModel.getId(), newSupplierActual.getId());
        Assertions.assertEquals(existingSupplierServiceModel.getName(), newSupplierActual.getName());
        Assertions.assertEquals(existingSupplierServiceModel.getAddress().getId(), newSupplierActual.getAddressEntity().getId());
        Assertions.assertEquals(existingSupplierServiceModel.getAddress().getCity(), newSupplierActual.getAddressEntity().getCity());

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testEditMethodThrowsConstraintViolationException() throws Exception {

        when(mockValidationUtil.isValid(existingSupplierServiceModel)).thenReturn(false);

        Assertions.assertThrows(ConstraintViolationException.class, () -> this.supplierServiceToTest.edit(existingSupplierServiceModel));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testEditMethodThrowsNotFoundEntityExceptionForSupplierEntity() throws Exception {

        when(mockValidationUtil.isValid(existingSupplierServiceModel)).thenReturn(true);
        when(mockSupplierRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> this.supplierServiceToTest.edit(existingSupplierServiceModel));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testEditMethodThrowsNotFoundEntityExceptionForAddressEntity() throws Exception {

        when(mockValidationUtil.isValid(existingSupplierServiceModel)).thenReturn(true);
        when(mockSupplierRepository.findById(any(Long.class))).thenReturn(Optional.of(supplierEntity));
        when(mockAddressService.addressExists(any(Long.class))).thenReturn(false);

        Assertions.assertThrows(EntityNotFoundException.class, () -> this.supplierServiceToTest.edit(existingSupplierServiceModel));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testBlockMethod() {

        when(mockSupplierRepository.findById(any(Long.class))).thenReturn(Optional.of(supplierEntity));

        supplierServiceToTest.block(1L);

        ArgumentCaptor<SupplierEntity> argument = ArgumentCaptor.forClass(SupplierEntity.class);
        Mockito.verify(mockSupplierRepository, times(1)).saveAndFlush(argument.capture());
        SupplierEntity newSupplierActual = argument.getValue();

        Assertions.assertTrue(newSupplierActual.isBlocked());

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testBlockMethodThrowsEntityNotFoundException() {

        when(mockSupplierRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> this. supplierServiceToTest.block(1L));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testUnlockMethod() {

        supplierEntity.setBlocked(true);

        when(mockSupplierRepository.findById(any(Long.class))).thenReturn(Optional.of(supplierEntity));

        supplierServiceToTest.unblock(1L);

        ArgumentCaptor<SupplierEntity> argument = ArgumentCaptor.forClass(SupplierEntity.class);
        Mockito.verify(mockSupplierRepository, times(1)).saveAndFlush(argument.capture());
        SupplierEntity newSupplierActual = argument.getValue();

        Assertions.assertFalse(newSupplierActual.isBlocked());

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testUnlockMethodThrowsEntityNotFoundException() {

        when(mockSupplierRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> this. supplierServiceToTest.unblock(1L));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testGetSupplierTurnoverMethod() {

        LocalDateTime[] timeBorders = this.getTimeBorders();
        when(mockTimeBordersConvertor
                .getTimeBordersAsLocalDateTime(any(String.class), any(String.class), any(LocalDateTime.class))).thenReturn(timeBorders);

        when(mockOrderService.getDateTimeFirstArchiveOrder()).thenReturn(LocalDateTime.now());

        List<Object[]> result = this.getResult();
        when(mockSupplierRepository.findSupplierTurnover(any(LocalDateTime.class), any(LocalDateTime.class), any(String.class), any(Pageable.class)))
                .thenReturn(result);

        List<SupplierTurnoverViewModel> supplierTurnoverViewModels = this.supplierServiceToTest.getSupplierTurnover("", "", "null");

        Assertions.assertEquals(SUPPLIER_NAME, supplierTurnoverViewModels.get(0).getName());
        Assertions.assertEquals(BigDecimal.valueOf(1000), supplierTurnoverViewModels.get(0).getTurnover());
        Assertions.assertEquals(10, supplierTurnoverViewModels.get(0).getSoldItems());

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testGetTopSuppliersNamesMap(){

        LocalDateTime[] timeBorders = this.getTimeBorders();
        when(mockTimeBordersConvertor
                .getTimeBordersAsLocalDateTime(any(String.class), any(String.class), any(LocalDateTime.class))).thenReturn(timeBorders);

        String[] timeBordersPieChart = new String[]{"2021-03-12", "2021-03-15"};
        when(mockTimeBordersConvertor
                .getTimeBordersAsString(any(String.class), any(String.class), any(LocalDateTime.class))).thenReturn(timeBordersPieChart);


        when(mockOrderService.getDateTimeFirstArchiveOrder()).thenReturn(LocalDateTime.now());

        List<Object[]> result = this.getResult();
        when(mockSupplierRepository
                .findTopSuppliers(any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class))).thenReturn(result);

        HashMap<Integer, String> resultMap = new HashMap<>();
        resultMap.put(1, "2021-03-12");
        resultMap.put(2, "2021-03-15");
        resultMap.put(3, "supplier");

        when(mockTimeBordersConvertor.getBordersAndNamesMap(any(HashMap.class), any(String[].class), any(List.class))).thenReturn(resultMap);

        HashMap<Integer, String> topSuppliersNamesMap = this.supplierServiceToTest.getTopSuppliersNamesMap("", "");


        Assertions.assertEquals("2021-03-12", topSuppliersNamesMap.get(1));
        Assertions.assertEquals("supplier", topSuppliersNamesMap.get(3));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testGetTopSuppliersTurnoverMap(){

        LocalDateTime[] timeBorders = this.getTimeBorders();
        when(mockTimeBordersConvertor
                .getTimeBordersAsLocalDateTime(any(String.class), any(String.class), any(LocalDateTime.class))).thenReturn(timeBorders);

        when(mockOrderService.getDateTimeFirstArchiveOrder()).thenReturn(LocalDateTime.now());

        List<Object[]> result = this.getResult();
        when(mockSupplierRepository
                .findTopSuppliers(any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class))).thenReturn(result);

        HashMap<Integer, BigDecimal> topSuppliersTurnoverMap = this.supplierServiceToTest.getTopSuppliersTurnoverMap("", "");

        Assertions.assertEquals(BigDecimal.valueOf(1000), topSuppliersTurnoverMap.get(3));

    }

    private List<Object[]> getResult() {

        List<Object[]> result = new ArrayList<>();
        Object[] objects = new Object[3];
        objects[0] = "supplier";
        objects[1] = BigDecimal.valueOf(1000);
        objects[2] = 10;
        result.add(objects);
        return result;
    }

    private LocalDateTime[] getTimeBorders() {

        return new LocalDateTime[]{LocalDateTime.now(), LocalDateTime.now()};
    }

    private Pageable initPageable() {

        String option = "name";
        Sort sort = Sort.by(option).ascending();
        int page = 0;
        int pageSize = 1;

        return PageRequest.of(page, pageSize, sort);
    }

    private SupplierEntity createExistingSupplierEntity() {

        SupplierEntity supplierEntity = new SupplierEntity();
        supplierEntity.setId(1L);
        supplierEntity.setName("supplier");
        supplierEntity.setEmail("supplier@mail.bg");
        supplierEntity.setBlocked(false);

        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setId(1L);
        addressEntity.setRegion("Sofia city");
        addressEntity.setCity("Sofia");
        addressEntity.setStreet("Tintyava 15");
        addressEntity.setPhone("02111222");

        supplierEntity.setAddressEntity(addressEntity);

        return supplierEntity;
    }

    private SupplierServiceModel createSupplierServiceModel() {

        SupplierServiceModel supplierServiceModel = new SupplierServiceModel();
        supplierServiceModel.setName("supplier");
        supplierServiceModel.setEmail("supplier@mail.bg");

        AddressServiceModel addressServiceModel = new AddressServiceModel();
        addressServiceModel.setRegion("Sofia city");
        addressServiceModel.setCity("Sofia");
        addressServiceModel.setStreet("Tintyava 15");
        addressServiceModel.setPhone("02111222");

        supplierServiceModel.setAddress(addressServiceModel);

        return supplierServiceModel;
    }

    private SupplierServiceModel createExistingSupplierServiceModel() {

        SupplierServiceModel supplierServiceModel = this.createSupplierServiceModel();
        supplierServiceModel.setId(1L);
        supplierServiceModel.getAddress().setId(1L);

        return supplierServiceModel;
    }

}
