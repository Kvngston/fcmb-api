package com.tk.fcmb.Entities.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankBranchDto {

    private String branchName;

    private String branchCode;

    private String branchState;

    private String branchAddress;


    @Override
    public String toString() {
        return "BankBranchDto{" +
                "branchName='" + branchName + '\'' +
                ", branchCode='" + branchCode + '\'' +
                ", branchState='" + branchState + '\'' +
                ", branchAddress='" + branchAddress + '\'' +
                '}';
    }
}
