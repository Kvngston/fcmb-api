package com.tk.fcmb.Service.impl;

import com.tk.fcmb.Entities.*;
import com.tk.fcmb.Entities.dto.*;
import com.tk.fcmb.Enums.AccountStatus;
import com.tk.fcmb.Enums.RequestStatus;
import com.tk.fcmb.Enums.RoleType;
import com.tk.fcmb.Repositories.BankBranchRepository;
import com.tk.fcmb.Repositories.RoleRepository;
import com.tk.fcmb.Repositories.UserRepository;
import com.tk.fcmb.Service.EmailServices;
import com.tk.fcmb.Service.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.util.StringUtils.isEmpty;

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
    private BCryptPasswordEncoder passwordEncoder;

    private String emailRegex = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}";

    @Override
    public ResponseEntity<?> addUser(UserDto userCreationRequest, BankBranchDto bankBranchDetailsRequest) {

        Response response = new Response();
        String mobileNumber = userCreationRequest.getPhoneNumber();
        mobileNumber = !mobileNumber.startsWith("234") ? "234" + mobileNumber.substring(1) : mobileNumber;

        if(userRepository.findByEmail(userCreationRequest.getEmail()) != null) {
            response.setResponseCode(400);
            response.setResponseMessage("Email already exists");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        if(userRepository.findByStaffId(userCreationRequest.getStaffId()) != null) {
            response.setResponseCode(400);
            response.setResponseMessage("Staff ID already exists");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(userRepository.findByPhoneNumber(mobileNumber) != null) {
            response.setResponseCode(400);
            response.setResponseMessage("Phone Number already exists");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        if (isEmpty(userCreationRequest.getRoleName())){
            response.setResponseCode(400);
            response.setResponseMessage("User Role Name Cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        else{
            return getUserResponseEntity(userCreationRequest, bankBranchDetailsRequest, userCreationRequest.getRoleName());
        }
    }

    private ResponseEntity<?> getUserResponseEntity(UserDto userCreationRequest, BankBranchDto bankBranchDetailsRequest, String roleName) {
        Response response = new Response();
        User user = new User();

        if (isEmpty(userCreationRequest.getEmail())){
            response.setResponseCode(400);
            response.setResponseMessage("User Email Cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (!Pattern.matches(emailRegex, userCreationRequest.getEmail())){
            response.setResponseData(400);
            response.setResponseMessage("Invalid Email address");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        if (isEmpty(userCreationRequest.getFirstName())){
            response.setResponseCode(400);
            response.setResponseMessage("User FirstName Cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        if (isEmpty(userCreationRequest.getLastName())){
            response.setResponseCode(400);
            response.setResponseMessage("User LastName Cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        if (isEmpty(userCreationRequest.getPhoneNumber())){
            response.setResponseCode(400);
            response.setResponseMessage("User Phone Number Cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(userCreationRequest.getPhoneNumber().length() > 13 || userCreationRequest.getPhoneNumber().length() < 11){
            response.setResponseCode(400);
            response.setResponseMessage("Invalid Phone Number");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (isEmpty(userCreationRequest.getStaffId())){
            response.setResponseCode(400);
            response.setResponseMessage("User Staff id Cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (isEmpty(bankBranchDetailsRequest.getBranchAddress())){
            response.setResponseCode(400);
            response.setResponseMessage("Branch Address Cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (isEmpty(bankBranchDetailsRequest.getBranchCode())){
            response.setResponseCode(400);
            response.setResponseMessage("Branch Code Cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (isEmpty(bankBranchDetailsRequest.getBranchName())){
            response.setResponseCode(400);
            response.setResponseMessage("Branch Name Cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (isEmpty(bankBranchDetailsRequest.getBranchState())){
            response.setResponseCode(400);
            response.setResponseMessage("Branch State Cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        user.setFirstName(userCreationRequest.getFirstName());
        user.setLastName(userCreationRequest.getLastName());
        user.setEmail(userCreationRequest.getEmail());




        user.setPhoneNumber(!userCreationRequest.getPhoneNumber().startsWith("234") ? userCreationRequest.getPhoneNumber().replaceFirst("0","234") : userCreationRequest.getPhoneNumber());
        user.setStaffId(userCreationRequest.getStaffId());
//        user.setLga(userCreationRequest.getLga());
//        user.setCountry(userCreationRequest.getCountry());
//        user.setStateOfOrigin(userCreationRequest.getStateOfOrigin());


//        if (roleType == RoleType.SUPER_ADMIN) {
//            Role superAdmin = roleRepository.findByRoleName(roleType.getRoleName());
//            if (userRepository.findByRole(superAdmin).size() == 0){
//                System.out.println("here");
//                user.setAccountStatus(AccountStatus.ACCOUNT_UNLOCKED);
//                user.setAccountLock(false);
//                user.setApproved(true);
//            }else{
//                System.out.println("Running this");
//            }
//
//        }
        Role role = roleRepository.findByRoleName(roleName);

        if(role == null){
            response.setResponseCode(400);
            response.setResponseMessage("Role Not Found, Check Role Type");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (!role.isApproved()){
            response.setResponseCode(400);
            response.setResponseMessage("Cannot assign role to user, Role not approved");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (role.getRequestStatus().equals(RequestStatus.DECLINED)){
            response.setResponseCode(400);
            response.setResponseMessage("Cannot assign role to user, Role was Declined");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        user.setRole(role);

        BankBranch bankBranch = new BankBranch();
        bankBranch.setBranchAddress(bankBranchDetailsRequest.getBranchAddress());
        bankBranch.setBranchCode(bankBranchDetailsRequest.getBranchCode());
        bankBranch.setBranchName(bankBranchDetailsRequest.getBranchName());
        bankBranch.setBranchState(bankBranchDetailsRequest.getBranchState());

        bankBranchRepository.save(bankBranch);
        user.setBankBranchDetails(bankBranch);

        userRepository.save(user);
        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData(user);


        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<?> approveUserCreation(String email) {

        Response response = new Response();
        User user = userRepository.findByEmail(email);

        if (user == null){
            response.setResponseCode(400);
            response.setResponseMessage("User not Found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (user.isApproved()){
            response.setResponseCode(400);
            response.setResponseMessage("User has been Approved already");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String generatedPassword = randomPasswordGenerator();
        user.setPassword(generatedPassword);

        try{
            emailServices.sendMail("Temporary Password", "Your generated password is " + generatedPassword, user.getEmail());
        } catch (MessagingException e) {
            e.printStackTrace();
        }



        userRepository.save(user);


        user.setRequestStatus(RequestStatus.APPROVED);
        user.setApproved(true);


        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData(userRepository.save(user));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> declineUserCreation(String email) {

        Response response = new Response();
        User user = userRepository.findByEmail(email);

        if (user == null){
            response.setResponseCode(400);
            response.setResponseMessage("User not Found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (user.isApproved()){
            response.setResponseCode(400);
            response.setResponseMessage("User has been Approved already");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        user.setRequestStatus(RequestStatus.DECLINED);
        user.setApproved(false);

        userRepository.save(user);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Declined User Creation");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> updateUserDetails(UserUpdateRequest userUpdateRequest, User loggedInUser) {

        Response response = new Response();

        if (!isEmpty(userUpdateRequest.getEmail())){
            User user = userRepository.findByEmail(userUpdateRequest.getEmail());
             if (user != null && user != loggedInUser){
                 response.setResponseCode(400);
                 response.setResponseMessage("Email already exists for a different User");
                 response.setResponseData("");
                 return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
             }else{
                 if (!Pattern.matches(emailRegex, userUpdateRequest.getEmail())){
                     response.setResponseData(400);
                     response.setResponseMessage("Invalid Email address");
                     response.setResponseData("");
                     return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                 }else{
                     loggedInUser.setEmail(userUpdateRequest.getEmail());
                 }
             }

        }

        if (!isEmpty(userUpdateRequest.getFirstName())){
            loggedInUser.setFirstName(userUpdateRequest.getFirstName());
        }


        if (!isEmpty(userUpdateRequest.getLastName())){
            loggedInUser.setLastName(userUpdateRequest.getLastName());
        }

        if (!isEmpty(userUpdateRequest.getPhoneNumber())){
            User user = userRepository.findByPhoneNumber(userUpdateRequest.getPhoneNumber());
            if (user != null && user != loggedInUser){
                response.setResponseCode(400);
                response.setResponseMessage("Phone Number already exists for a different User");
                response.setResponseData("");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }else{
                if(userUpdateRequest.getPhoneNumber().length() > 13 || userUpdateRequest.getPhoneNumber().length() < 11){
                    response.setResponseCode(400);
                    response.setResponseMessage("Invalid Phone Number");
                    response.setResponseData("");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }else{
                    loggedInUser.setPhoneNumber(!userUpdateRequest.getPhoneNumber().startsWith("234") ? userUpdateRequest.getPhoneNumber().replaceFirst("0", "234") : userUpdateRequest.getPhoneNumber());
                }
            }

        }


        if (!isEmpty(userUpdateRequest.getStaffId())){
            User user = userRepository.findByStaffId(userUpdateRequest.getStaffId());
            if (user != null && user != loggedInUser){
                response.setResponseCode(400);
                response.setResponseMessage("Staff ID already exists for a different User");
                response.setResponseData("");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }else{
                loggedInUser.setStaffId(userUpdateRequest.getStaffId());
            }
        }

        userRepository.save(loggedInUser);

        response.setResponseData(200);
        response.setResponseMessage("Successful");
        response.setResponseData("User Profile Updated Successfully");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> updateContactInfo(UserContactUpdateRequest userContactUpdateRequest, String email) {

        Response response = new Response();

        if (!Pattern.matches(emailRegex, email)){
            response.setResponseData(400);
            response.setResponseMessage("Invalid Email address");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (isEmpty(userContactUpdateRequest.getCountry())){
            response.setResponseCode(400);
            response.setResponseMessage("Country Cannot be Null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        if (isEmpty(userContactUpdateRequest.getLga())){
            response.setResponseCode(400);
            response.setResponseMessage("LGA Cannot be Null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        if (isEmpty(userContactUpdateRequest.getStateOfOrigin())){
            response.setResponseCode(400);
            response.setResponseMessage("State Of Origin Cannot be Null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User with Email "+ email+ "was not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        userRepository.save(user);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Updated User Contact Details Successfully");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> resetPassword(User loggedInUser, String userEmail) throws MessagingException {


        User user = userRepository.findByEmail(userEmail);
        Response response = new Response();

        if (user == null){
            response.setResponseData(400);
            response.setResponseMessage("Requested User Account was not found, check the email address");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        user.setOverrideLoginFlow(true);

        user.setPassword(randomPasswordGenerator());

        userRepository.save(user);
        emailServices.sendMail("Reset Password", "Reset the password with this new Password " + user.getPassword(), loggedInUser.getEmail());


        response.setResponseData(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Password has been reset");

        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> changePassword(String oldPassword, String newPassword, User user) throws MessagingException {


        Response response = new Response();

        if (passwordEncoder.matches(oldPassword, user.getPassword()) || oldPassword.equals(user.getPassword())){
            if (oldPassword.equals(newPassword)){
                response.setResponseData(400);
                response.setResponseMessage("Old Password and new Password cannot be equal");
                response.setResponseData("");
                return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setAccountLock(false);
            user.setAccountStatus(AccountStatus.ACCOUNT_UNLOCKED);
            user.setLoginCleared(true);
            userRepository.save(user);
            emailServices.sendMail("Password Changed", "Your password has been Changed ", user.getEmail());


            response.setResponseData(200);
            response.setResponseMessage("Successful");
            response.setResponseData("Password Changed");

            return new ResponseEntity<>(response, HttpStatus.OK);
        }else{
            response.setResponseData(400);
            response.setResponseMessage("Old Password mismatch");
            response.setResponseData("");
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }


    }

    @Override
    public ResponseEntity<?> forgetPassword(User user) throws MessagingException {
        Response response = new Response();

        String generatedPassword = randomPasswordGenerator();

        user.setPassword(passwordEncoder.encode(generatedPassword));
        emailServices.sendMail("Forget Password", "Your generated password is " + generatedPassword, user.getEmail());
        user.setLoginCleared(false);
        user.setAccountStatus(AccountStatus.ACCOUNT_LOCKED);
        user.setAccountLock(true);

        userRepository.save(user);

        response.setResponseData(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Password Changed");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAllUsersOnPlatform(String email, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Response response = new Response();

        List<User> users = userRepository.findAll();

        if (!email.equals("")){
           users = users.stream().filter(user -> user.getEmail().equals(email)).collect(Collectors.toList());
        }

        List<UserDetailsResponse> userDetailsResponses = new ArrayList<>();
        users.forEach(user -> userDetailsResponses.add(new UserDetailsResponse(user)));

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData(new PageImpl<>(userDetailsResponses,pageable,userDetailsResponses.size()));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getUsersCount() {
        Response response = new Response();
        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData("User Count = " + userRepository.count());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getUserDetails(String email) {

        Response response = new Response();
        if (StringUtils.isEmpty(email)){
            response.setResponseCode(400);
            response.setResponseMessage("Email cannot be empty");
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

        UserDetailsResponse userDetailsResponse = new UserDetailsResponse(user);
        System.out.println(userDetailsResponse);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData(userDetailsResponse);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getUserCountByCategory(String roleName) {

        Role role = roleRepository.findByRoleName(roleName);
        Response response = new Response();
        if (role == null){
            response.setResponseCode(400);
            response.setResponseMessage("Role not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        List<User> users = userRepository.findByRole(role);


        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData(users.size());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public Map<String, List<Integer>> userAnalysis() {

        Role superAdmin = roleRepository.findByRoleName(RoleType.SUPER_ADMIN.getRoleName());
        Role admin = roleRepository.findByRoleName(RoleType.ADMIN.getRoleName());
        Role agent = roleRepository.findByRoleName(RoleType.AGENT.getRoleName());


        List<User> users = userRepository.findAll();
        List<LocalDate> superAdminDates = users.stream()
                .filter(user -> user.getRole() == superAdmin)
                .map(AuditModel::getCreatedAt).collect(Collectors.toList())
                .stream()
                .map(date -> Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate())
                .collect(Collectors.toList());
        System.out.println(superAdminDates);

        List<LocalDate> adminDates = users.stream()
                .filter(user -> user.getRole() == admin)
                .map(AuditModel::getCreatedAt).collect(Collectors.toList()).stream()
                .map(date -> Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate())
                .collect(Collectors.toList());
        System.out.println(adminDates);

        List<LocalDate> agentDates = users.stream()
                .filter(user -> user.getRole() == agent)
                .map(AuditModel::getCreatedAt).collect(Collectors.toList())
                .stream()
                .map(date -> Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate())
                .collect(Collectors.toList());

        System.out.println(agentDates);

        int[] dailySuperAdmin = new int[7];
        List<Integer> superAdminDailyList = new ArrayList<>();

        int[] dailyAdmin = new int[7];
        List<Integer> adminDailyList = new ArrayList<>();

        int[] dailyAgent = new int[7];
        List<Integer> agentDailyList = new ArrayList<>();

        int[] superAdminWeekly = new int[5];
        List<Integer> superAdminWeeklyList = new ArrayList<>();

        int[] adminWeekly = new int[5];
        List<Integer> adminWeeklyList = new ArrayList<>();

        int[] agentWeekly = new int[5];
        List<Integer> agentWeeklyList = new ArrayList<>();


        int[] superAdminMonthly = new int[12];
        List<Integer> superAdminMonthlyList = new ArrayList<>();

        int[] adminMonthly = new int[12];
        List<Integer> adminMonthlyList = new ArrayList<>();

        int[] agentMonthly = new int[12];
        List<Integer> agentMonthlyList = new ArrayList<>();


        List<Integer> superAdminYearlyList = new ArrayList<>();
        List<Integer> adminYearlyList = new ArrayList<>();
        List<Integer> agentYearlyList = new ArrayList<>();

        superAdminYearlyList.add( (int) superAdminDates.stream().map(date -> date.getYear() == LocalDate.now().getYear()).count());
        adminYearlyList.add( (int) adminDates.stream().map(date -> date.getYear() == LocalDate.now().getYear()).count());
        agentYearlyList.add( (int) agentDates.stream().map(date -> date.getYear() == LocalDate.now().getYear()).count());

        Map<String, List<Integer>> specificResponse = new HashMap<>();
        specificResponse.put("superAdminYearly", superAdminYearlyList);
        specificResponse.put("adminYearly", adminYearlyList);
        specificResponse.put("agentYearly", agentYearlyList);


        listCheckDaily(superAdminDates, dailySuperAdmin);
        IntStream.range(0,dailySuperAdmin.length).forEach(i -> superAdminDailyList.add(dailySuperAdmin[i]));

        listCheckDaily(adminDates, dailyAdmin);
        IntStream.range(0,dailyAdmin.length).forEach(i->adminDailyList.add(dailyAdmin[i]));

        listCheckDaily(agentDates, dailyAgent);
        IntStream.range(0,dailyAgent.length).forEach(i -> agentDailyList.add(dailyAgent[i]));

        specificResponse.put("dailySuperAdmin", superAdminDailyList);
        specificResponse.put("dailyAdmin", adminDailyList);
        specificResponse.put("dailyAgent", agentDailyList);

        listCheckWeekLy(superAdminDates,superAdminWeekly);
        IntStream.range(0,superAdminWeekly.length).forEach(i -> superAdminWeeklyList.add(superAdminWeekly[i]));

        listCheckWeekLy(adminDates,adminWeekly);
        IntStream.range(0,adminWeekly.length).forEach(i -> adminWeeklyList.add(adminWeekly[i]));

        listCheckWeekLy(agentDates,agentWeekly);
        IntStream.range(0,agentWeekly.length).forEach(i -> agentWeeklyList.add(agentWeekly[i]));

        specificResponse.put("weeklySuperAdmin", superAdminWeeklyList);
        specificResponse.put("weeklyAdmin", adminWeeklyList);
        specificResponse.put("weeklyAgent", agentWeeklyList);

        listCheckMonthly(superAdminDates,superAdminMonthly);
        IntStream.range(0,superAdminMonthly.length).forEach(i -> superAdminMonthlyList.add(superAdminMonthly[i]));

        listCheckMonthly(adminDates,adminMonthly);
        IntStream.range(0,adminMonthly.length).forEach(i -> adminMonthlyList.add(adminMonthly[i]));

        listCheckMonthly(agentDates,agentMonthly);
        IntStream.range(0,agentMonthly.length).forEach(i -> agentMonthlyList.add(agentMonthly[i]));

        specificResponse.put("monthlySuperAdmin", superAdminMonthlyList);
        specificResponse.put("monthlyAdmin", adminMonthlyList);
        specificResponse.put("monthlyAgent", agentMonthlyList);

        System.out.println(specificResponse);


        return specificResponse;
    }

    private void listCheckDaily(List<LocalDate> roleDates, int[] daily) {
        roleDates.forEach(date -> {
            switch (date.getDayOfWeek()){
                case SUNDAY:{
                    daily[0]++;
                    break;
                }
                case MONDAY:{
                    daily[1]++;
                    break;
                }
                case TUESDAY:{
                    daily[2]++;
                    break;
                }
                case WEDNESDAY:{
                    daily[3]++;
                    break;
                }
                case THURSDAY:{
                    daily[4]++;
                    break;
                }
                case FRIDAY:{
                    daily[5]++;
                    break;
                }
                case SATURDAY:{
                    daily[6]++;
                    break;
                }

                default:
                    throw new IllegalStateException("Unexpected value: " + date.getDayOfWeek());
            }
        });
    }

    private void listCheckWeekLy(List<LocalDate> roleDates, int[] weekly) {
        roleDates.forEach(date -> {
            switch (date.get(ChronoField.ALIGNED_WEEK_OF_MONTH)){
                case 1:{
                    weekly[0]++;
                    break;
                }
                case 2:{
                    weekly[1]++;
                    break;
                }
                case 3:{
                    weekly[2]++;
                    break;
                }
                case 4:{
                    weekly[3]++;
                    break;
                }
                case 5:{
                    weekly[4]++;
                    break;
                }

                default:
                    throw new IllegalStateException("Unexpected value: " + date.get(ChronoField.ALIGNED_WEEK_OF_MONTH));
            }
        });
    }

    private void listCheckMonthly(List<LocalDate> roleDates, int[] monthly) {
        roleDates.forEach(date -> {
            switch (date.getMonth()){
                case JANUARY:{
                    monthly[0]++;
                    break;
                }
                case FEBRUARY:{
                    monthly[1]++;
                    break;
                }
                case MARCH:{
                    monthly[2]++;
                    break;
                }
                case APRIL:{
                    monthly[3]++;
                    break;
                }
                case MAY:{
                    monthly[4]++;
                    break;
                }
                case JUNE:{
                    monthly[5]++;
                    break;
                }
                case JULY:{
                    monthly[6]++;
                    break;
                }
                case AUGUST:{
                    monthly[7]++;
                    break;
                }
                case SEPTEMBER:{
                    monthly[8]++;
                    break;
                }
                case OCTOBER:{
                    monthly[9]++;
                    break;
                }
                case NOVEMBER:{
                    monthly[10]++;
                    break;
                }
                case DECEMBER:{
                    monthly[11]++;
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + date.getMonth());
            }
        });

    }

    public String randomPasswordGenerator(){

        return RandomStringUtils.randomAlphabetic(26);

    }
}
