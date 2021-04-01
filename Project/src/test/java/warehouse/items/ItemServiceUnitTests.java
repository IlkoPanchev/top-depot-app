package warehouse.items;

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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import warehouse.addresses.model.AddressEntity;
import warehouse.categories.model.CategoryEntity;
import warehouse.categories.repository.CategoryRepository;
import warehouse.categories.service.CategoryService;
import warehouse.cloudinary.CloudinaryService;
import warehouse.customers.model.CustomerEntity;
import warehouse.customers.model.CustomerServiceModel;
import warehouse.customers.service.impl.CustomerServiceImpl;
import warehouse.items.model.ItemAddServiceModel;
import warehouse.items.model.ItemEntity;
import warehouse.items.model.ItemViewServiceModel;
import warehouse.items.repository.ItemRepository;
import warehouse.items.service.ItemService;
import warehouse.items.service.impl.ItemServiceImpl;
import warehouse.orders.service.OrderService;
import warehouse.suppliers.model.SupplierEntity;
import warehouse.suppliers.repository.SupplierRepository;
import warehouse.suppliers.service.SupplierService;
import warehouse.utils.time.TimeBordersConvertor;
import warehouse.utils.validation.ValidationUtil;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceUnitTests {

    private final String CATEGORY_NAME = "category_name";
    private final String SUPPLIER_NAME = "supplier_name";
    private final String ITEM_NAME = "item_name";

    private ItemService itemServiceToTest;
    private ItemEntity itemEntity;
    private ItemAddServiceModel itemAddServiceModel;
    private ItemAddServiceModel existingItemAddServiceModel;
    private CategoryEntity categoryEntity;
    private SupplierEntity supplierEntity;
    private Pageable pageable;


    @Mock
    ItemRepository mockItemRepository;
    @Mock
    CategoryRepository mockCategoryRepository;
    @Mock
    SupplierRepository mockSupplierRepository;
    @Mock
    CloudinaryService mockCloudinaryService;
    @Mock
    CategoryService mockCategoryService;
    @Mock
    SupplierService mockSupplierService;
    @Mock
    OrderService mockOrderService;
    @Mock
    TimeBordersConvertor mockTimeBordersConvertor;
    @Mock
    ValidationUtil mockValidationUtil;

    @BeforeEach
    public void SetUp(){
        this.itemServiceToTest = new ItemServiceImpl(new ModelMapper(),
                mockItemRepository,
                mockCategoryRepository,
                mockSupplierRepository,
                mockCloudinaryService,
                mockCategoryService,
                mockSupplierService,
                mockOrderService,
                mockTimeBordersConvertor,
                mockValidationUtil);
        this.itemEntity = this.createExistingItemEntity();
        this.itemAddServiceModel = this.createItemAddServiceModel();
        this.existingItemAddServiceModel = this.createExistingItemAddServiceModel();
        this.categoryEntity = this.createExistingCategoryEntity();
        this.supplierEntity = this.createExistingSupplierEntity();
        this.pageable = this.initPageable();
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testAddMethodWithValidServiceModel() throws IOException {

        when(mockValidationUtil.isValid(any(ItemAddServiceModel.class))).thenReturn(true);
        when(mockCategoryService.getAllCategoryNames()).thenReturn(List.of(CATEGORY_NAME));
        when(mockCategoryService.findByName(any(String.class))).thenReturn(categoryEntity);
        when(mockSupplierService.getAllSupplierNames()).thenReturn(List.of(SUPPLIER_NAME));
        when(mockSupplierService.findByName(any(String.class))).thenReturn(supplierEntity);
        when(mockItemRepository.saveAndFlush(any(ItemEntity.class))).thenReturn(itemEntity);

        itemServiceToTest.add(itemAddServiceModel);

        ArgumentCaptor<ItemEntity> argument = ArgumentCaptor.forClass(ItemEntity.class);
        Mockito.verify(mockItemRepository, times(1)).saveAndFlush(argument.capture());
        ItemEntity itemEntityActual = argument.getValue();

        Assertions.assertEquals(itemAddServiceModel.getName(), itemEntityActual.getName());
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testAddMethodThrowsConstraintViolationException(){

        when(mockValidationUtil.isValid(any(ItemAddServiceModel.class))).thenReturn(false);

        Assertions.assertThrows(ConstraintViolationException.class, () ->  itemServiceToTest.add(itemAddServiceModel));
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testFindAllPageableMethod(){

        Page<ItemEntity> itemEntities = new PageImpl<>(List.of(itemEntity), pageable, 1L);

        when(mockItemRepository.findAll(any(Pageable.class))).thenReturn(itemEntities);

        Page<ItemViewServiceModel> itemViewServiceModels = itemServiceToTest.findAllPageable(pageable);

        Assertions.assertEquals(itemViewServiceModels.getTotalElements(), 1L);
        Assertions.assertEquals(itemViewServiceModels.getTotalPages(), 1);
    }

    @Test@MockitoSettings(strictness = Strictness.WARN)
    public void testFindAllPageableUnblockedMethod(){

        Page<ItemEntity> itemEntities = new PageImpl<>(List.of(itemEntity), pageable, 1L);

        when(mockItemRepository.findAllByBlockedFalse(any(Pageable.class))).thenReturn(itemEntities);

        Page<ItemViewServiceModel> itemViewServiceModels = itemServiceToTest.findAllPageableUnblocked(pageable);

        Assertions.assertEquals(itemViewServiceModels.getTotalElements(), 1L);
        Assertions.assertEquals(itemViewServiceModels.getTotalPages(), 1);

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testFindByIdMethod() {

        when(mockItemRepository.findById(any(Long.class))).thenReturn(Optional.of(itemEntity));

        ItemViewServiceModel itemViewServiceModel = this.itemServiceToTest.findById(1L);


        Assertions.assertEquals(1L, itemViewServiceModel.getId());
        Assertions.assertEquals(ITEM_NAME, itemViewServiceModel.getName());


    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testFindByIdMethodThrowsEntityNotFoundException() {

        when(mockItemRepository.findById(any(Long.class))).thenThrow(EntityNotFoundException.class);

        Assertions.assertThrows(EntityNotFoundException.class, () -> this.itemServiceToTest.findById(5L));
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testSearchMethod(){

        Page<ItemEntity> itemEntities = new PageImpl<>(List.of(itemEntity), pageable, 1L);

        when(mockItemRepository.search(any(String.class), any(Pageable.class))).thenReturn(itemEntities);

        Page<ItemViewServiceModel> itemViewServiceModels = itemServiceToTest.search(ITEM_NAME, pageable);

        Assertions.assertEquals(itemViewServiceModels.getTotalElements(), 1L);
        Assertions.assertEquals(itemViewServiceModels.getTotalPages(), 1);
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testSearchUnblockedMethod(){

        Page<ItemEntity> itemEntities = new PageImpl<>(List.of(itemEntity), pageable, 1L);

        when(mockItemRepository.searchUnblocked(any(String.class), any(Pageable.class))).thenReturn(itemEntities);

        Page<ItemViewServiceModel> itemViewServiceModels = itemServiceToTest.searchUnblocked(ITEM_NAME, pageable);

        Assertions.assertEquals(itemViewServiceModels.getTotalElements(), 1L);
        Assertions.assertEquals(itemViewServiceModels.getTotalPages(), 1);

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testItemExistMethodReturnFalse(){

        Optional<ItemEntity> itemEntity = Optional.empty();

        when(mockItemRepository.findByName(any(String.class))).thenReturn(itemEntity);

        Assertions.assertFalse(this.itemServiceToTest.itemExists(ITEM_NAME));
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testCustomerExistMethodReturnTrue(){

        Optional<ItemEntity> itemEntity = Optional.of(this.createExistingItemEntity());

        when(mockItemRepository.findByName(any(String.class))).thenReturn(itemEntity);

        Assertions.assertTrue(this.itemServiceToTest.itemExists(ITEM_NAME));
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testBlockMethod() {

        when(mockItemRepository.findById(any(Long.class))).thenReturn(Optional.of(itemEntity));

        itemServiceToTest.block(1L);

        ArgumentCaptor<ItemEntity> argument = ArgumentCaptor.forClass(ItemEntity.class);
        Mockito.verify(mockItemRepository, times(1)).saveAndFlush(argument.capture());
        ItemEntity newItemActual = argument.getValue();

        Assertions.assertTrue(newItemActual.isBlocked());

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testBlockMethodThrowsEntityNotFoundException() {

        when(mockItemRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> this. itemServiceToTest.block(1L));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testUnlockMethod() {

        itemEntity.setBlocked(true);

        when(mockItemRepository.findById(any(Long.class))).thenReturn(Optional.of(itemEntity));

        itemServiceToTest.unblock(1L);

        ArgumentCaptor<ItemEntity> argument = ArgumentCaptor.forClass(ItemEntity.class);
        Mockito.verify(mockItemRepository, times(1)).saveAndFlush(argument.capture());
        ItemEntity newItemActual = argument.getValue();

        Assertions.assertFalse(newItemActual.isBlocked());

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testUnlockMethodThrowsEntityNotFoundException() {

        when(mockItemRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> this. itemServiceToTest.unblock(1L));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testEditMethodWithValidServiceModel() throws Exception {




        when(mockValidationUtil.isValid(any(ItemAddServiceModel.class))).thenReturn(true);
        when(mockCategoryService.getAllCategoryNames()).thenReturn(List.of(CATEGORY_NAME));
        when(mockCategoryService.findByName(any(String.class))).thenReturn(categoryEntity);
        when(mockSupplierService.getAllSupplierNames()).thenReturn(List.of(SUPPLIER_NAME));
        when(mockSupplierService.findByName(any(String.class))).thenReturn(supplierEntity);
        when(mockItemRepository.findById(any(Long.class))).thenReturn(Optional.of(itemEntity));
        when(mockItemRepository.saveAndFlush(any(ItemEntity.class))).thenReturn(itemEntity);

        itemServiceToTest.edit(existingItemAddServiceModel);

        ArgumentCaptor<ItemEntity> argument = ArgumentCaptor.forClass(ItemEntity.class);
        Mockito.verify(mockItemRepository, times(1)).saveAndFlush(argument.capture());
        ItemEntity itemEntityActual = argument.getValue();

        Assertions.assertEquals(itemAddServiceModel.getName(), itemEntityActual.getName());

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testEditMethodThrowsConstraintViolationException() throws Exception {


        when(mockValidationUtil.isValid(existingItemAddServiceModel)).thenReturn(false);

        Assertions.assertThrows(ConstraintViolationException.class, () -> this.itemServiceToTest.edit(existingItemAddServiceModel));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testEditMethodThrowsNotFoundEntityExceptionForItemEntity() throws Exception {

        when(mockValidationUtil.isValid(existingItemAddServiceModel)).thenReturn(true);
        when(mockItemRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> this.itemServiceToTest.edit(existingItemAddServiceModel));

    }

    private ItemAddServiceModel createExistingItemAddServiceModel() {

        ItemAddServiceModel itemAddServiceModel = this.createItemAddServiceModel();
        itemAddServiceModel.setId(1L);

        return itemAddServiceModel;
    }


    private ItemEntity createExistingItemEntity() {

        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setId(1L);
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

    private ItemAddServiceModel createItemAddServiceModel() {

        ItemAddServiceModel itemAddServiceModel = new ItemAddServiceModel();
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
                MediaType.IMAGE_JPEG_VALUE,
                "Hello, World!".getBytes()
        );
        return file;
    }

    private Pageable initPageable() {

        String option = "name";
        Sort sort = Sort.by(option).ascending();
        int page = 0;
        int pageSize = 1;

        return PageRequest.of(page, pageSize, sort);
    }

}
