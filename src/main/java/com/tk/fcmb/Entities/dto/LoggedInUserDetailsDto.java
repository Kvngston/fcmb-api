package com.tk.fcmb.Entities.dto;

import com.tk.fcmb.Enums.AccountStatus;
import com.tk.fcmb.Enums.LoginFlag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoggedInUserDetailsDto {

    private String response;

    private AccountStatus accountStatus;

    private LoginFlag loginFlag;

    private boolean isAccountLock;

    private boolean approved;

    private boolean loginCleared;

    private boolean overrideLoginFlow;

}
