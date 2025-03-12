package org.johndoe.kitchensink.dtos;


/**
 * RefreshTokenRequest is a record that holds the refresh token request data.
 *
 * @param refreshToken the refresh token for the user
 */
public record RefreshTokenRequest(String refreshToken) {
}