package com.patientmanagement.patientservice.controller;

import com.patientmanagement.patientservice.dto.UserInfoResponse;
import com.patientmanagement.patientservice.dto.UserRequestDto;
import com.patientmanagement.patientservice.dto.validators.LoginValidationGroup;
import com.patientmanagement.patientservice.dto.validators.RegistrationValidationGroup;
import com.patientmanagement.patientservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication API", description = "Endpoints for user authentication and registration")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(
            summary = "Signup user",
            description = "Register user and return user with valid",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User Registered  successfully"),
                    @ApiResponse(responseCode = "400", description = "Please provide correct details"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            }
    )
    public ResponseEntity<String> registerUser(@Validated(RegistrationValidationGroup.class) @RequestBody UserRequestDto userRequest) {
        return authService.registerUser(userRequest);
    }


    @PostMapping("/signin")
    @Operation(summary = "Authenticate user", description = "Authenticate user and return JWT token")
    public ResponseEntity<UserInfoResponse> authenticateUser(@Validated(LoginValidationGroup.class) @RequestBody @Valid UserRequestDto userRequest) {
        return authService.authenticateUser(userRequest);
    }

    @GetMapping("/api/v1/login/google")
    public void googleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google"); // or your provider
    }

    @GetMapping("/api/v1/login/github")
    public void githubLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google"); // or your provider
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
        return authService.logout(request);
    }


    @PostMapping("/user-profile")
    @Operation(
            summary = "User Profile",
            description = "Update profile of existing user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User Profile Updated  successfully"),
                    @ApiResponse(responseCode = "400", description = "Please provide correct details"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error ")
            }
    )
    public ResponseEntity<String> completeProfile(@Validated(RegistrationValidationGroup.class) @RequestBody UserRequestDto userRequest) {
        return authService.completeProfile(userRequest);
    }


}
