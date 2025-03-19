package org.johndoe.kitchensink.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueInDatabaseValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueInDatabase {
    String message() default "Email and/or Phone number already registered, please use another";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
