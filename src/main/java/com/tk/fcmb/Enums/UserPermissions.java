package com.tk.fcmb.Enums;

public enum UserPermissions {
    CREATE_USER("create_user"),
    BLOCK_USER("block_user"),
    UNBLOCK_USER("unblock_user"),
    UPDATE_TRANSACTION("update_transaction"),
    UPGRADE_MOBILE_APP_USERTYPE("update_mobile_app_usertype"),
    UPDATE_PHONE_NUMBER("update_phone_number"),
    CONFIRM_OTP("confirm_otp"),
    RESET_MOBILE_USER_PASSWORD("update_mobile_user_password"),
    CLEAR_IMEI("clear_imei"),
    BLOCK_MOBILE_USER_APP_USER("block_mobile_user_app_user"),
    UNBLOCK_MOBILE_USER_APP_USER("unblock_mobile_user_app_user"),
    RESET_PIN("reset_pin"),
    AUDIT_TRAIL_QUERYING("audit_trail_querying"),
    VIEW_QUERY_MOBILE_APP_USER("view_query_mobile_app_user"),
    MODIFY_FUNCTIONS("modify_functions"),
    CREATE_NEW_ROLE("create_new_role"),
    APPROVE_FUNCTIONS("approve_functions"),
    RESET_PASSWORD("reset_password"),
    VIEW_QUERY_TRANSACTION_HISTORY("view_query_transaction_history");


    private String permission;

    UserPermissions(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
