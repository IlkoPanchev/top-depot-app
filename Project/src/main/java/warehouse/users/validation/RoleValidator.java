package warehouse.users.validation;

import warehouse.roles.model.RoleName;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RoleValidator implements ConstraintValidator<ValidRole, String> {


    @Override
    public boolean isValid(String role, ConstraintValidatorContext constraintValidatorContext) {
        boolean valid;

        List<String> roles = Arrays.stream(RoleName.values()).map(RoleName::name).collect(Collectors.toList());

        valid = role != null && roles.contains(role);

        return valid;
    }
}
