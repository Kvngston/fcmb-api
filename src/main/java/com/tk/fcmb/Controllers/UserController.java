package com.tk.fcmb.Controllers;

import com.tk.fcmb.Entities.Role;
import com.tk.fcmb.Entities.TokenBlackList;
import com.tk.fcmb.Entities.User;
import com.tk.fcmb.Entities.dto.*;
import com.tk.fcmb.Enums.AccountStatus;
import com.tk.fcmb.Enums.LoginFlag;
import com.tk.fcmb.Enums.RoleType;
import com.tk.fcmb.Job.IpAddressGetter;
import com.tk.fcmb.Repositories.TokenBlackListRepository;
import com.tk.fcmb.Repositories.UserRepository;
import com.tk.fcmb.Service.AuditTrailService;
import com.tk.fcmb.Service.CustomUserDetailsService;
import com.tk.fcmb.Service.OtpService;
import com.tk.fcmb.Service.UserService;
import com.tk.fcmb.utils.GetAuthenticatedUser;
import com.tk.fcmb.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.isEmpty;

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

    @Autowired
    private TokenBlackListRepository tokenBlackListRepository;


    @Autowired
    private GetAuthenticatedUser AuthenticatedUser;

    private String emailRegex = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}";

    @PostMapping("/createUser")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> createUser(@RequestBody UserCreationRequest userCreationRequest, HttpServletRequest request) throws Exception {

        User user = AuthenticatedUser.getAuthenticatedUser(request);

        Response response = new Response();
        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User was not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> userResponseEntity =  userService.addUser(userCreationRequest.getUserDto(), userCreationRequest.getBankBranchDto());
        if (userResponseEntity.getStatusCode() == HttpStatus.CREATED){
            AuditTrailDto auditTrailDto = new AuditTrailDto();
            auditTrailDto.setTitle(userCreationRequest.getUserDto().getRoleName() + " Creation");
            auditTrailDto.setTransactionDetails("Created a User - "+ userCreationRequest.getUserDto().getEmail());
            auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
            auditTrailDto.setStaffId(user.getStaffId());
            auditTrailService.createNewEvent(auditTrailDto);



            response.setResponseCode(200);
            response.setResponseMessage("Successful");
            response.setResponseData("User created Successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        return userResponseEntity;

    }

    @PostMapping("/updateUserProfile")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> updateUserDetails(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request){
        User user = AuthenticatedUser.getAuthenticatedUser(request);
        Response response = new Response();
        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User was not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> responseEntity = userService.updateUserDetails(userUpdateRequest,user);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            AuditTrailDto auditTrailDto = new AuditTrailDto();
            auditTrailDto.setTitle("Update User Details");
            auditTrailDto.setTransactionDetails("Updated a User's Profile");
            auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
            auditTrailDto.setStaffId(user.getStaffId());
            auditTrailService.createNewEvent(auditTrailDto);


            response.setResponseCode(200);
            response.setResponseMessage("Successful");
            response.setResponseData("User Updated Successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return responseEntity;
    }

//    @PostMapping("/updateUserContact")
//    @PreAuthorize("permitAll()")
//    public ResponseEntity<?> updateUserContactInfo(@RequestBody UserContactUpdateRequest userContactUpdateRequest, HttpServletRequest request){
//        User user = AuthenticatedUser.getAuthenticatedUser(request);
//        Response response = new Response();
//        if (user == null) {
//            response.setResponseCode(400);
//            response.setResponseMessage("User was not found");
//            response.setResponseData("");
//            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//        }
//
//        ResponseEntity<?> responseEntity = userService.updateContactInfo(userContactUpdateRequest,user.getEmail());
//
//        if (responseEntity.getStatusCode() == HttpStatus.OK) {
//            AuditTrailDto auditTrailDto = new AuditTrailDto();
//            auditTrailDto.setTitle("Update User Details");
//            auditTrailDto.setTransactionDetails("Updated a User's Contact Info");
//            auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
//            auditTrailDto.setStaffId(user.getStaffId());
//            auditTrailService.createNewEvent(auditTrailDto);
//
//
//            response.setResponseCode(200);
//            response.setResponseMessage("Successful");
//            response.setResponseData("User Contact Updated Successfully");
//
//            return new ResponseEntity<>(response, HttpStatus.OK);
//        }
//        return responseEntity;
//    }

    @PostMapping("/approveCreatedUser")
    @PreAuthorize("hasAuthority('approve_functions')")
    public ResponseEntity<?> approveCreatedUser(@RequestParam("userEmail") String email,  HttpServletRequest request){

        Response response = new Response();
        if (StringUtils.isEmpty(email)){
            response.setResponseCode(400);
            response.setResponseMessage("Email Cannot be Null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (!Pattern.matches(emailRegex, email)){
            response.setResponseData(400);
            response.setResponseMessage("Invalid Email address");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User was not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> responseEntity = userService.approveUserCreation(email);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            AuditTrailDto auditTrailDto = new AuditTrailDto();
            auditTrailDto.setTitle("SUCCESS - Approve User Created");
            auditTrailDto.setTransactionDetails("verified a Created User - " +user.getEmail());
            auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
            auditTrailDto.setStaffId(user.getStaffId());
            auditTrailService.createNewEvent(auditTrailDto);

            response.setResponseCode(200);
            response.setResponseMessage("Successful");
            response.setResponseData("User Approved Successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        return responseEntity;
    }

    @PostMapping("/declineCreatedUser")
    @PreAuthorize("hasAuthority('approve_functions')")
    public ResponseEntity<?> declineCreatedUser(@RequestParam("userEmail") String email,  HttpServletRequest request){

        Response response = new Response();
        if (StringUtils.isEmpty(email)){
            response.setResponseCode(400);
            response.setResponseMessage("Email Cannot be Null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (!Pattern.matches(emailRegex, email)){
            response.setResponseData(400);
            response.setResponseMessage("Invalid Email address");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User was not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> responseEntity = userService.approveUserCreation(email);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            AuditTrailDto auditTrailDto = new AuditTrailDto();
            auditTrailDto.setTitle("FAILED - Approve User Created");
            auditTrailDto.setTransactionDetails("Declined a Created User - " +user.getEmail());
            auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
            auditTrailDto.setStaffId(user.getStaffId());
            auditTrailService.createNewEvent(auditTrailDto);

            response.setResponseCode(200);
            response.setResponseMessage("Successful");
            response.setResponseData("User Declined Successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        return responseEntity;
    }

    @PostMapping("/reSendOtp")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> resendOtp(@RequestParam("email") String email, @RequestParam("otpMode") String otpMode, HttpServletRequest request) throws Exception {

        Response response = new Response();
        if (StringUtils.isEmpty(email)){
            response.setResponseCode(400);
            response.setResponseMessage("Email Cannot be Null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (!Pattern.matches(emailRegex, email)){
            response.setResponseData(400);
            response.setResponseMessage("Invalid Email address");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isEmpty(otpMode)){
            response.setResponseCode(400);
            response.setResponseMessage("Otp Mode cannot be Null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User was not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        user.setLoginFlag(LoginFlag.VERIFY_OTP_FLAG);


        if (otpMode.toLowerCase().equals("email".toLowerCase())){
            otpService.generateOtp(user.getEmail(), "email");
        }else if (otpMode.toLowerCase().equals("sms".toLowerCase())){
            otpService.generateOtp(user.getEmail(), "sms");
        }else {
            response.setResponseCode(400);
            response.setResponseMessage("Invalid OtpMode, OtpMode must be between email and sms");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("OTP ReGeneration");
        auditTrailDto.setTransactionDetails("Regenerated an OTP");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        userRepository.save(user);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Otp Resent Successfully, Confirm Otp");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/resetPassword")
    @PreAuthorize("hasAuthority('reset_password')")
    public ResponseEntity<?> resetPassword(@RequestParam(name = "userEmail") String userEmail, HttpServletRequest request) throws Exception {

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        Response response = new Response();

        if (!Pattern.matches(emailRegex, userEmail)){
            response.setResponseData(400);
            response.setResponseMessage("Invalid Email address");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (user == null) {
            response.setResponseData(400);
            response.setResponseMessage("User was not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> temporaryPasswordResponseEntity = userService.resetPassword(user, userEmail);

        if (temporaryPasswordResponseEntity.getStatusCode() == HttpStatus.OK){
            AuditTrailDto auditTrailDto = new AuditTrailDto();
            auditTrailDto.setTitle("Password Reset");
            auditTrailDto.setTransactionDetails("Resetting Password");
            auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
            auditTrailDto.setStaffId(user.getStaffId());
            auditTrailService.createNewEvent(auditTrailDto);

            response.setResponseData(200);
            response.setResponseMessage("Successful");
            response.setResponseData("Password has been reset");
            return new ResponseEntity<>(response, HttpStatus.OK);

        }

        return temporaryPasswordResponseEntity;
    }

    @PostMapping("/changePassword")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> changePassword(@RequestParam(name = "newPassword") String newPassword,
                                                 @RequestParam(name = "oldPassword") String oldPassword,
                                                 @RequestParam(name = "email") String email,
                                            HttpServletRequest request) throws Exception {


        Response response = new Response();

        User user = userRepository.findByEmail(email);

        if (user == null) {
            response.setResponseData(400);
            response.setResponseMessage("User was not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        if (StringUtils.isEmpty(newPassword)){
            response.setResponseData(400);
            response.setResponseMessage("New Password Cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        if (StringUtils.isEmpty(oldPassword)){
            response.setResponseData(400);
            response.setResponseMessage("Old Password Cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isEmpty(email)){
            response.setResponseData(400);
            response.setResponseMessage("Email Cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        if (!Pattern.matches(emailRegex, email)){
            response.setResponseData(400);
            response.setResponseMessage("Invalid Email address");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> changePassword = userService.changePassword(oldPassword, newPassword, user);



        if (changePassword.getStatusCode() == HttpStatus.OK){
            AuditTrailDto auditTrailDto = new AuditTrailDto();
            auditTrailDto.setTitle("Password Changed");
            auditTrailDto.setTransactionDetails("Password has been  Changed");
            auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
            auditTrailDto.setStaffId(user.getStaffId());
            auditTrailService.createNewEvent(auditTrailDto);
        }

        return changePassword;
    }



    @PostMapping("/forgetPassword")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> changePassword(@RequestParam(name = "email") String email, HttpServletRequest request) throws MessagingException {
        Response response = new Response();

        if (StringUtils.isEmpty(email)){
            response.setResponseData(400);
            response.setResponseMessage("Email Cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        if (!Pattern.matches(emailRegex, email)){
            response.setResponseData(400);
            response.setResponseMessage("Invalid Email address");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByEmail(email);

        if (user == null) {
            response.setResponseData(400);
            response.setResponseMessage("User was not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> responseEntity = userService.forgetPassword(user);

        if (responseEntity.getStatusCode() == HttpStatus.OK){
            AuditTrailDto auditTrailDto = new AuditTrailDto();
            auditTrailDto.setTitle("Forgot Password");
            auditTrailDto.setTransactionDetails("Resetting Password");
            auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
            auditTrailDto.setStaffId(user.getStaffId());
            auditTrailService.createNewEvent(auditTrailDto);

        }

        response.setResponseData(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Password has been reset");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    public AuthenticationResponse createAuthenticationToken(UserDetails userDetails){


        final String jwt = jwtUtil.generateToken(userDetails);

        return new AuthenticationResponse(jwt);
    }



    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getLoginDetails(@RequestBody AuthenticationRequest authenticationRequest, HttpServletRequest request){
        Response response = new Response();


        if(StringUtils.isEmpty(authenticationRequest.getEmail())){
            response.setResponseCode(400);
            response.setResponseMessage("Email Cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(StringUtils.isEmpty(authenticationRequest.getPassword())){
            response.setResponseCode(400);
            response.setResponseMessage("Password Cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        if (!Pattern.matches(emailRegex, authenticationRequest.getEmail())){
            response.setResponseCode(400);
            response.setResponseMessage("Invalid Email address");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByEmail(authenticationRequest.getEmail());

        if (user == null){
            response.setResponseCode(400);
            response.setResponseMessage("user with email " + authenticationRequest.getEmail() + " doesn't exist");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (user.isOverrideLoginFlow()){
            final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setAuthenticationResponse(createAuthenticationToken(userDetails));
            loginResponse.setFirstName(user.getFirstName());
            loginResponse.setLastName(user.getLastName());
            loginResponse.setRole(user.getRole().getRoleName());

            response.setResponseCode(200);
            response.setResponseMessage("Successful");
            response.setResponseData(loginResponse);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        if (!user.isLoginCleared()){
            response.setResponseCode(400);
            response.setResponseMessage("User account is not cleared for login, Please change your password");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (user.getAccountStatus() == AccountStatus.ACCOUNT_LOCKED){
            response.setResponseCode(400);
            response.setResponseMessage("User account has been locked or not verified");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (!passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword())){
            response.setResponseCode(400);
            response.setResponseMessage("Password mismatch");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Login");
        auditTrailDto.setTransactionDetails("User Login");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        user.setLoginFlag(LoginFlag.CONFIRM_OTP_FLAG);
        userRepository.save(user);

        LoggedInUserDetailsDto detailsDto = new LoggedInUserDetailsDto();

        detailsDto.setAccountLock(user.isAccountLock());
        detailsDto.setAccountStatus(user.getAccountStatus());
        detailsDto.setApproved(user.isApproved());
        detailsDto.setLoginCleared(user.isLoginCleared());
        detailsDto.setLoginFlag(user.getLoginFlag());
        detailsDto.setOverrideLoginFlow(user.isOverrideLoginFlow());
        detailsDto.setResponse("Login details accepted, Confirm Otp mode");

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData(detailsDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> logout(HttpServletRequest request){

        Response response = new Response();
        User user = AuthenticatedUser.getAuthenticatedUser(request);
        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User was not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String token = AuthenticatedUser.getUserToken(request);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());


        if (jwtUtil.validateToken(token,userDetails)){
            TokenBlackList tokenBlackList = new TokenBlackList();
            tokenBlackList.setToken(token);
            tokenBlackListRepository.save(tokenBlackList);
        }else {
            response.setResponseCode(400);
            response.setResponseMessage("Invalid Token");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Logout");
        auditTrailDto.setTransactionDetails("User Logout");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        user.setLoginFlag(LoginFlag.DETAILS_FLAG);
        user.setOverrideLoginFlow(false);
        userRepository.save(user);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Successfully logged out");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/confirmOtpMode")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> confirmOtpMode(@RequestParam("otpMode") String otpMode, @RequestParam("email") String email, HttpServletRequest request) throws Exception {

        Response response = new Response();

        if (StringUtils.isEmpty(email)){
            response.setResponseCode(400);
            response.setResponseMessage("Email cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        if (StringUtils.isEmpty(otpMode)){
            response.setResponseCode(400);
            response.setResponseMessage("Otp mode cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (!Pattern.matches(emailRegex, email)){
            response.setResponseData(400);
            response.setResponseMessage("Invalid Email address");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        User user = userRepository.findByEmail(email);

        if (user == null){
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (user.getLoginFlag() != LoginFlag.CONFIRM_OTP_FLAG && user.getLoginFlag() != LoginFlag.VERIFY_OTP_FLAG){
            response.setResponseCode(400);
            response.setResponseMessage("User not at required stage to confirm otp mode");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Confirm otp Mode");
        auditTrailDto.setTransactionDetails("User Confirmed Otp Mode");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        if (otpMode.toLowerCase().equals("email".toLowerCase())){

            otpService.generateOtp(user.getEmail(), "email");
            response.setResponseCode(200);
            response.setResponseMessage("Otp has been sent");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }else if (otpMode.toLowerCase().equals("sms".toLowerCase())){
            otpService.generateOtp(user.getEmail(), "sms");
            response.setResponseCode(200);
            response.setResponseMessage("Otp has been sent");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }else {
            response.setResponseCode(400);
            response.setResponseMessage("Invalid OtpMode, OtpMode must be between email and sms");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


    }

    @PostMapping("/verifyOtp")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> login(@RequestParam(name = "email") String email, @RequestParam(name = "otp") String otp, HttpServletRequest request){
        //change the request to accept otp as a field and the user id

        Response response = new Response();

        if (StringUtils.isEmpty(email)){
            response.setResponseCode(400);
            response.setResponseMessage("Email cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isEmpty(otp)){
            response.setResponseCode(400);
            response.setResponseMessage("Otp cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (!Pattern.matches(emailRegex, email)){
            response.setResponseData(400);
            response.setResponseMessage("Invalid Email address");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByEmail(email);

        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (user.getLoginFlag() != LoginFlag.VERIFY_OTP_FLAG){
            response.setResponseCode(400);
            response.setResponseMessage("User not at required stage to verify otp");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (Objects.equals(otpService.verifyOtp(user.getId(), otp).getBody(), true)){
            final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

            AuditTrailDto auditTrailDto = new AuditTrailDto();
            auditTrailDto.setTitle("Otp verification");
            auditTrailDto.setTransactionDetails("Verified User Otp");
            auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
            auditTrailDto.setStaffId(user.getStaffId());
            auditTrailService.createNewEvent(auditTrailDto);

            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setAuthenticationResponse(createAuthenticationToken(userDetails));
            loginResponse.setFirstName(user.getFirstName());
            loginResponse.setLastName(user.getLastName());
            loginResponse.setRole(user.getRole().getRoleName());

            user.setLoginFlag(LoginFlag.DETAILS_FLAG);
            userRepository.save(user);

            response.setResponseCode(200);
            response.setResponseMessage("Successful");
            response.setResponseData(loginResponse);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }else{
            response.setResponseCode(400);
            response.setResponseMessage("Otp doesn't match");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/getAllUsers")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getAllUsersOnPlatform( HttpServletRequest request,@RequestParam(value = "email", defaultValue = "") String email,  @RequestParam("page") int page, @RequestParam("size") int size) {

        Role superAdmin = new Role();
        superAdmin.setRoleType(RoleType.SUPER_ADMIN);

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        Response response = new Response();
        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User was not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(!email.equals("")){
            if (!Pattern.matches(emailRegex, email)){
                response.setResponseData(400);
                response.setResponseMessage("Invalid Email address");
                response.setResponseData("");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        }


        if (userRepository.findByStaffId(user.getStaffId()).getRole().equals(superAdmin)){
            ResponseEntity<?> userResponse = userService.getAllUsersOnPlatform(email,page, size);

            if (userResponse.getStatusCode() == HttpStatus.OK){
                AuditTrailDto auditTrailDto = new AuditTrailDto();
                auditTrailDto.setTitle("Query all users");
                auditTrailDto.setTransactionDetails("Returning all users on the platform");
                auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                auditTrailDto.setStaffId(user.getStaffId());
                auditTrailService.createNewEvent(auditTrailDto);
            }
            return userResponse;
        }

        response.setResponseCode(400);
        response.setResponseMessage("User not permitted");
        response.setResponseData("");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/getUsersCount")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getUsersCountOnThePlatform( HttpServletRequest request) {

        Role superAdmin = new Role();
        superAdmin.setRoleType(RoleType.SUPER_ADMIN);

        Response response = new Response();

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User was not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (user.getRole().equals(superAdmin)){
            ResponseEntity<?> userResponse = userService.getUsersCount();

            if (userResponse.getStatusCode() == HttpStatus.OK){
                AuditTrailDto auditTrailDto = new AuditTrailDto();
                auditTrailDto.setTitle("Query Users Count");
                auditTrailDto.setTransactionDetails("Returning the count on users of the platform");
                auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                auditTrailDto.setStaffId(user.getStaffId());
                auditTrailService.createNewEvent(auditTrailDto);
            }
            return userResponse;
        }

        response.setResponseCode(400);
        response.setResponseMessage("User not permitted");
        response.setResponseData("");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    @GetMapping("/getUsersDetails")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getUserDetails(@RequestParam("email") String username,  HttpServletRequest request){


        Response response = new Response();

        if (StringUtils.isEmpty(username)){
            response.setResponseCode(400);
            response.setResponseMessage("Email cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (!Pattern.matches(emailRegex, username)){
            response.setResponseData(400);
            response.setResponseMessage("Invalid Email address");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> responseEntity = userService.getUserDetails(username);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            AuditTrailDto auditTrailDto = new AuditTrailDto();
            auditTrailDto.setTitle("Returned User details");
            auditTrailDto.setTransactionDetails("Returned user details of a Created User");
            auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
            auditTrailDto.setStaffId(user.getStaffId());
            auditTrailService.createNewEvent(auditTrailDto);
        }
        return responseEntity;
    }

    //do this later
    @GetMapping("/getUserCountByCategories")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getUserCountByCategory(@RequestParam(name = "roleName") String roleName, HttpServletRequest request){

        Response response = new Response();
        User user = AuthenticatedUser.getAuthenticatedUser(request);
        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isEmpty(roleName)){
            response.setResponseCode(400);
            response.setResponseMessage("Role name cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> responseEntity = userService.getUserCountByCategory(roleName);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            AuditTrailDto auditTrailDto = new AuditTrailDto();
            auditTrailDto.setTitle("Returned User Count by Category");
            auditTrailDto.setTransactionDetails("Returned user count of a Created Users in a category");
            auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
            auditTrailDto.setStaffId(user.getStaffId());
            auditTrailService.createNewEvent(auditTrailDto);
        }
        return responseEntity;
    }

    @GetMapping("/getUnApprovedUsers")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getUnApprovedUsers(@RequestParam("page") int page, @RequestParam("size") int size, HttpServletRequest request){
        Response response = new Response();
        User user = AuthenticatedUser.getAuthenticatedUser(request);
        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Pageable pageable = PageRequest.of(page,size);

        List<User> users =  userRepository.findAllByApproved(false);

        List<UserDetailsResponse> userDetailsResponses = new ArrayList<>();
        users.forEach(user1 -> userDetailsResponses.add(new UserDetailsResponse(user1)));


        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData(new PageImpl<>(userDetailsResponses, pageable, userDetailsResponses.size()));


        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Returned UnApproved Users");
        auditTrailDto.setTransactionDetails("Returned UnApproved users in the platform");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        return new ResponseEntity<>(response,HttpStatus.OK);


    }
}
