package com.patientmanagement.patientservice.exception;

import com.patientmanagement.patientservice.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@code GlobalExceptionHandler} is a centralized Spring Boot exception handler
 * for REST APIs in the Patient Management Service.
 * <p>
 * This class uses Spring's {@link RestControllerAdvice} to intercept exceptions
 * thrown by controllers and return consistent, structured error responses
 * based on {@link ProblemDetail}.
 * <p>
 * Each handler method logs the error, sets useful metadata (e.g., {@code path}, {@code correlationId}),
 * and ensures API consumers get a predictable JSON structure for errors.
 * <p>
 * Typical error response format:
 * <pre>
 * {
 *   "type": "about:blank",
 *   "title": "Validation failed",
 *   "status": 400,
 *   "detail": "Request validation failed; check 'errors' for details.",
 *   "path": "/patients",
 *   "correlationId": "abc123",
 *   "errors": { "name": "must not be blank" }
 * }
 * </pre>
 *
 * @see ProblemDetailBuilder Utility to create {@link ProblemDetail} objects with custom attributes
 * @see MDC Mapped Diagnostic Context for adding correlation IDs to logs
 */
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles validation failures for request bodies annotated with {@code @Valid}.
     *
     * @param ex  the {@link MethodArgumentNotValidException} containing validation errors
     * @param req the {@link HttpServletRequest} for retrieving request metadata
     * @return a {@link ProblemDetail} object with validation error messages and request details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a + "; " + b));

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), LocalDateTime.now(), "Validation Failed", fieldErrors);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles custom resource-not-found errors.
     *
     * @param ex  the {@link ResourceNotFound} exception
     * @param req the current HTTP request
     * @return a {@link ProblemDetail} indicating that the requested resource was not found
     */
    @ExceptionHandler(ResourceNotFound.class)
    public ProblemDetail handleNotFound(ResourceNotFound ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetailBuilder.forStatus(HttpStatus.NOT_FOUND.value(), "Resource not found", ex.getMessage());
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("correlationId", MDC.get("correlationId"));
        log.info("Resource not found: {} {}", req.getRequestURI(), ex.getMessage());
        return pd;
    }

    /**
     * Handles application-specific exceptions represented by {@link ApiException}.
     *
     * @param ex  the API exception
     * @param req the current HTTP request
     * @return a {@link ProblemDetail} describing the application error
     */
    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(ApiException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetailBuilder.forStatus(HttpStatus.BAD_REQUEST.value(), "Application error", ex.getMessage());
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("correlationId", MDC.get("correlationId"));
        log.warn("API error: {}", ex.getMessage());
        return pd;
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInputException(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a + "; " + b));

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), LocalDateTime.now(), "Validation Failed", fieldErrors);
        log.warn("API error: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles malformed JSON input.
     *
     * @param ex  the {@link HttpMessageNotReadableException} indicating parse failure
     * @param req the current HTTP request
     * @return a {@link ProblemDetail} instructing the client to fix JSON syntax or types
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleBadJson(HttpMessageNotReadableException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetailBuilder.forStatus(HttpStatus.BAD_REQUEST.value(), "Malformed JSON request", "Request body could not be parsed. Check JSON syntax and types.");
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("correlationId", MDC.get("correlationId"));
        log.warn("Malformed JSON on {}: {}", req.getRequestURI(), ex.getMessage());
        return pd;
    }

    /**
     * Handles database constraint violations (e.g., unique key conflicts).
     *
     * @param ex  the {@link DataIntegrityViolationException}
     * @param req the current HTTP request
     * @return a {@link ProblemDetail} describing the constraint violation
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetailBuilder.forStatus(HttpStatus.CONFLICT.value(), "Data integrity violation", "A database constraint was violated.");
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("correlationId", MDC.get("correlationId"));
        log.error("Data integrity violation on {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return pd;
    }

    /**
     * Handles requests for unmapped endpoints.
     *
     * @param ex  the {@link NoHandlerFoundException} indicating no matching handler
     * @param req the current HTTP request
     * @return a {@link ProblemDetail} describing the missing endpoint
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ProblemDetail handleNotFound(NoHandlerFoundException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetailBuilder.forStatus(HttpStatus.NOT_FOUND.value(), "Endpoint not found", String.format("No handler for %s %s", ex.getHttpMethod(), ex.getRequestURL()));
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("correlationId", MDC.get("correlationId"));
        return pd;
    }

    /**
     * Handles all other unhandled exceptions.
     * <p>
     * This is a catch-all fallback to prevent stack traces from leaking to clients.
     * The full exception stack trace is logged on the server side.
     *
     * @param ex  the uncaught exception
     * @param req the current HTTP request
     * @return a generic {@link ProblemDetail} advising the client to contact support
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAll(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception for request {} - correlationId={}", req.getRequestURI(), MDC.get("correlationId"), ex);

        ProblemDetail pd = ProblemDetailBuilder.forStatus(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error", "An unexpected error occurred; contact support with the correlationId.");
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("correlationId", MDC.get("correlationId"));
        return pd;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = ex.getConstraintViolations().stream().collect(Collectors.toMap(violation -> violation.getPropertyPath().toString(), violation -> violation.getMessage(), (a, b) -> a + "; " + b));

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), LocalDateTime.now(), "Validation Failed", fieldErrors);

        log.warn("Constraint violation: {}", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex, HttpServletRequest request) {
        String correlationId = (String) request.getAttribute("correlationId");
        log.warn("Authorization denied for request {} - correlationId={}: {}", request.getRequestURI(), correlationId, ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                LocalDateTime.now(),
                "Access Denied: You do not have the necessary permissions to access this resource.",
                null // No specific field errors for this type of exception
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

}
