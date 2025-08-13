package com.patientmanagement.patientservice.exception;

import org.springframework.http.ProblemDetail;

import java.time.OffsetDateTime;
import java.util.Map;


public final class ProblemDetailBuilder {

    /**
     * Private constructor to prevent instantiation since this is a utility class.
     */
    private ProblemDetailBuilder() {
    }

    /**
     * Creates a new {@link ProblemDetail} with the specified status, title, and detail message.
     * Also adds a {@code timestamp} property in ISO-8601 format (UTC offset).
     *
     * @param status the HTTP status code (e.g., 400, 404, 500)
     * @param title  a short, human-readable summary of the problem
     * @param detail a detailed explanation of the problem for debugging
     * @return a new {@link ProblemDetail} object with basic metadata
     */
    public static ProblemDetail forStatus(int status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setTitle(title);
        pd.setDetail(detail);
        pd.setProperty("timestamp", OffsetDateTime.now().toString());
        return pd;
    }

    /**
     * Adds extra properties to an existing {@link ProblemDetail} instance.
     * <p>
     * This can be used to add context-specific information like:
     * <ul>
     *     <li>Custom error codes</li>
     *     <li>Request path</li>
     *     <li>Correlation IDs</li>
     *     <li>Support contact details</li>
     * </ul>
     *
     * @param base       the existing {@link ProblemDetail} to enrich
     * @param extraProps a map of additional properties to set; ignored if {@code null}
     * @return the same {@link ProblemDetail} instance with new properties added
     */
    public static ProblemDetail withDetails(ProblemDetail base, Map<String, Object> extraProps) {
        if (extraProps != null) {
            extraProps.forEach(base::setProperty);
        }
        return base;
    }
}
