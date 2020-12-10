package com.tk.fcmb.Entities.dto;

import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {

    private int responseCode;

    private String responseMessage;

    private Object responseData;


    @Override
    public String toString() {
        return "Response{" +
                "responseCode=" + responseCode +
                ", responseMessage='" + responseMessage + '\'' +
                ", responseData=" + responseData +
                '}';
    }
}
