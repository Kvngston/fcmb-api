package com.tk.fcmb.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OTP{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String phoneNumber;

    private String code;

    private String email;

    @ManyToOne
    private User user;

    private boolean valid = true;

    private LocalTime createdAt;

    private LocalTime expiryTime;


}
