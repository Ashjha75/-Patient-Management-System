package com.patientmanagement.patientservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserInfoResponse {
//    private Long id;
    private String jwtToken;

    private String userName;
    private List<String> roles;

    public UserInfoResponse( String jwtToken, String userName, List<String> roles) {
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

}
