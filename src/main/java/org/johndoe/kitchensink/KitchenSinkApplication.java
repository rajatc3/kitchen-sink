package org.johndoe.kitchensink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Main application class for the KitchenSink application.
 */
@SpringBootApplication
@EnableCaching
@EnableMethodSecurity(prePostEnabled = true)
public class KitchenSinkApplication {

    /**
     * Default constructor for KitchenSinkApplication.
     */
    public KitchenSinkApplication() {
    }

    /**
     * Main method to run the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(KitchenSinkApplication.class, args);
    }

}
