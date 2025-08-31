package com.patientmanagement.patientservice.controller;

import com.patientmanagement.patientservice.dto.PatientRequestDto;
import com.patientmanagement.patientservice.dto.UserInfoResponse;
import com.patientmanagement.patientservice.dto.UserRequestDto;
import com.patientmanagement.patientservice.security.JwtUtils;
import com.patientmanagement.patientservice.security.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication API", description = "Endpoints for user authentication and registration")
public class AuthController {

    // Constants for response messages
    private static final String MESSAGE_KEY = "message";
    private static final String STATUS_KEY = "status";
    private static final String TIMESTAMP_KEY = "timestamp";

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, TokenBlacklistService tokenBlacklistService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/signin")
    @Operation(summary = "Authenticate user", description = "Authenticate user and return JWT token")
    public ResponseEntity<?> authenticateUser(@RequestBody UserRequestDto userRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userRequest.getUsername(), userRequest.getPassword())
            );
        } catch (AuthenticationException e) {
            Map<String, Object> map = new HashMap<>();
            map.put(MESSAGE_KEY, "Invalid username or password");
            map.put(STATUS_KEY, false);
            return new ResponseEntity<>(map, HttpStatus.UNAUTHORIZED);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwtToken = jwtUtils.generateTokenFromUsername(userDetails.getUsername());

        List<String> roles = userDetails.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .toList();
        UserInfoResponse response = new UserInfoResponse(jwtToken, userDetails.getUsername(), roles);

        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout user",
            description = "Invalidate JWT token and logout user securely",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Logout successful"),
                    @ApiResponse(responseCode = "400", description = "No valid token found"),
                    @ApiResponse(responseCode = "500", description = "Logout failed")
            }
    )
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String token = jwtUtils.getJwtFromHeader(request);

            if (token != null && !token.trim().isEmpty()) {
                // Extract expiration time from token
                LocalDateTime tokenExpiration = jwtUtils.getExpirationFromToken(token);

                // Add token to blacklist with its actual expiration time
                tokenBlacklistService.blacklistToken(token, tokenExpiration);

                // Clear security context
                SecurityContextHolder.clearContext();

                response.put(MESSAGE_KEY, "Logged out successfully");
                response.put(STATUS_KEY, true);
                response.put(TIMESTAMP_KEY, LocalDateTime.now());
                return ResponseEntity.ok(response);
            } else {
                response.put(MESSAGE_KEY, "No valid token found in request");
                response.put(STATUS_KEY, false);
                response.put(TIMESTAMP_KEY, LocalDateTime.now());
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            response.put(MESSAGE_KEY, "Logout failed: " + e.getMessage());
            response.put(STATUS_KEY, false);
            response.put(TIMESTAMP_KEY, LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}