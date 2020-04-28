package com.tk.fcmb.Entities;

import com.tk.fcmb.Enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends AuditModel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String firstName;

    private String lastName;

    private String middleName;

    private String username;

    private String email;

    private String phoneNumber;

    private String password;

    private String staffId;

    private AccountStatus accountStatus = AccountStatus.ACCOUNT_LOCKED;

    private boolean isAccountLock;

    @OneToOne(fetch = FetchType.EAGER)
    private Role role;

    @OneToOne
    private BankBranch bankBranchDetails;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", staffId='" + staffId + '\'' +
                ", bankBranchDetails=" + bankBranchDetails +
                '}';
    }
}
