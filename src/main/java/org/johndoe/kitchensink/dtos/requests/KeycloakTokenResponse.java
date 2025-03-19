package org.johndoe.kitchensink.dtos.requests;


/**
 * Represents a response from Keycloak containing tokens.
 *
 * @param access_token  the access token
 * @param refresh_token the refresh token
 * @param expires_in    the expiration time of the token
 * @param token_type    the type of the token
 */
public record KeycloakTokenResponse(String access_token, String refresh_token, String expires_in,
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
