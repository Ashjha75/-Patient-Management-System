package com.patientmanagement.patientservice.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(int statusCode,
                            LocalDateTime timestamp,
                            String message,
                            String path,
                            Map<String,String> details)
{
    public ErrorResponse(int statusCode, LocalDateTime timestamp, String message, String path) {
        this(statusCode, timestamp, message, path, null);
    }
}
