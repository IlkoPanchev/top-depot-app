package warehouse.categories.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import warehouse.categories.model.CategoryEntity;
import warehouse.categories.model.CategoryServiceModel;
import warehouse.categories.repository.CategoryRepository;
import warehouse.categories.service.CategoryService;
import warehouse.utils.validation.ValidationUtil;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import static warehouse.constants.GlobalConstants.*;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, ModelMapper modelMapper, ValidationUtil validationUtil) {

        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
    }


    @Override
    public void initCategories() {

        if(this.categoryRepository.count() == 0){

            CATEGORY_NAMES.forEach(this::initCategory);

        }
    }

    private void initCategory(String name, String description) {

        CategoryEntity categoryEntityPrinters = new CategoryEntity();
        categoryEntityPrinters.setName(name);
        categoryEntityPrinters.setDescription(description);

        this.categoryRepository.saveAndFlush(categoryEntityPrinters);
    }

    @Override
    public List<CategoryServiceModel> findAll() {

        return this.categoryRepository.findAll()
                .stream()
                .map(categoryEntity -> this.modelMapper.map(categoryEntity, CategoryServiceModel.class))
                .collect(Collectors.toList());
    }

    @Override
    public Page<CategoryServiceModel> findAllPageable(Pageable pageable) {

        Page<CategoryEntity> categoryEntities = this.categoryRepository.findAll(pageable);

        List<CategoryServiceModel> categoryServiceModels = categoryEntities.stream()
                .map(categoryEntity -> this.modelMapper.map(categoryEntity, CategoryServiceModel.class)).collect(Collectors.toList());

        Page<CategoryServiceModel> categoryServiceModelPage = new PageImpl<>(categoryServiceModels, pageable, categoryEntities.getTotalElements());

        return categoryServiceModelPage;
    }

    @Override
    public CategoryEntity findByName(String name) {

        CategoryEntity categoryEntity = this.categoryRepository.
                findByName(name).orElseThrow(() -> new EntityNotFoundException("Not found category with name: " + name));

        return categoryEntity;
    }

    @Override
    public Page<CategoryServiceModel> search(String keyword, Pageable pageable) {

        Page<CategoryEntity> categoryEntities = this.categoryRepository.search(keyword, pageable);

        List<CategoryServiceModel> categoryServiceModels = categoryEntities
                .stream()
                .map(categoryEntity -> this.modelMapper.map(categoryEntity, CategoryServiceModel.class))
                .collect(Collectors.toList());

        Page<CategoryServiceModel> categoryServiceModelPage = new PageImpl<>(categoryServiceModels, pageable, categoryEntities.getTotalElements());

        return categoryServiceModelPage;
    }

    @Override
    public boolean categoryExistsByName(String name) {

        Optional<CategoryEntity> categoryEntity = this.categoryRepository.findByName(name);

        return categoryEntity.isPresent();
    }

    @Override
    public CategoryServiceModel findById(Long id) {

        CategoryEntity categoryEntity = this.categoryRepository
                .findById(id).orElseThrow(() -> new EntityNotFoundException("Not found category with id: " + id));

        return this.modelMapper.map(categoryEntity, CategoryServiceModel.class);
    }

    @Override
    public CategoryServiceModel add(CategoryServiceModel categoryServiceModel) {

        if (this.validationUtil.isValid(categoryServiceModel)){

            CategoryEntity categoryEntity = this.modelMapper.map(categoryServiceModel, CategoryEntity.class);
            categoryEntity = this.categoryRepository.saveAndFlush(categoryEntity);
            categoryServiceModel = this.modelMapper.map(categoryEntity, CategoryServiceModel.class);

        }
        else {

            throw  new ConstraintViolationException(this.validationUtil.getViolations(categoryServiceModel));

        }
        return categoryServiceModel;
    }


    @Override
    public CategoryServiceModel block(Long id) {

        CategoryEntity categoryEntity = this.categoryRepository
                .findById(id).orElseThrow(() -> new EntityNotFoundException("Not found category with id: " + id));

        categoryEntity.setBlocked(true);
        this.categoryRepository.saveAndFlush(categoryEntity);

        return this.modelMapper.map(categoryEntity, CategoryServiceModel.class);
    }

    @Override
    public CategoryServiceModel unblock(Long id) {

        CategoryEntity categoryEntity = this.categoryRepository
                .findById(id).orElseThrow(() -> new EntityNotFoundException("Not found category with id: " + id));

        categoryEntity.setBlocked(false);
        this.categoryRepository.saveAndFlush(categoryEntity);

        return this.modelMapper.map(categoryEntity, CategoryServiceModel.class);
    }

    @Override
    public CategoryServiceModel edit(CategoryServiceModel categoryServiceModel) {

        if (this.validationUtil.isValid(categoryServiceModel)){

            long id = categoryServiceModel.getId();
            CategoryEntity categoryEntity = this.categoryRepository
                    .findById(id).orElseThrow(() -> new EntityNotFoundException("Not found category with id: " + id));

            boolean currentStatus = categoryEntity.isBlocked();
            categoryEntity = this.modelMapper.map(categoryServiceModel, CategoryEntity.class);
            categoryEntity.setBlocked(currentStatus);
            categoryEntity = this.categoryRepository.saveAndFlush(categoryEntity);
            categoryServiceModel = this.modelMapper.map(categoryEntity, CategoryServiceModel.class);

        }
        else {

            throw  new ConstraintViolationException(this.validationUtil.getViolations(categoryServiceModel));

        }
        return categoryServiceModel;
    }

    @Override
    public CategoryEntity getById(long id) {

        CategoryEntity categoryEntity = this.categoryRepository
                .findById(id).orElseThrow(() -> new EntityNotFoundException("Not found category with id: " + id));

        return categoryEntity;
    }

    @Override
    public List<String> getAllCategoryNames() {

        return this.categoryRepository.findAllCategoryNames();
    }
}
