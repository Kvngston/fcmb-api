package com.tk.fcmb.Controllers;

import com.tk.fcmb.Entities.OTP;
import com.tk.fcmb.Entities.Role;
import com.tk.fcmb.Entities.User;
import com.tk.fcmb.Entities.dto.*;
import com.tk.fcmb.Enums.AccountStatus;
import com.tk.fcmb.Enums.RoleType;
import com.tk.fcmb.Job.IpAddressGetter;
import com.tk.fcmb.Repositories.UserRepository;
import com.tk.fcmb.Service.AuditTrailService;
import com.tk.fcmb.Service.CustomUserDetailsService;
import com.tk.fcmb.Service.OtpService;
import com.tk.fcmb.Service.UserService;
import com.tk.fcmb.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@RestController
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private OtpService otpService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuditTrailService auditTrailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private IpAddressGetter ipAddressGetter;


    @PostMapping("/createSuperAdmin")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> createSuperAdmin(@RequestBody UserCreationRequest userCreationRequest, HttpServletRequest request) throws Exception {

        System.out.println(userCreationRequest.getBankBranchDto());
        ResponseEntity<?> user =  userService.addSuperAdmin(userCreationRequest.getUserDto(), userCreationRequest.getBankBranchDto());

        if (user.getStatusCode() == HttpStatus.CREATED){
            AuditTrailDto auditTrailDto = new AuditTrailDto();
            auditTrailDto.setTitle("Super Admin creation");
            auditTrailDto.setTransactionDetails("Created a Super Admin");
            auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
            auditTrailDto.setStaffId(userCreationRequest.getUserDto().getStaffId());
            auditTrailService.createNewEvent(auditTrailDto);
        }


        return new ResponseEntity<>("User created Successfully", HttpStatus.CREATED);
    }


    @PostMapping("/createAdmin")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN,IT_CONTROL')")
    public ResponseEntity<?> createAdmin(@RequestBody UserCreationRequest userCreationRequest, HttpServletRequest request) throws Exception{
        ResponseEntity<?> user =  userService.addAdmin(userCreationRequest.getUserDto(), userCreationRequest.getBankBranchDto());

        if (user.getStatusCode() == HttpStatus.CREATED) {
            AuditTrailDto auditTrailDto = new AuditTrailDto();
            auditTrailDto.setTitle("Admin creation");
            auditTrailDto.setTransactionDetails("Created an Admin");
            auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
            auditTrailDto.setStaffId(userCreationRequest.getUserDto().getStaffId());
            auditTrailService.createNewEvent(auditTrailDto);
        }
        return user;
    }

    @PostMapping("/createRegularUser")
    @PreAuthorize("hasAuthority('create_user')")
    public ResponseEntity<?> createRegularUser(@RequestBody UserDto userDto, @RequestBody BankBranchDto bankBranchDto, HttpServletRequest request) throws Exception{
        ResponseEntity<?> user =  userService.addUser(userDto, bankBranchDto);


        if (user.getStatusCode() == HttpStatus.CREATED) {
            AuditTrailDto auditTrailDto = new AuditTrailDto();
            auditTrailDto.setTitle("User creation");
            auditTrailDto.setTransactionDetails("Created a User");
            auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
            auditTrailDto.setStaffId(userDto.getStaffId());
            auditTrailService.createNewEvent(auditTrailDto);
        }
        return user;
    }

    @PostMapping("/approveCreatedUser")
    @PreAuthorize("hasAuthority('approve_functions')")
    public ResponseEntity<?> approveCreatedUser(@RequestParam("username") String username, @RequestParam("staffId") String staffId,  HttpServletRequest request){

        User user = userRepository.findByStaffId(staffId);
        if (user == null) {
            return new ResponseEntity<>("User with staff id "+ staffId+ "was not found", HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> response = userService.approveUserCreation(username);

        if (response.getStatusCode() == HttpStatus.OK) {
            AuditTrailDto auditTrailDto = new AuditTrailDto();
            auditTrailDto.setTitle("Approve User Created");
            auditTrailDto.setTransactionDetails("verified a Created User");
            auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
            auditTrailDto.setStaffId(user.getStaffId());
            auditTrailService.createNewEvent(auditTrailDto);
        }
        return response;

    }


    @PostMapping("/reGenerateOtp")
    @PreAuthorize("permitAll()")
    public ResponseEntity<OTP> reGenerateOtpForUser(@RequestParam(name = "id") long userId, @RequestParam("otpSendMode") String otpSendMode, HttpServletRequest request) throws Exception {
        ResponseEntity<OTP> otp =  otpService.reGenerateOtp(userId,otpSendMode);

        User staff = userRepository.findById(userId).orElse(null);

        if (staff == null){
            throw new Exception("Staff not found, check user id");
        }

        if (otp.getStatusCode() == HttpStatus.CREATED) {
            AuditTrailDto auditTrailDto = new AuditTrailDto();
            auditTrailDto.setTitle("OTP ReGeneration");
            auditTrailDto.setTransactionDetails("Regenerated an OTP");
            auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
            auditTrailDto.setStaffId(staff.getStaffId());
            auditTrailService.createNewEvent(auditTrailDto);
        }

        return otp;
    }

    @PostMapping("/resetPassword")
    @PreAuthorize("hasAuthority('create_user')")
    public ResponseEntity<?> resetPassword(@RequestParam(name = "userId") long userId, @RequestParam(name = "initiatorsEmail") String initiatorsEmail, HttpServletRequest request) throws Exception {

        ResponseEntity<?> temporaryPasswordResponseEntity = userService.resetPassword(userId, initiatorsEmail);

        User staff = userRepository.findById(userId).orElse(null);

        if (staff == null){
            throw new Exception("Staff not found, check user id");
        }


        if (temporaryPasswordResponseEntity.getStatusCode() == HttpStatus.OK){
            AuditTrailDto auditTrailDto = new AuditTrailDto();
            auditTrailDto.setTitle("Password Reset");
            auditTrailDto.setTransactionDetails("Resetting Password");
            auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
            auditTrailDto.setStaffId(staff.getStaffId());
            auditTrailService.createNewEvent(auditTrailDto);
        }

        return temporaryPasswordResponseEntity;
    }

    @PostMapping("/changePassword")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> changePassword(@RequestParam(name = "newPassword") String newPassword,
                                                 @RequestParam(name = "generatedPassword") String generatedPassword,
                                                 @RequestParam(name = "userId") long userId,
                                                 @RequestParam(name = "initiatorsEmail") String initiatorsEmail,
                                            HttpServletRequest request) throws Exception {

        ResponseEntity<?> changePassword = userService.changePassword(newPassword, generatedPassword, userId, initiatorsEmail);

        User staff = userRepository.findById(userId).orElse(null);

        if (staff == null){
            throw new Exception("Staff not found, check user id");
        }

        if (changePassword.getStatusCode() == HttpStatus.ACCEPTED){
            AuditTrailDto auditTrailDto = new AuditTrailDto();
            auditTrailDto.setTitle("Password Changed");
            auditTrailDto.setTransactionDetails("Password has been  Changed");
            auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
            auditTrailDto.setStaffId(staff.getStaffId());
            auditTrailService.createNewEvent(auditTrailDto);
        }

        return changePassword;
    }





    public AuthenticationResponse createAuthenticationToken(UserDetails userDetails) throws Exception{


        final String jwt = jwtUtil.generateToken(userDetails);

        return new AuthenticationResponse(jwt);
    }


//    @GetMapping("/hello")
//    @PreAuthorize("permitAll()")
//    public String hello(){
//        return "Hello world";
//    }
//
//    @GetMapping("/adminHi")
//    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
//    public String hi(){
//        return "Admin Hi there";
//    }


    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getLoginDetails(@RequestBody AuthenticationRequest authenticationRequest, HttpServletRequest request) throws Exception {
        User user = userRepository.findByUsername(authenticationRequest.getUsername()).orElse(null);

        if (user == null){
            return new ResponseEntity<>("user with username " + authenticationRequest.getUsername() + " doesn't exist", HttpStatus.BAD_REQUEST);
        }

        if (user.getAccountStatus() == AccountStatus.ACCOUNT_LOCKED){
            return new ResponseEntity<>("User account has been locked or not verified", HttpStatus.BAD_REQUEST);
        }

        if (authenticationRequest.getOtpMode().isEmpty()){
            return new ResponseEntity<>("Otp mode has to be between email and sms", HttpStatus.BAD_REQUEST);
        }

        if (passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword())){
            System.out.println(user);
            otpService.generateOtp(user.getId(), authenticationRequest.getOtpMode().toLowerCase());
        }else {
            return new ResponseEntity<>("Password mismatch", HttpStatus.BAD_REQUEST);
        }

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Login");
        auditTrailDto.setTransactionDetails("User Login");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);


        //get ip address and save it in the audit


        return ResponseEntity.ok("Otp has been sent to the required medium");
    }


    @PostMapping("/verifyOtp")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> login(@RequestParam(name = "username") String username, @RequestParam(name = "otp") String otp, HttpServletRequest request) throws Exception {
        //change the request to accept otp as a field and the user id

        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            throw new Exception("User not found");
        }

        if (Objects.equals(otpService.verifyOtp(user.getId(), otp).getBody(), true)){
            final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

            AuditTrailDto auditTrailDto = new AuditTrailDto();
            auditTrailDto.setTitle("Otp verification");
            auditTrailDto.setTransactionDetails("Verified User Otp");
            auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
            auditTrailDto.setStaffId(user.getStaffId());
            auditTrailService.createNewEvent(auditTrailDto);

            return new ResponseEntity<>(createAuthenticationToken(userDetails), HttpStatus.ACCEPTED);
        }else{
            return new ResponseEntity<>("Otp doesn't match", HttpStatus.NOT_FOUND);
        }
    }


    @GetMapping("/getAllUsers")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getAllUsersOnPlatform(@RequestParam(name = "staffId") String staffId, HttpServletRequest request) throws Exception {

        Role superAdmin = new Role();
        superAdmin.setRoleType(RoleType.SUPER_ADMIN);

        User user = userRepository.findByStaffId(staffId);

        if (user == null) {
            throw new Exception("Staff not found, check staff id");
        }

        if (userRepository.findByStaffId(staffId).getRole().equals(superAdmin)){
            ResponseEntity<?> userResponse = userService.getAllUsersOnPlatform();

            if (userResponse.getStatusCode() == HttpStatus.FOUND){
                AuditTrailDto auditTrailDto = new AuditTrailDto();
                auditTrailDto.setTitle("Query all users");
                auditTrailDto.setTransactionDetails("Returning all users on the platform");
                auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                auditTrailDto.setStaffId(staffId);
                auditTrailService.createNewEvent(auditTrailDto);
            }
            return userResponse;
        }

        return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/getUsersCount")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getUsersCountOnThePlatform(@RequestParam(name = "staffId") String staffId, HttpServletRequest request) throws Exception {
        Role superAdmin = new Role();
        superAdmin.setRoleType(RoleType.SUPER_ADMIN);

        User user = userRepository.findByStaffId(staffId);

        if (user == null) {
            throw new Exception("Staff not found, check staff id");
        }

        if (user.getRole().equals(superAdmin)){
            ResponseEntity<?> userResponse = userService.getUsersCount();

            if (userResponse.getStatusCode() == HttpStatus.FOUND){
                AuditTrailDto auditTrailDto = new AuditTrailDto();
                auditTrailDto.setTitle("Query Users Count");
                auditTrailDto.setTransactionDetails("Returning the count on users of the platform");
                auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                auditTrailDto.setStaffId(staffId);
                auditTrailService.createNewEvent(auditTrailDto);
            }
            return userResponse;
        }

        return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
    }


    @GetMapping("/getUsersDetails")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getUserDetails(@RequestParam("username") String username, @RequestParam("staffId") String staffId,  HttpServletRequest request){

        User user = userRepository.findByStaffId(staffId);
        if (user == null) {
            return new ResponseEntity<>("User with staff id "+ staffId+ "was not found", HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> response = userService.getUserDetails(username);

        if (response.getStatusCode() == HttpStatus.OK) {
            AuditTrailDto auditTrailDto = new AuditTrailDto();
            auditTrailDto.setTitle("Returned User details");
            auditTrailDto.setTransactionDetails("Returned user details of a Created User");
            auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
            auditTrailDto.setStaffId(user.getStaffId());
            auditTrailService.createNewEvent(auditTrailDto);
        }
        return response;
    }

    //do this later
    @GetMapping("/getUserCountByCategories")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getUserCountByCategory(@RequestParam(name = "staffId") String staffId, @RequestParam(name = "roleType") RoleType roleType, HttpServletRequest request){

        User user = userRepository.findByStaffId(staffId);
        if (user == null) {
            return new ResponseEntity<>("User with staff id "+ staffId+ "was not found", HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> response = userService.getUserCountByCategory(roleType);
        if (response.getStatusCode() == HttpStatus.FOUND) {
            AuditTrailDto auditTrailDto = new AuditTrailDto();
            auditTrailDto.setTitle("Returned User Count by Category");
            auditTrailDto.setTransactionDetails("Returned user count of a Created Users in a category");
            auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
            auditTrailDto.setStaffId(user.getStaffId());
            auditTrailService.createNewEvent(auditTrailDto);
        }
        return response;
    }



}
