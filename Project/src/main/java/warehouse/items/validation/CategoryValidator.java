package warehouse.items.validation;

import warehouse.categories.model.CategoryServiceModel;
import warehouse.categories.service.CategoryService;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryValidator implements ConstraintValidator<ValidCategory, String> {

private final CategoryService categoryService;

    public CategoryValidator(CategoryService categoryService) {
        this.categoryService = categoryService;
    }


    @Override
    public boolean isValid(String category, ConstraintValidatorContext constraintValidatorContext) {

       List<String> categories = this.categoryService.findAll().stream().map(CategoryServiceModel::getName).collect(Collectors.toList());
        return categories.contains(category);
    }
}
