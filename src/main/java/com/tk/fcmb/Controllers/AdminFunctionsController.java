package com.tk.fcmb.Controllers;

import com.tk.fcmb.Entities.*;
import com.tk.fcmb.Entities.dto.*;
import com.tk.fcmb.Enums.*;
import com.tk.fcmb.Job.IpAddressGetter;
import com.tk.fcmb.Repositories.*;
import com.tk.fcmb.Service.AuditTrailService;
import com.tk.fcmb.utils.ConvertToPageable;
import com.tk.fcmb.utils.GetAuthenticatedUser;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
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

    @Autowired
    private UserActionsRequestRepository userActionsRequestRepository;

    @Autowired
    private UserRoleUpdateRequestRepository roleUpdateRequestRepository;

    @Autowired
    private AddNewFunctionToRoleRequestRepository newFunctionToRoleRequestRepository;

    @Autowired
    private DeleteRoleFunctionRequestRepository deleteRoleFunctionRequestRepository;

    @Autowired
    private ChangedAllRoleFunctionsRequestRepository changedAllRoleFunctionsRequestRepository;

    @Autowired
    private GetAuthenticatedUser AuthenticatedUser;

    @Value("${fcmb.processor.base.url}")
    private String fcmbProcessorBaseUrl;


    @PostMapping("/addNewRole")
    @PreAuthorize("hasAuthority('create_new_role')")
    public ResponseEntity<?> addNewRole(@RequestBody RoleCreationRequest roleCreationRequest, HttpServletRequest request) {

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        Response response = new Response();

        if (user == null){
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        System.out.println(roleCreationRequest.getRoleFunctions());

        if (StringUtils.isEmpty(roleCreationRequest.getRoleName())){
            response.setResponseCode(400);
            response.setResponseMessage("Role Name cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (roleCreationRequest.getRoleFunctions().size() == 0){
            response.setResponseCode(400);
            response.setResponseMessage("Role functions must contain atleast one function");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Role oldRole = roleRepository.findByRoleName(roleCreationRequest.getRoleName());

        if (oldRole == null) {
            Role role = new Role();
            role.setRoleName(roleCreationRequest.getRoleName().toUpperCase());

            List<UserPermissions> roleFunctions = roleCreationRequest.getRoleFunctions();
            for (UserPermissions permissions : roleFunctions) {
                if (!Arrays.asList(UserPermissions.values()).contains(permissions)) {
                    response.setResponseCode(400);
                    response.setResponseMessage("Invalid role function, " + permissions);
                    response.setResponseData("");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }

            List<GrantedAuthority> authorities = roleCreationRequest.getRoleFunctions().stream().map(userPermissions -> new SimpleGrantedAuthority(userPermissions.getPermission())).collect(Collectors.toList());

            role.setAuthorities(authorities);
            role.setTicketNumber("TKT"+(roleRepository.count()+1));
            roleRepository.save(role);


            AuditTrailDto auditTrailDto = new AuditTrailDto();
            auditTrailDto.setTitle("Role creation request");
            auditTrailDto.setTransactionDetails("Initiated a new Role Creation Request");
            auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
            auditTrailDto.setStaffId(user.getStaffId());
            auditTrailService.createNewEvent(auditTrailDto);

            response.setResponseCode(200);
            response.setResponseMessage("Successful");
            response.setResponseData("Role added Successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        }else {
            if (!oldRole.isApproved()){
                response.setResponseCode(400);
                response.setResponseMessage("Role already exists but it has not been approved");
                response.setResponseData("");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            response.setResponseCode(400);
            response.setResponseMessage("Role already exists");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("/getListOfUnApprovedRoleRequests")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> getListOfUnApprovedRoleRequests(@RequestParam("page") int page, @RequestParam("size") int size,  HttpServletRequest request){
        Response response = new Response();

        User loggedInUser = AuthenticatedUser.getAuthenticatedUser(request);

        if (loggedInUser == null){
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Pageable pageable = PageRequest.of(page, size);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("UnApproved Roles List Query");
        auditTrailDto.setTransactionDetails("Returned a list of User UnApproved Role Requests");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(loggedInUser.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData(roleRepository.findAllByRequestStatus(RequestStatus.PENDING,pageable));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/approveRoleCreation")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> approveRoleCreation(@RequestParam("ticketNumber") String ticketNumber, HttpServletRequest request){
        Response response = new Response();

        User loggedInUser = AuthenticatedUser.getAuthenticatedUser(request);

        if (loggedInUser == null){
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isEmpty(ticketNumber)){
            response.setResponseData(400);
            response.setResponseMessage("Ticket Number cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Role role = roleRepository.findByTicketNumber(ticketNumber);

        if (role == null){
            response.setResponseData(400);
            response.setResponseMessage("Request not found, check ticket number");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(role.isApproved()){
            response.setResponseCode(400);
            response.setResponseMessage("Role is approved already");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (role.getRequestStatus().equals(RequestStatus.DECLINED)){
            response.setResponseCode(400);
            response.setResponseMessage("Role was Declined");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        role.setApproved(true);
        role.setRequestStatus(RequestStatus.APPROVED);
        roleRepository.save(role);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("SUCCESS - Role Creation Approval");
        auditTrailDto.setTransactionDetails("Approved Created Role");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(loggedInUser.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Role Approved");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/declineRoleCreation")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> declineRoleCreation(@RequestParam("ticketNumber") String ticketNumber, HttpServletRequest request){
        Response response = new Response();

        User loggedInUser = AuthenticatedUser.getAuthenticatedUser(request);

        if (loggedInUser == null){
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isEmpty(ticketNumber)){
            response.setResponseData(400);
            response.setResponseMessage("Ticket Number cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Role role = roleRepository.findByTicketNumber(ticketNumber);

        if (role == null){
            response.setResponseData(400);
            response.setResponseMessage("Request not found, check ticket number");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        role.setApproved(false);
        role.setRequestStatus(RequestStatus.DECLINED);
        roleRepository.save(role);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("FAILED - Role Creation Approval");
        auditTrailDto.setTransactionDetails("Declined Role Creation");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(loggedInUser.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Role Declined");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }






    @PostMapping("/initiateUserRoleUpdate")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> approveUserRoleUpdate(@RequestParam("userEmail") String userEmail, @RequestParam("roleName") String roleName, HttpServletRequest request){
        Response response = new Response();

        User loggedInUser = AuthenticatedUser.getAuthenticatedUser(request);

        if (loggedInUser == null){
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isEmpty(userEmail)){
            response.setResponseCode(400);
            response.setResponseMessage("User email cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isEmpty(roleName)){
            response.setResponseCode(400);
            response.setResponseMessage("Role Name cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByEmail(userEmail);

        if (user == null){
            response.setResponseCode(400);
            response.setResponseMessage("User with email " + userEmail+ " not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Role role = roleRepository.findByRoleName(roleName);
        if (role == null){
            response.setResponseCode(400);
            response.setResponseMessage("Role not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(user.getRole().equals(role)){
            response.setResponseCode(400);
            response.setResponseMessage("User already has this role");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (roleUpdateRequestRepository.findAllByUserAndApproved(user,false).size() > 0){
            response.setResponseCode(400);
            response.setResponseMessage("User has a pending role update request");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(!role.isApproved()){
            response.setResponseCode(400);
            response.setResponseMessage("Role is awaiting approval");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        UserRoleUpdateRequest userRoleUpdateRequest = new UserRoleUpdateRequest();
        userRoleUpdateRequest.setRoleName(role.getRoleName());
        userRoleUpdateRequest.setUser(user);
        userRoleUpdateRequest.setTicketNumber("TKT"+(roleUpdateRequestRepository.count()+1));
        userRoleUpdateRequest.setApproved(false);

        roleUpdateRequestRepository.save(userRoleUpdateRequest);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Role Update Request");
        auditTrailDto.setTransactionDetails("Initiated User Role Update Request");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(loggedInUser.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);



        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Role Update Initiated");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getListOfRoleUpdateRequests")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> getListOfRoleUpdateRequests(@RequestParam("page") int page, @RequestParam("size") int size,  HttpServletRequest request){
        Response response = new Response();

        User loggedInUser = AuthenticatedUser.getAuthenticatedUser(request);

        if (loggedInUser == null){
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Pageable pageable = PageRequest.of(page, size);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Roles Update Requests List Query");
        auditTrailDto.setTransactionDetails("Returned a list of User Role Update Requests");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(loggedInUser.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData(roleUpdateRequestRepository.findAllByRequestStatus(RequestStatus.PENDING, pageable));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/approveUserRoleUpdate")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> approveUserRoleUpdate(@RequestParam("ticketNumber") String ticketNumber, HttpServletRequest request){
        Response response = new Response();

        User loggedInUser = AuthenticatedUser.getAuthenticatedUser(request);

        if (loggedInUser == null){
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isEmpty(ticketNumber)){
            response.setResponseData(400);
            response.setResponseMessage("Ticket Number cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        UserRoleUpdateRequest roleUpdateRequest = roleUpdateRequestRepository.findByTicketNumber(ticketNumber);

        if (roleUpdateRequest == null){
            response.setResponseData(400);
            response.setResponseMessage("Request not found, check ticket number");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (roleUpdateRequest.isApproved()){
            response.setResponseData(400);
            response.setResponseMessage("Request has been approved already");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (roleUpdateRequest.getRequestStatus().equals(RequestStatus.DECLINED)){
            response.setResponseData(400);
            response.setResponseMessage("Request was Declined");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        roleUpdateRequest.setApproved(true);
        roleUpdateRequest.setRequestStatus(RequestStatus.APPROVED);

        roleUpdateRequest.getUser().setRole(roleRepository.findByRoleName(roleUpdateRequest.getRoleName()));
        roleUpdateRequestRepository.save(roleUpdateRequest);
        userRepository.save(roleUpdateRequest.getUser());


        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("SUCCESS - Role Update Approval");
        auditTrailDto.setTransactionDetails("Approved User Role Update Request");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(loggedInUser.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);



        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Role Update Approved");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/declineUserRoleUpdate")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> declineUserRoleUpdate(@RequestParam("ticketNumber") String ticketNumber, HttpServletRequest request){
        Response response = new Response();

        User loggedInUser = AuthenticatedUser.getAuthenticatedUser(request);

        if (loggedInUser == null){
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isEmpty(ticketNumber)){
            response.setResponseData(400);
            response.setResponseMessage("Ticket Number cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        UserRoleUpdateRequest roleUpdateRequest = roleUpdateRequestRepository.findByTicketNumber(ticketNumber);

        if (roleUpdateRequest == null){
            response.setResponseData(400);
            response.setResponseMessage("Request not found, check ticket number");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        roleUpdateRequest.setApproved(false);
        roleUpdateRequest.setRequestStatus(RequestStatus.DECLINED);

        roleUpdateRequestRepository.save(roleUpdateRequest);



        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("FAILED - Role Update Approval");
        auditTrailDto.setTransactionDetails("Approved User Role Update Request");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(loggedInUser.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);



        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Role Update Declined");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }





    @GetMapping("/getAllFunctionsOfARole")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getFunctionsInRole(@RequestParam("roleName") String roleName, HttpServletRequest request, @RequestParam("page") int page,  @RequestParam("size") int size){
        User user = AuthenticatedUser.getAuthenticatedUser(request);
        Response response = new Response();
        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isEmpty(roleName)){
            response.setResponseCode(400);
            response.setResponseMessage("Role Name cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Role role = roleRepository.findByRoleName(roleName);

        if (role == null){
            response.setResponseCode(400);
            response.setResponseMessage("Role not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Role Search");
        auditTrailDto.setTransactionDetails("Queried all functions in a  roles");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);


        ConvertToPageable convertToPageable = new ConvertToPageable(page, size);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData(convertToPageable.convertListToPage(role.getAuthorities()));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getAllFunctions")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getFunctionsInRole(HttpServletRequest request){
        User user = AuthenticatedUser.getAuthenticatedUser(request);
        Response response = new Response();
        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        List<UserPermissions> functions = Arrays.asList(UserPermissions.values());

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Functions Search");
        auditTrailDto.setTransactionDetails("Queried all functions");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);


        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData(functions);

        return new ResponseEntity<>(response,HttpStatus.OK);

    }

    @GetMapping("/getAllRoles")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> getAllRoles(HttpServletRequest request, @RequestParam("page") int page,  @RequestParam("size") int size){
        User user = AuthenticatedUser.getAuthenticatedUser(request);
        Response response = new Response();
        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Role Search");
        auditTrailDto.setTransactionDetails("Queried all the roles");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        ConvertToPageable convertToPageable = new ConvertToPageable(page, size);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData(convertToPageable.convertListToPage(roleRepository.findAll()));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }





    @PostMapping("/initiateChangeAllRoleFunctions")
    @PreAuthorize("hasAuthority('modify_functions')")
    public ResponseEntity<?> initiateChangeAllRoleFunctions(@RequestBody RoleFunctionChangeRequest roleFunctionChangeRequest, HttpServletRequest request){

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        Response response = new Response();
        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Role role = roleRepository.findByRoleName(roleFunctionChangeRequest.getRoleName());

        if (role == null) {
            response.setResponseCode(400);
            response.setResponseMessage("Role not found");
            response.setResponseData("");
            return new ResponseEntity<>("role not found", HttpStatus.BAD_REQUEST);
        }

        if(!role.isApproved()){
            response.setResponseCode(400);
            response.setResponseMessage("Role is awaiting approval");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        List<UserPermissions> roleFunctions = roleFunctionChangeRequest.getNewFunctions();
//        for (UserPermissions permissions : roleFunctions) {
//            if (!Arrays.asList(UserPermissions.values()).contains(permissions)) {
//                response.setResponseCode(400);
//                response.setResponseMessage("Invalid role function, " + permissions);
//                response.setResponseData("");
//                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//            }else{
//                if (role.getAuthorities().contains(new SimpleGrantedAuthority(permissions.getPermission()))){
//                    response.setResponseCode(400);
//                    response.setResponseMessage("Role already has this function " + permissions);
//                    response.setResponseData("");
//                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//                }
//            }
//        }


        ChangeAllRoleFunctionsRequest changeAllRoleFunctionsRequest = new ChangeAllRoleFunctionsRequest();
        changeAllRoleFunctionsRequest.setRoleName(role.getRoleName());
        changeAllRoleFunctionsRequest.setApproved(false);
        changeAllRoleFunctionsRequest.setUserPermissionsList(roleFunctions);
        changeAllRoleFunctionsRequest.setTicketNumber("TKT"+(changedAllRoleFunctionsRequestRepository.count()+1));

        changedAllRoleFunctionsRequestRepository.save(changeAllRoleFunctionsRequest);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Role function Change Request");
        auditTrailDto.setTransactionDetails("Initiated Role Functions Change");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Added new Change All Role Functions Request");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getListOfChangeAllRoleFunctionsRequests")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> getListOfChangeAllRoleFunctionsRequests(@RequestParam("page") int page, @RequestParam("size") int size,  HttpServletRequest request){
        Response response = new Response();

        User loggedInUser = AuthenticatedUser.getAuthenticatedUser(request);

        if (loggedInUser == null){
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Pageable pageable = PageRequest.of(page, size);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Change All Role Functions Requests List Query");
        auditTrailDto.setTransactionDetails("Returned a list of Change all role functions requests");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(loggedInUser.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData(changedAllRoleFunctionsRequestRepository.findAllByRequestStatus(RequestStatus.PENDING, pageable));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/approveChangeAllRoleFunctionsRequest")
    @PreAuthorize("hasAuthority('modify_functions')")
    public ResponseEntity<?> approveChangeAllRoleFunctionsRequest(@RequestParam("ticketNumber") String ticketNumber, HttpServletRequest request){

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        Response response =  new Response();

        if (StringUtils.isEmpty(ticketNumber)){
            response.setResponseData(400);
            response.setResponseMessage("Ticket Number cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        ChangeAllRoleFunctionsRequest changeAllRoleFunctionsRequest = changedAllRoleFunctionsRequestRepository.findByTicketNumber(ticketNumber);

        if (changeAllRoleFunctionsRequest == null) {
            response.setResponseData(400);
            response.setResponseMessage("Request not found, check ticket number");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        Role role = roleRepository.findByRoleName(changeAllRoleFunctionsRequest.getRoleName());

        if (role == null) {
            response.setResponseCode(400);
            response.setResponseMessage("Role not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (changeAllRoleFunctionsRequest.isApproved()){
            response.setResponseCode(400);
            response.setResponseMessage("Request has already been approved");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (changeAllRoleFunctionsRequest.getRequestStatus().equals(RequestStatus.DECLINED)){
            response.setResponseData(400);
            response.setResponseMessage("Request was Declined");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        List<GrantedAuthority>  authorities = changeAllRoleFunctionsRequest.getUserPermissionsList()
                .stream()
                .map(userPermissions -> new SimpleGrantedAuthority(userPermissions.getPermission()))
                .collect(Collectors.toList());

        authorities.add(new SimpleGrantedAuthority(changeAllRoleFunctionsRequest.getRoleName().toUpperCase()));

        role.setAuthorities(authorities);

        roleRepository.save(role);

        changeAllRoleFunctionsRequest.setApproved(true);
        changeAllRoleFunctionsRequest.setRequestStatus(RequestStatus.APPROVED);
        changedAllRoleFunctionsRequestRepository.save(changeAllRoleFunctionsRequest);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("SUCCESS - Role functions Change Approval");
        auditTrailDto.setTransactionDetails("Changed All functions in a Role");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Successfully Approved Change All Role functions");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/declineChangeAllRoleFunctionsRequest")
    @PreAuthorize("hasAuthority('modify_functions')")
    public ResponseEntity<?> declineChangeAllRoleFunctionsRequest(@RequestParam("ticketNumber") String ticketNumber, HttpServletRequest request){

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        Response response =  new Response();

        if (StringUtils.isEmpty(ticketNumber)){
            response.setResponseData(400);
            response.setResponseMessage("Ticket Number cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        ChangeAllRoleFunctionsRequest changeAllRoleFunctionsRequest = changedAllRoleFunctionsRequestRepository.findByTicketNumber(ticketNumber);

        if (changeAllRoleFunctionsRequest == null) {
            response.setResponseData(400);
            response.setResponseMessage("Request not found, check ticket number");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }



        changeAllRoleFunctionsRequest.setApproved(false);
        changeAllRoleFunctionsRequest.setRequestStatus(RequestStatus.DECLINED);
        changedAllRoleFunctionsRequestRepository.save(changeAllRoleFunctionsRequest);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("FAILED - Role functions Change Approval");
        auditTrailDto.setTransactionDetails("Failed to Changed All functions in a Role");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Successfully Declined Change All Role functions request");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }







    @PostMapping("/initiateNewFunctionToRoleAddition")
    @PreAuthorize("hasAuthority('modify_functions')")
    public ResponseEntity<?> initiateNewFunctionToRoleAddition(@RequestBody RoleFunctionChangeRequest roleFunctionChangeRequest, HttpServletRequest request){

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        Response response =  new Response();

        if (StringUtils.isEmpty(roleFunctionChangeRequest.getRoleName())){
            response.setResponseCode(400);
            response.setResponseMessage("Role Name cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (roleFunctionChangeRequest.getNewFunctions().size() == 0){
            response.setResponseCode(400);
            response.setResponseMessage("Role functions must contain atleast one function");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Role role = roleRepository.findByRoleName(roleFunctionChangeRequest.getRoleName());

        if (role == null) {
            response.setResponseCode(400);
            response.setResponseMessage("Role not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(!role.isApproved()){
            response.setResponseCode(400);
            response.setResponseMessage("Role is awaiting approval");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        List<UserPermissions> roleFunctions = roleFunctionChangeRequest.getNewFunctions();
        for (UserPermissions permissions : roleFunctions) {
            if (!Arrays.asList(UserPermissions.values()).contains(permissions)) {
                response.setResponseCode(400);
                response.setResponseMessage("Invalid role function, " + permissions);
                response.setResponseData("");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }else{
                if (role.getAuthorities().contains(new SimpleGrantedAuthority(permissions.getPermission()))){
                    response.setResponseCode(400);
                    response.setResponseMessage("Role already has this function " + permissions);
                    response.setResponseData("");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }
        }


        AddNewFunctionToRoleRequest newFunctionToRoleRequest = new AddNewFunctionToRoleRequest();
        newFunctionToRoleRequest.setRoleName(role.getRoleName());
        newFunctionToRoleRequest.setApproved(false);
        newFunctionToRoleRequest.setUserPermissionsList(roleFunctions);
        newFunctionToRoleRequest.setTicketNumber("TKT"+(newFunctionToRoleRequestRepository.count()+1));

        newFunctionToRoleRequestRepository.save(newFunctionToRoleRequest);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Role function Change");
        auditTrailDto.setTransactionDetails("Initiated new function to a role Addition");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Successful added new Functions to Role");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getListOfNewFunctionToRoleRequests")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> getListOfNewFunctionToRoleRequests(@RequestParam("page") int page, @RequestParam("size") int size,  HttpServletRequest request){
        Response response = new Response();

        User loggedInUser = AuthenticatedUser.getAuthenticatedUser(request);

        if (loggedInUser == null){
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Pageable pageable = PageRequest.of(page, size);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Adding new Functions to Roles Requests List Query");
        auditTrailDto.setTransactionDetails("Returned a list of User Role function addition Requests");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(loggedInUser.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData(newFunctionToRoleRequestRepository.findAllByRequestStatus(RequestStatus.PENDING, pageable));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/approveNewFunctionToRoleAddition")
    @PreAuthorize("hasAuthority('modify_functions')")
    public ResponseEntity<?> addNewFunctionToRole(@RequestParam("ticketNumber") String ticketNumber, HttpServletRequest request){

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        Response response =  new Response();

        if (StringUtils.isEmpty(ticketNumber)){
            response.setResponseData(400);
            response.setResponseMessage("Ticket Number cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        AddNewFunctionToRoleRequest newFunctionToRoleRequest = newFunctionToRoleRequestRepository.findByTicketNumber(ticketNumber);

        if (newFunctionToRoleRequest == null) {
            response.setResponseData(400);
            response.setResponseMessage("Request not found, check ticket number");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        Role role = roleRepository.findByRoleName(newFunctionToRoleRequest.getRoleName());

        if (role == null) {
            response.setResponseCode(400);
            response.setResponseMessage("Role not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (newFunctionToRoleRequest.isApproved()){
            response.setResponseCode(400);
            response.setResponseMessage("Request has already been approved");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (newFunctionToRoleRequest.getRequestStatus().equals(RequestStatus.DECLINED)){
            response.setResponseData(400);
            response.setResponseMessage("Request was Declined");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        List<GrantedAuthority>  authorities = newFunctionToRoleRequest.getUserPermissionsList()
                .stream()
                .map(userPermissions -> new SimpleGrantedAuthority(userPermissions.getPermission()))
                .collect(Collectors.toList());


        role.getAuthorities().addAll(authorities);

        roleRepository.save(role);

        newFunctionToRoleRequest.setApproved(true);
        newFunctionToRoleRequest.setRequestStatus(RequestStatus.APPROVED);
        newFunctionToRoleRequestRepository.save(newFunctionToRoleRequest);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("SUCCESS - Role function Change Approval");
        auditTrailDto.setTransactionDetails("Added new function to a role");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Successful added new Functions to Role");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/declineNewFunctionToRoleAddition")
    @PreAuthorize("hasAuthority('modify_functions')")
    public ResponseEntity<?> declineNewFunctionToRoleAddition(@RequestParam("ticketNumber") String ticketNumber, HttpServletRequest request){

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        Response response =  new Response();

        if (StringUtils.isEmpty(ticketNumber)){
            response.setResponseData(400);
            response.setResponseMessage("Ticket Number cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        AddNewFunctionToRoleRequest newFunctionToRoleRequest = newFunctionToRoleRequestRepository.findByTicketNumber(ticketNumber);

        if (newFunctionToRoleRequest == null) {
            response.setResponseData(400);
            response.setResponseMessage("Request not found, check ticket number");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        newFunctionToRoleRequest.setApproved(false);
        newFunctionToRoleRequest.setRequestStatus(RequestStatus.DECLINED);
        newFunctionToRoleRequestRepository.save(newFunctionToRoleRequest);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("FAILED - Role function Change Approval");
        auditTrailDto.setTransactionDetails("Declined add new function to a role");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Successfully declined add new Functions to Role request");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }







    @PostMapping("/initiateDeleteRoleFunction")
    @PreAuthorize("hasAuthority('modify_functions')")
    public ResponseEntity<?> deleteRoleFunction(@RequestBody RoleFunctionChangeRequest roleFunctionChangeRequest, HttpServletRequest request){

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        Response response = new Response();

        if (StringUtils.isEmpty(roleFunctionChangeRequest.getRoleName())){
            response.setResponseCode(400);
            response.setResponseMessage("Role Name cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (roleFunctionChangeRequest.getNewFunctions().size() == 0){
            response.setResponseCode(400);
            response.setResponseMessage("Role functions must contain atleast one function");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }



        Role role = roleRepository.findByRoleName(roleFunctionChangeRequest.getRoleName());

        if (role == null) {
            response.setResponseCode(400);
            response.setResponseMessage("Role not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(!role.isApproved()){
            response.setResponseCode(400);
            response.setResponseMessage("Role is awaiting approval");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        List<UserPermissions> roleFunctions = roleFunctionChangeRequest.getNewFunctions();
        for (UserPermissions permissions : roleFunctions) {
            if (!Arrays.asList(UserPermissions.values()).contains(permissions)) {
                response.setResponseCode(400);
                response.setResponseMessage("Invalid role function, " + permissions);
                response.setResponseData("");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }else{
                if (!role.getAuthorities().contains(new SimpleGrantedAuthority(permissions.getPermission()))){
                    response.setResponseCode(400);
                    response.setResponseMessage("Role doesn't have the role function, " + permissions);
                    response.setResponseData("");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }
        }

        DeleteRoleFunctionRequest deleteRoleFunctionRequest = new DeleteRoleFunctionRequest();
        deleteRoleFunctionRequest.setRoleName(role.getRoleName());
        deleteRoleFunctionRequest.setApproved(false);
        deleteRoleFunctionRequest.setUserPermissionsList(roleFunctions);
        deleteRoleFunctionRequest.setTicketNumber("TKT"+(deleteRoleFunctionRequestRepository.count()+1));

        deleteRoleFunctionRequestRepository.save(deleteRoleFunctionRequest);


        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Role function Change");
        auditTrailDto.setTransactionDetails("Initiated a Delete Role function");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Successfully Initiated a Delete Role functions");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getListOfDeleteRoleFunctionRequests")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> getListOfDeleteRoleFunctionRequests(@RequestParam("page") int page, @RequestParam("size") int size,  HttpServletRequest request){
        Response response = new Response();

        User loggedInUser = AuthenticatedUser.getAuthenticatedUser(request);

        if (loggedInUser == null){
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Pageable pageable = PageRequest.of(page, size);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Role Function Delete Requests List Query");
        auditTrailDto.setTransactionDetails("Returned a list of User Role Function Delete Requests");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(loggedInUser.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData(deleteRoleFunctionRequestRepository.findAllByRequestStatus(RequestStatus.PENDING, pageable));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/approveDeleteRoleFunction")
    @PreAuthorize("hasAuthority('modify_functions')")
    public ResponseEntity<?> approveDeleteRoleFunction(@RequestParam("ticketNumber") String ticketNumber, HttpServletRequest request){

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        Response response =  new Response();

        if (StringUtils.isEmpty(ticketNumber)){
            response.setResponseData(400);
            response.setResponseMessage("Ticket Number cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        DeleteRoleFunctionRequest deleteRoleFunctionRequest = deleteRoleFunctionRequestRepository.findByTicketNumber(ticketNumber);

        if (deleteRoleFunctionRequest == null) {
            response.setResponseData(400);
            response.setResponseMessage("Request not found, check ticket number");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(deleteRoleFunctionRequest.getRequestStatus().equals(RequestStatus.DECLINED)){
            response.setResponseData(400);
            response.setResponseMessage("Request was Declined");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        Role role = roleRepository.findByRoleName(deleteRoleFunctionRequest.getRoleName());

        if (role == null) {
            response.setResponseCode(400);
            response.setResponseMessage("Role not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        List<GrantedAuthority>  authorities = deleteRoleFunctionRequest.getUserPermissionsList()
                .stream()
                .map(userPermissions -> new SimpleGrantedAuthority(userPermissions.getPermission()))
                .collect(Collectors.toList());


        role.getAuthorities().removeAll(authorities);

        roleRepository.save(role);

        deleteRoleFunctionRequest.setApproved(true);
        deleteRoleFunctionRequest.setRequestStatus(RequestStatus.APPROVED);
        deleteRoleFunctionRequestRepository.save(deleteRoleFunctionRequest);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("SUCCESS - Role function Deletion Approval");
        auditTrailDto.setTransactionDetails("Deleted Role function");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Successfully deleted Functions from a Role");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/declineDeleteRoleFunction")
    @PreAuthorize("hasAuthority('modify_functions')")
    public ResponseEntity<?> declineDeleteRoleFunction(@RequestParam("ticketNumber") String ticketNumber, HttpServletRequest request){

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        Response response =  new Response();

        if (StringUtils.isEmpty(ticketNumber)){
            response.setResponseData(400);
            response.setResponseMessage("Ticket Number cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        DeleteRoleFunctionRequest deleteRoleFunctionRequest = deleteRoleFunctionRequestRepository.findByTicketNumber(ticketNumber);

        if (deleteRoleFunctionRequest == null) {
            response.setResponseData(400);
            response.setResponseMessage("Request not found, check ticket number");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        Role role = roleRepository.findByRoleName(deleteRoleFunctionRequest.getRoleName());

        if (role == null) {
            response.setResponseCode(400);
            response.setResponseMessage("Role not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        deleteRoleFunctionRequest.setApproved(false);
        deleteRoleFunctionRequest.setRequestStatus(RequestStatus.DECLINED);
        deleteRoleFunctionRequestRepository.save(deleteRoleFunctionRequest);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("FAILED - Role function Deletion Approval");
        auditTrailDto.setTransactionDetails("Failed to Deleted Role function");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Successfully declined delete Functions from a Role request");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }







    @GetMapping("/getAllFunctionsByUser")
    @PreAuthorize("hasAuthority('modify_functions')")
    public ResponseEntity<?> getAllFunctionsByUser(@RequestParam("email") String email, HttpServletRequest request){
        Response response = new Response();

        if (StringUtils.isEmpty(email)){
            response.setResponseData(400);
            response.setResponseMessage("Email cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String emailRegex = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}";

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

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Query Functions by User");
        auditTrailDto.setTransactionDetails("Got all role assigned to a function");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData(user.getRole().getAuthorities());
        return new ResponseEntity<>(response, HttpStatus.OK);

    }


    @PostMapping("/confirmOtpMobile")
    @PreAuthorize("hasAuthority('confirm_otp')")
    public ResponseEntity<?> confirmOtpMobile(@RequestParam("otpType") String otpType, @RequestParam("mobileNumber") String mobileNumber,  HttpServletRequest request){
        User user = AuthenticatedUser.getAuthenticatedUser(request);
        Response response = new Response();

        if (StringUtils.isEmpty(otpType)){
            response.setResponseCode(400);
            response.setResponseMessage("OtpType cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isEmpty(mobileNumber)){
            response.setResponseCode(400);
            response.setResponseMessage("Mobile Number cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (mobileNumber.length() < 11 || mobileNumber.length() > 13){
            response.setResponseCode(400);
            response.setResponseMessage("Invalid Mobile Number");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String userMobileNumber = !mobileNumber.startsWith("234") ? mobileNumber.replaceFirst("0", "234") : mobileNumber;


        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fcmbProcessorBaseUrl+"/transactions/getSuccessfulOtp");
        params.add("mobileNumber", userMobileNumber);
        params.add("type", otpType);
        builder.queryParams(params);


        try{

            Response data = Objects.requireNonNull(restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, Response.class).getBody());

            if (data.getResponseCode() == 0){
                response.setResponseCode(200);
                response.setResponseMessage("Successful");
                response.setResponseData(data.getResponseData() == null ? data.getResponseMessage() : data.getResponseData());

                AuditTrailDto auditTrailDto = new AuditTrailDto();
                auditTrailDto.setTitle("Mobile Otp Confirmation");
                auditTrailDto.setTransactionDetails("OTP code confirmed for Mobile - " + userMobileNumber);
                auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                auditTrailDto.setStaffId(user.getStaffId());
                auditTrailService.createNewEvent(auditTrailDto);

                return new ResponseEntity<>(response,HttpStatus.OK);
            }else{
                response.setResponseCode(400);
                response.setResponseMessage(data.getResponseMessage());
                response.setResponseData("");
                return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
            }
        }catch (HttpStatusCodeException e) {
            response.setResponseCode(400);
            response.setResponseMessage("Request Failed");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

    }






    @PostMapping("/addNewRequest")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> processRequest(@RequestBody ProcessApprovalRequestDto approvalRequestDto, HttpServletRequest request){

        Response response = new Response();

        if (StringUtils.isEmpty(approvalRequestDto.getReasonForInitiation())){
            response.setResponseCode(400);
            response.setResponseMessage("Reason for initiating cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        if (StringUtils.isEmpty(approvalRequestDto.getProcessType())){
            response.setResponseCode(400);
            response.setResponseMessage("Process Type cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(!approvalRequestDto.getProcessType().equals(ProcessType.UPDATE_TRANSACTION_LIMIT)){
            if (StringUtils.isEmpty(approvalRequestDto.getMobileNumber())){
                response.setResponseCode(400);
                response.setResponseMessage("Mobile Number cannot be null");
                response.setResponseData("");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        }

        if (processApprovalRequestRepository.findAllByMobileNumberAndApproved(approvalRequestDto.getMobileNumber(), false).size() > 0) {
            response.setResponseCode(400);
            response.setResponseMessage("There's a pending request for this account");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(processApprovalRequestRepository.findAllByMobileNumberAndProcessTypeAndApproved(approvalRequestDto.getMobileNumber(), ProcessType.BLOCK_USER, false).size() > 0){
            response.setResponseCode(400);
            response.setResponseMessage("There's a pending Block request on this account, all further activities have been paused");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }



        if (approvalRequestDto.getProcessType().equals(ProcessType.BLOCK_USER) || approvalRequestDto.getProcessType().equals(ProcessType.UNBLOCK_USER)){
            if(approvalRequestDto.getAction() != null){
                if (!(approvalRequestDto.getProcessType() == ProcessType.BLOCK_USER && approvalRequestDto.getAction().toUpperCase().equals("BLOCK"))
                        && !(approvalRequestDto.getProcessType() == ProcessType.UNBLOCK_USER && approvalRequestDto.getAction().toUpperCase().equals("UNBLOCK"))){
                    response.setResponseCode(400);
                    response.setResponseMessage("ProcessType and action must be the same");
                    response.setResponseData("");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }

        }



        User user = AuthenticatedUser.getAuthenticatedUser(request);
        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


      



        ProcessApprovalRequest approvalRequest = new ProcessApprovalRequest();
        approvalRequest.setProcessType(approvalRequestDto.getProcessType());
        approvalRequest.setReasonForInitiation(approvalRequestDto.getReasonForInitiation());
        approvalRequest.setStaffId(user.getStaffId());

        String mobileNumber = approvalRequestDto.getMobileNumber();
        if(!approvalRequestDto.getProcessType().equals(ProcessType.UPDATE_TRANSACTION_LIMIT)){
            mobileNumber = !mobileNumber.startsWith("234") ? mobileNumber.replaceFirst("0", "234") : mobileNumber;
            approvalRequest.setMobileNumber(mobileNumber);
        }




        switch (approvalRequest.getProcessType()){
            case UPDATE_TRANSACTION_LIMIT:{
                if (!(StringUtils.isEmpty(approvalRequestDto.getTransactionLimit())) && !(StringUtils.isEmpty(approvalRequestDto.getDailyAmountLimit()))&& !(StringUtils.isEmpty(approvalRequestDto.getUserTypeId()))){
                    approvalRequest.setDailyAmountLimit(approvalRequestDto.getDailyAmountLimit());
                    approvalRequest.setTransactionLimit(approvalRequestDto.getTransactionLimit());
                    approvalRequest.setUserTypeId(approvalRequestDto.getUserTypeId());

                    AuditTrailDto auditTrailDto = new AuditTrailDto();
                    auditTrailDto.setTitle("Created New Request");
                    auditTrailDto.setTransactionDetails("Update Transaction Limit");
                    auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                    auditTrailDto.setStaffId(user.getStaffId());
                    auditTrailService.createNewEvent(auditTrailDto);

                    break;
                }else{
                    response.setResponseCode(400);
                    response.setResponseMessage("One or more required entities are missing");
                    response.setResponseData("");
                    return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
                }
            }
            case UPGRADE_MOBILE_APP_USER_TYPE:{

                if (!(StringUtils.isEmpty(approvalRequestDto.getUserTypeId()))){
                    approvalRequest.setUserTypeId(approvalRequestDto.getUserTypeId());

                    AuditTrailDto auditTrailDto = new AuditTrailDto();
                    auditTrailDto.setTitle("Created New Request");
                    auditTrailDto.setTransactionDetails("Upgrade Mobile App User Type - "+ approvalRequest.getMobileNumber());
                    auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                    auditTrailDto.setStaffId(user.getStaffId());
                    auditTrailService.createNewEvent(auditTrailDto);

                    break;
                }else{
                    response.setResponseCode(400);
                    response.setResponseMessage("One or more required entities are missing");
                    response.setResponseData("");
                    return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
                }
            }
            case RESET_PIN:{
                if (!(StringUtils.isEmpty(approvalRequest.getMobileNumber()))){
                    approvalRequest.setMobileNumber(mobileNumber);

                    AuditTrailDto auditTrailDto = new AuditTrailDto();
                    auditTrailDto.setTitle("Created New Request");
                    auditTrailDto.setTransactionDetails("Reset Pin - "+ approvalRequest.getMobileNumber());
                    auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                    auditTrailDto.setStaffId(user.getStaffId());
                    auditTrailService.createNewEvent(auditTrailDto);

                    break;
                }else {
                    response.setResponseCode(400);
                    response.setResponseMessage("One or more required entities are missing");
                    response.setResponseData("");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }
            case CLEAR_IMEI: {
                if (!(StringUtils.isEmpty(approvalRequest.getMobileNumber()))){
                    approvalRequest.setMobileNumber(mobileNumber);

                    AuditTrailDto auditTrailDto = new AuditTrailDto();
                    auditTrailDto.setTitle("Created New Request");
                    auditTrailDto.setTransactionDetails("Clear Imei - "+ approvalRequest.getMobileNumber());
                    auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                    auditTrailDto.setStaffId(user.getStaffId());
                    auditTrailService.createNewEvent(auditTrailDto);

                    break;
                }else {
                    response.setResponseCode(400);
                    response.setResponseMessage("One or more required entities are missing");
                    response.setResponseData("");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }
            case BLOCK_USER:{
                if (!(StringUtils.isEmpty(approvalRequestDto.getMobileNumber())) && !(StringUtils.isEmpty(approvalRequestDto.getAction()))){
                    approvalRequest.setMobileNumber(mobileNumber);
                    approvalRequest.setAction(approvalRequestDto.getAction());

                    AuditTrailDto auditTrailDto = new AuditTrailDto();
                    auditTrailDto.setTitle("Created New Request");
                    auditTrailDto.setTransactionDetails("Block User - "+ approvalRequest.getMobileNumber());
                    auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                    auditTrailDto.setStaffId(user.getStaffId());
                    auditTrailService.createNewEvent(auditTrailDto);

                    break;
                }else{
                    response.setResponseCode(400);
                    response.setResponseMessage("One or more required entities are missing");
                    response.setResponseData("");
                    return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
                }
            }
            case UNBLOCK_USER: {
                if (!(StringUtils.isEmpty(approvalRequestDto.getMobileNumber())) && !(StringUtils.isEmpty(approvalRequestDto.getAction()))){
                    approvalRequest.setMobileNumber(mobileNumber);
                    approvalRequest.setAction(approvalRequestDto.getAction());

                    AuditTrailDto auditTrailDto = new AuditTrailDto();
                    auditTrailDto.setTitle("Created New Request");
                    auditTrailDto.setTransactionDetails("Unblock User - "+ approvalRequest.getMobileNumber());
                    auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                    auditTrailDto.setStaffId(user.getStaffId());
                    auditTrailService.createNewEvent(auditTrailDto);

                    break;
                }else{
                    response.setResponseCode(400);
                    response.setResponseMessage("One or more required entities are missing");
                    response.setResponseData("");
                    return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
                }
            }
            case UPDATE_MOBILE_NUMBER:{
                if (!(StringUtils.isEmpty(approvalRequestDto.getOldNumber())) && !(StringUtils.isEmpty(approvalRequestDto.getNewNumber()))){

                    String oldNumber = approvalRequestDto.getOldNumber();
                    oldNumber = !oldNumber.startsWith("234") ? oldNumber.replace("0", "234") : oldNumber;

                    String newNumber = approvalRequestDto.getNewNumber();
                    newNumber = !newNumber.startsWith("234") ? newNumber.replaceFirst("0", "234") : newNumber;

                    if (!oldNumber.equals(mobileNumber)){
                        response.setResponseCode(400);
                        response.setResponseMessage("Old number must be equal to mobile number");
                        response.setResponseData("");
                        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
                    }

                    if (newNumber.equals(oldNumber)){
                        response.setResponseCode(400);
                        response.setResponseMessage("New Number cannot be equal to old number");
                        response.setResponseData("");
                        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
                    }

                    approvalRequest.setOldNumber(oldNumber);
                    approvalRequest.setNewNumber(newNumber);

                    AuditTrailDto auditTrailDto = new AuditTrailDto();
                    auditTrailDto.setTitle("Created New Request");
                    auditTrailDto.setTransactionDetails("Update Mobile Number - "+ approvalRequest.getMobileNumber());
                    auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                    auditTrailDto.setStaffId(user.getStaffId());
                    auditTrailService.createNewEvent(auditTrailDto);

                    break;
                }else{
                    response.setResponseCode(400);
                    response.setResponseMessage("One or more required entities are missing");
                    response.setResponseData("");
                    return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
                }


            }
            case RESET_MOBILE_USER_PASSWORD:{
                if (!(StringUtils.isEmpty(approvalRequestDto.getMobileNumber()))){
                    approvalRequest.setMobileNumber(mobileNumber);

                    AuditTrailDto auditTrailDto = new AuditTrailDto();
                    auditTrailDto.setTitle("Created New Request");
                    auditTrailDto.setTransactionDetails("Reset Mobile User Pasword - "+ approvalRequest.getMobileNumber());
                    auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                    auditTrailDto.setStaffId(user.getStaffId());
                    auditTrailService.createNewEvent(auditTrailDto);

                    break;
                }else{
                    response.setResponseCode(400);
                    response.setResponseMessage("One or more required entities are missing");
                    response.setResponseData("");
                    return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
                }

            }
            default:{
                response.setResponseCode(400);
                response.setResponseMessage("unsuccessful, invalid process type");
                response.setResponseData("");
                return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);

            }
        }


        approvalRequest.setTrackingNumber("TKT"+(processApprovalRequestRepository.count()+1));

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData(processApprovalRequestRepository.save(approvalRequest));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/listOfRequests")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> getListOfRequests(HttpServletRequest request,  @RequestParam("page") int page,  @RequestParam("size") int size){

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        Response response = new Response();
        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Query List of Requests");
        auditTrailDto.setTransactionDetails("Returned a list of requests");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        ConvertToPageable convertToPageable = new ConvertToPageable(page, size);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData(convertToPageable.convertListToPage(processApprovalRequestRepository.findAllByRequestStatus(RequestStatus.PENDING)));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/approveRequest")
    @PreAuthorize("hasAuthority('approve_functions')")
    public ResponseEntity<?> approveRequest(@RequestParam("trackingNumber") String trackingNumber, HttpServletRequest request){

        Response response = new Response();

        if (StringUtils.isEmpty(trackingNumber)){
            response.setResponseCode(400);
            response.setResponseMessage("Tracking number cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        ProcessApprovalRequest approvalRequest = processApprovalRequestRepository.findByTrackingNumber(trackingNumber);

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        if (approvalRequest == null){
            response.setResponseCode(400);
            response.setResponseMessage("Request with tracking number not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(approvalRequest.isApproved()){
            response.setResponseCode(400);
            response.setResponseMessage("Request has been approved already");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }




        RestTemplate restTemplate = new RestTemplate();

        switch (approvalRequest.getProcessType()){
            case UPDATE_TRANSACTION_LIMIT:{

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);


                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fcmbProcessorBaseUrl+"/list/userTypes/{id}");

                Map<String, Integer> path = new HashMap<>();
                path.put("id", approvalRequest.getUserTypeId());

                URI uri = builder.buildAndExpand(path).toUri();
                System.out.println(uri.toString());

                JSONObject upgradeDto = new JSONObject();
                upgradeDto.put("dailyAmountLimit", approvalRequest.getDailyAmountLimit());
                upgradeDto.put("transactionLimit", approvalRequest.getTransactionLimit());

                HttpEntity<?> entity = new HttpEntity<>(upgradeDto.toJSONString(), headers);




                try{

                    Response data = Objects.requireNonNull(restTemplate.exchange(uri.toString(), HttpMethod.PUT, entity, Response.class)).getBody();

                    if (data.getResponseCode() == 0){
                        response.setResponseCode(200);
                        response.setResponseMessage("Successful");
                        response.setResponseData(data.getResponseData() == null ? data.getResponseMessage() : data.getResponseData());

                        AuditTrailDto auditTrailDto = new AuditTrailDto();
                        auditTrailDto.setTitle("SUCCESS - Request Approved");
                        auditTrailDto.setTransactionDetails("Transaction limit upgraded - " + approvalRequest.getMobileNumber());
                        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                        auditTrailDto.setStaffId(user.getStaffId());
                        auditTrailService.createNewEvent(auditTrailDto);

                        approvalRequest.setApproved(true);
                        approvalRequest.setRequestStatus(RequestStatus.APPROVED);

                        processApprovalRequestRepository.save(approvalRequest);

                        return new ResponseEntity<>(response,HttpStatus.OK);
                    }else{
                        response.setResponseCode(400);
                        response.setResponseMessage(data.getResponseMessage());
                        response.setResponseData("");
                        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
                    }
                }catch (HttpStatusCodeException e){
                    response.setResponseCode(400);
                    response.setResponseMessage("Request Failed");
                    response.setResponseData("");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }

            }
            case UPGRADE_MOBILE_APP_USER_TYPE:{
                HttpHeaders headers = new HttpHeaders();
                HttpEntity<?> entity = new HttpEntity<>(headers);

                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fcmbProcessorBaseUrl+"/management/changeMobileUserType");
                params.add("mobileNumber", approvalRequest.getMobileNumber());
                params.add("userTypeId", ""+approvalRequest.getUserTypeId());


                builder.queryParams(params);


                try{

                    Response data = Objects.requireNonNull(restTemplate.exchange(builder.toUriString(),
                            HttpMethod.PUT,
                            entity,
                            Response.class).getBody());

                    if (data.getResponseCode() == 0){
                        response.setResponseCode(200);
                        response.setResponseMessage("Successful");
                        response.setResponseData(data.getResponseData() == null ? data.getResponseMessage() : data.getResponseData());

                        AuditTrailDto auditTrailDto = new AuditTrailDto();
                        auditTrailDto.setTitle("SUCCESS - Request Approved");
                        auditTrailDto.setTransactionDetails("Updated Mobile App UserType - " + approvalRequest.getMobileNumber());
                        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                        auditTrailDto.setStaffId(user.getStaffId());
                        auditTrailService.createNewEvent(auditTrailDto);

                        approvalRequest.setApproved(true);
                        approvalRequest.setRequestStatus(RequestStatus.APPROVED);

                        processApprovalRequestRepository.save(approvalRequest);

                        return new ResponseEntity<>(response,HttpStatus.OK);
                    }else{
                        response.setResponseCode(400);
                        response.setResponseMessage(data.getResponseMessage());
                        response.setResponseData("");
                        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
                    }


                }catch (HttpStatusCodeException e) {
                    response.setResponseCode(400);
                    response.setResponseMessage("Request Failed");
                    response.setResponseData("");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }

            }
            case RESET_PIN:{
                HttpHeaders headers = new HttpHeaders();
                headers.add("mobileNumber", approvalRequest.getMobileNumber());
                HttpEntity<?> entity = new HttpEntity<>(headers);

                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fcmbProcessorBaseUrl+"/management/forgetPin");
                params.add("mobileNumber", approvalRequest.getMobileNumber());
                builder.queryParams(params);


                try{

                    Response data = Objects.requireNonNull(restTemplate.exchange(builder.toUriString(),
                            HttpMethod.POST,
                            entity,
                            Response.class).getBody());

                    if (data.getResponseCode() == 0){
                        response.setResponseCode(200);
                        response.setResponseMessage("Successful");
                        response.setResponseData(data.getResponseData() == null ? data.getResponseMessage() : data.getResponseData());

                        AuditTrailDto auditTrailDto = new AuditTrailDto();
                        auditTrailDto.setTitle("SUCCESS - Request Approved");
                        auditTrailDto.setTransactionDetails("Pin Reset - " + approvalRequest.getMobileNumber());
                        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                        auditTrailDto.setStaffId(user.getStaffId());
                        auditTrailService.createNewEvent(auditTrailDto);

                        approvalRequest.setApproved(true);
                        approvalRequest.setRequestStatus(RequestStatus.APPROVED);

                        processApprovalRequestRepository.save(approvalRequest);

                        return new ResponseEntity<>(response,HttpStatus.OK);
                    }else{
                        response.setResponseCode(400);
                        response.setResponseMessage(data.getResponseMessage());
                        response.setResponseData("");
                        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
                    }
                }catch (HttpStatusCodeException e) {
                    response.setResponseCode(400);
                    response.setResponseMessage("Request Failed");
                    response.setResponseData("");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }
            case BLOCK_USER:{
                HttpHeaders headers = new HttpHeaders();
                headers.add("mobileNumber", approvalRequest.getMobileNumber());
                HttpEntity<?> entity = new HttpEntity<>(headers);

                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fcmbProcessorBaseUrl+"/management/blockAndUnblockUser");
                params.add("mobileNumber", approvalRequest.getMobileNumber());
                params.add("action", "BLOCK");
                builder.queryParams(params);


                try{

                    Response data = Objects.requireNonNull(restTemplate.exchange(builder.toUriString(),
                            HttpMethod.POST,
                            entity,
                            Response.class).getBody());

                    if (data.getResponseCode() == 0){
                        response.setResponseCode(200);
                        response.setResponseMessage("Successful");
                        response.setResponseData(data.getResponseData() == null ? data.getResponseMessage() : data.getResponseData());

                        AuditTrailDto auditTrailDto = new AuditTrailDto();
                        auditTrailDto.setTitle("SUCCESS - Request Approved");
                        auditTrailDto.setTransactionDetails("Blocked User - "+ approvalRequest.getMobileNumber());
                        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                        auditTrailDto.setStaffId(user.getStaffId());
                        auditTrailService.createNewEvent(auditTrailDto);

                        approvalRequest.setApproved(true);

                        processApprovalRequestRepository.save(approvalRequest);

                        return new ResponseEntity<>(response,HttpStatus.OK);
                    }else if (data.getResponseCode() == 99){

                        AuditTrailDto auditTrailDto = new AuditTrailDto();
                        auditTrailDto.setTitle("Request Failed");
                        auditTrailDto.setTransactionDetails("BLOCK USER - FAILED " + approvalRequest.getMobileNumber());
                        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                        auditTrailDto.setStaffId(user.getStaffId());
                        auditTrailService.createNewEvent(auditTrailDto);

                        approvalRequest.setApproved(true);
                        approvalRequest.setRequestStatus(RequestStatus.APPROVED);

                        processApprovalRequestRepository.save(approvalRequest);

                        response.setResponseCode(400);
                        response.setResponseMessage(data.getResponseMessage());
                        response.setResponseData("");
                        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
                    }else{
                        response.setResponseCode(400);
                        response.setResponseMessage(data.getResponseMessage());
                        response.setResponseData("");
                        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
                    }
                }catch (HttpStatusCodeException e) {
                    response.setResponseCode(400);
                    response.setResponseMessage("Request Failed");
                    response.setResponseData("");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }
            case CLEAR_IMEI:{
                HttpHeaders headers = new HttpHeaders();
                headers.add("mobileNumber", approvalRequest.getMobileNumber());
                HttpEntity<?> entity = new HttpEntity<>(headers);

                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fcmbProcessorBaseUrl+"/management/clearImei");



                try{
                    System.out.println(builder.toUriString());
                    Response data = Objects.requireNonNull(restTemplate.exchange(builder.toUriString(),
                            HttpMethod.POST,
                            entity,
                            Response.class).getBody());

                    if (data.getResponseCode() == 0){
                        response.setResponseCode(200);
                        response.setResponseMessage("Successful");
                        response.setResponseData(data.getResponseData() == null ? data.getResponseMessage() : data.getResponseData());

                        AuditTrailDto auditTrailDto = new AuditTrailDto();
                        auditTrailDto.setTitle("SUCCESS - Request Approved");
                        auditTrailDto.setTransactionDetails("Imei Cleared - " + approvalRequest.getMobileNumber());
                        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                        auditTrailDto.setStaffId(user.getStaffId());
                        auditTrailService.createNewEvent(auditTrailDto);

                        approvalRequest.setApproved(true);
                        approvalRequest.setRequestStatus(RequestStatus.APPROVED);

                        processApprovalRequestRepository.save(approvalRequest);

                        return new ResponseEntity<>(response,HttpStatus.OK);
                    }else{
                        response.setResponseCode(400);
                        response.setResponseMessage(data.getResponseMessage());
                        response.setResponseData("");
                        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
                    }
                }catch (HttpStatusCodeException e) {
                    response.setResponseCode(400);
                    response.setResponseMessage("Request Failed");
                    response.setResponseData("");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }

            }
            case UNBLOCK_USER:{

                HttpHeaders headers = new HttpHeaders();
                headers.add("mobileNumber", approvalRequest.getMobileNumber());
                HttpEntity<?> entity = new HttpEntity<>(headers);

                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fcmbProcessorBaseUrl+"/management/blockAndUnblockUser");
                params.add("mobileNumber", approvalRequest.getMobileNumber());
                params.add("action", "UNBLOCK");
                builder.queryParams(params);


                try{

                    Response data = Objects.requireNonNull(restTemplate.exchange(builder.toUriString(),
                            HttpMethod.POST,
                            entity,
                            Response.class).getBody());

                    if (data.getResponseCode() == 0){
                        response.setResponseCode(200);
                        response.setResponseMessage("Successful");
                        response.setResponseData(data.getResponseData() == null ? data.getResponseMessage() : data.getResponseData());

                        AuditTrailDto auditTrailDto = new AuditTrailDto();
                        auditTrailDto.setTitle("SUCCESS - Request Approved");
                        auditTrailDto.setTransactionDetails("Unblocked User - "+ approvalRequest.getMobileNumber());
                        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                        auditTrailDto.setStaffId(user.getStaffId());
                        auditTrailService.createNewEvent(auditTrailDto);

                        approvalRequest.setApproved(true);

                        processApprovalRequestRepository.save(approvalRequest);

                        return new ResponseEntity<>(response,HttpStatus.OK);
                    }else if (data.getResponseCode() == 99){

                        AuditTrailDto auditTrailDto = new AuditTrailDto();
                        auditTrailDto.setTitle("Request Failed");
                        auditTrailDto.setTransactionDetails("UNBLOCK USER - FAILED " + approvalRequest.getMobileNumber());
                        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                        auditTrailDto.setStaffId(user.getStaffId());
                        auditTrailService.createNewEvent(auditTrailDto);

                        approvalRequest.setApproved(true);
                        approvalRequest.setRequestStatus(RequestStatus.APPROVED);
                        processApprovalRequestRepository.save(approvalRequest);

                        response.setResponseCode(400);
                        response.setResponseMessage(data.getResponseMessage());
                        response.setResponseData("");
                        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
                    }
                    else{
                        response.setResponseCode(400);
                        response.setResponseMessage(data.getResponseMessage());
                        response.setResponseData("");
                        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
                    }
                }catch (HttpStatusCodeException e) {
                    response.setResponseCode(400);
                    response.setResponseMessage("Request Failed");
                    response.setResponseData("");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }
            case UPDATE_MOBILE_NUMBER:{

                HttpHeaders headers = new HttpHeaders();
                HttpEntity<?> entity = new HttpEntity<>(headers);

                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fcmbProcessorBaseUrl+"/management/changeMobileNumber");
                params.add("mobileNumber", approvalRequest.getOldNumber());
                params.add("newMobileNumber", approvalRequest.getNewNumber());
                builder.queryParams(params);



                try{

                    Response data = Objects.requireNonNull(restTemplate.exchange(builder.toUriString(),
                            HttpMethod.PUT,
                            entity,
                            Response.class).getBody());

                    if (data.getResponseCode() == 0){
                        response.setResponseCode(200);
                        response.setResponseMessage("Successful");
                        response.setResponseData(data.getResponseData() == null ? data.getResponseMessage() : data.getResponseData());

                        AuditTrailDto auditTrailDto = new AuditTrailDto();
                        auditTrailDto.setTitle("SUCCESS - Request Approved");
                        auditTrailDto.setTransactionDetails("UPDATE - SUCCESS " + approvalRequest.getMobileNumber() + " - " +  approvalRequest.getNewNumber());
                        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                        auditTrailDto.setStaffId(user.getStaffId());
                        auditTrailService.createNewEvent(auditTrailDto);

                        approvalRequest.setApproved(true);

                        processApprovalRequestRepository.save(approvalRequest);

                        return new ResponseEntity<>(response,HttpStatus.OK);
                    }else if (data.getResponseCode() == 3){

                        AuditTrailDto auditTrailDto = new AuditTrailDto();
                        auditTrailDto.setTitle("Request Failed");
                        auditTrailDto.setTransactionDetails("UPDATE - FAILED " + approvalRequest.getMobileNumber() + " - " +  approvalRequest.getNewNumber());
                        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                        auditTrailDto.setStaffId(user.getStaffId());
                        auditTrailService.createNewEvent(auditTrailDto);

                        approvalRequest.setApproved(true);
                        approvalRequest.setRequestStatus(RequestStatus.APPROVED);

                        processApprovalRequestRepository.save(approvalRequest);

                        response.setResponseCode(400);
                        response.setResponseMessage(data.getResponseMessage());
                        response.setResponseData("");
                        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
                    }
                    else{
                        response.setResponseCode(400);
                        response.setResponseMessage(data.getResponseMessage());
                        response.setResponseData("");
                        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
                    }
                }catch (HttpStatusCodeException e) {
                    response.setResponseCode(400);
                    response.setResponseMessage("Request Failed");
                    response.setResponseData("");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }


            }
            case RESET_MOBILE_USER_PASSWORD:{
                HttpHeaders headers = new HttpHeaders();
                headers.add("mobileNumber", approvalRequest.getMobileNumber());
                HttpEntity<?> entity = new HttpEntity<>(headers);

                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fcmbProcessorBaseUrl+"/management/forgetPassword");
                params.add("mobileNumber", approvalRequest.getMobileNumber());
                builder.queryParams(params);



                try{

                    Response data = Objects.requireNonNull(restTemplate.exchange(builder.toUriString(),
                            HttpMethod.POST,
                            entity,
                            Response.class).getBody());

                    if (data.getResponseCode() == 0){
                        response.setResponseCode(200);
                        response.setResponseMessage("Successful");
                        response.setResponseData(data.getResponseData() == null ? data.getResponseMessage() : data.getResponseData());

                        AuditTrailDto auditTrailDto = new AuditTrailDto();
                        auditTrailDto.setTitle("SUCCESS - Request Approved");
                        auditTrailDto.setTransactionDetails("Mobile User Password Reset - " + approvalRequest.getMobileNumber());
                        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
                        auditTrailDto.setStaffId(user.getStaffId());
                        auditTrailService.createNewEvent(auditTrailDto);

                        approvalRequest.setApproved(true);
                        approvalRequest.setRequestStatus(RequestStatus.APPROVED);

                        processApprovalRequestRepository.save(approvalRequest);

                        return new ResponseEntity<>(response,HttpStatus.OK);
                    }else{
                        response.setResponseCode(400);
                        response.setResponseMessage(data.getResponseMessage());
                        response.setResponseData("");
                        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
                    }
                }catch (HttpStatusCodeException e) {
                    response.setResponseCode(400);
                    response.setResponseMessage("Request Failed");
                    response.setResponseData("");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }

            }
            default:{
                response.setResponseCode(400);
                response.setResponseMessage("Unsuccessful");
                response.setResponseData("");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        }

    }

    @PostMapping("/declineRequest")
    @PreAuthorize("hasAuthority('approve_functions')")
    public ResponseEntity<?> declineRequest(@RequestParam("trackingNumber") String trackingNumber, HttpServletRequest request){
        Response response = new Response();

        if (StringUtils.isEmpty(trackingNumber)){
            response.setResponseCode(400);
            response.setResponseMessage("Tracking number cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        ProcessApprovalRequest approvalRequest = processApprovalRequestRepository.findByTrackingNumber(trackingNumber);

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        if (approvalRequest == null){
            response.setResponseCode(400);
            response.setResponseMessage("Request with tracking number not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        approvalRequest.setApproved(false);
        approvalRequest.setRequestStatus(RequestStatus.DECLINED);
        processApprovalRequestRepository.save(approvalRequest);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("FAILED - Request Declined");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());


        switch (approvalRequest.getProcessType()){
            case UPDATE_TRANSACTION_LIMIT:{
                auditTrailDto.setTransactionDetails("Transaction limit Upgrade Failed - " + approvalRequest.getMobileNumber());
                break;
            }
            case UPGRADE_MOBILE_APP_USER_TYPE:{
                auditTrailDto.setTransactionDetails("Update Mobile App UserType Failed - " + approvalRequest.getMobileNumber());
                break;
            }
            case RESET_PIN:{
                auditTrailDto.setTransactionDetails("Pin Reset Failed - " + approvalRequest.getMobileNumber());
                break;
            }
            case BLOCK_USER:{
                auditTrailDto.setTransactionDetails("Blocked User Failed - "+ approvalRequest.getMobileNumber());
                break;
            }
            case CLEAR_IMEI:{
                auditTrailDto.setTransactionDetails("Imei Clear Failed - " + approvalRequest.getMobileNumber());
                break;
            }
            case UNBLOCK_USER:{
                auditTrailDto.setTransactionDetails("Unblocked User Failed - "+ approvalRequest.getMobileNumber());
                break;
            }
            case UPDATE_MOBILE_NUMBER:{
                auditTrailDto.setTransactionDetails("UPDATE - FAILED " + approvalRequest.getMobileNumber() + " - " +  approvalRequest.getNewNumber());
                break;
            }
            case RESET_MOBILE_USER_PASSWORD:{
                auditTrailDto.setTransactionDetails("Mobile User Password Reset Failed - " + approvalRequest.getMobileNumber());
                break;
            }
            default:{
                break;
            }
        }

        auditTrailService.createNewEvent(auditTrailDto);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Successfully declined request");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }






    @GetMapping("/getAllAuditTrailsForPortal")
    @PreAuthorize("hasAnyAuthority('audit_trail_querying')")
    public ResponseEntity<?> getAllAuditTrails(@RequestParam("page") int page, @RequestParam("size") int size){

        Response response =  new Response();
        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData(auditTrailService.getAllAuditTrails(page, size));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //mobile audit trail
    @GetMapping("/getAllAuditTrailsForMobile")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN,ADMIN,AGENT')")
    public ResponseEntity<?> getAllAuditTrailsForMobile(ActivityLogFilterRequestForMobile activityLogFilterRequestForMobile, HttpServletRequest request){

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        PagedResponse response = new PagedResponse();
        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        //validate

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Query Mobile Audit Trail");
        auditTrailDto.setTransactionDetails("Returned a list of Mobile AuditTrails");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        //validate

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fcmbProcessorBaseUrl+"/list/activityLog")
                .queryParam("accountNumber", activityLogFilterRequestForMobile.getAccountNumber())
                .queryParam("action", activityLogFilterRequestForMobile.getAction())
                .queryParam("date", activityLogFilterRequestForMobile.getDate())
                .queryParam("mobileNumber", activityLogFilterRequestForMobile.getMobileNumber())
                .queryParam("status", activityLogFilterRequestForMobile.getStatus())
                .queryParam("size", activityLogFilterRequestForMobile.getSize())
                .queryParam("page", activityLogFilterRequestForMobile.getPage())
                .queryParam("orderAscending", activityLogFilterRequestForMobile.getOrderAscending());

        try{
            PagedResponse processorResponse = Objects.requireNonNull(restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, PagedResponse.class).getBody());
            response.setResponseCode(200);
            response.setResponseMessage("Successful");
            response.setResponseData(processorResponse.getResponseData());
            response.setPage(processorResponse.getPage());
            response.setTotal(processorResponse.getTotal());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (HttpStatusCodeException e){
            response.setResponseCode(400);
            response.setResponseMessage("Request Failed");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("/getAllAuditTrailsByDate")
    @PreAuthorize("hasAnyAuthority('audit_trail_querying')")
    public ResponseEntity<?> getAllAuditTrailsByDate(@RequestParam("localDate") String localDate, HttpServletRequest request, @RequestParam("page") int page, @RequestParam("size") int size){

        String dateRegex = "[0-9]{4}-[0-9]{2}-[0-9]{2}";

        Response response = new Response();

        if (StringUtils.isEmpty(localDate)){
            response.setResponseCode(400);
            response.setResponseMessage("Date cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (!Pattern.matches(dateRegex, localDate)){
            response.setResponseCode(400);
            response.setResponseMessage("Invalid Date");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        LocalDate date = LocalDate.parse(localDate);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData(auditTrailService.getAllAuditTrailsByDate(date, page, size));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getAllAuditTrailsByRole")
    @PreAuthorize("hasAnyAuthority('audit_trail_querying')")
    public ResponseEntity<?> getAllByRole(@RequestParam("roleType") RoleType roleType, HttpServletRequest request, @RequestParam("page") int page, @RequestParam("size") int size){

        Response response = new Response();

        if (StringUtils.isEmpty(roleType)){
            response.setResponseCode(400);
            response.setResponseMessage("Role cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData(auditTrailService.getAllByRole(roleType, page, size));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getAllAuditTrailsByStaffId")
    @PreAuthorize("hasAnyAuthority('audit_trail_querying')")
    public ResponseEntity<?> getAllByStaffId(@RequestParam("staffId") String staffId, HttpServletRequest request, @RequestParam("page") int page, @RequestParam("size") int size){

        Response response = new Response();

        if (StringUtils.isEmpty(staffId)){
            response.setResponseCode(400);
            response.setResponseMessage("Staff Id cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData(auditTrailService.getAllByStaffId(staffId, page, size));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getAllAuditTrailsByIpAddress")
    @PreAuthorize("hasAnyAuthority('audit_trail_querying')")
    public ResponseEntity<?> getAllByIpAddress(@RequestParam("ipAddress") String ipAddress, HttpServletRequest request, @RequestParam("page") int page, @RequestParam("size") int size){



        InetAddressValidator validator = InetAddressValidator.getInstance();
        Response response = new Response();

        if (StringUtils.isEmpty(ipAddress)){
            response.setResponseCode(400);
            response.setResponseMessage("Ip Address cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

         if (!validator.isValid(ipAddress)){
             response.setResponseCode(400);
             response.setResponseMessage("Ip Address is Invalid");
             response.setResponseData("");
             return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
         }


        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData(auditTrailService.getAllByIpAddress(ipAddress, page, size));

        return new ResponseEntity<>(response, HttpStatus.OK);

    }


    @GetMapping("/transactionHistory")
    @PreAuthorize("hasAuthority('view_query_transaction_history')")
    public ResponseEntity<?> transactionHistory(TransactionLogFilterRequest transactionLogFilterRequest,
                                                HttpServletRequest request){

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        PagedResponse response = new PagedResponse();
        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        //validate

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Query List of Transactions");
        auditTrailDto.setTransactionDetails("Returned a list of Transactions");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        //validate
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);


        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fcmbProcessorBaseUrl+"/transactions/logs")
                .queryParam("accountNumber", transactionLogFilterRequest.getAccountNumber())
                .queryParam("alphabetical", transactionLogFilterRequest.getAlphabetical())
                .queryParam("date", transactionLogFilterRequest.getDate())
                .queryParam("narration", transactionLogFilterRequest.getNarration())
                .queryParam("mobileNumber", transactionLogFilterRequest.getMobileNumber())
                .queryParam("status", transactionLogFilterRequest.getStatus())
                .queryParam("size", transactionLogFilterRequest.getSize())
                .queryParam("page", transactionLogFilterRequest.getPage())
                .queryParam("orderAscending", transactionLogFilterRequest.getOrderAscending());

        System.out.println(builder.toUriString());

        try{

            PagedResponse processorResponse = Objects.requireNonNull(restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, PagedResponse.class).getBody());

            response.setResponseCode(200);
            response.setResponseMessage("Successful");
            response.setResponseData(processorResponse.getResponseData());
            response.setPage(processorResponse.getPage());
            response.setTotal(processorResponse.getTotal());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (HttpStatusCodeException e){
            response.setResponseCode(400);
            response.setResponseMessage("Request Failed");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

    }


    @GetMapping("/customersList")
    @PreAuthorize("hasAuthority('view_query_mobile_app_user')")
    public ResponseEntity<?> getCustomersList(CustomersFilterRequest customersFilterRequest,
                                              HttpServletRequest request){
        User user = AuthenticatedUser.getAuthenticatedUser(request);
        PagedResponse response = new PagedResponse();
        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Query List of Customers");
        auditTrailDto.setTransactionDetails("Returned a list of Customers");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        RestTemplate restTemplate = new RestTemplate();


        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);


        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fcmbProcessorBaseUrl+"/list/usersList")
                .queryParam("alphabetical", customersFilterRequest.getAlphabetical())
                .queryParam("date", customersFilterRequest.getDate())
                .queryParam("accountNumber", customersFilterRequest.getAccountNumber())
                .queryParam("mobileNumber", customersFilterRequest.getMobileNumber())
                .queryParam("status", customersFilterRequest.getStatus())
                .queryParam("size", customersFilterRequest.getSize())
                .queryParam("page", customersFilterRequest.getPage())
                .queryParam("orderAscending", customersFilterRequest.getOrderAscending());

        try{

            PagedResponse processorResponse = Objects.requireNonNull(restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, PagedResponse.class).getBody());

            response.setResponseCode(200);
            response.setResponseMessage("Successful");
            response.setResponseData(processorResponse.getResponseData());
            response.setPage(processorResponse.getPage());
            response.setTotal(processorResponse.getTotal());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (HttpStatusCodeException e){
            response.setResponseCode(400);
            response.setResponseMessage("Request Failed");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }








    @PostMapping("/initiateUserDeActivationOrActivation")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN,IT_CONTROL')")
    public ResponseEntity<?> initiateUserDeActivationOrActivation(@RequestParam("userEmail") String email, @RequestParam("action") ActionType actionType, HttpServletRequest request){
        Response response = new Response();

        User loggedInUser = AuthenticatedUser.getAuthenticatedUser(request);

        if (loggedInUser == null){
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isEmpty(email)){
            response.setResponseData(400);
            response.setResponseMessage("Email cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String emailRegex = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}";

        if (!Pattern.matches(emailRegex, email)){
            response.setResponseData(400);
            response.setResponseMessage("Invalid Email address");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isEmpty(actionType)){
            response.setResponseData(400);
            response.setResponseMessage("Action cannot be null");
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

        if (userActionsRequestRepository.findAllByUserAndAttendedToAndActionType(user,false,actionType).size() > 0){
            response.setResponseCode(400);
            response.setResponseMessage("User cannot undergo "+ actionType.getName() + " there's a request awaiting approval");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        UserActionsRequest userActionsRequest = new UserActionsRequest();

        switch (actionType){
            case ACTIVATE:{
                if (!user.isAccountLock()){
                    response.setResponseCode(400);
                    response.setResponseMessage("User is already Activated");
                    response.setResponseData("");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
                userActionsRequest.setUser(user);
                userActionsRequest.setAttendedTo(false);
                userActionsRequest.setActionType(ActionType.ACTIVATE);
                break;
            }case DEACTIVATE:{
                if (user.isAccountLock()){
                    response.setResponseCode(400);
                    response.setResponseMessage("User is already DeActivated");
                    response.setResponseData("");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
                userActionsRequest.setUser(user);
                userActionsRequest.setAttendedTo(false);
                userActionsRequest.setActionType(ActionType.DEACTIVATE);
                break;
            } default:{
                response.setResponseCode(400);
                response.setResponseMessage("Invalid action type");
                response.setResponseData("");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        }
        userActionsRequest.setTicketNumber("TKT"+(userActionsRequestRepository.count()+1));
        userActionsRequestRepository.save(userActionsRequest);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("New User "+ actionType.getName() + " Request");
        auditTrailDto.setTransactionDetails("Initiated new User "+ actionType.getName() + " request");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(loggedInUser.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Initiated new User "+ actionType.getName() + " request, TicketNumber is " +userActionsRequest.getTicketNumber());
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @GetMapping("/getListOfDeActivationOrActivationRequests")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN,IT_CONTROL')")
    public ResponseEntity<?> getListOfDeActivationOrActivationRequests(@RequestParam("page") int page, @RequestParam("size") int size,  HttpServletRequest request){
        Response response = new Response();

        User loggedInUser = AuthenticatedUser.getAuthenticatedUser(request);

        if (loggedInUser == null){
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Pageable pageable = PageRequest.of(page, size);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Activation or DeActivation List Query");
        auditTrailDto.setTransactionDetails("Returned a list of User Activation or DeActivation Requests");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(loggedInUser.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData(userActionsRequestRepository.findAllByRequestStatus(RequestStatus.PENDING, pageable));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/approveUserDeActivationOrActivation")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN,IT_CONTROL')")
    public ResponseEntity<?> approveUserDeActivationOrActivation(@RequestParam("ticketNumber") String ticketNumber, HttpServletRequest request){
        Response response = new Response();

        User loggedInUser = AuthenticatedUser.getAuthenticatedUser(request);

        if (loggedInUser == null){
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isEmpty(ticketNumber)){
            response.setResponseData(400);
            response.setResponseMessage("Ticket Number cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        UserActionsRequest userActionsRequest = userActionsRequestRepository.findByTicketNumber(ticketNumber);

        if (userActionsRequest == null){
            response.setResponseData(400);
            response.setResponseMessage("Request not found, check ticket number");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if(userActionsRequest.getRequestStatus().equals(RequestStatus.DECLINED)){
            response.setResponseData(400);
            response.setResponseMessage("Request was Declined");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        switch (userActionsRequest.getActionType()){
            case DEACTIVATE:{
                if(!userActionsRequest.getUser().isAccountLock()){
                    userActionsRequest.getUser().setAccountLock(true);
                    userActionsRequest.setAttendedTo(true);
                    break;
                }else{
                    response.setResponseCode(400);
                    response.setResponseMessage("User has been Activated Already");
                    response.setResponseData("");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }

            }case ACTIVATE:{
                if(userActionsRequest.getUser().isAccountLock()){
                    userActionsRequest.getUser().setAccountLock(false);
                    userActionsRequest.setAttendedTo(true);
                    break;
                }else{
                    response.setResponseCode(400);
                    response.setResponseMessage("User has been DeActivated Already");
                    response.setResponseData("");
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            } default:{
                response.setResponseCode(400);
                response.setResponseMessage("Invalid action type");
                response.setResponseData("");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        }

        userRepository.save(userActionsRequest.getUser());
        userActionsRequest.setRequestStatus(RequestStatus.APPROVED);
        userActionsRequestRepository.save(userActionsRequest);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("SUCCESS - " +userActionsRequest.getActionType().getName() +" Approval");
        auditTrailDto.setTransactionDetails("Approved User "+ userActionsRequest.getActionType().getName() + " request");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(loggedInUser.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Approved User "+ userActionsRequest.getActionType().getName() + " request");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/declineUserDeActivationOrActivation")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN,IT_CONTROL')")
    public ResponseEntity<?> declineUserDeActivationOrActivation(@RequestParam("ticketNumber") String ticketNumber, HttpServletRequest request){
        Response response = new Response();

        User loggedInUser = AuthenticatedUser.getAuthenticatedUser(request);

        if (loggedInUser == null){
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isEmpty(ticketNumber)){
            response.setResponseData(400);
            response.setResponseMessage("Ticket Number cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        UserActionsRequest userActionsRequest = userActionsRequestRepository.findByTicketNumber(ticketNumber);

        if (userActionsRequest == null){
            response.setResponseData(400);
            response.setResponseMessage("Request not found, check ticket number");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        userActionsRequest.setRequestStatus(RequestStatus.DECLINED);
        userActionsRequest.setAttendedTo(false);
        userActionsRequestRepository.save(userActionsRequest);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("FAILED - "+userActionsRequest.getActionType().getName() +" Approval");
        auditTrailDto.setTransactionDetails("Failed to Approve User "+ userActionsRequest.getActionType().getName() + " request");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(loggedInUser.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        response.setResponseCode(200);
        response.setResponseMessage("Successful");
        response.setResponseData("Declined Approve User "+ userActionsRequest.getActionType().getName() + " request");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }





    @GetMapping("/getUserPasswordAndPinTrailCount")
    @PreAuthorize("hasAuthority('view_query_mobile_app_user')")
    public ResponseEntity<?> getUserPasswordAndPinTrailCount(@RequestParam("userMobileNumber") String mobileNumber, HttpServletRequest request){

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        Response response = new Response();

        if (StringUtils.isEmpty(mobileNumber)){
            response.setResponseCode(400);
            response.setResponseMessage("Mobile Number cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (mobileNumber.length() < 11 || mobileNumber.length() > 13){
            response.setResponseCode(400);
            response.setResponseMessage("Invalid Mobile Number");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        String userMobileNumber = !mobileNumber.startsWith("234") ? mobileNumber.replaceFirst("0", "234") : mobileNumber;

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fcmbProcessorBaseUrl+"/management/getPasswordAndPinTrialCount");
        builder.queryParam("mobileNumber", userMobileNumber);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle(" User Password And Pin Trail Count Query");
        auditTrailDto.setTransactionDetails("Queried User Password And Pin Trail Count");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        try{
            Response data = Objects.requireNonNull(restTemplate.exchange(builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    Response.class)).getBody();

            if (data.getResponseCode() == 0){
                response.setResponseCode(200);
                response.setResponseMessage("Successful");
                response.setResponseData(data.getResponseData());
                return new ResponseEntity<>(response,HttpStatus.OK);
            }else{
                response.setResponseCode(400);
                response.setResponseMessage(data.getResponseMessage());
                response.setResponseData("");
                return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
            }
        }catch (HttpStatusCodeException e) {
            response.setResponseCode(400);
            response.setResponseMessage("Request Failed");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("/getUserAccountStatus")
    @PreAuthorize("hasAuthority('view_query_transaction_history')")
    public ResponseEntity<?> getUserAccountStatus(@RequestParam("userMobileNumber") String mobileNumber, HttpServletRequest request){

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        Response response = new Response();

        if (StringUtils.isEmpty(mobileNumber)){
            response.setResponseCode(400);
            response.setResponseMessage("Mobile Number cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (mobileNumber.length() < 11 || mobileNumber.length() > 13){
            response.setResponseCode(400);
            response.setResponseMessage("Invalid Mobile Number");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        String userMobileNumber = !mobileNumber.startsWith("234") ? mobileNumber.replaceFirst("0", "234") : mobileNumber;

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fcmbProcessorBaseUrl+"/management/getStatus");
        builder.queryParam("mobileNumber", userMobileNumber);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle(" User Account Status Query");
        auditTrailDto.setTransactionDetails("Queried User Account Status");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        try{
            Response data = Objects.requireNonNull(restTemplate.exchange(builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    Response.class)).getBody();

            if (data.getResponseCode() == 0){
                response.setResponseCode(200);
                response.setResponseMessage("Successful");
                response.setResponseData(data.getResponseData());
                return new ResponseEntity<>(response,HttpStatus.OK);
            }else{
                response.setResponseCode(400);
                response.setResponseMessage(data.getResponseMessage());
                response.setResponseData("");
                return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
            }

        }catch (HttpStatusCodeException e) {
            response.setResponseCode(400);
            response.setResponseMessage("Request Failed");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("/generateAllTransactionsPdf")
    @PreAuthorize("hasAuthority('view_query_transaction_history')")
    public ResponseEntity<?> generatePdfOfAllTransactions(@RequestParam(value = "userMobileNumber", defaultValue = "") String mobileNumber, @RequestParam(value = "duration", defaultValue = "") String duration, HttpServletRequest request){

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        Response response = new Response();


        if (!mobileNumber.equals("")){
            if (mobileNumber.length() < 11 || mobileNumber.length() > 13){
                response.setResponseCode(400);
                response.setResponseMessage("Invalid Mobile Number");
                response.setResponseData("");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        }

        if(!duration.equals("")){
            if (!duration.contains("TO")){
                response.setResponseCode(400);
                response.setResponseMessage("Invalid Duration");
                response.setResponseData("");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        }


        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        String userMobileNumber = !mobileNumber.startsWith("234") ? mobileNumber.replaceFirst("0", "234") : mobileNumber;

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fcmbProcessorBaseUrl+"/transactions/PDFReport");
        builder.queryParam("mobileNumber", userMobileNumber);
        builder.queryParam("duration", duration);

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Transactions PDF Report Generation");
        auditTrailDto.setTransactionDetails("Generated Pdf report of all transactions");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        try{
           Resource data = Objects.requireNonNull(restTemplate.exchange(builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    Resource.class)).getBody();

            HttpHeaders headers2 = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=FCMB_TransactionLog.pdf");

            return ResponseEntity.ok().headers(headers2).contentType(MediaType.APPLICATION_PDF).body(data);
        }catch (HttpStatusCodeException e) {
            response.setResponseCode(400);
            response.setResponseMessage("Request Failed");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("/generateUserTransactionsPdf")
    @PreAuthorize("hasAuthority('view_query_transaction_history')")
    public ResponseEntity<?> generateUserTransactionsPdf(@RequestParam("userMobileNumber") String mobileNumber, HttpServletRequest request){

        User user = AuthenticatedUser.getAuthenticatedUser(request);
        Response response = new Response();

        if (StringUtils.isEmpty(mobileNumber)){
            response.setResponseCode(400);
            response.setResponseMessage("Mobile Number cannot be null");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (mobileNumber.length() < 11 || mobileNumber.length() > 13){
            response.setResponseCode(400);
            response.setResponseMessage("Invalid Mobile Number");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        String userMobileNumber = !mobileNumber.startsWith("234") ? mobileNumber.replaceFirst("0", "234") : mobileNumber;

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fcmbProcessorBaseUrl+"/transactions/PDFReport/{mobileNumber}");

        Map<String, String> path = new HashMap<>();
        path.put("mobileNumber", userMobileNumber);

        URI uri = builder.buildAndExpand(path).toUri();
        System.out.println(uri.toString());

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("User Transactions PDF Report Generation");
        auditTrailDto.setTransactionDetails("Generated Pdf report of all User transactions");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        try{
            Resource data = Objects.requireNonNull(restTemplate.exchange(builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    Resource.class)).getBody();

            HttpHeaders headers2 = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=FCMB_TransactionLog.pdf");

            return ResponseEntity.ok().headers(headers2).contentType(MediaType.APPLICATION_PDF).body(data);
        }catch (HttpStatusCodeException e) {
            response.setResponseCode(400);
            response.setResponseMessage("Request Failed");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

    }


    @GetMapping("/getUserTypes")
    @PreAuthorize("hasAuthority('view_query_mobile_app_user')")
    public ResponseEntity<?> getUserTypes(HttpServletRequest request) throws URISyntaxException {
        User user = AuthenticatedUser.getAuthenticatedUser(request);
        PagedResponse response = new PagedResponse();
        if (user == null) {
            response.setResponseCode(400);
            response.setResponseMessage("User not found");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }


        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Query List of User Types");
        auditTrailDto.setTransactionDetails("Returned a list of User Types");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        URI uri = new URI(fcmbProcessorBaseUrl+"/list/userTypes");

        try{

            PagedResponse processorResponse = Objects.requireNonNull(restTemplate.exchange(uri, HttpMethod.GET, entity, PagedResponse.class).getBody());

            response.setResponseCode(200);
            response.setResponseMessage("Successful");
            response.setResponseData(processorResponse.getResponseData());
            response.setPage(processorResponse.getPage());
            response.setTotal(processorResponse.getTotal());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (HttpStatusCodeException e){
            response.setResponseCode(400);
            response.setResponseMessage("Request Failed");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

    }


}
