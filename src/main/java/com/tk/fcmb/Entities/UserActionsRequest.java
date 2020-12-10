package com.tk.fcmb.Entities;

import com.tk.fcmb.Enums.ActionType;
import com.tk.fcmb.Enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserActionsRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    private User user;

    private boolean attendedTo = false;

    private RequestStatus requestStatus = RequestStatus.PENDING;


    private ActionType actionType;

    private String ticketNumber;

}
