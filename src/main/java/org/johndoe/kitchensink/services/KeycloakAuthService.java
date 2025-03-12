package org.johndoe.kitchensink.services;

import lombok.Data;
import org.johndoe.kitchensink.dtos.*;
import org.johndoe.kitchensink.exceptions.ApplicationException;
import org.johndoe.kitchensink.security.config.JwtAuthConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Service for handling authentication with Keycloak.
 */
@Data
@Service
public class KeycloakAuthService {

    /**
     * WebClient for KeycloakAuthService.
     */
    private final WebClient webClient;

    /**
     * JwtAuthConverter for converting JWT tokens to authentication tokens.
     */
    private final JwtAuthConverter jwt;

    /**
     * Master client ID for Keycloak.
     */
    private static final String MASTER_CLIENTID = "admin-cli";

    /**
     * Keycloak URL.
     */
    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String keycloakUrl;
    /**
     * MemberService for creating members.
     */
    private MemberService memberService;

    /**
     * Client ID for Keycloak.
     */
    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;
    /**
     * Keycloak base URL.
     */
    @Value("${keycloak.base-url}")
    private String keycloakBaseUrl;
    /**
     * Authorization grant type for Keycloak.
     */
    @Value("${spring.security.oauth2.client.registration.keycloak.authorization-grant-type}")
    private String grantType;

    /**
     * Client secret for Keycloak.
     */
    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;

    /**
     * Constructs a KeycloakAuthService with the given WebClient, JwtAuthConverter, and MemberService.
     *
     * @param webClientBuilder the WebClient.Builder to use for creating a WebClient
     * @param jwt the JwtAuthConverter to use for converting JWT tokens
     * @param memberService the MemberService to use for creating members
     */
    public KeycloakAuthService(WebClient.Builder webClientBuilder, JwtAuthConverter jwt, MemberService memberService) {
        this.webClient = webClientBuilder.build();
        this.jwt = jwt;
        this.memberService = memberService;
    }

    /**
     * Logs in a user with the given authentication request.
     *
     * @param request the authentication request
     * @return a Mono emitting the authentication response
     */
    public Mono<AuthResponse> login(AuthRequest request) {
        return webClient.post()
                .uri(keycloakUrl + "/protocol/openid-connect/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("client_id=" + clientId +
                        "&client_secret=" + clientSecret +
                        "&grant_type==" + grantType +
                        "&username=" + request.email() +
                        "&password=" + request.password())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new RuntimeException("Error: " + error))))
                .bodyToMono(KeycloakTokenResponse.class)
                .map(tokenResponse -> new AuthResponse(
                        tokenResponse.accessToken(),
                        tokenResponse.refreshToken(),
                        jwt.getRoleFromJWT(tokenResponse.accessToken())
                ));
    }

    /**
     * Refreshes the access token using the given refresh token.
     *
     * @param refreshToken the refresh token
     * @return a Mono emitting the refresh token response
     */
    public Mono<RefreshTokenResponse> refreshToken(String refreshToken) {
        return webClient.post()
                .uri(keycloakUrl + "/protocol/openid-connect/token")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("grant_type", "refresh_token")
                        .with("refresh_token", refreshToken))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new RuntimeException("Error: " + error))))
                .bodyToMono(KeycloakTokenResponse.class)
                .map(tokenResponse -> new RefreshTokenResponse(
                        tokenResponse.accessToken(),
                        tokenResponse.refreshToken(),
                        tokenResponse.expiresIn(),
                        tokenResponse.tokenType()
                ));
    }

    public Mono<Void> register(MemberDto request) {
        String password = String.copyValueOf(request.getPassword());

        String token = getAdminToken().block();
        if (token == null) {
            throw new RuntimeException("Failed to fetch admin token");
        }

        webClient.post()
                .uri(keycloakBaseUrl + "/admin/realms/{realm}/users", "kitchensink")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(Map.of(
                        "username", request.getUsername(),
                        "firstName", request.getFirstName(),
                        "lastName", request.getLastName(),
                        "email", request.getEmail(),
                        "enabled", true,
                        "credentials", new Object[]{
                                Map.of(
                                        "type", grantType,
                                        "value", password,
                                        "temporary", false
                                )
                        }
                ))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    if (response.statusCode() == HttpStatus.CONFLICT) {
                        return Mono.error(new ApplicationException("User already exists!!"));
                    }
                    return Mono.error(new ApplicationException("Keycloak client error: " + response.statusCode()));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ApplicationException("Keycloak server error: " + response.statusCode()))
                )
                .toBodilessEntity()
                .block();

        memberService.createMember(request);
        // TODO Handle Partial Failures (Consistency Between Keycloak and Database) - Catch the exception and call a delete user API. TOBE Picked in future.

        return Mono.empty();
    }

    private Mono<String> getAdminToken() {
        //TODO Admin credentials are currently hard-coded - TOBE handled more securely in future using either .env file or External Password Manager like HashiCorp Vault.
        return webClient.post()
                .uri(keycloakBaseUrl + "/realms/{realm}/protocol/openid-connect/token", "master")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .bodyValue("grant_type=" + grantType + "&client_id=" + MASTER_CLIENTID +
                        //"&client_secret=" + clientSecret +
                        "&username=" + "admin" +
                        "&password=" + "admin123")
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("access_token"))
                .switchIfEmpty(Mono.error(new RuntimeException("Failed to get Admin Token from Keycloak")));

    }

}
