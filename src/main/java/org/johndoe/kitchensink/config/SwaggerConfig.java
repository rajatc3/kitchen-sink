package org.johndoe.kitchensink.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Swagger.
 * Sets up the OpenAPI documentation for the KitchenSink API.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Default constructor for SwaggerConfig.
     */
    public SwaggerConfig() {
    }

    /**
     * Creates a custom OpenAPI bean with API information.
     *
     * @return an OpenAPI instance with custom information
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("KitchenSink API")
                        .version("1.0")
                        .description("This API provides access to Kitchensink members management system"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth")) // Link security to the API
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth", // Name of the security scheme
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT") // Or whatever token type you're using (JWT, etc.)
                                        .in(SecurityScheme.In.HEADER) // Specify that the token is passed in the Authorization header
                                        .name("Authorization") // Explicitly set the header name as Authorization
                        ));
    }
}
