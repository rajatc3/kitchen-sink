package org.johndoe.kitchensink.dtos;

/**
 * AuthRequest is a record that holds the authentication request data.
 *
 * @param email    the email address of the user
 * @param password the password of the user
 */
public record AuthRequest(String email, String password) {
}