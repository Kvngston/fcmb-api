package com.tk.fcmb.Entities.dto;

import com.tk.fcmb.Enums.RoleType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class UserDto {

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

//    private String country;
//
//    private String stateOfOrigin;
//
//    private String lga;

    @Email
    @NotNull
    private String email;

    @NotNull
    private String phoneNumber;

    @NotNull
    private String staffId;

    @NotNull
    private String roleName;



}
