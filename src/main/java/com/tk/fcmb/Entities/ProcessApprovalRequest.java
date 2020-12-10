package com.tk.fcmb.Entities;

import com.tk.fcmb.Enums.ProcessType;
import com.tk.fcmb.Enums.RequestStatus;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String trackingNumber;

    private RequestStatus requestStatus = RequestStatus.PENDING;

    @NotNull
    private String staffId;

    private String mobileNumber;

    private String oldNumber;

    private String newNumber;

    private boolean approved = false;

    private String otpType;

    @NotNull
    private String reasonForInitiation;

    private int userTypeId;

    private long dailyAmountLimit;

    private long transactionLimit;

    private String action;

    private String confirmPassword;

    private ProcessType processType;

    @Override
    public String toString() {
        return "ProcessApprovalRequest{" +
                "id=" + id +
                ", trackingNumber='" + trackingNumber + '\'' +
                ", staffId='" + staffId + '\'' +
                ", mobileNumber='" + mobileNumber + '\'' +
                ", oldNumber='" + oldNumber + '\'' +
                ", newNumber='" + newNumber + '\'' +
                ", approved=" + approved +
                ", otpType='" + otpType + '\'' +
                ", reasonForInitiation='" + reasonForInitiation + '\'' +
                ", userTypeId=" + userTypeId +
                ", dailyAmountLimit=" + dailyAmountLimit +
                ", transactionLimit=" + transactionLimit +
                ", action='" + action + '\'' +
                ", confirmPassword='" + confirmPassword + '\'' +
                ", processType=" + processType +
                '}';
    }
}
