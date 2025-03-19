package org.johndoe.kitchensink.security.config;

import org.johndoe.kitchensink.utils.ApplicationConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Converter to extract roles from a JWT and convert them to GrantedAuthority.
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    /**
     * JwtDecoder for decoding JWT tokens.
     */
    private final JwtDecoder jwtDecoder;

    /**
     * Constructs a JwtAuthConverter with the given JwtDecoder.
     *
     * @param jwtDecoder the JwtDecoder to use for decoding JWT tokens
     */
    public JwtAuthConverter(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * Extracts the username from a Principal object.
     *
     * @param principal the Principal object
     * @return the username from the Principal
     */
    public static String getUsernameFromPrincipal(Principal principal) {
        if (principal instanceof JwtAuthenticationToken jwtToken) {
            Map<String, Object> claims = jwtToken.getToken().getClaims();
            return (String) claims.get("preferred_username");
        }
        return null;
    }

    /**
     * Converts a Jwt to an AbstractAuthenticationToken by extracting roles.
     *
     * @param jwt the JWT token
     * @return the authentication token with granted authorities
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            for (String role : roles) {
                if (role.equalsIgnoreCase(ApplicationConstants.ROLES.USER.name()) || role.equalsIgnoreCase(ApplicationConstants.ROLES.ADMIN.name()))
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
            }
        }

        return new JwtAuthenticationToken(jwt, authorities);
    }

    /**
     * Extracts the role from a JWT string.
     *
     * @param jwtString the JWT string
     * @return the role from the JWT
     */
    public String getRoleFromJWT(String jwtString) {
        return convert(jwtDecoder.decode(jwtString)).getAuthorities().toString();
    }
}
