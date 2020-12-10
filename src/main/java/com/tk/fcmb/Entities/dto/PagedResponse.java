package com.tk.fcmb.Entities.dto;

import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse {

    private int responseCode;

    private String responseMessage;

    private Object responseData;

    private Object page;

    private long total;

    @Override
    public String toString() {
        return "PagedResponse{" +
                "responseCode=" + responseCode +
                ", responseMessage='" + responseMessage + '\'' +
                ", responseData=" + responseData +
                ", page=" + page +
                ", total=" + total +
                '}';
    }



}
