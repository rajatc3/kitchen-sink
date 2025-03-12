package org.johndoe.kitchensink.services;

import org.johndoe.kitchensink.dtos.AuthRequest;
import org.johndoe.kitchensink.dtos.AuthResponse;
import org.johndoe.kitchensink.dtos.RefreshTokenResponse;
import org.johndoe.kitchensink.security.config.JwtAuthConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Service for handling authentication with Keycloak.
 */
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
     * Keycloak URL.
     */
    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String keycloakUrl;

    /**
     * Client ID for Keycloak.
     */
    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;

    /**
     * Client secret for Keycloak.
     */
    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;

    /**
     * Constructs a new KeycloakAuthService with the given WebClient builder and JwtAuthConverter.
     *
     * @param webClientBuilder the WebClient builder
     * @param jwt              the JwtAuthConverter
     */
    public KeycloakAuthService(WebClient.Builder webClientBuilder, JwtAuthConverter jwt) {
        this.webClient = webClientBuilder.build();
        this.jwt = jwt;
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
                        "&grant_type=password" +
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

    /**
     * Represents a response from Keycloak containing tokens.
     *
     * @param access_token  the access token
     * @param refresh_token the refresh token
     * @param expires_in    the expiration time of the token
     * @param token_type    the type of the token
     */
    record KeycloakTokenResponse(String access_token, String refresh_token, String expires_in,
                                         String token_type) {
        /**
         * Gets the access token.
         *
         * @return the access token
         */
        public String accessToken() {
            return access_token;
        }

        /**
         * Gets the refresh token.
         *
         * @return the refresh token
         */
        public String refreshToken() {
            return refresh_token;
        }

        /**
         * Gets the expiration time of the token.
         *
         * @return the expiration time of the token
         */
        public String expiresIn() {
            return expires_in;
        }

        /**
         * Gets the type of the token.
         *
         * @return the type of the token
         */
        public String tokenType() {
            return token_type;
        }
    }
}
