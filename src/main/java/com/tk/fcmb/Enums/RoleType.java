package com.tk.fcmb.Enums;

import com.google.common.collect.Sets;
import lombok.Getter;

import java.util.Set;

import static com.tk.fcmb.Enums.UserPermissions.*;

@Getter
public enum RoleType {

    SUPER_ADMIN("SUPER_ADMIN", Sets.newHashSet(CREATE_USER,BLOCK_USER,
            UNBLOCK_USER, UPDATE_TRANSACTION, UPGRADE_MOBILE_APP_USERTYPE, UPDATE_PHONE_NUMBER, CONFIRM_OTP,
            RESET_MOBILE_USER_PASSWORD,CLEAR_IMEI,BLOCK_MOBILE_USER_APP_USER,UNBLOCK_MOBILE_USER_APP_USER,RESET_PIN,AUDIT_TRAIL_QUERYING,
            VIEW_QUERY_MOBILE_APP_USER,MODIFY_FUNCTIONS,CREATE_NEW_ROLE,APPROVE_FUNCTIONS,RESET_PASSWORD,VIEW_QUERY_TRANSACTION_HISTORY)),

    ADMIN("ADMIN", Sets.newHashSet(CONFIRM_OTP,AUDIT_TRAIL_QUERYING,VIEW_QUERY_MOBILE_APP_USER,APPROVE_FUNCTIONS,VIEW_QUERY_TRANSACTION_HISTORY)),
    AGENT("AGENT", Sets.newHashSet(CONFIRM_OTP,VIEW_QUERY_MOBILE_APP_USER,VIEW_QUERY_TRANSACTION_HISTORY)),
    IT_CONTROL("IT_CONTROL", Sets.newHashSet(CREATE_USER,BLOCK_USER, UNBLOCK_USER,RESET_PASSWORD)),
    REGULAR("REGULAR", Sets.newHashSet());


    private String roleName;

    private Set<UserPermissions> permissions;

    RoleType(String roleName, Set<UserPermissions> permissions) {
        this.roleName = roleName;
        this.permissions = permissions;
    }

    public String getRoleName() {
        return roleName;
    }

    public Set<UserPermissions> getPermissions() {
        return permissions;
    }
}
