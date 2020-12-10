package com.tk.fcmb.Service;

import com.tk.fcmb.Entities.User;
import com.tk.fcmb.Entities.dto.BankBranchDto;
import com.tk.fcmb.Entities.dto.UserContactUpdateRequest;
import com.tk.fcmb.Entities.dto.UserDto;
import com.tk.fcmb.Entities.dto.UserUpdateRequest;
import org.springframework.http.ResponseEntity;

import javax.mail.MessagingException;
import java.util.List;
import java.util.Map;

public interface UserService {

    ResponseEntity<?> addUser(UserDto userCreationRequest, BankBranchDto bankBranchDetailsRequest) throws Exception;
    ResponseEntity<?> approveUserCreation(String username);
    ResponseEntity<?> declineUserCreation(String username);
    ResponseEntity<?> updateUserDetails(UserUpdateRequest userUpdateRequest, User loggedInUser);
    ResponseEntity<?> updateContactInfo(UserContactUpdateRequest userContactUpdateRequest, String email);

    ResponseEntity<?> resetPassword(User user, String userEmail) throws MessagingException;
    ResponseEntity<?> changePassword(String oldPassword, String newPassword, User user) throws MessagingException;
    ResponseEntity<?> forgetPassword(User user) throws MessagingException;

    ResponseEntity<?> getAllUsersOnPlatform(String email, int page, int size);
    ResponseEntity<?> getUsersCount();
    ResponseEntity<?> getUserDetails(String email);
    ResponseEntity<?> getUserCountByCategory(String roleName);
    Map<String, List<Integer>> userAnalysis();
}
