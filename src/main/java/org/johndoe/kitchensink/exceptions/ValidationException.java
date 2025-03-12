package org.johndoe.kitchensink.exceptions;

import java.util.List;

/**
 * Exception thrown when validation errors occur.
 */
public class ValidationException extends RuntimeException {

    /**
     * Constructs a new ValidationException with the specified detail message.
     *
     * @param message the detail message
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new ValidationException with a list of validation errors.
     *
     * @param validationErrors the list of validation errors
     */
    public ValidationException(List<String> validationErrors) {
        super(validationErrors.toString());
    }
}
