package com.patientmanagement.patientservice.serviceImplementation;

import com.patientmanagement.patientservice.dto.RefreshTokenRequest;
import com.patientmanagement.patientservice.dto.UserInfoResponse;
import com.patientmanagement.patientservice.dto.UserRequestDto;
import com.patientmanagement.patientservice.exception.ApiException;
import com.patientmanagement.patientservice.exception.ResourceNotFound;
import com.patientmanagement.patientservice.model.RefreshToken;
import com.patientmanagement.patientservice.model.Role;
import com.patientmanagement.patientservice.model.User;
import com.patientmanagement.patientservice.repository.RoleRepository;
import com.patientmanagement.patientservice.repository.UserRepository;
import com.patientmanagement.patientservice.security.JwtUtils;
import com.patientmanagement.patientservice.security.TokenBlacklistService;
import com.patientmanagement.patientservice.service.AuthService;
import com.patientmanagement.patientservice.service.IRefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private final IRefreshTokenService iRefreshTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public ResponseEntity<String> registerUser(UserRequestDto userRequestDto) {
        if (userRequestDto.getUsername() == null || userRequestDto.getUsername().isBlank() ||
                userRequestDto.getPassword() == null || userRequestDto.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Username and password must not be empty.");
        }

        if (userRepository.existsByUsername(userRequestDto.getUsername())) {
            throw new ApiException("Username Already Exist");
        }
        if (userRepository.existsByEmail(userRequestDto.getEmail())) {
            throw new ApiException("Email already exists.");
        }

        User user = new User(
                userRequestDto.getUsername(),
                passwordEncoder.encode(userRequestDto.getPassword()),
                userRequestDto.getEmail()
        );

        // Assign default role
        Role defaultRole = roleRepository.findByRoleName("ROLE_USER")
                .orElseThrow(() -> new ApiException("Default role not found"));
        user.getRoles().add(defaultRole);

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully.");
    }

    @Override
    public ResponseEntity<UserInfoResponse> authenticateUser(UserRequestDto userRequest) {
        String loginInput = userRequest.getUsername(); // Can be username or email
        if (!StringUtils.hasText(loginInput)) {
            log.warn("Authentication failed - Username or email is blank");
            throw new ApiException("Username or email is blank");
        }

        // --- Logic to resolve email to username (this part is good, no changes) ---
        String usernameToAuth = loginInput;
        if (loginInput.contains("@")) {
            Optional<User> userOpt = userRepository.findByEmail(loginInput);
            if (userOpt.isEmpty()) {
                log.warn("Authentication failed - Email not found: {}", loginInput);
                throw new ApiException("Email not found");
            }
            usernameToAuth = userOpt.get().getUsername();
        }

        // --- Standard authentication logic (this part is good, no changes) ---
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(usernameToAuth, userRequest.getPassword()));
            log.debug("Authentication successful for {}", usernameToAuth);
        } catch (AuthenticationException e) {
            log.error("Authentication failed for input: {}", loginInput, e);
            throw new ApiException("Invalid username or password");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // --- Token Generation and Response Construction (THIS PART IS UPDATED) ---

        // 1. Generate the short-lived JWT Access Token
        String jwtToken = jwtUtils.generateTokenFromUsername(userDetails.getUsername());

        // 2. ✅ Create and persist the long-lived Refresh Token
        RefreshToken refreshToken = iRefreshTokenService.createRefreshToken(userDetails.getUsername());

        // 3. Get the user's roles for the response
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // 4. ✅ Create the response object containing BOTH tokens
        UserInfoResponse response = new UserInfoResponse(
                jwtToken,
                refreshToken.getToken(), // Pass the refresh token string
                userDetails.getUsername(),
                roles
        );

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

    @Override
    public UserInfoResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.refreshToken();

        return iRefreshTokenService.findByToken(requestRefreshToken)
                .map(iRefreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String newAccessToken = jwtUtils.generateTokenFromUsername(user.getUsername());
                    iRefreshTokenService.deleteByToken(requestRefreshToken);
                    RefreshToken newRefreshToken = iRefreshTokenService.createRefreshToken(user.getUsername());

                    List<String> roles = user.getRoles().stream()
                            .map(Role::getRoleName)
                            .distinct()
                            .toList();

                    return new UserInfoResponse(newAccessToken, newRefreshToken.getToken(), user.getUsername(), roles);
                })
                .orElseThrow(() -> new ResourceNotFound("Refresh token", "token", requestRefreshToken));
    }


    @Override
    @Transactional
    public void logoutUser(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            log.warn("Logout attempt with no refresh token provided.");
            return; // Fail silently, the goal is to be logged out.
        }
        // The only action required for a secure logout is to delete the refresh token.
        // This instantly revokes the user's ability to get a new access token.
        iRefreshTokenService.deleteByToken(refreshToken);
        log.info("Successfully logged out user by revoking their refresh token.");
    }


}
