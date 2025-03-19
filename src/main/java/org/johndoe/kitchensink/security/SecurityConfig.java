package org.johndoe.kitchensink.security;

import org.johndoe.kitchensink.config.CorsConfig;
import org.johndoe.kitchensink.security.config.JwtAuthConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.StaticHeadersWriter;


/**
 * Security configuration class for the application.
 */
@Configuration
public class SecurityConfig {

    /**
     * Logger for the SecurityConfig class.
     */
    static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * JwtAuthConverter for converting JWT tokens.
     */
    final JwtAuthConverter jwtAuthConverter;

    /**
     * CorsConfig for configuring CORS settings.
     */
    final CorsConfig corsConfig;

    /**
     * Flag to enable or disable security.
     */
    boolean securityEnabled;

    /**
     * Constructs a SecurityConfig with the given JwtAuthConverter.
     *
     * @param jwtAuthConverter the JwtAuthConverter to use for converting JWT tokens
     * @param corsConfig       the CorsConfig to use for configuring CORS settings
     */
    public SecurityConfig(
            JwtAuthConverter jwtAuthConverter,
            CorsConfig corsConfig,
            @Value("${app.security.enabled:false}") boolean securityEnabled) {
        this.jwtAuthConverter = jwtAuthConverter;
        this.corsConfig = corsConfig;
        this.securityEnabled = securityEnabled;
    }

    /**
     * Configures the security filter chain for the application.
     *
     * @param http the HttpSecurity to modify
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs while configuring the security filter chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        if (!securityEnabled) {
            log.warn("Security is disabled. All requests will be permitted.");
            http.cors(cors -> cors.disable()).csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        http.cors(cors -> cors.configurationSource(corsConfig.getUrlBasedCorsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionFixation(sessionFixation -> sessionFixation.none())
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        http.headers(headers -> headers
                // ✅ Prevent Clickjacking
                .frameOptions(frameOptions -> frameOptions.deny())
                // ✅ Prevent XSS attacks (modern alternative to xssProtection)
                .xssProtection(xss -> xss.disable()) // No longer needed in modern browsers
                // ✅ Prevent MIME-sniffing attacks
                .addHeaderWriter(new StaticHeadersWriter("X-Content-Type-Options", "nosniff"))
                .addHeaderWriter((request, response) -> {
                    response.setHeader("Content-Security-Policy",
                            "default-src 'self'; " +
                                    "script-src 'self' 'unsafe-inline'; " +
                                    "style-src 'self' 'unsafe-inline'; " +
                                    "img-src 'self'; " +
                                    "font-src 'self'; " +
                                    "object-src 'none'; " +
                                    "frame-src 'none';"
                    );
                    // ✅ Set Download options
                    response.setHeader("X-Download-Options", "noopen");
                })

                // ✅ HTTP Strict Transport Security (HSTS)
                .httpStrictTransportSecurity(hsts -> hsts
                        .includeSubDomains(true)
                        .maxAgeInSeconds(31536000)  // 1 year
                        .preload(true)
                )
        );

        return http.build();
    }

    /**
     * Provides a password encoder bean.
     *
     * @return the password encoder
     */
    @Bean
    protected PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
