package com.tk.fcmb.Controllers;

import com.google.common.net.InetAddresses;
import com.google.gson.JsonObject;
import com.tk.fcmb.Entities.ProcessApprovalRequest;
import com.tk.fcmb.Entities.dto.*;
import com.tk.fcmb.Entities.Role;
import com.tk.fcmb.Entities.User;
import com.tk.fcmb.Enums.RoleType;
import com.tk.fcmb.Job.IpAddressGetter;
import com.tk.fcmb.Repositories.ProcessApprovalRequestRepository;
import com.tk.fcmb.Repositories.RoleRepository;
import com.tk.fcmb.Repositories.UserRepository;
import com.tk.fcmb.Service.AuditTrailService;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/")
public class AdminFunctionsController {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditTrailService auditTrailService;

    @Autowired
    private IpAddressGetter ipAddressGetter;

    @Autowired
    private ProcessApprovalRequestRepository processApprovalRequestRepository;




    @PostMapping("/addNewRole")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> addNewRole(@RequestBody RoleCreationRequest roleCreationRequest, HttpServletRequest request) {

        User user = userRepository.findByStaffId(roleCreationRequest.getStaffId());

        if (user == null){
            return new ResponseEntity<>("User not found, check staff id", HttpStatus.NOT_FOUND);
        }

//        Role superAdmin = new Role();
//        superAdmin.setRoleName(RoleType.SUPER_ADMIN.name());
//        superAdmin.setRoleType(RoleType.SUPER_ADMIN);
//
//        if (!user.getRole().equals(superAdmin)){
//            throw new Exception("UnAuthorized to add a new Role");
//        }

        Role role = new Role();
        role.setRoleName(roleCreationRequest.getRoleName());

        List<GrantedAuthority> authorities = roleCreationRequest.getRoleFunctions().stream().map(userPermissions -> new SimpleGrantedAuthority(userPermissions.getPermission())).collect(Collectors.toList());

        role.setAuthorities(authorities);
        roleRepository.save(role);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("New Role creation");
        auditTrailDto.setTransactionDetails("Created a new Role");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        return new ResponseEntity<>("Role added successfully", HttpStatus.OK);

    }


    @PostMapping("/changeAllRoleFunctions")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> changeRoleFunctions(@RequestBody RoleFunctionChangeRequest roleFunctionChangeRequest, HttpServletRequest request){

        User user = userRepository.findByStaffId(roleFunctionChangeRequest.getStaffId());

        if (user == null){
            return new ResponseEntity<>("User not found, check staff id", HttpStatus.NOT_FOUND);
        }

        Role role = roleRepository.findByRoleName(roleFunctionChangeRequest.getRoleName());

        if (role == null) {
            return new ResponseEntity<>("role not found", HttpStatus.NOT_FOUND);
        }


        List<GrantedAuthority>  authorities = roleFunctionChangeRequest.getNewFunctions()
                .stream()
                .map(userPermissions -> new SimpleGrantedAuthority(userPermissions.getPermission()))
                .collect(Collectors.toList());


        role.setAuthorities(authorities);

        roleRepository.save(role);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Role function Change");
        auditTrailDto.setTransactionDetails("Changed the functions in a role");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        return new ResponseEntity<>("Successful", HttpStatus.OK);
    }

    @PostMapping("/addNewFunctionToRole")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> addNewFunctionToRole(@RequestBody RoleFunctionChangeRequest roleFunctionChangeRequest, HttpServletRequest request){

        User user = userRepository.findByStaffId(roleFunctionChangeRequest.getStaffId());

        if (user == null){
            return new ResponseEntity<>("User not found, check staff id", HttpStatus.NOT_FOUND);
        }

        Role role = roleRepository.findByRoleName(roleFunctionChangeRequest.getRoleName());

        if (role == null) {
            return new ResponseEntity<>("role not found", HttpStatus.NOT_FOUND);
        }


        List<GrantedAuthority>  authorities = roleFunctionChangeRequest.getNewFunctions()
                .stream()
                .map(userPermissions -> new SimpleGrantedAuthority(userPermissions.getPermission()))
                .collect(Collectors.toList());


        role.getAuthorities().addAll(authorities);

        roleRepository.save(role);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Role function Change");
        auditTrailDto.setTransactionDetails("Added new function to a role");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        return new ResponseEntity<>("Successful added new Functions to Role", HttpStatus.OK);
    }

    @PostMapping("/deleteRoleFunction")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> deleteRoleFunction(@RequestBody RoleFunctionChangeRequest roleFunctionChangeRequest, HttpServletRequest request){

        User user = userRepository.findByStaffId(roleFunctionChangeRequest.getStaffId());

        if (user == null){
            return new ResponseEntity<>("User not found, check staff id", HttpStatus.NOT_FOUND);
        }

        Role role = roleRepository.findByRoleName(roleFunctionChangeRequest.getRoleName());

        if (role == null) {
            return new ResponseEntity<>("role not found", HttpStatus.NOT_FOUND);
        }


        List<GrantedAuthority>  authorities = roleFunctionChangeRequest.getNewFunctions()
                .stream()
                .map(userPermissions -> new SimpleGrantedAuthority(userPermissions.getPermission()))
                .collect(Collectors.toList());

        role.getAuthorities().removeAll(authorities);

        roleRepository.save(role);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Role function Change");
        auditTrailDto.setTransactionDetails("Deleted a function assigned in a role");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        return new ResponseEntity<>("Successfully deleted functions from role", HttpStatus.OK);
    }


    @GetMapping("/getAllFunctionsByUser")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> getAllFunctionsByUser(@RequestParam("username") String username, HttpServletRequest request){

        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return new ResponseEntity<>("User with username" + username + " not found", HttpStatus.NOT_FOUND);
        }

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Query Functions by User");
        auditTrailDto.setTransactionDetails("Got all role assigned to a function");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        return new ResponseEntity<>(user.getRole().getAuthorities(), HttpStatus.OK);

    }

    @PostMapping("/addNewRequest")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> processRequest(@RequestBody ProcessApprovalRequestDto approvalRequestDto, HttpServletRequest request){

        if (Objects.isNull(approvalRequestDto.getStaffId())){
            return new ResponseEntity<>("Staff id cannot be null", HttpStatus.BAD_REQUEST);
        }
        if (Objects.isNull(approvalRequestDto.getReasonForInitiation())){
            return new ResponseEntity<>("Reason for initiating cannot be null", HttpStatus.BAD_REQUEST);
        }
        if (Objects.isNull(approvalRequestDto.getProcessType())){
            return new ResponseEntity<>("Process Type cannot be null", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByStaffId(approvalRequestDto.getStaffId());

        if (user == null){
            return new ResponseEntity<>("User not found, check staff id", HttpStatus.NOT_FOUND);
        }

        ProcessApprovalRequest approvalRequest = new ProcessApprovalRequest();
        approvalRequest.setMobileNumber(approvalRequestDto.getMobileNumber());
        approvalRequest.setAction(approvalRequestDto.getAction());
        approvalRequest.setNewDailyLimitAmount(approvalRequestDto.getNewDailyLimitAmount());
        approvalRequest.setNewNumber(approvalRequestDto.getNewNumber());
        approvalRequest.setNewUserType(approvalRequestDto.getNewUserType());
        approvalRequest.setOldDailyLimitAmount(approvalRequestDto.getOldDailyLimitAmount());
        approvalRequest.setOldNumber(approvalRequestDto.getOldNumber());
        approvalRequest.setOldUserType(approvalRequestDto.getOldUserType());
        approvalRequest.setProcessType(approvalRequestDto.getProcessType());
        approvalRequest.setReasonForInitiation(approvalRequestDto.getReasonForInitiation());
        approvalRequest.setStaffId(approvalRequestDto.getStaffId());
        approvalRequest.setUserTypeId(approvalRequestDto.getUserTypeId());
        approvalRequest.setConfirmPassword(approvalRequestDto.getConfirmPassword());
        approvalRequest.setNewPassword(approvalRequestDto.getNewPassword());
        approvalRequest.setOldPassword(approvalRequestDto.getOldPassword());
        approvalRequest.setOtpType(approvalRequestDto.getOtpType());


        approvalRequest.setTrackingNumber("TKT"+processApprovalRequestRepository.count()+1);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("New Approval Request Creation");
        auditTrailDto.setTransactionDetails("Created a new approval request");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);


        return new ResponseEntity<>(processApprovalRequestRepository.save(approvalRequest), HttpStatus.OK);
    }

    @GetMapping("/listOfRequests")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> getListOfRequests(@RequestParam("staffId") String staffId, HttpServletRequest request){

        if (Objects.isNull(staffId)){
            return new ResponseEntity<>("Staff id cannot be null", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByStaffId(staffId);

        if (user == null){
            return new ResponseEntity<>("User not found, check staff id", HttpStatus.NOT_FOUND);
        }

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Query List of Requests");
        auditTrailDto.setTransactionDetails("Returned a list of requests");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        return new ResponseEntity<>(processApprovalRequestRepository.findAll(), HttpStatus.OK);
    }

    @PostMapping("/approveRequest")
    @PreAuthorize("hasAuthority('approve_functions')")
    public ResponseEntity<?> approveRequest(@RequestParam("trackingNumber") String trackingNumber, @RequestParam("staffId") String staffId, HttpServletRequest request){

        if (Objects.isNull(trackingNumber)){
            return new ResponseEntity<>("Tracking number cannot be null", HttpStatus.BAD_REQUEST);
        }

        if (Objects.isNull(staffId)){
            return new ResponseEntity<>("Staff id cannot be null", HttpStatus.BAD_REQUEST);
        }
        ProcessApprovalRequest approvalRequest = processApprovalRequestRepository.findByTrackingNumber(trackingNumber);
        User user = userRepository.findByStaffId(staffId);


        if (approvalRequest == null){
            return new ResponseEntity<>("Request with tracking number not found", HttpStatus.NOT_FOUND);
        }

        if (user == null){
            return new ResponseEntity<>("User not found, check staff id", HttpStatus.NOT_FOUND);
        }

        approvalRequest.setApproved(true);

        processApprovalRequestRepository.save(approvalRequest);

        RestTemplate restTemplate = new RestTemplate();
        String url = "http://18.190.12.249:8001/FCMBProcessor";

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        switch (approvalRequest.getProcessType()){
            case UPDATE_TRANSACTION_LIMIT:{
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://18.190.12.249:8001/FCMBProcessor/list/userTypes/{id}");

                Map<String, Integer> path = new HashMap<>();
                path.put("id", approvalRequest.getUserTypeId());

                URI uri = builder.buildAndExpand(path).toUri();
                System.out.println(uri.toString());

                TransactionLimitUpgradeDto upgradeDto = new TransactionLimitUpgradeDto();
                upgradeDto.setDailyAmountLimit(approvalRequest.getOldDailyLimitAmount());
                upgradeDto.setTransactionLimit(approvalRequest.getNewDailyLimitAmount());

                AuditTrailDto auditTrailDto = new AuditTrailDto();
                auditTrailDto.setTitle("Request Approved");
                auditTrailDto.setTransactionDetails("Transaction limit upgraded");
                auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                auditTrailDto.setStaffId(user.getStaffId());
                auditTrailService.createNewEvent(auditTrailDto);

                return restTemplate.postForEntity(uri.toString(),upgradeDto,Response.class);


            }
            case UPGRADE_MOBILE_APP_USER_TYPE:{
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url+"/management/changeMobileUserType");
                params.add("mobileNumber", approvalRequest.getMobileNumber());
                params.add("userTypeId", ""+approvalRequest.getUserTypeId());


                builder.queryParams(params);

                AuditTrailDto auditTrailDto = new AuditTrailDto();
                auditTrailDto.setTitle("Request Approved");
                auditTrailDto.setTransactionDetails("Updated Mobile App UserType");
                auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                auditTrailDto.setStaffId(user.getStaffId());
                auditTrailService.createNewEvent(auditTrailDto);

                return restTemplate.exchange(builder.toUriString(),
                        HttpMethod.PUT,
                        entity,
                        Response.class);
            }
            case RESET_PIN:{
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://18.190.12.249:8001/FCMBProcessor/management/forgetPin");
                params.add("mobileNumber", approvalRequest.getMobileNumber());
                builder.queryParams(params);

                AuditTrailDto auditTrailDto = new AuditTrailDto();
                auditTrailDto.setTitle("Request Approved");
                auditTrailDto.setTransactionDetails("Pin Reset");
                auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                auditTrailDto.setStaffId(user.getStaffId());
                auditTrailService.createNewEvent(auditTrailDto);

                return restTemplate.exchange(builder.toUriString(),
                        HttpMethod.POST,
                        entity,
                        Response.class);

            }
            case BLOCK_USER:{
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://18.190.12.249:8001/FCMBProcessor/management/blockAndUnblockUser");
                params.add("mobileNumber", approvalRequest.getMobileNumber());
                params.add("action", "block");
                builder.queryParams(params);

                AuditTrailDto auditTrailDto = new AuditTrailDto();
                auditTrailDto.setTitle("Request Approved");
                auditTrailDto.setTransactionDetails("Blocked User");
                auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                auditTrailDto.setStaffId(user.getStaffId());
                auditTrailService.createNewEvent(auditTrailDto);

                return restTemplate.exchange(builder.toUriString(),
                        HttpMethod.POST,
                        entity,
                        Response.class);
            }
            case CLEAR_IMEI:{
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://18.190.12.249:8001/FCMBProcessor/management/clearImei");
                params.add("mobileNumber", approvalRequest.getMobileNumber());
                builder.queryParams(params);

                AuditTrailDto auditTrailDto = new AuditTrailDto();
                auditTrailDto.setTitle("Request Approved");
                auditTrailDto.setTransactionDetails("Imei Cleared");
                auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                auditTrailDto.setStaffId(user.getStaffId());
                auditTrailService.createNewEvent(auditTrailDto);

                return restTemplate.exchange(builder.toUriString(),
                        HttpMethod.POST,
                        entity,
                        Response.class);
            }
            case UNBLOCK_USER:{
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://18.190.12.249:8001/FCMBProcessor/management/blockAndUnblockUser");
                params.add("mobileNumber", approvalRequest.getMobileNumber());
                params.add("action", "unblock");
                builder.queryParams(params);

                AuditTrailDto auditTrailDto = new AuditTrailDto();
                auditTrailDto.setTitle("Request Approved");
                auditTrailDto.setTransactionDetails("Unblocked User");
                auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                auditTrailDto.setStaffId(user.getStaffId());
                auditTrailService.createNewEvent(auditTrailDto);

                return restTemplate.exchange(builder.toUriString(),
                        HttpMethod.POST,
                        entity,
                        Response.class);
            }
            case CONFIRM_OTP_CODE:{
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://18.190.12.249:8001/FCMBProcessor/transactions/getSuccessfulOtp");
                params.add("mobileNumber", approvalRequest.getMobileNumber());
                params.add("type", approvalRequest.getOtpType());
                builder.queryParams(params);

                AuditTrailDto auditTrailDto = new AuditTrailDto();
                auditTrailDto.setTitle("Request Approved");
                auditTrailDto.setTransactionDetails("OTP code confirmed");
                auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                auditTrailDto.setStaffId(user.getStaffId());
                auditTrailService.createNewEvent(auditTrailDto);

                return  restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, Response.class);
            }
            case UPDATE_MOIBLE_NUMBER:{
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://18.190.12.249:8001/FCMBProcessor/management/changeMobileNumber");
                params.add("mobileNumber", approvalRequest.getOldNumber());
                params.add("newMobileNumber", approvalRequest.getNewNumber());
                builder.queryParams(params);

                AuditTrailDto auditTrailDto = new AuditTrailDto();
                auditTrailDto.setTitle("Request Approved");
                auditTrailDto.setTransactionDetails("Mobile Number Updated");
                auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                auditTrailDto.setStaffId(user.getStaffId());
                auditTrailService.createNewEvent(auditTrailDto);

                return restTemplate.exchange(builder.toUriString(),
                        HttpMethod.POST,
                        entity,
                        Response.class);

            }
            case RESET_MOBILE_USER_PASSWORD:{
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://18.190.12.249:8001/FCMBProcessor/management/changePassword");
                params.add("mobileNumber", approvalRequest.getOldNumber());
                builder.queryParams(params);

                ChangePasswordDto passwordDto = new ChangePasswordDto();
                passwordDto.setConfirmPassword(approvalRequest.getConfirmPassword());
                passwordDto.setNewPassword(approvalRequest.getNewPassword());
                passwordDto.setOldPassword(approvalRequest.getOldPassword());

                AuditTrailDto auditTrailDto = new AuditTrailDto();
                auditTrailDto.setTitle("Request Approved");
                auditTrailDto.setTransactionDetails("Mobile User Password Reset");
                auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                auditTrailDto.setStaffId(user.getStaffId());
                auditTrailService.createNewEvent(auditTrailDto);

                return restTemplate.postForEntity(builder.toUriString(), passwordDto, Response.class);
            }
            default:{
                return new ResponseEntity<>("unsuccessful", HttpStatus.BAD_REQUEST);
            }
        }

    }


    @GetMapping("/getAllAuditTrails")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN,IT_CONTROL')")
    public ResponseEntity<?> getAllAuditTrails(HttpServletRequest request){

        return new ResponseEntity<>(auditTrailService.getAllAuditTrails(), HttpStatus.FOUND);
    }

    @GetMapping("/getAllAuditTrailsByDate")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN,IT_CONTROL')")
    public ResponseEntity<?> getAllAuditTrailsByDate(@RequestParam("localDate") String localDate, HttpServletRequest request){

        String dateRegex = "[0-9]{4}-[0-9]{2}-[0-9]{2}";

        if (!Pattern.matches(dateRegex, localDate)){
            return new ResponseEntity<>("invalid date", HttpStatus.BAD_REQUEST);
        }

        LocalDate date = LocalDate.parse(localDate);

        return new ResponseEntity<>(auditTrailService.getAllAuditTrailsByDate(date), HttpStatus.FOUND);
    }

    @GetMapping("/getAllByRole")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN,IT_CONTROL')")
    public ResponseEntity<?> getAllByRole(@RequestParam("roleType") RoleType roleType, HttpServletRequest request){

        return new ResponseEntity<>(auditTrailService.getAllByRole(roleType), HttpStatus.FOUND);
    }

    @GetMapping("/getAllByStaffId")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN,IT_CONTROL')")
    public ResponseEntity<?> getAllByStaffId(@RequestParam("staffId") String staffId, HttpServletRequest request){

        return new ResponseEntity<>(auditTrailService.getAllByStaffId(staffId), HttpStatus.FOUND);
    }

    @GetMapping("/getAllByIpAddress")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN,IT_CONTROL')")
    public ResponseEntity<?> getAllByIpAddress(@RequestParam("ipAddress") String ipAddress, HttpServletRequest request){

        InetAddressValidator validator = InetAddressValidator.getInstance();

         if (!validator.isValid(ipAddress)){
             return new ResponseEntity<>("Ip Address is invalid", HttpStatus.BAD_REQUEST);
         }


        return new ResponseEntity<>(auditTrailService.getAllByIpAddress(ipAddress), HttpStatus.FOUND);

    }


}
