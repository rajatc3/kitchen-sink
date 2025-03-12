package org.johndoe.kitchensink.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotBlankCharArrayValidator implements ConstraintValidator<NotBlankCharArray, char[]> {
    @Override
    public boolean isValid(char[] value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        for (char c : value) {
            if (!Character.isWhitespace(c)) {
                return true;
            }
        }
        return false;
    }
}
