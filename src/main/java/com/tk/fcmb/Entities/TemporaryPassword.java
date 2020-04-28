package com.tk.fcmb.Entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TemporaryPassword {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String generatedPassword;

    private boolean used;

    @ManyToOne
    private User user;


}
