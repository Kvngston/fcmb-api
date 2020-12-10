package com.tk.fcmb.Entities.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomersFilterRequest {

    private String mobileNumber;
    private String accountNumber;
    private String status;
    private String date;

    @Builder.Default
    private Boolean alphabetical = false;

    @Builder.Default
    private Integer page = 1;

    @Builder.Default
    private Integer size = 25;

    @Builder.Default
    private Boolean orderAscending = false;

}
