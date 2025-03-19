package org.johndoe.kitchensink.services;

import lombok.Data;
import org.johndoe.kitchensink.dtos.MemberDto;
import org.johndoe.kitchensink.dtos.requests.AuthRequest;
import org.johndoe.kitchensink.dtos.requests.AuthResponse;
import org.johndoe.kitchensink.dtos.requests.KeycloakTokenResponse;
import org.johndoe.kitchensink.dtos.requests.RefreshTokenResponse;
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
     * Master client ID for Keycloak.
     */
    private static final String MASTER_CLIENTID = "admin-cli";
    private final WebClient webClient;
    private final JwtAuthConverter jwt;
    private final MemberService memberService;

    private final String keycloakUrl;
    private final String clientId;
    private final String keycloakBaseUrl;
    private final String grantType;
    private final String clientSecret;

    public KeycloakAuthService(
            WebClient.Builder webClientBuilder,
            JwtAuthConverter jwt,
            MemberService memberService,
            @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}") String keycloakUrl,
            @Value("${spring.security.oauth2.client.registration.keycloak.client-id}") String clientId,
            @Value("${keycloak.base-url}") String keycloakBaseUrl,
            @Value("${spring.security.oauth2.client.registration.keycloak.authorization-grant-type}") String grantType,
            @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}") String clientSecret) {

        this.webClient = webClientBuilder.build();
        this.jwt = jwt;
        this.memberService = memberService;
        this.keycloakUrl = keycloakUrl;
        this.clientId = clientId;
        this.keycloakBaseUrl = keycloakBaseUrl;
        this.grantType = grantType;
        this.clientSecret = clientSecret;
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

        String adminAccessToken = getAdminAccessToken();

        createUserInKeyCloak(memberDto, adminAccessToken);

        String userRole = memberDto.getUserRole() != null ? memberDto.getUserRole() : ApplicationConstants.ROLES.USER.name().toLowerCase();

        assignRoleToUser(memberDto, adminAccessToken, userRole);

        memberDto.setUserRole(userRole);
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

    public void assignRoleToUser(MemberDto memberDto, String roleToAssign) {
        String adminAccessToken = getAdminAccessToken();
        assignRoleToUser(memberDto, adminAccessToken, roleToAssign);
    }

    /**
     * Assigns a role to a user in keycloak.
     *
     * @param memberDto        the member DTO
     * @param adminAccessToken the admin access token
     * @param roleToAssign     the role to assign
     */
    private void assignRoleToUser(MemberDto memberDto, String adminAccessToken, String roleToAssign) {
        Mono<String> userIdMono = getUserIdFromKeycloakUsingUsername(memberDto, adminAccessToken);

        userIdMono.flatMap(userId -> WebClient.create()
                .get()
                .uri(keycloakBaseUrl + "/admin/realms/{realm}/roles/{roleName}", REALM_KITCHENSINK, roleToAssign.toLowerCase()) // Fetch user role ID
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
     * @param memberDto        the member DTO
     * @param adminAccessToken the admin access token
     */
    private void createUserInKeyCloak(MemberDto memberDto, String adminAccessToken) {

        Map<String, Object> attributes = generateAttributes(memberDto);

        Mono<ResponseEntity<Void>> createUserResponse = webClient.post()
                .uri(keycloakBaseUrl + "/admin/realms/{realm}/users", REALM_KITCHENSINK)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminAccessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromValue(attributes))
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

    private Map<String, Object> generateAttributes(MemberDto memberDto) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("username", memberDto.getUsername());
        attributes.put("firstName", memberDto.getFirstName());
        attributes.put("lastName", memberDto.getLastName());
        attributes.put("email", memberDto.getEmail());
        attributes.put("enabled", true);
        attributes.put("credentials", new Object[]{
                Map.of(
                        "type", grantType,
                        "value", memberDto.getPasswordAsString(),
                        "temporary", false
                )
        });

        Map<String, Object> nestedAttributes = new HashMap<>();
        nestedAttributes.put("phoneNumber", memberDto.getPhoneNumber());

        attributes.put("attributes", nestedAttributes);
        return attributes;
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
