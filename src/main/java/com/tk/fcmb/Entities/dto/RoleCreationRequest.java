package com.tk.fcmb.Entities.dto;

import com.tk.fcmb.Enums.UserPermissions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleCreationRequest {

    private String roleName;

    private String staffId;

    private List<UserPermissions> roleFunctions;

}
