package com.patientmanagement.patientservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Oauth2LoginRequestDto {
    @NotBlank(message = "Username is required")
    private String username;

//    @NotBlank(message = "Email is required", groups = {RegistrationValidationGroup.class})
//    @Email(message = "Provide valid email")
//    @Schema(defaultValue = "user1@gmail.com")
//    private String email;

    private String password;

    public Oauth2LoginRequestDto(String username, Object o) {
    }
}
