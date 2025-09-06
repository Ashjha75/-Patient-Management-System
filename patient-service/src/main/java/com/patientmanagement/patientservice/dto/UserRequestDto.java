package com.patientmanagement.patientservice.dto;


import com.patientmanagement.patientservice.dto.validators.RegistrationValidationGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserRequestDto {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Schema(defaultValue = "user1")
    private String username;

    @NotBlank(message = "Email is required", groups = {RegistrationValidationGroup.class})
    @Email(message = "Provide valid email")
    @Schema(defaultValue = "user1@gmail.com")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 8, message = "Password must be atleast 8  characters")
    @Schema(defaultValue = "password1")
    private String password;
}
