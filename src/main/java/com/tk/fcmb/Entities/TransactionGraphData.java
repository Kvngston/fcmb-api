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
public class TransactionGraphData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String identifier;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<Integer> dailySuccessful;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<Integer> dailyFailed;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Integer> weeklySuccessful;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<Integer> weeklyFailed;

    @ElementCollection(fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Integer> monthlySuccessful;

    @ElementCollection(fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Integer> monthlyFailed;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<Integer> yearlySuccessful;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<Integer> yearlyFailed;

    @Override
    public String toString() {
        return "TransactionGraphData{" +
                "id=" + id +
                ", identifier='" + identifier + '\'' +
                ", dailySuccessful=" + dailySuccessful +
                ", dailyFailed=" + dailyFailed +
                ", weeklySuccessful=" + weeklySuccessful +
                ", weeklyFailed=" + weeklyFailed +
                ", monthlySuccessful=" + monthlySuccessful +
                ", monthlyFailed=" + monthlyFailed +
                ", yearlySuccessful=" + yearlySuccessful +
                ", yearlyFailed=" + yearlyFailed +
                '}';
    }
}
