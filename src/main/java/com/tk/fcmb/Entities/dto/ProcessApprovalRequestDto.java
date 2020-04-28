package com.tk.fcmb.Entities.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tk.fcmb.Enums.ProcessType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessApprovalRequestDto {

    @NotNull
    private String staffId;

    private String mobileNumber;
    
    private String oldNumber;

    private String newNumber;

    @NotNull
    private String reasonForInitiation;

    private String oldUserType;

    private String newUserType;

    private int userTypeId;

    private long newDailyLimitAmount;

    private long oldDailyLimitAmount;

    private String otpType;

    private String action;

    private String confirmPassword;

    private String newPassword;

    private String oldPassword;

    @NotNull
    private ProcessType processType;

}
