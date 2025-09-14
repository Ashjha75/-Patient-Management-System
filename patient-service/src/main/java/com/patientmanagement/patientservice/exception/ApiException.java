package com.patientmanagement.patientservice.exception;

import java.io.IOException;
import java.io.Serial;

/**
 * Custom exception class for API-related errors in the Patient Management System.
 * <p>
 * This unchecked exception is used throughout the application to signal various
 * API-related issues such as invalid requests, resource not found, validation failures,
 * or business rule violations.
 * </p>
 * <p>
 * Usage examples:
 * <pre>
 *     // Basic usage
 *     throw new ApiException("Patient not found");
 *
 *     // In service layer
 *     if (patientRepository.findById(id).isEmpty()) {
 *         throw new ApiException("Patient with ID " + id + " not found");
 *     }
 *
 *     // In validation
 *     if (patient.getName() == null || patient.getName().isEmpty()) {
 *         throw new ApiException("Patient name cannot be empty");
 *     }
 * </pre>
 * </p>
 *
 * @see RuntimeException
 */
public class ApiException extends RuntimeException {

    /**
     * Serial version UID for serialization compatibility.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new ApiException with no message.
     */
    public ApiException() {
    }

    /**
     * Constructs a new ApiException with the specified error message.
     *
     * @param message the detailed error message explaining the cause of the exception
     */
    public ApiException(String message) {
        super(message);
    }

    public ApiException(String s, IOException e) {
    }
}