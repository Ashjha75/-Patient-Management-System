package com.patientmanagement.patientservice.dto.validators;

/**
 * Marker interface for the "create patient" validation group.
 * <p>
 * Use this interface to group validation constraints that should be applied
 * when creating a new patient. Annotate your DTO fields with
 * {@code groups = createPatientValidationGroup.class} in validation annotations.
 * <p>
 * <b>Usage:</b>
 * <ul>
 *   <li>Apply stricter validation rules for creation.</li>
 *   <li>Use with {@code @Validated(createPatientValidationGroup.class)} in controller methods.</li>
 * </ul>
 * <p>
 * <b>Difference between {@code @Validated} and {@code @Valid}:</b>
 * <ul>
 *   <li>{@code @Valid} applies all validation constraints without grouping.</li>
 *   <li>{@code @Validated} allows you to specify validation groups, enabling different rules for different operations (e.g., create vs update).</li>
 * </ul>
 */
public interface CreatePatientValidationGroup {
}