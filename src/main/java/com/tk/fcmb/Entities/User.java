package com.tk.fcmb.Entities;

import com.tk.fcmb.Enums.AccountStatus;
import com.tk.fcmb.Enums.LoginFlag;
import com.tk.fcmb.Enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends AuditModel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String firstName;

    private String lastName;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phoneNumber;

    private String password;

    @Column(unique = true)
    private String staffId;

    private RequestStatus requestStatus = RequestStatus.PENDING;

    private AccountStatus accountStatus = AccountStatus.ACCOUNT_LOCKED;

    private LoginFlag loginFlag = LoginFlag.DETAILS_FLAG;

    private boolean isAccountLock = true;

    private boolean approved = false;

    private boolean loginCleared = false;

    private boolean overrideLoginFlow = false;

    @OneToOne(fetch = FetchType.EAGER)
    private Role role;

    @OneToOne
    private BankBranch bankBranchDetails;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return getFirstName().equals(user.getFirstName()) &&
                getLastName().equals(user.getLastName()) &&
                getEmail().equals(user.getEmail()) &&
                getPhoneNumber().equals(user.getPhoneNumber()) &&
                getStaffId().equals(user.getStaffId()) &&
                getRole().equals(user.getRole()) &&
                getBankBranchDetails().equals(user.getBankBranchDetails());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFirstName(), getLastName(), getEmail(), getPhoneNumber(), getStaffId(), getRole(), getBankBranchDetails());
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", staffId='" + staffId + '\'' +
                ", bankBranchDetails=" + bankBranchDetails +
                '}';
    }
}
