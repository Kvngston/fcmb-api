package com.tk.fcmb.Entities.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private AuthenticationResponse authenticationResponse;
    private String firstName;
    private String lastName;
    private String role;
}
