package org.johndoe.kitchensink.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SwaggerConfigTest {

    @Test
    void testCustomOpenAPI() {
        SwaggerConfig swaggerConfig = new SwaggerConfig();
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        assertNotNull(openAPI, "OpenAPI instance should not be null");
        assertNotNull(openAPI.getInfo(), "OpenAPI info should not be null");

        Info info = openAPI.getInfo();
        assertEquals("KitchenSink API", info.getTitle(), "API title should match");
        assertEquals("1.0", info.getVersion(), "API version should match");
        assertEquals("This API provides access to Kitchensink members management system", info.getDescription(), "API description should match");
    }
}
