package com.tk.fcmb.Entities.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserContactUpdateRequest  {
    private String lga;
    private String country;
    private String stateOfOrigin;

}
