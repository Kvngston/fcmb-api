package com.tk.fcmb.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenBlackList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String token;

}
