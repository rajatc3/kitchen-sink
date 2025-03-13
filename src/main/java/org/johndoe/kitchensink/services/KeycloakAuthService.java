package org.johndoe.kitchensink.services;

import lombok.Data;
import org.johndoe.kitchensink.dtos.*;
import org.johndoe.kitchensink.exceptions.ApplicationException;
import org.johndoe.kitchensink.security.config.JwtAuthConverter;
import org.johndoe.kitchensink.utils.ApplicationConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for handling authentication with Keycloak.
 */
@Data
@Service
public class KeycloakAuthService {

    public static final String REALM_KITCHENSINK = "kitchensink";
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
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("grant_type", grantType)
                        .with("username", request.userIdentifier())
                        .with("password", request.password()))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> {
                            if (clientResponse.statusCode() == HttpStatus.UNAUTHORIZED) {
                                return Mono.error(new ApplicationException("Invalid Credentials!!"));
                            }
                            return clientResponse.bodyToMono(String.class)
                                    .flatMap(error -> Mono.error(new RuntimeException("Error: " + error)));
                        })
                .bodyToMono(KeycloakTokenResponse.class)
                .map(tokenResponse -> new AuthResponse(
                        memberService.findMemberIdByEmailOrUsername(request.userIdentifier()),
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
     * Registers a user with the given member DTO.
     *
     * @param memberDto the member DTO
     * @return a Mono emitting the registration response
     */
    public Mono<Void> register(MemberDto memberDto) {
        String password = String.copyValueOf(memberDto.getPassword());

        String adminAccessToken = getAdminAccessToken();

        createUserInKeyCloak(memberDto, adminAccessToken, password);

        assignRoleToUser(memberDto, adminAccessToken, ApplicationConstants.ROLES.USER.toString());

        memberService.createMember(memberDto);
        // TODO Handle Partial Failures (Consistency Between Keycloak and Database) - Catch the exception and call a delete user API. TOBE Picked in future.

        return Mono.empty();
    }

    /**
     * Gets the admin access token.
     *
     * @return a Mono emitting the admin access token
     */
    private String getAdminAccessToken() {
        String adminAccessToken = getAdminToken().block();
        if (adminAccessToken == null) {
            throw new RuntimeException("Failed to fetch admin adminAccessToken");
        }
        return adminAccessToken;
    }

    /**
     * Assigns a role to a user.
     *
     * @param memberDto the member DTO
     * @param adminAccessToken the admin access token
     * @param roleToAssign the role to assign
     */
    private void assignRoleToUser(MemberDto memberDto, String adminAccessToken, String roleToAssign) {
        Mono<String> userIdMono = getUserIdFromKeycloakUsingUsername(memberDto, adminAccessToken);

        userIdMono.flatMap(userId -> WebClient.create()
                .get()
                .uri(keycloakBaseUrl + "/admin/realms/{realm}/roles/{roleName}", REALM_KITCHENSINK, roleToAssign) // Fetch user role ID
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ApplicationException("Keycloak client error: " + response.statusCode()))
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ApplicationException("Keycloak server error: " + response.statusCode()))
                )
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .map(role -> Map.of("id", role.get("id"), "name", role.get("name")))
                .flatMap(role -> WebClient.create()
                        .post()
                        .uri(keycloakBaseUrl + "/admin/realms/{realm}/users/{userId}/role-mappings/realm", REALM_KITCHENSINK, userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                        .body(BodyInserters.fromValue(List.of(role))) // Assign the role
                        .retrieve()
                        .toBodilessEntity()
                )).subscribe();

        //Blocking as this is a synchronous application
        userIdMono.block();
    }

    private Mono<String> getUserIdFromKeycloakUsingUsername(MemberDto memberDto, String adminAccessToken) {
        Mono<String> userIdMono = WebClient.create()
                .get()
                .uri(keycloakBaseUrl + "/admin/realms/{realm}/users?username={username}", REALM_KITCHENSINK, memberDto.getUsername())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ApplicationException("Keycloak client error: " + response.statusCode()))
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ApplicationException("Keycloak server error: " + response.statusCode()))
                ).bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                })
                .map(users -> (String) users.get(0).get("id")
                );

        //Blocking as this is a synchronous application
        userIdMono.block();
        return userIdMono;
    }

    /**
     * Creates a user in Keycloak.
     *
     * @param request the member DTO
     * @param adminAccessToken the admin access token
     * @param password the password
     */
    private void createUserInKeyCloak(MemberDto request, String adminAccessToken, String password) {
        Mono<ResponseEntity<Void>> createUserResponse = webClient.post()
                .uri(keycloakBaseUrl + "/admin/realms/{realm}/users", REALM_KITCHENSINK)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(Map.of(
                        "username", request.getUsername(),
                        "firstName", request.getFirstName(),
                        "lastName", request.getLastName(),
                        "email", request.getEmail(),
                        "phoneNumber", request.getPhoneNumber(),

                        "enabled", true,
                        "credentials", new Object[]{
                                Map.of(
                                        "type", grantType,
                                        "value", password,
                                        "temporary", false
                                )
                        }
                )))
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
                .toBodilessEntity();

        //Blocking as this is a synchronous application
        createUserResponse.block();
    }

    /**
     * Gets the admin token.
     *
     * @return a Mono emitting the admin token
     */
    private Mono<String> getAdminToken() {
        //TODO Admin credentials are currently hard-coded - TOBE handled more securely in future using either .env file or External Password Manager like HashiCorp Vault.
        return webClient.post()
                .uri(keycloakBaseUrl + "/realms/{realm}/protocol/openid-connect/token", "master")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData("grant_type", grantType)
                        .with("client_id", MASTER_CLIENTID)
                        //.with("client_secret", clientSecret) // Uncomment if needed
                        .with("username", "admin")
                        .with("password", "admin123"))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("access_token"))
                .switchIfEmpty(Mono.error(new RuntimeException("Failed to get Admin Token from Keycloak")));

    }

    public void updateUserInKeycloak(MemberDto memberDto) {
        String adminAccessToken = getAdminAccessToken();
        String userId = getUserIdFromKeycloakUsingUsername(memberDto, adminAccessToken).block();

        if (userId == null) {
            throw new ApplicationException("User not found in Keycloak.");
        }

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("firstName", memberDto.getFirstName());
        attributes.put("lastName", memberDto.getLastName());
        attributes.put("email", memberDto.getEmail());
        attributes.put("enabled", true);

        Map<String, Object> nestedAttributes = new HashMap<>();
        nestedAttributes.put("phoneNumber", memberDto.getPhoneNumber());

        attributes.put("attributes", nestedAttributes);

        if (memberDto.getPasswordAsString() != null) {
            attributes.put("credentials", new Object[]{
                    Map.of(
                            "type", grantType,
                            "value", memberDto.getPasswordAsString(),
                            "temporary", false
                    )
            });
        }


        Mono<ResponseEntity<Void>> updateUserResponse = WebClient.create()
                .put()
                .uri(keycloakBaseUrl + "/admin/realms/{realm}/users/{userId}", REALM_KITCHENSINK, userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(attributes))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ApplicationException("Keycloak client error: " + response.statusCode())))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ApplicationException("Keycloak server error: " + response.statusCode())))
                .toBodilessEntity();

        updateUserResponse.block();
    }
}
