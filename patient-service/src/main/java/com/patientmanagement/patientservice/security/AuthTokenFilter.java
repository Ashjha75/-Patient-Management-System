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

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService; // ADD THIS LINE

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        logger.debug("Processing authentication for request: {}", requestURI);

        try {
            String jwt = parseJwt(request);

            if (jwt != null) {
                // FIRST: Check if token is blacklisted
                if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                    logger.warn("Blocked blacklisted JWT token for request: {}", requestURI);
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return; // Stop processing and continue to next filter
                }

                // SECOND: Validate token only if not blacklisted
                if (jwtUtils.validateJwtToken(jwt)) {
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    logger.debug("Valid JWT found for user: {}", username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("User authenticated successfully with roles: {}", userDetails.getAuthorities());
                } else {
                    logger.debug("Invalid JWT token found for request: {}", requestURI);
                }
            } else {
                logger.debug("No JWT token found for request: {}", requestURI);
            }

        } catch (UsernameNotFoundException ex) {
            logger.warn("User not found during JWT authentication: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        } catch (Exception ex) {
            logger.error("JWT authentication failed for request {}: {}", requestURI, ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        // CRITICAL: Always continue the filter chain
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String jwt = jwtUtils.getJwtFromHeader(request);
        logger.debug("JWT received: {}", jwt != null ? "Present" : "Null");
        return jwt;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/") ||
                path.startsWith("/api/public/") ||
                path.startsWith("/api/v1/docs") ||
                path.equals("/health") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/api/v1/swagger-ui") ||
                path.equals("/api/v1/swagger-ui.html") ||
                path.startsWith("/api/v1/swagger-resources") ||
                path.startsWith("/webjars") ||
                path.equals("/favicon.ico");
    }
}
