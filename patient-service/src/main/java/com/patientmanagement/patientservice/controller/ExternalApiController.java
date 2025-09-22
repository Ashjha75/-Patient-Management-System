package com.patientmanagement.patientservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication API", description = "Endpoints for user authentication and registration")
@RequiredArgsConstructor
public class ExternalApiController {

    private final WebClient.Builder webClientBuilder;
}
