package blogic.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = DTOLogicConsistencyConstraintValidator.class)
@Target(ElementType.TYPE)
public @interface DTOLogicValid {

    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
