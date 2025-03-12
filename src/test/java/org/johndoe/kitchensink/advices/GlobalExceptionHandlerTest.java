package org.johndoe.kitchensink.advices;

import org.johndoe.kitchensink.exceptions.UserNotFoundException;
import org.johndoe.kitchensink.exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.BadJwtException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    public void testHandleUserNotFoundException() {
        UserNotFoundException ex = new UserNotFoundException("User not found");
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleUserNotFoundException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User not found", response.getBody().get("error"));
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().get("status"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    public void testHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Access Denied! Please contact administrator.");
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleAccessDeniedException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Access Denied! Please contact administrator.", response.getBody().get("error"));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().get("status"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    public void testHandleValidationException() {
        ValidationException ex = new ValidationException("Validation failed");
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleValidationException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(List.of("Validation failed"), response.getBody().get("errors"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().get("status"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    public void testHandleBadJwtException() {
        BadJwtException ex = new BadJwtException("Bad JWT");
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleBadJwtException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Access Denied! Please re-validate your token.", response.getBody().get("error"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().get("status"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    public void testHandleException() {
        Exception ex = new Exception("Something bad happened");
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Something bad happened! Please contact administrator.", response.getBody().get("error"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().get("status"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    public void testGetErrorResponse() {
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.getErrorResponse("Error", HttpStatus.BAD_REQUEST);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error", response.getBody().get("error"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().get("status"));
        assertNotNull(response.getBody().get("timestamp"));
    }
}