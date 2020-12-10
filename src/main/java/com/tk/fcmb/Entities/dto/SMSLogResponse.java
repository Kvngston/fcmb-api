package com.tk.fcmb.Entities.dto;

import lombok.Data;

import java.util.Date;

@Data
public class SMSLogResponse {
    public String destinationMobile;
    private Date createdAt;
    private String responseCode;
}
