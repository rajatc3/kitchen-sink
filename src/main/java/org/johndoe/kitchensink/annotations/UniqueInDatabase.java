package org.johndoe.kitchensink.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UniqueInDatabaseValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueInDatabase {
    String message() default "Email and/or Phone number already registered, please use another";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
