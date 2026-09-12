package com.tvmaze.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentacion interactiva expuesta en {@code /swagger-ui.html}.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tvMazeMiddlewareOpenApi() {
        return new OpenAPI().info(new Info()
                .title("TVmaze Middleware API")
                .version("1.0.0")
                .description("""
                        Middleware sobre el API publico de TVmaze.
                        Expone la busqueda de shows por criterio y la consulta de un show por id.""")
                .contact(new Contact().name("Examen Tecnico Backend"))
                .license(new License().name("TVmaze API").url("https://www.tvmaze.com/api")));
    }
}
