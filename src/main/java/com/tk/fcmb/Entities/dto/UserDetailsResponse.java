package com.tk.fcmb.Entities.dto;

import com.tk.fcmb.Entities.BankBranch;
import com.tk.fcmb.Entities.Role;
import com.tk.fcmb.Entities.User;
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
public class UserDetailsResponse {

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private String staffId;

    private AccountStatus accountStatus;

    private LoginFlag loginFlag;

    private boolean isAccountLock;

    private boolean approved;

    private boolean loginCleared;

    private boolean overrideLoginFlow;

    private Role role;

    private BankBranch bankBranchDetails;

    public UserDetailsResponse(User user) {
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.staffId = user.getStaffId();
        this.accountStatus = user.getAccountStatus();
        this.loginFlag = user.getLoginFlag();
        this.isAccountLock = user.isAccountLock();
        this.approved = user.isApproved();
        this.loginCleared = user.isLoginCleared();
        this.overrideLoginFlow = user.isOverrideLoginFlow();
        this.role = user.getRole();
        this.bankBranchDetails = user.getBankBranchDetails();
    }
}
