package org.johndoe.kitchensink.dtos.requests;

/**
 * AuthResponse is a record that holds the authentication response data.
 *
 * @param memberId     the member ID of the user
 * @param accessToken  the access token for the user
 * @param refreshToken the refresh token for the user
 * @param role         the role of the user
 */
public record AuthResponse(Long memberId, String accessToken, String refreshToken, String role) {
}
