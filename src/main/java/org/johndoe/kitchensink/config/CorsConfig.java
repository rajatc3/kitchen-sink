package org.johndoe.kitchensink.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Configuration class for setting up CORS (Cross-Origin Resource Sharing) settings.
 */
@Configuration
public class CorsConfig {

    /**
     * Logger for the CorsConfig class.
     */
    private static final Logger log = LoggerFactory.getLogger(CorsConfig.class);
    /**
     * CorsConfigurationSource for configuring CORS settings.
     */
    public CorsConfigurationSource getConfigurationSource;
    /**
     * Flag to indicate if CORS is disabled.
     * Configured via the `app.cors.disabled` property.
     */
    @Value("${app.cors.disabled:true}")
    boolean corsDisabled;
    /**
     * Creates a new CorsConfig.
     */
    CorsConfig() {
    }

    /**
     * Creates a CorsFilter bean to handle CORS requests.
     *
     * @return a CorsFilter configured with the specified CORS settings
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = getUrlBasedCorsConfigurationSource();
        return new CorsFilter(source);
    }

    /**
     * Creates a UrlBasedCorsConfigurationSource bean to configure CORS settings.
     *
     * @return a UrlBasedCorsConfigurationSource configured with the specified CORS settings
     */
    public UrlBasedCorsConfigurationSource getUrlBasedCorsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        if (corsDisabled) {
            log.warn("CORS is disabled. All requests will be permitted.");

            config.setAllowCredentials(true);
            config.setAllowedOriginPatterns(List.of("*"));  // Allow all origins
            config.setAllowedHeaders(List.of("*"));        // Allow all headers
            config.setAllowedMethods(List.of("*"));        // Allow all HTTP methods

            source.registerCorsConfiguration("/**", config);
        } else {
            config.setAllowCredentials(true);
            config.setAllowedOriginPatterns(List.of("http://localhost:80", "http://localhost", "http://localhost:5173", "http://host.docker.internal:8080")); // Ensure frontend Origins are allowed
            config.setAllowedHeaders(List.of("Origin", "Content-Type", "Accept", "Authorization"));
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            config.setExposedHeaders(List.of("Authorization")); // Allow frontend to access headers
            config.setMaxAge(3600L); // Cache preflight requests for 1 hour

            source.registerCorsConfiguration("/**", config);
        }
        return source;
    }
}