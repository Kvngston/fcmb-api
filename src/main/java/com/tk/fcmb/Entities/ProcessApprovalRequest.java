package com.tk.fcmb.Entities;

import com.tk.fcmb.Enums.ProcessType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessApprovalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String trackingNumber;

    @NotNull
    private String staffId;

    private String mobileNumber;

    private String oldNumber;

    private String newNumber;

    private boolean approved = false;

    private String otpType;

    @NotNull
    private String reasonForInitiation;

    private String oldUserType;

    private String newUserType;

    private int userTypeId;

    private long newDailyLimitAmount;

    private long oldDailyLimitAmount;

    private String action;

    private String confirmPassword;

    private String newPassword;

    private String oldPassword;

    private ProcessType processType;

}
