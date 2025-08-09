package com.patientmanagement.patientservice.exception;

/**
 * Custom exception to handle resource not found scenarios in the Patient Management System.
 * <p>
 * This exception is thrown when a requested resource cannot be found in the system,
 * such as when searching for a patient, appointment, or other entity by its identifier.
 * The exception includes details about the resource type and the search criteria used.
 * </p>
 * <p>
 * Usage examples:
 * <pre>
 *     // When searching by string identifier
 *     throw new ResourceNotFound("Patient", "email", "patient@example.com");
 *
 *     // When searching by numeric ID
 *     throw new ResourceNotFound(123L, "id", "Patient");
 *
 *     // In repository or service layer
 *     if (patientRepository.findById(id).isEmpty()) {
 *         throw new ResourceNotFound(id, "id", "Patient");
 *     }
 * </pre>
 * </p>
 *
 * @see RuntimeException
 */

public class ResourceNotFound extends RuntimeException {
    String resourceName;
    String field;
    String fieldName;
    Long fieldId;

    public ResourceNotFound(String resourceName, String field, String fieldName) {
        super(String.format("%s not found with %s : %s", resourceName, field, fieldName));
        this.resourceName = resourceName;
        this.field = field;
        this.fieldName = fieldName;
    }

    public ResourceNotFound(Long fieldId, String field, String resourceName) {
        super(String.format("%s not found with %s : %d", resourceName, field, fieldId));
        this.fieldId = fieldId;
        this.field = field;
        this.resourceName = resourceName;
    }

    public ResourceNotFound() {
    }
}