package warehouse.users.validation;

import warehouse.departments.model.DepartmentName;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DepartmentValidator implements ConstraintValidator<ValidDepartment, DepartmentName> {
    @Override
    public boolean isValid(DepartmentName departmentName, ConstraintValidatorContext context) {

        boolean valid;

        List<String> departmentNames = Arrays.stream(DepartmentName.values())
                .map(DepartmentName::name).collect(Collectors.toList());

        valid = departmentName != null && departmentNames.contains(departmentName.name());

        return valid;
    }
}
