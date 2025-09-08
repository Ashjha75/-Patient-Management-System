package com.patientmanagement.patientservice.serviceImplementation;

import com.patientmanagement.patientservice.dto.UserInfoResponse;
import com.patientmanagement.patientservice.dto.UserRequestDto;
import com.patientmanagement.patientservice.exception.ApiException;
import com.patientmanagement.patientservice.model.User;
import com.patientmanagement.patientservice.repository.UserRepository;
import com.patientmanagement.patientservice.security.JwtUtils;
import com.patientmanagement.patientservice.security.Oauth2utils;
import com.patientmanagement.patientservice.security.TokenBlacklistService;
import com.patientmanagement.patientservice.service.AuthService;
import com.patientmanagement.patientservice.util.enums.AuthProviderType;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.security.oauth2.core.user.OAuth2User;
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
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Oauth2utils oauth2utils;

    @Override
    public ResponseEntity<String> registerUser(UserRequestDto userRequestDto) {
        if (userRequestDto.getUsername() == null || userRequestDto.getUsername().isBlank() || userRequestDto.getPassword() == null || userRequestDto.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Username and password must not be empty.");
        }

        boolean userExists = userRepository.existsByUsername(userRequestDto.getUsername());
        if (userExists) {
            throw new ApiException("Username Already Exist");
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
        String loginInput = userRequest.getUsername(); // username or email
        if (!StringUtils.hasText(loginInput)) {
            log.warn("Authentication failed - Username or email is blank");
            throw new ApiException("Username or email is blank");
        }

        // Normalize: if email, resolve to username
        String usernameToAuth = loginInput;
        if (loginInput.contains("@")) {
            Optional<User> userOpt = userRepository.findByEmail(loginInput);
            if (userOpt.isEmpty()) {
                log.warn("Authentication failed - Email not found: {}", loginInput);
                throw new ApiException("Email not found");
            }
            usernameToAuth = userOpt.get().getUsername();
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(usernameToAuth, userRequest.getPassword()));
            log.debug("Authentication successful for {}", usernameToAuth);
        } catch (AuthenticationException e) {
            log.error("Authentication failed for input: {}", loginInput, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwtToken = jwtUtils.generateTokenFromUsername(userDetails.getUsername());

        List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

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


    @Override
    public ResponseEntity<String> completeProfile(UserRequestDto userRequest) {
        return null;
    }

    public ResponseEntity<UserInfoResponse> handleOauth2loginRequest(OAuth2User user, String accessToken) {


//        Fetch providertype and provider id both
        AuthProviderType authProviderType = oauth2utils.getOauthProvider(accessToken);
        String providerId = oauth2utils.determineProviderIdFromOauth2user(user, accessToken);

//        check if user is present with same type and id
        Optional<User>r user = userRepository.findByProviderIdAndProviderType(providerId,authProviderType);


        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hi");
    }
}
