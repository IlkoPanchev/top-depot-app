package warehouse.categories.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.validation.annotation.Validated;
import warehouse.categories.model.CategoryEntity;
import warehouse.categories.model.CategoryServiceModel;
import warehouse.categories.repository.CategoryRepository;
import warehouse.customers.model.CustomerServiceModel;
import warehouse.validated.OnCreate;
import warehouse.validated.OnUpdate;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
@Validated
public interface CategoryService {

    void initCategories();

    List<CategoryServiceModel> findAll();

    Page<CategoryServiceModel> findAllPageable(Pageable pageable);

    CategoryEntity findByName(String name);

    Page<CategoryServiceModel> search(String keyword, Pageable pageable);

    boolean categoryExistsByName(String name);

    CategoryServiceModel findById(Long id);

    @Validated(OnCreate.class)
    CategoryServiceModel add(@Valid CategoryServiceModel categoryServiceModel);

//    CategoryServiceModel deleteById(Long id);

    CategoryServiceModel block(Long id);

    CategoryServiceModel unblock(Long id);

    @Validated(OnUpdate.class)
    CategoryServiceModel edit(@Valid CategoryServiceModel categoryServiceModel);

    CategoryEntity getById(long id);

    List<String> getAllCategoryNames();
}
