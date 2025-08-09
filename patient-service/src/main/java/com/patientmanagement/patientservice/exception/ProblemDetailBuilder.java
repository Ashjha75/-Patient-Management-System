package com.patientmanagement.patientservice.exception;

import org.springframework.http.ProblemDetail;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Utility class for creating and enriching RFC 7807 Problem Details in the Patient Management System.
 * <p>
 * This builder simplifies the creation of standardized error responses following the
 * Problem Details for HTTP APIs specification (RFC 7807). It automatically adds common
 * properties like timestamps and provides methods to add custom details.
 * </p>
 * <p>
 * Usage examples:
 * <pre>
 *     // Basic usage with status, title, and detail
 *     ProblemDetail problem = ProblemDetailBuilder.forStatus(404, "Resource Not Found",
 *                               "Patient with ID 123 could not be found");
 *
 *     // Adding extra properties
 *     Map&lt;String, Object&gt; extras = Map.of(
 *         "errorCode", "PATIENT-404",
 *         "supportContact", "support@example.com"
 *     );
 *     ProblemDetail enriched = ProblemDetailBuilder.withDetails(problem, extras);
 *
 *     // In exception handlers
 *     return ResponseEntity
 *            .status(HttpStatus.NOT_FOUND)
 *            .body(ProblemDetailBuilder.forStatus(404, "Not Found", ex.getMessage()));
 * </pre>
 * </p>
 *
 * @see ProblemDetail
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7807">RFC 7807</a>
 */
public final class ProblemDetailBuilder {
    private ProblemDetailBuilder() {
    }

    public static ProblemDetail forStatus(int status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setTitle(title);
        pd.setDetail(detail);
        pd.setProperty("timestamp", OffsetDateTime.now().toString());
        return pd;
    }

    public static ProblemDetail withDetails(ProblemDetail base, Map<String, Object> extraProps) {
        if (extraProps != null) extraProps.forEach(base::setProperty);
        return base;
    }
}