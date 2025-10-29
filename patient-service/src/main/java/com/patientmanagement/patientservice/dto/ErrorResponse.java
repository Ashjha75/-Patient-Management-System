package com.patientmanagement.patientservice.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(int statusCode,
                            LocalDateTime timestamp,
                            String message,
                            Map<String, String> details) {
    public ErrorResponse(int statusCode, LocalDateTime timestamp, String message) {
        this(statusCode, timestamp, message, null);
    }
}
