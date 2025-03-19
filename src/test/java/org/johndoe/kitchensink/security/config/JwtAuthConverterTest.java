package org.johndoe.kitchensink.security.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthConverterTest {

    @Mock
    private JwtDecoder jwtDecoder;

    private JwtAuthConverter jwtAuthConverter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtAuthConverter = new JwtAuthConverter(jwtDecoder);
    }

    @Test
    void convert_ShouldReturnJwtAuthenticationToken_WithCorrectAuthorities() {
        Jwt jwt = Jwt.withTokenValue("dummy-token")
                .header("alg", "HS256")
                .claim("realm_access", Map.of("roles", List.of("USER", "ADMIN")))
                .build();

        AbstractAuthenticationToken authenticationToken = jwtAuthConverter.convert(jwt);
        assertNotNull(authenticationToken);
        assertInstanceOf(JwtAuthenticationToken.class, authenticationToken);

        Set<String> expectedRoles = Set.of("ROLE_USER", "ROLE_ADMIN");
        Set<String> actualRoles = authenticationToken.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet());

        assertEquals(expectedRoles, actualRoles);
    }

    @Test
    void convert_ShouldReturnEmptyAuthorities_WhenNoRolesPresent() {
        Jwt jwt = Jwt.withTokenValue("dummy-token")
                .header("alg", "HS256")
                .claim("realm_access", Map.of("roles", List.of()))
                .build();

        AbstractAuthenticationToken authenticationToken = jwtAuthConverter.convert(jwt);
        assertNotNull(authenticationToken);
        assertTrue(authenticationToken.getAuthorities().isEmpty());
    }

    @Test
    void getUsernameFromPrincipal_ShouldReturnCorrectUsername() {
        Jwt jwt = Jwt.withTokenValue("dummy-token")
                .header("alg", "HS256")
                .claim("preferred_username", "john.doe")
                .build();

        JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(jwt);
        String username = JwtAuthConverter.getUsernameFromPrincipal(jwtAuthenticationToken);

        assertEquals("john.doe", username);
    }

    @Test
    void getUsernameFromPrincipal_ShouldReturnNull_WhenInvalidPrincipal() {
        Principal principal = mock(Principal.class);
        assertNull(JwtAuthConverter.getUsernameFromPrincipal(principal));
    }

    @Test
    void getRoleFromJWT_ShouldReturnCorrectRoles() {
        Jwt jwt = Jwt.withTokenValue("dummy-token")
                .header("alg", "HS256")
                .claim("realm_access", Map.of("roles", List.of("USER")))
                .build();

        when(jwtDecoder.decode("dummy-token")).thenReturn(jwt);

        String roles = jwtAuthConverter.getRoleFromJWT("dummy-token");
        assertEquals("[ROLE_USER]", roles);
    }

    @Test
    void getRoleFromJWT_ShouldReturnEmptyList_WhenNoRoles() {
        Jwt jwt = Jwt.withTokenValue("dummy-token")
                .header("alg", "HS256")
                .claim("realm_access", Map.of("roles", List.of()))
                .build();

        when(jwtDecoder.decode("dummy-token")).thenReturn(jwt);

        String roles = jwtAuthConverter.getRoleFromJWT("dummy-token");
        assertEquals("[]", roles);
    }
}
