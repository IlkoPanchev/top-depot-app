package warehouse.users.validation;

import warehouse.users.validation.RoleValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RoleValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRole {

    String message() default "Not valid role";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
