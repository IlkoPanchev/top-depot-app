package warehouse.users.validation;

import warehouse.users.validation.DepartmentValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DepartmentValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDepartment {

    String message() default "Not valid department";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
