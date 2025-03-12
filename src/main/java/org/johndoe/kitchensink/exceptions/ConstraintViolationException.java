package org.johndoe.kitchensink.exceptions;

/**
 * Exception thrown when a constraint violation occurs.
 */
public class ConstraintViolationException extends RuntimeException {

    /**
     * Constructs a new ConstraintViolationException with the specified detail message.
     *
     * @param message the detail message
     */
    public ConstraintViolationException(String message) {
        super(message);
    }
}
