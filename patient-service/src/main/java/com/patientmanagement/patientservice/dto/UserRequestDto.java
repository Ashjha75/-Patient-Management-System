package com.patientmanagement.patientservice.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequestDto {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Schema(defaultValue = "user1")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 8, message = "Password must be atleast 8  characters")
    @Schema(defaultValue = "password1")
    private String password;
}
