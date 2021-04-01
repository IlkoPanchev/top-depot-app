package warehouse.items.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SupplierValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSupplier {

    String message() default "Not valid supplier";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
