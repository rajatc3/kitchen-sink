package org.johndoe.kitchensink.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
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
                        .description("This API provides access to Kitchensink members management system"));
    }
}