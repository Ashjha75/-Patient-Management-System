package com.patientmanagement.patientservice.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Getter
@Setter
public class UserInfoResponse {
    //    private Long id;
    private String jwtToken;
    private String refreshToken;
    private String userName;
    private List<String> roles;

    public UserInfoResponse(String jwtToken, String userName, List<String> roles) {
        this.userName = userName;
        this.roles = roles;
        this.jwtToken = jwtToken;
    }

    public UserInfoResponse(String jwtToken, String refreshToken, String userName, List<String> roles) {
        this.jwtToken = jwtToken;
        this.refreshToken = refreshToken;
        this.userName = userName;
        this.roles = roles;
    }

    public UserInfoResponse(String jwtToken, String userName, String email, String provider) {
        this.jwtToken = jwtToken;
        this.userName = userName;
        this.roles = new ArrayList<>();
        this.roles.add(provider);
    }
}
