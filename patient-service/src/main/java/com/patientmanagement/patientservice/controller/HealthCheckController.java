package com.patientmanagement.patientservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Tag(name = "1. Health Check", description = "Health check for Patient Service")
public class HealthCheckController {

    @GetMapping("/health")
    public String healthCheck() {
        return "Health Is ok👌";
    }
    
    @GetMapping("/api/v1/login/google")
    public void googleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google"); // or your provider
    }

    @GetMapping("/api/v1/login/github")
    public void githubLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/github"); // or your provider
    }

}