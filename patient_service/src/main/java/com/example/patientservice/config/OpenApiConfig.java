package com.example.patientservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration.
 * Accessible at: http://localhost:8080/swagger-ui/index.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI patientServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Patient Service API")
                        .description("REST API quản lý thông tin bệnh nhân")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Patient Service Team")
                                .email("admin@example.com")));
    }
}
