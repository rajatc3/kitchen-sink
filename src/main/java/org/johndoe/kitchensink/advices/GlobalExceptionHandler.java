package org.johndoe.kitchensink.advices;

import org.johndoe.kitchensink.exceptions.ApplicationException;
import org.johndoe.kitchensink.exceptions.UserNotFoundException;
import org.johndoe.kitchensink.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * GlobalExceptionHandler handles all exceptions thrown by the application
 * and provides appropriate HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Logger for GlobalExceptionHandler.
     */
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    /**
     * Response map for exception handling.
     */
    private final Map<String, Object> response = new HashMap<>();

    /**
     * Default constructor for GlobalExceptionHandler.
     */
    public GlobalExceptionHandler() {
    }

    /**
     * Handles UserNotFoundException and returns a NOT_FOUND response.
     *
     * @param ex the UserNotFoundException
     * @return a ResponseEntity containing the error message and NOT_FOUND status
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFoundException(UserNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return getErrorResponse(ex.getMessage(), status);
    }

    /**
     * Handles AccessDeniedException and returns an UNAUTHORIZED response.
     *
     * @param ex the AccessDeniedException
     * @return a ResponseEntity containing the error message and UNAUTHORIZED status
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return getErrorResponse("Access Denied! Please contact administrator.", status);
    }

    /**
     * Handles BadJwtException and returns a BAD_REQUEST response.
     *
     * @param ex the BadJwtException
     * @return a ResponseEntity containing the error message and BAD_REQUEST status
     */
    @ExceptionHandler(BadJwtException.class)
    public ResponseEntity<Map<String, Object>> handleBadJwtException(BadJwtException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return getErrorResponse("Access Denied! Please re-validate your token.", status);
    }

    /**
     * Handles generic exceptions and returns an INTERNAL_SERVER_ERROR response.
     *
     * @param ex the Exception
     * @return a ResponseEntity containing the error message and INTERNAL_SERVER_ERROR status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        logger.error("An error occurred: ", ex);
        return getErrorResponse("Something bad happened! Please contact administrator.", status);
    }

    /**
     * Handles ApplicationException and returns an INTERNAL_SERVER_ERROR response.
     * These exceptions are thrown when we need to show internal error to the user and not a static something went wrong message.
     *
     * @param ex the ApplicationException
     * @return a ResponseEntity containing the error message and INTERNAL_SERVER_ERROR status
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<Map<String, Object>> handleApplicationException(ApplicationException ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        logger.error("Application Exception occurred: {} ",ex.getMessage(), ex);
        return getErrorResponse(ex.getMessage(), status);
    }

    /**
     * Handles ValidationException and returns a BAD_REQUEST response.
     *
     * @param ex the ValidationException
     * @return a ResponseEntity containing the error messages and BAD_REQUEST status
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ValidationException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logger.warn("Validation failed: {}", ex.getMessage());
        return getErrorResponse(
                Arrays.stream(ex.getMessage().replaceAll("[\\[\\]\"]", "").split(","))
                        .map(String::trim)
                        .collect(Collectors.toList()),
                status
        );
    }

    /**
     * Handles MethodArgumentNotValidException and returns a BAD_REQUEST response.
     *
     * @param ex the MethodArgumentNotValidException
     * @return a ResponseEntity containing the error messages and BAD_REQUEST status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        List<String> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.toSet()).stream().toList();

        return getErrorResponse(errors, status);
    }

    /**
     * Constructs a ResponseEntity with a list of error messages and the given HTTP status.
     *
     * @param errors the list of error messages
     * @param status the HTTP status
     * @return a ResponseEntity containing the error messages and the given status
     */
    ResponseEntity<Map<String, Object>> getErrorResponse(List<String> errors, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.value());
        response.put("errors", errors);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Constructs a ResponseEntity with a single error message and the given HTTP status.
     *
     * @param errorMessage the error message
     * @param status       the HTTP status
     * @return a ResponseEntity containing the error message and the given status
     */
    ResponseEntity<Map<String, Object>> getErrorResponse(String errorMessage, HttpStatus status) {
        response.put("error", errorMessage);
        response.put("status", status.value());
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(status).body(response);
    }
}
