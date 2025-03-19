package org.johndoe.kitchensink.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PasswordRulesValidator implements ConstraintValidator<PasswordRules, char[]> {
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern SPECIAL_CHAR = Pattern.compile("[@$!%*?&]");

    @Override
    public boolean isValid(char[] value, ConstraintValidatorContext context) {
        if (value == null || value.length < 8) {
            return false;
        }

        String password = new String(value);

        return containsNonWhitespace(password)
                && UPPERCASE.matcher(password).find()
                && LOWERCASE.matcher(password).find()
                && DIGIT.matcher(password).find()
                && SPECIAL_CHAR.matcher(password).find();
    }

    private boolean containsNonWhitespace(String value) {
        for (char c : value.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                return true;
            }
        }
        return false;
    }
}
