package warehouse.items.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CategoryValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCategory {

    String message() default "Not valid category";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
