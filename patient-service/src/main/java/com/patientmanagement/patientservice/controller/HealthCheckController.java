package com.patientmanagement.patientservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "1. Health Check", description = "Health check for Patient Service")
public class HealthCheckController {

    @GetMapping("/health")
    public String healthCheck() {
        return "Health Is ok👌";
    }


}