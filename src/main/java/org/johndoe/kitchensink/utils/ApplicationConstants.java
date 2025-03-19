package org.johndoe.kitchensink.utils;

/**
 * Constants used in the application.
 */
public class ApplicationConstants {

    public enum ROLES {
        USER("user"),
        ADMIN("admin");

        private final String name;

        ROLES(String name) {
            this.name = name;
        }
    }
}
