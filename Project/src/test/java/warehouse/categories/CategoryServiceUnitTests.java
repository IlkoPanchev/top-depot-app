package warehouse.categories;

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
import warehouse.categories.model.CategoryEntity;
import warehouse.categories.model.CategoryServiceModel;
import warehouse.categories.repository.CategoryRepository;
import warehouse.categories.service.CategoryService;
import warehouse.categories.service.impl.CategoryServiceImpl;
import warehouse.customers.model.CustomerEntity;
import warehouse.customers.model.CustomerServiceModel;
import warehouse.utils.validation.ValidationUtil;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceUnitTests {

    private final String CATEGORY_NAME = "category_name";

    private CategoryService categoryServiceToTest;
    private CategoryEntity categoryEntity;
    private CategoryServiceModel categoryServiceModel;
    private Pageable pageable;

    @Mock
    CategoryRepository mockCategoryRepository;
    @Mock
    ValidationUtil mockValidationUtil;

    @BeforeEach
    public void setUp(){
        this.categoryServiceToTest = new CategoryServiceImpl(mockCategoryRepository,
                new ModelMapper(),
                mockValidationUtil);
        this.categoryEntity = this.createExistingCategoryEntity();
        this.categoryServiceModel = this.createExistingCategoryServiceModel();
        this.pageable = this.initPageable();
    }

    @Test@MockitoSettings(strictness = Strictness.WARN)
    public void testFindAllMethod(){

        when(mockCategoryRepository.findAll()).thenReturn(List.of(categoryEntity));

        List<CategoryServiceModel> categoryServiceModels = this.categoryServiceToTest.findAll();

        Assertions.assertEquals(categoryServiceModels.size(), 1);
        Assertions.assertEquals(categoryServiceModels.get(0).getName(), CATEGORY_NAME);

    }

    @Test@MockitoSettings(strictness = Strictness.WARN)
    public void testFindAllPageableMethod(){

        Page<CategoryEntity> categoryEntities = new PageImpl<>(List.of(categoryEntity), pageable, 1L);

        when(mockCategoryRepository.findAll(any(Pageable.class))).thenReturn(categoryEntities);

        Page<CategoryServiceModel> categoryServiceModelPage = categoryServiceToTest.findAllPageable(pageable);

        Assertions.assertEquals(categoryServiceModelPage.getTotalElements(), 1L);
        Assertions.assertEquals(categoryServiceModelPage.getTotalPages(), 1);

    }

    @Test@MockitoSettings(strictness = Strictness.WARN)
    public void testFindByNameMethod(){

        when(mockCategoryRepository.findByName(any(String.class))).thenReturn(Optional.of(categoryEntity));

        CategoryEntity existingCategoryEntity  = categoryServiceToTest.findByName(CATEGORY_NAME);

        Assertions.assertEquals(existingCategoryEntity.getName(), CATEGORY_NAME);

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testFindByNameMethodThrowsEntityNotFoundException() {

        when(mockCategoryRepository.findByName(any(String.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> this. categoryServiceToTest.findByName(CATEGORY_NAME));

    }


    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testSearchMethod(){

        Page<CategoryEntity> categoryEntities = new PageImpl<>(List.of(categoryEntity), pageable, 1L);

        when(mockCategoryRepository.search(any(String.class), any(Pageable.class))).thenReturn(categoryEntities);

        Page<CategoryServiceModel> categoryServiceModelPage = categoryServiceToTest.search(CATEGORY_NAME, pageable);

        Assertions.assertEquals(categoryServiceModelPage.getTotalElements(), 1L);
        Assertions.assertEquals(categoryServiceModelPage.getTotalPages(), 1);
    }

    @Test@MockitoSettings(strictness = Strictness.WARN)
    public void testCategoryExistByNameMethodReturnsTrue(){

        when(mockCategoryRepository.findByName(any(String.class))).thenReturn(Optional.of(categoryEntity));

        boolean result  = categoryServiceToTest.categoryExistsByName(CATEGORY_NAME);

        Assertions.assertTrue(result);

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testCategoryExistByNameMethodReturnsFalse(){


        when(mockCategoryRepository.findByName(any(String.class))).thenReturn(Optional.empty());

        boolean result  = categoryServiceToTest.categoryExistsByName(CATEGORY_NAME);

        Assertions.assertFalse(result);

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testFindByIdMethod() {

        when(mockCategoryRepository.findById(any(Long.class))).thenReturn(Optional.of(categoryEntity));

        CategoryServiceModel categoryServiceModel = this.categoryServiceToTest.findById(1L);


        Assertions.assertEquals(categoryServiceModel.getId(), 1L);
        Assertions.assertEquals(categoryServiceModel.getName(), CATEGORY_NAME);


    }


    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testFindByIdMethodThrowsEntityNotFoundException() {

        when(mockCategoryRepository.findById(any(Long.class))).thenThrow(EntityNotFoundException.class);

        Assertions.assertThrows(EntityNotFoundException.class, () -> this.categoryServiceToTest.findById(5L));
    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testBlockMethod() {

        when(mockCategoryRepository.findById(any(Long.class))).thenReturn(Optional.of(categoryEntity));

        categoryServiceToTest.block(1L);

        ArgumentCaptor<CategoryEntity> argument = ArgumentCaptor.forClass(CategoryEntity.class);
        Mockito.verify(mockCategoryRepository, times(1)).saveAndFlush(argument.capture());
        CategoryEntity newCategoryActual = argument.getValue();

        Assertions.assertTrue(newCategoryActual.isBlocked());

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testBlockMethodThrowsEntityNotFoundException() {

        when(mockCategoryRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> this. categoryServiceToTest.block(1L));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testUnlockMethod() {

        categoryEntity.setBlocked(true);

        when(mockCategoryRepository.findById(any(Long.class))).thenReturn(Optional.of(categoryEntity));

        categoryServiceToTest.unblock(1L);

        ArgumentCaptor<CategoryEntity> argument = ArgumentCaptor.forClass(CategoryEntity.class);
        Mockito.verify(mockCategoryRepository, times(1)).saveAndFlush(argument.capture());
        CategoryEntity newCategoryActual = argument.getValue();

        Assertions.assertFalse(newCategoryActual.isBlocked());

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testUnlockMethodThrowsEntityNotFoundException() {

        when(mockCategoryRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> this. categoryServiceToTest.unblock(1L));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testEditMethodWithValidServiceModel() throws Exception {

        when(mockCategoryRepository.findById(any(Long.class))).thenReturn(Optional.of(categoryEntity));
        when(mockValidationUtil.isValid(categoryServiceModel)).thenReturn(true);
        when(mockCategoryRepository.saveAndFlush(any(CategoryEntity.class)))
                .thenReturn(categoryEntity);

        categoryServiceToTest.edit(categoryServiceModel);

        ArgumentCaptor<CategoryEntity> argument = ArgumentCaptor.forClass(CategoryEntity.class);
        Mockito.verify(mockCategoryRepository, times(1)).saveAndFlush(argument.capture());
        CategoryEntity newCategoryActual = argument.getValue();

        Assertions.assertEquals(categoryServiceModel.getId(), newCategoryActual.getId());
        Assertions.assertEquals(categoryServiceModel.getName(), newCategoryActual.getName());


    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testEditMethodThrowsConstraintViolationException() throws Exception {

        when(mockValidationUtil.isValid(categoryServiceModel)).thenReturn(false);

        Assertions.assertThrows(ConstraintViolationException.class, () -> this.categoryServiceToTest.edit(categoryServiceModel));

    }

    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testEditMethodThrowsNotFoundEntityExceptionForCategoryEntity() throws Exception {

        when(mockValidationUtil.isValid(categoryServiceModel)).thenReturn(true);
        when(mockCategoryRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> this.categoryServiceToTest.edit(categoryServiceModel));

    }


    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testGetByIdMethod() {

        when(mockCategoryRepository.findById(any(Long.class))).thenReturn(Optional.of(categoryEntity));

        CategoryEntity existingCategory = this.categoryServiceToTest.getById(1L);


        Assertions.assertEquals(existingCategory.getId(), 1L);
        Assertions.assertEquals(existingCategory.getName(), CATEGORY_NAME);

    }


    @Test
    @MockitoSettings(strictness = Strictness.WARN)
    public void testGetByIdMethodThrowsEntityNotFoundException() {

        when(mockCategoryRepository.findById(any(Long.class))).thenThrow(EntityNotFoundException.class);

        Assertions.assertThrows(EntityNotFoundException.class, () -> this.categoryServiceToTest.getById(5L));
    }




    private Pageable initPageable() {

        String option = "name";
        Sort sort = Sort.by(option).ascending();
        int page = 0;
        int pageSize = 1;

        return PageRequest.of(page, pageSize, sort);
    }


    private CategoryEntity createExistingCategoryEntity(){

        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setId(1L);
        categoryEntity.setName("category_name");
        categoryEntity.setDescription("description");


        return categoryEntity;

    }

    private CategoryServiceModel createExistingCategoryServiceModel(){

        CategoryServiceModel categoryServiceModel = new CategoryServiceModel();
        categoryServiceModel.setId(1L);
        categoryServiceModel.setName("category_name");
        categoryServiceModel.setDescription("description");

        return categoryServiceModel;

    }
}
