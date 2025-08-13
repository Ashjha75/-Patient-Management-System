package com.patientmanagement.patientservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SwaggerConfig sets up and customizes the OpenAPI (Swagger) documentation for the Patient Management System API.
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li><b>API Metadata:</b> Sets title, description, version, contact, and license for the API docs.</li>
 *   <li><b>Server Environments:</b> Defines local and DEV server URLs for API testing and documentation.</li>
 *   <li><b>API Tags:</b> Groups endpoints for better organization in the Swagger UI (e.g., Health Check, Patient).</li>
 *   <li><b>Security:</b> Configures JWT Bearer authentication for secured endpoints.</li>
 * </ul>
 *
 * <p><b>How it works:</b></p>
 * <ul>
 *   <li>Spring Boot auto-detects this configuration and exposes interactive API docs at /swagger-ui.html (if using springdoc-openapi-ui).</li>
 *   <li>Developers and testers can explore, try, and understand the API endpoints directly from the browser.</li>
 * </ul>
 *
 * <p><b>Author:</b> Ashish Jha</p>
 * <p><b>GitHub:</b> https://github.com/Ashjha75/-Patient-Management-System</p>
 */
@Configuration
public class SwaggerConfig {

    @Value("${baseUrl:http://localhost:8080}")
    String baseUrl;

    @Bean
    public OpenAPI swaggerConfiguration() {
        return new OpenAPI()
                .openapi("3.0.1")
                .info(new Info()
                        .title("Patient Management System")
                        .description("Patient Management System for managing a large number of patients")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Ashish Jha")
                                .email("ajha5645@gmail.com")
                                .url("https://github.com/Ashjha75/-Patient-Management-System"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org"))
                )
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local server"),
                        new Server().url(baseUrl).description("DEV server")
                ))
                .tags(List.of(
                        new Tag().name("1. Health Check").description("Health Check for patient service"),
                        new Tag().name("2. Patient").description("Patient management")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")));
    }
}