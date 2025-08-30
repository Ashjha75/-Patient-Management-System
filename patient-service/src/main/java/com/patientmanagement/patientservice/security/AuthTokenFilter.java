package com.patientmanagement.patientservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring Security filter that intercepts incoming HTTP requests to validate JWT tokens.
 * <p>
 * This filter extends {@link OncePerRequestFilter} to ensure it runs exactly once per request.
 * It performs JWT-based authentication by extracting, validating, and processing JWT tokens
 * from either the Authorization header or cookies.
 * <p>
 * Main responsibilities:
 * <ul>
 *   <li>Intercept HTTP requests (OncePerRequestFilter)</li>
 *   <li>Extract JWT from Authorization header with fallback to cookies</li>
 *   <li>Validate JWT using JwtUtils</li>
 *   <li>Extract username from JWT</li>
 *   <li>Load UserDetails from database</li>
 *   <li>Create Authentication token (UsernamePasswordAuthenticationToken)</li>
 *   <li>Attach request details to authentication</li>
 *   <li>Set authentication in SecurityContext</li>
 *   <li>Handle invalid or missing token scenarios</li>
 *   <li>Continue with filter chain</li>
 * </ul>
 * <p>
 * If a valid JWT is found and successfully validated, the filter sets up the Spring Security
 * context with the authenticated user details. If no valid token is found or validation fails,
 * the request continues without authentication.
 *
 * @see OncePerRequestFilter
 * @see JwtUtils
 * @see org.springframework.security.core.context.SecurityContextHolder
 */
@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        logger.debug("Processing authentication for request: {}", requestURI);

        try {
            String jwt = parseJwt(request);

            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                logger.debug("Valid JWT found for user: {}", username);

                // Load user details from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Create authentication token
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // Set additional request details
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("User authenticated successfully with roles: {}", userDetails.getAuthorities());

            } else {
                logger.debug("No valid JWT token found for request: {}", requestURI);
            }

        } catch (UsernameNotFoundException ex) {
            logger.warn("User not found during JWT authentication: {}", ex.getMessage());
            // Clear any partial authentication context
            SecurityContextHolder.clearContext();

        } catch (Exception ex) {
            logger.error("JWT authentication failed for request {}: {}", requestURI, ex.getMessage());
            // Clear security context on any authentication error
            SecurityContextHolder.clearContext();
        }
    }

    private String parseJwt(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String jwt = jwtUtils.getJwtFromHeader(request);
        logger.debug("JWT received: {}", jwt);
        return jwt;
    }
}
