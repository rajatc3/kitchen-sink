package org.johndoe.kitchensink.dtos.requests;

/**
 * AuthRequest is a record that holds the authentication request data.
 *
 * @param userIdentifier the userIdentifier address of the user
 * @param password       the password of the user
 */
public record AuthRequest(String userIdentifier, String password) {
}