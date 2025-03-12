package org.johndoe.kitchensink.dtos;

/**
 * AuthResponse is a record that holds the authentication response data.
 *
 * @param accessToken  the access token for the user
 * @param refreshToken the refresh token for the user
 * @param role         the role of the user
 */
public record AuthResponse(String accessToken, String refreshToken, String role) {
}
