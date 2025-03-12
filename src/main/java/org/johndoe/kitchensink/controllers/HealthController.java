package org.johndoe.kitchensink.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * HealthController handles health check and user information endpoints.
 */
@RestController
@RequestMapping("/")
public class HealthController {

    /**
     * Default constructor for HealthController.
     */
    public HealthController() {
    }

    /**
     * Handles health check requests.
     *
     * @return a string indicating the service is alive
     */
    @GetMapping
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("I'm alive!");
    }

    /**
     * Handles requests to get the authenticated user's information.
     *
     * @param principal the authenticated user's principal
     * @return the authenticated user's principal
     */
    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/user")
    public Principal user(Principal principal) {
        return principal;
    }
}