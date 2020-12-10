package com.tk.fcmb.Entities;

import com.tk.fcmb.Enums.RequestStatus;
import com.tk.fcmb.Enums.UserPermissions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteRoleFunctionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String roleName;

    @ElementCollection
    private List<UserPermissions> userPermissionsList;

    private RequestStatus requestStatus = RequestStatus.PENDING;

    private boolean approved = false;

    private String ticketNumber;


}
