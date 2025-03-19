package org.johndoe.kitchensink.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CorsConfigTest {

    @InjectMocks
    CorsConfig corsConfig;

    @Mock
    private Logger log;

    @BeforeEach
    void setUp() {
        corsConfig = new CorsConfig();
    }

    @Test
    void testCorsFilterBeanCreation() {
        CorsFilter corsFilter = corsConfig.corsFilter();
        assertNotNull(corsFilter, "CorsFilter bean should be created");
    }

    @Test
    void testGetUrlBasedCorsConfigurationSource_WhenCorsDisabled() {
        // Simulate CORS disabled
        corsConfig.corsDisabled = true;

        UrlBasedCorsConfigurationSource source = corsConfig.getUrlBasedCorsConfigurationSource();
        CorsConfiguration config = source.getCorsConfigurations().get("/**");

        assertNotNull(config, "CORS configuration should exist");
        assertTrue(config.getAllowCredentials(), "Allow credentials should be true");
        assertEquals(List.of("*"), config.getAllowedOriginPatterns(), "All origins should be allowed");
        assertEquals(List.of("*"), config.getAllowedHeaders(), "All headers should be allowed");
        assertEquals(List.of("*"), config.getAllowedMethods(), "All methods should be allowed");
    }

    @Test
    void testGetUrlBasedCorsConfigurationSource_WhenCorsEnabled() {
        // Simulate CORS enabled
        corsConfig.corsDisabled = false;

        UrlBasedCorsConfigurationSource source = corsConfig.getUrlBasedCorsConfigurationSource();
        CorsConfiguration config = source.getCorsConfigurations().get("/**");

        assertNotNull(config, "CORS configuration should exist");
        assertTrue(config.getAllowCredentials(), "Allow credentials should be true");
        assertEquals(List.of("http://localhost:80", "http://localhost", "http://localhost:5173", "http://host.docker.internal:8080"),
                config.getAllowedOriginPatterns(), "Specific origins should be allowed");
        assertEquals(List.of("Origin", "Content-Type", "Accept", "Authorization"),
                config.getAllowedHeaders(), "Allowed headers should match expected values");
        assertEquals(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"),
                config.getAllowedMethods(), "Allowed methods should match expected values");
        assertEquals(List.of("Authorization"), config.getExposedHeaders(), "Exposed headers should include Authorization");
        assertEquals(3600L, config.getMaxAge(), "Max age should be 3600 seconds (1 hour)");
    }
}
