package com.patientmanagement.patientservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@NoArgsConstructor
@Getter
@Setter
public class UserInfoResponse {
    //    private Long id;
    private String jwtToken;

    private String userName;
    private List<String> roles;

    public UserInfoResponse(String jwtToken, String userName, List<String> roles) {
//        this.id = id;
        this.userName = userName;
        this.roles = roles;
        this.jwtToken = jwtToken;
    }

    public UserInfoResponse(Long id, String userName, List<String> roles) {
//        this.id = id;
        this.userName = userName;
        this.roles = roles;
    }

    public UserInfoResponse(Object o, @NotBlank @Size(min = 3, max = 50) String username, @NotBlank @Size(max = 100) String email, String name) {
    }
}
