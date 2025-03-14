package org.johndoe.kitchensink.controllers;

import jakarta.validation.Valid;
import org.johndoe.kitchensink.dtos.MemberDto;
import org.johndoe.kitchensink.dtos.requests.AuthRequest;
import org.johndoe.kitchensink.dtos.requests.AuthResponse;
import org.johndoe.kitchensink.dtos.requests.RefreshTokenRequest;
import org.johndoe.kitchensink.dtos.requests.RefreshTokenResponse;
import org.johndoe.kitchensink.services.KeycloakAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * AuthController handles authentication-related endpoints.
 */
@RestController
@RequestMapping("/api/auth/")
public class AuthController {

    /**
     * The KeycloakAuthService used for authentication.
     */
    private final KeycloakAuthService authService;

    /**
     * Constructs an AuthController with the given KeycloakAuthService.
     *
     * @param authService the KeycloakAuthService to use for authentication
     */
    public AuthController(KeycloakAuthService authService) {
        this.authService = authService;
    }

    /**
     * Handles login requests.
     *
     * @param request the authentication request containing user credentials
     * @return a ResponseEntity containing the authentication response
     */
    @PreAuthorize("permitAll()")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return authService.login(request)
                .map(ResponseEntity::ok)
                .block();
    }

    /**
     * Handles refresh token requests.
     *
     * @param request the refresh token request containing the refresh token
     * @return a ResponseEntity containing the refresh token response
     */
    @PreAuthorize("permitAll()")
    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity
                .ok(authService.refreshToken(request.refreshToken())
                        .block());
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/register")
    public ResponseEntity<Map<String,String>> register(@Valid @RequestBody MemberDto request) {
        return authService.register(request)
                .then(Mono.just(ResponseEntity.ok(Map.of("message", "User registered successfully")))).block();
    }
}