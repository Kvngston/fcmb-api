package com.tk.fcmb.Entities.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActivityLogFilterRequestForMobile {
    private String mobileNumber;
    private String date;
    private String accountNumber;
    private String action;
    private String status;

    @Builder.Default
    private Integer page = 1;

    @Builder.Default
    private Integer size = 25;

    @Builder.Default
    private Boolean orderAscending = false;

}
