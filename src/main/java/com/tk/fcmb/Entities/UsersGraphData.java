package com.tk.fcmb.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsersGraphData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String identifier;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<Integer> dailySuccessful;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<Integer> week1Successful;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<Integer> week2Successful;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<Integer> week3Successful;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<Integer> week4Successful;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<Integer> week5Successful;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<Integer> weeklySuccessful;

    @ElementCollection(fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Integer> monthlySuccessful;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<Integer> yearlySuccessful;
}
