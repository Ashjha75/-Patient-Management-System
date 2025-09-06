package com.patientmanagement.patientservice.serviceImplementation;

import com.patientmanagement.patientservice.dto.UserInfoResponse;
import com.patientmanagement.patientservice.dto.UserRequestDto;
import com.patientmanagement.patientservice.model.User;
import com.patientmanagement.patientservice.repository.UserRepository;
import com.patientmanagement.patientservice.security.JwtUtils;
import com.patientmanagement.patientservice.security.TokenBlacklistService;
import com.patientmanagement.patientservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final String MESSAGE_KEY = "message";
    private static final String STATUS_KEY = "status";
    private static final String TIMESTAMP_KEY = "timestamp";

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<String> registerUser(UserRequestDto userRequestDto) {
        if (userRequestDto.getUsername() == null || userRequestDto.getUsername().isBlank() || userRequestDto.getPassword() == null || userRequestDto.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Username and password must not be empty.");
        }

        boolean userExists = userRepository.existsByUsername(userRequestDto.getUsername());
        if (userExists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists.");
        }
        boolean emailExists = userRepository.existsByEmail(userRequestDto.getEmail());
        if (emailExists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists.");
        }

        userRepository.save(new User(userRequestDto.getUsername(), passwordEncoder.encode(userRequestDto.getPassword()), userRequestDto.getEmail()));

        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully.");
    }

    @Override
    public ResponseEntity<UserInfoResponse> authenticateUser(UserRequestDto userRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userRequest.getUsername(), userRequest.getPassword()));
            log.debug("Authentication Successful {}", authentication);
        } catch (AuthenticationException e) {
            log.error("Authentication failed.", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwtToken = jwtUtils.generateTokenFromUsername(userDetails.getUsername());

        List<String> roles = userDetails.getAuthorities().stream().map(auth -> auth.getAuthority()).toList();

        UserInfoResponse response = new UserInfoResponse(jwtToken, userDetails.getUsername(), roles);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String token = jwtUtils.getJwtFromHeader(request);

            if (token != null && !token.trim().isEmpty()) {
                LocalDateTime tokenExpiration = jwtUtils.getExpirationFromToken(token);

                tokenBlacklistService.blacklistToken(token, tokenExpiration);

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
