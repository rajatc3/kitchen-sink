package org.johndoe.kitchensink.dtos.requests;

/**
 * RefreshTokenResponse is a record that holds the refresh token response data.
 *
 * @param accessToken  the new access token for the user
 * @param refreshToken the new refresh token for the user
 * @param expiresIn    the expiration time of the access token
 * @param tokenType    the type of the token
 */
public record RefreshTokenResponse(String accessToken, String refreshToken, String expiresIn, String tokenType) {
}
