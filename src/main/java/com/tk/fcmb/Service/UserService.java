package com.tk.fcmb.Service;

import com.tk.fcmb.Entities.TemporaryPassword;
import com.tk.fcmb.Entities.User;
import com.tk.fcmb.Entities.dto.BankBranchDto;
import com.tk.fcmb.Entities.dto.UserDto;
import com.tk.fcmb.Enums.RoleType;
import org.springframework.http.ResponseEntity;

import javax.mail.MessagingException;
import java.util.List;

public interface UserService {

    ResponseEntity<?> addUser(UserDto userCreationRequest, BankBranchDto bankBranchDetailsRequest) throws Exception;
    ResponseEntity<?> addSuperAdmin(UserDto userCreationRequest, BankBranchDto bankBranchDetailsRequest) throws Exception;
    ResponseEntity<?> addAdmin(UserDto userCreationRequest, BankBranchDto bankBranchDetailsRequest) throws Exception;
    ResponseEntity<?> approveUserCreation(String username);


    ResponseEntity<?> resetPassword(long userId, String initiatorsEmail) throws MessagingException;
    ResponseEntity<?> changePassword(String generatedPassword, String newPassword, long userId, String initiatorsEmail) throws MessagingException;

    ResponseEntity<?> getAllUsersOnPlatform();
    ResponseEntity<?> getUsersCount();
    ResponseEntity<?> getUserDetails(String username);
    ResponseEntity<?> getUserCountByCategory(RoleType roleType);
}
