package com.patientmanagement.patientservice.exception;// package com.example.exception;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 1) Validation errors (request body)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a + "; " + b));

        ProblemDetail pd = ProblemDetailBuilder.forStatus(HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                "Request validation failed; check 'errors' for details.");

        pd.setProperty("errors", fieldErrors);
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("correlationId", MDC.get("correlationId"));
        log.warn("Validation failed for request {}: {}", req.getRequestURI(), fieldErrors);
        return pd;
    }

    // 2) Custom domain exceptions (example)
    @ExceptionHandler(ResourceNotFound.class)
    public ProblemDetail handleNotFound(ResourceNotFound ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetailBuilder.forStatus(HttpStatus.NOT_FOUND.value(),
                "Resource not found",
                ex.getMessage());
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("correlationId", MDC.get("correlationId"));
        log.info("Resource not found: {} {}", req.getRequestURI(), ex.getMessage());
        return pd;
    }

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(ApiException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetailBuilder.forStatus(HttpStatus.BAD_REQUEST.value(),
                "Application error",
                ex.getMessage());
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("correlationId", MDC.get("correlationId"));
        log.warn("API error: {}", ex.getMessage());
        return pd;
    }

    // 3) Common framework/infrastructure exceptions
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleBadJson(HttpMessageNotReadableException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetailBuilder.forStatus(HttpStatus.BAD_REQUEST.value(),
                "Malformed JSON request",
                "Request body could not be parsed. Check JSON syntax and types.");
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("correlationId", MDC.get("correlationId"));
        log.warn("Malformed JSON on {}: {}", req.getRequestURI(), ex.getMessage());
        return pd;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetailBuilder.forStatus(HttpStatus.CONFLICT.value(),
                "Data integrity violation",
                "A database constraint was violated.");
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("correlationId", MDC.get("correlationId"));
        log.error("Data integrity violation on {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return pd;
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ProblemDetail handleNotFound(NoHandlerFoundException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetailBuilder.forStatus(HttpStatus.NOT_FOUND.value(),
                "Endpoint not found",
                String.format("No handler for %s %s", ex.getHttpMethod(), ex.getRequestURL()));
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("correlationId", MDC.get("correlationId"));
        return pd;
    }

    // 4) Catch-all (very important)
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAll(Exception ex, HttpServletRequest req) {
        // Log full stacktrace (server-side only)
        log.error("Unhandled exception for request {} - correlationId={}", req.getRequestURI(), MDC.get("correlationId"), ex);

        ProblemDetail pd = ProblemDetailBuilder.forStatus(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                "An unexpected error occurred; contact support with the correlationId.");
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("correlationId", MDC.get("correlationId"));
        return pd;
    }
}
