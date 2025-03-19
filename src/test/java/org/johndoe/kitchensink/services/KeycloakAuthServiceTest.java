package org.johndoe.kitchensink.services;

import org.johndoe.kitchensink.dtos.requests.AuthRequest;
import org.johndoe.kitchensink.dtos.requests.AuthResponse;
import org.johndoe.kitchensink.dtos.requests.KeycloakTokenResponse;
import org.johndoe.kitchensink.security.config.JwtAuthConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeycloakAuthServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private RequestHeadersSpec<?> requestHeadersSpec;


    @Mock
    private WebClient.ResponseSpec responseSpec;


    @Mock
    private JwtAuthConverter jwtAuthConverter;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private KeycloakAuthService keycloakAuthService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(webClientBuilder.build()).thenReturn(webClient);

        keycloakAuthService = new KeycloakAuthService(
                webClientBuilder,
                jwtAuthConverter,
                memberService,
                "http://mock-keycloak-url",
                "mock-client-id",
                "http://mock-keycloak-base-url",
                "password",
                "mock-client-secret"
        );
    }

    @Test
    void login_Successful() {
        AuthRequest authRequest = new AuthRequest("testUser", "testPass");
        KeycloakTokenResponse tokenResponse = new KeycloakTokenResponse("mockAccessToken", "mockRefreshToken", "3600", "Bearer");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header(any(), any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any(BodyInserters.FormInserter.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(KeycloakTokenResponse.class)).thenReturn(Mono.just(tokenResponse));

        when(memberService.findMemberIdByEmailOrUsername(any())).thenReturn(1L);
        when(jwtAuthConverter.getRoleFromJWT(any())).thenReturn("USER");

        Mono<AuthResponse> result = keycloakAuthService.login(authRequest);

        AuthResponse response = result.block();
        assertNotNull(response);
        assertEquals(1L, response.memberId());
        assertEquals("mockAccessToken", response.accessToken());
        assertEquals("mockRefreshToken", response.refreshToken());
        assertEquals("USER", response.role());
    }

}
