package ceb.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI Configuration
 * URL: http://localhost:8080/swagger-ui.html
 * API Docs: http://localhost:8080/v3/api-docs
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cây Cảnh API")
                        .version("1.0.0")
                        .description("API Documentation cho ứng dụng quản lý bán cây cảnh")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@caycanh.com")
                                .url("https://caycanh.com")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Authentication Token")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}

