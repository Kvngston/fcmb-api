package com.tk.fcmb.Entities.dto;

import com.tk.fcmb.Enums.UserPermissions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.units.qual.A;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleFunctionChangeRequest {

    private String roleName;

    private List<UserPermissions> newFunctions;

    private String staffId;

}
