package com.tk.fcmb.Service.impl;

import com.tk.fcmb.Entities.BankBranch;
import com.tk.fcmb.Entities.Role;
import com.tk.fcmb.Entities.TemporaryPassword;
import com.tk.fcmb.Entities.User;
import com.tk.fcmb.Entities.dto.BankBranchDto;
import com.tk.fcmb.Entities.dto.UserDto;
import com.tk.fcmb.Enums.AccountStatus;
import com.tk.fcmb.Enums.RoleType;
import com.tk.fcmb.Enums.UserPermissions;
import com.tk.fcmb.Repositories.BankBranchRepository;
import com.tk.fcmb.Repositories.RoleRepository;
import com.tk.fcmb.Repositories.TemporaryPasswordRepository;
import com.tk.fcmb.Repositories.UserRepository;
import com.tk.fcmb.Service.AuditTrailService;
import com.tk.fcmb.Service.EmailServices;
import com.tk.fcmb.Service.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankBranchRepository bankBranchRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EmailServices emailServices;

    @Autowired
    private TemporaryPasswordRepository temporaryPasswordRepository;

    @Autowired
    private AuditTrailService auditTrailService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<?> addUser(UserDto userCreationRequest, BankBranchDto bankBranchDetailsRequest) throws Exception {

        if(userRepository.findByUsername(userCreationRequest.getUsername()).isPresent()) {
            throw new Exception("Username already exists");
        }else{
            return getUserResponseEntity(userCreationRequest, bankBranchDetailsRequest, RoleType.REGULAR);
        }
    }

    @Override
    public ResponseEntity<?> addSuperAdmin(UserDto userCreationRequest, BankBranchDto bankBranchDetailsRequest) throws Exception {

        if(userRepository.findByUsername(userCreationRequest.getUsername()).isPresent()) {
            throw new Exception("Username already exists");
        }else{
            return getUserResponseEntity(userCreationRequest, bankBranchDetailsRequest, RoleType.SUPER_ADMIN);
        }
    }

    private ResponseEntity<?> getUserResponseEntity(UserDto userCreationRequest, BankBranchDto bankBranchDetailsRequest, RoleType roleType) {
        User user = new User();
        user.setFirstName(userCreationRequest.getFirstName());
        user.setLastName(userCreationRequest.getLastName());
        user.setMiddleName(userCreationRequest.getMiddleName());
        user.setEmail(userCreationRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userCreationRequest.getPassword()));
        user.setPhoneNumber(userCreationRequest.getPhoneNumber());
        user.setStaffId(userCreationRequest.getStaffId());
        user.setUsername(userCreationRequest.getUsername());

        if (roleType == RoleType.SUPER_ADMIN) {
            user.setAccountStatus(AccountStatus.ACCOUNT_UNLOCKED);
        }
        Role role = roleRepository.findByRoleName(roleType.name());
        user.setRole(role);

        BankBranch bankBranch = new BankBranch();
        bankBranch.setBranchAddress(bankBranchDetailsRequest.getBranchAddress());
        bankBranch.setBranchCode(bankBranchDetailsRequest.getBranchCode());
        bankBranch.setBranchName(bankBranchDetailsRequest.getBranchName());
        bankBranch.setBranchState(bankBranchDetailsRequest.getBranchState());

        bankBranchRepository.save(bankBranch);
        user.setBankBranchDetails(bankBranch);


        return new ResponseEntity<>(userRepository.save(user).toString(), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<?> addAdmin(UserDto userCreationRequest, BankBranchDto bankBranchDetailsRequest) throws Exception {
        if(userRepository.findByUsername(userCreationRequest.getUsername()).isPresent()) {
            throw new Exception("Username already exists");
        }else{
            return getUserResponseEntity(userCreationRequest, bankBranchDetailsRequest, RoleType.ADMIN);
        }
    }

    @Override
    public ResponseEntity<?> approveUserCreation(String username) {

        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null){
            return new ResponseEntity<>("User not Found", HttpStatus.NOT_FOUND);
        }

        user.setAccountStatus(AccountStatus.ACCOUNT_UNLOCKED);

        userRepository.save(user);
        return new ResponseEntity<>("User Successfully approved", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> resetPassword(long userId, String initiatorsEmail) throws MessagingException {

        User user = userRepository.findById(userId).orElse(null);


        if (user != null){
            user.setAccountLock(true);
            user.setAccountStatus(AccountStatus.ACCOUNT_LOCKED);



            TemporaryPassword temporaryPassword = new TemporaryPassword();
            temporaryPassword.setUser(user);
            temporaryPassword.setUsed(false);
            temporaryPassword.setGeneratedPassword(randomPasswordGenerator());

            user.setPassword(temporaryPassword.getGeneratedPassword());

            userRepository.save(user);

            emailServices.sendMail("Reset Password", "Reset your password with this new Password " + temporaryPassword.getGeneratedPassword(), initiatorsEmail);

            temporaryPasswordRepository.save(temporaryPassword);

            return new ResponseEntity<>(temporaryPassword,HttpStatus.OK);

        }


        return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<?> changePassword(String generatedPassword, String newPassword, long userId, String initiatorsEmail) throws MessagingException {

        TemporaryPassword temporaryPassword = temporaryPasswordRepository.findByGeneratedPassword(generatedPassword).orElse(null);
        User user = userRepository.getOne(userId);
        if (temporaryPassword == null){
            return new ResponseEntity<>("Generated Password mismatch",HttpStatus.NOT_FOUND);
        }else   {

            if (temporaryPassword.getUser() != user){
                return new ResponseEntity<>("This generated Password isn't for this user ", HttpStatus.NOT_ACCEPTABLE);
            }else {
                user.setPassword(newPassword);
                user.setAccountLock(false);
                user.setAccountStatus(AccountStatus.ACCOUNT_UNLOCKED);
                temporaryPassword.setUsed(true);

                userRepository.save(user);
                temporaryPasswordRepository.save(temporaryPassword);

                emailServices.sendMail("Password Changed", "Your password has been Changed ", initiatorsEmail);

                return new ResponseEntity<>("password Changed", HttpStatus.ACCEPTED);
            }

        }
    }

    @Override
    public ResponseEntity<?> getAllUsersOnPlatform() {
        return new ResponseEntity<>(userRepository.findAll(), HttpStatus.FOUND);
    }

    @Override
    public ResponseEntity<?> getUsersCount() {
        return new ResponseEntity<>("" +userRepository.count(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getUserDetails(String username) {
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null){
            return new ResponseEntity<>("User not Found", HttpStatus.NOT_FOUND);
        }


        return new ResponseEntity<>(user, HttpStatus.FOUND);
    }

    @Override
    public ResponseEntity<?> getUserCountByCategory(RoleType roleType) {

        Role role = roleRepository.findByRoleName(roleType.name());

        List<GrantedAuthority> superAdminAuthorities = roleType.getPermissions()
                .stream().map(permission -> new SimpleGrantedAuthority(permission.getPermission())).collect(Collectors.toList());

        List<User> users = userRepository.findByRole(role);

        return new ResponseEntity<>(users.size(), HttpStatus.FOUND);
    }

    public String randomPasswordGenerator(){

        return RandomStringUtils.randomAlphabetic(26);

    }
}
