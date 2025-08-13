package com.patientmanagement.patientservice.exception;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Utility class that provides mapping functionality between Patient domain model
 * and various DTO representations.
 * <p>
 * This mapper simplifies the conversion between entity and DTOs, ensuring a clean
 * separation between the domain model and the external API representations.
 * </p>
 */

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    /**
     * The name of the HTTP header used to carry the correlation ID between services.
     */
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    /**
     * The key under which the correlation ID is stored in the {@link MDC}.
     */
    public static final String MDC_CORRELATION_ID = "correlationId";

    /**
     * Ensures that every HTTP request has a correlation ID set for tracing.
     * <p>
     * If the incoming request already has an {@code X-Correlation-Id} header, it is reused.
     * If not, a new UUID is generated. The correlation ID is stored in the {@link MDC}
     * for log enrichment and added to the response headers for client visibility.
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response
     * @param filterChain the filter chain to pass the request and response to the next filter
     * @throws ServletException if an error occurs in filter processing
     * @throws IOException      if an I/O error occurs in filter processing
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Get existing correlation ID from request header or generate a new one
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            if (!StringUtils.hasText(correlationId)) {
                correlationId = UUID.randomUUID().toString();
            }

            // Add correlation ID to logging context and response header
            MDC.put(MDC_CORRELATION_ID, correlationId);
            response.setHeader(CORRELATION_ID_HEADER, correlationId);

            // Continue filter chain
            filterChain.doFilter(request, response);
        } finally {
            // Always clean up MDC to avoid leaking IDs between threads
            MDC.remove(MDC_CORRELATION_ID);
        }
    }
}
