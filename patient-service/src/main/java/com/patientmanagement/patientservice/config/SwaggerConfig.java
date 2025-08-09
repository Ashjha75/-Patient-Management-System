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

@Configuration
public class SwaggerConfig {

    @Value("${baseUrl:http://localhost:8080}")
    String baseUrl;

    @Bean
    public OpenAPI swaggerConfiguration() {
        return new OpenAPI()
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