package com.tk.fcmb.Entities;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class AuditTrail{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;

    private String transactionDetails;

    @ManyToOne
    private User user;

    private String ipAddress;

    private LocalDate createdAtDate;

    private LocalTime createdAtTime;

    private LocalDate updatedAtDate;

    private LocalTime updatedAtTime;


    @PrePersist
    public void setCreatedAt() {
        this.createdAtDate = LocalDate.now();
        this.updatedAtDate = LocalDate.now();
        this.createdAtTime = LocalTime.now();
        this.updatedAtTime = LocalTime.now();
    }

    @Override
    public String toString() {
        return "AuditTrail{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", transactionDetails='" + transactionDetails + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", createdAtDate=" + createdAtDate +
                ", createdAtTime=" + createdAtTime +
                ", updatedAtDate=" + updatedAtDate +
                ", updatedAtTime=" + updatedAtTime +
                '}';
    }
}
