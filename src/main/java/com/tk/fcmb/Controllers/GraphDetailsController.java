package com.tk.fcmb.Controllers;

import com.tk.fcmb.Entities.TransactionGraphData;
import com.tk.fcmb.Entities.User;
import com.tk.fcmb.Entities.UsersGraphData;
import com.tk.fcmb.Entities.dto.AuditTrailDto;
import com.tk.fcmb.Entities.dto.Response;
import com.tk.fcmb.Enums.PortalUsersGraphCountType;
import com.tk.fcmb.Enums.TransactionGraphCountType;
import com.tk.fcmb.Enums.UserGraphCountType;
import com.tk.fcmb.Job.IpAddressGetter;
import com.tk.fcmb.Repositories.*;
import com.tk.fcmb.Service.AuditTrailService;
import com.tk.fcmb.Service.UserService;
import com.tk.fcmb.utils.GetAuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/graphDetails")
public class GraphDetailsController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditTrailService auditTrailService;

    @Autowired
    private UserService userService;

    @Autowired
    private IpAddressGetter ipAddressGetter;


    @Autowired
    private TransactionGraphDataRepository transactionGraphDataRepository;

    @Autowired
    private UsersGraphDataRepository usersGraphDataRepository;

    @Autowired
    private GetAuthenticatedUser AuthenticatedUser;


    @GetMapping("/transactionGraphCountByType")
    @PreAuthorize("hasAuthority('view_query_mobile_app_user')")
    public ResponseEntity<?> transactionGraphCountByType(@RequestParam("type") TransactionGraphCountType type, HttpServletRequest request){

        Response userResponse = new Response();
        User user = AuthenticatedUser.getAuthenticatedUser(request);
        if (user == null) {
            userResponse.setResponseCode(400);
            userResponse.setResponseMessage("User not found");
            userResponse.setResponseData("");
            return new ResponseEntity<>(userResponse, HttpStatus.BAD_REQUEST);
        }

        if(StringUtils.isEmpty(type)){
            userResponse.setResponseCode(400);
            userResponse.setResponseMessage("Type not found");
            userResponse.setResponseData("");
            return new ResponseEntity<>(userResponse, HttpStatus.BAD_REQUEST);
        }


        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Graph Count");
        auditTrailDto.setTransactionDetails("Returned a Graph count by type");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        TransactionGraphData graphData = transactionGraphDataRepository.findByIdentifier("graphData");

        List<Map<String, List<Integer>>> response = new ArrayList<>();
        Map<String, List<Integer>> specificResponse = new HashMap<>();

        switch (type){
            case COUNT_BY_DAY:{
                specificResponse.put("Successful", graphData.getDailySuccessful());
                specificResponse.put("Failed", graphData.getDailyFailed());
                response.add(specificResponse);
                break;
            }
            case COUNT_BY_WEEK:{
                specificResponse.put("Successful", graphData.getWeeklySuccessful());
                specificResponse.put("Failed", graphData.getWeeklyFailed());
                response.add(specificResponse);
                break;
            }
            case COUNT_BY_MONTH:{
                specificResponse.put("Successful", graphData.getMonthlySuccessful());
                specificResponse.put("Failed", graphData.getMonthlyFailed());
                response.add(specificResponse);
                break;
            }
            case COUNT_BY_YEAR:{
                specificResponse.put("Successful", graphData.getYearlySuccessful());
                specificResponse.put("Failed", graphData.getYearlyFailed());
                response.add(specificResponse);
                break;
            }
            default:{
                userResponse.setResponseCode(400);
                userResponse.setResponseMessage("Invalid type");
                userResponse.setResponseData("");
                return new ResponseEntity<>(userResponse,HttpStatus.BAD_REQUEST);
            }
        }

        userResponse.setResponseCode(200);
        userResponse.setResponseMessage("Successful");
        userResponse.setResponseData(response);
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @GetMapping("/customersGraphCountByType")
    @PreAuthorize("hasAuthority('view_query_mobile_app_user')")
    public ResponseEntity<?> customersGraphCountByType(@RequestParam("type") UserGraphCountType type, HttpServletRequest request){
        Response userResponse = new Response();
        User user = AuthenticatedUser.getAuthenticatedUser(request);
        if (user == null) {
            userResponse.setResponseCode(400);
            userResponse.setResponseMessage("User not found");
            userResponse.setResponseData("");
            return new ResponseEntity<>(userResponse, HttpStatus.BAD_REQUEST);
        }

        if(StringUtils.isEmpty(type)){
            userResponse.setResponseCode(400);
            userResponse.setResponseMessage("Type not found");
            userResponse.setResponseData("");
            return new ResponseEntity<>(userResponse, HttpStatus.BAD_REQUEST);
        }

        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Graph Count");
        auditTrailDto.setTransactionDetails("Returned a Graph count by type");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        UsersGraphData graphData = usersGraphDataRepository.findByIdentifier("graphData");

        List<Map<String, List<Integer>>> response = new ArrayList<>();
        Map<String, List<Integer>> specificResponse = new HashMap<>();

        switch (type){
            case COUNT_BY_DAY:{
                specificResponse.put("Successful", graphData.getDailySuccessful());
                response.add(specificResponse);
                break;
            }
            case COUNT_BY_WEEK:{
                specificResponse.put("Successful", graphData.getWeeklySuccessful());
                response.add(specificResponse);
                break;
            }
            case COUNT_BY_MONTH:{
                specificResponse.put("Successful", graphData.getMonthlySuccessful());
                response.add(specificResponse);
                break;
            }
            case COUNT_BY_YEAR:{
                specificResponse.put("Successful", graphData.getYearlySuccessful());
                response.add(specificResponse);
                break;
            }
            case COUNT_BY_DAYS_IN_WEEKS:{
                specificResponse.put("week1", graphData.getWeek1Successful());
                specificResponse.put("week2", graphData.getWeek2Successful());
                specificResponse.put("week3", graphData.getWeek3Successful());
                specificResponse.put("week4", graphData.getWeek4Successful());
                specificResponse.put("week5", graphData.getWeek5Successful());
                response.add(specificResponse);
                break;
            }
            default:{
                userResponse.setResponseCode(400);
                userResponse.setResponseMessage("Invalid Type");
                userResponse.setResponseData("");
                return new ResponseEntity<>(userResponse,HttpStatus.BAD_REQUEST);
            }
        }

        userResponse.setResponseCode(200);
        userResponse.setResponseMessage("Successful");
        userResponse.setResponseData(response);
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @GetMapping("/usersGraphCountByType")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> usersGraphCountByType(@RequestParam("type") PortalUsersGraphCountType type, HttpServletRequest request){
        Response userResponse = new Response();
        User user = AuthenticatedUser.getAuthenticatedUser(request);
        if (user == null) {
            userResponse.setResponseCode(400);
            userResponse.setResponseMessage("User not found");
            userResponse.setResponseData("");
            return new ResponseEntity<>(userResponse, HttpStatus.BAD_REQUEST);
        }

        if(StringUtils.isEmpty(type)){
            userResponse.setResponseCode(400);
            userResponse.setResponseMessage("Type not found");
            userResponse.setResponseData("");
            return new ResponseEntity<>(userResponse, HttpStatus.BAD_REQUEST);
        }


        AuditTrailDto auditTrailDto = new AuditTrailDto();
        auditTrailDto.setTitle("Graph Count");
        auditTrailDto.setTransactionDetails("Returned a Graph count by type");
        auditTrailDto.setIpAddress(ipAddressGetter.getClientIpAddress(request));
        auditTrailDto.setStaffId(user.getStaffId());
        auditTrailService.createNewEvent(auditTrailDto);

        List<Map<String, List<Integer>>> response = new ArrayList<>();
        Map<String, List<Integer>> data = userService.userAnalysis();
        Map<String, List<Integer>> specificResponse = new HashMap<>();


        switch (type){
            case COUNT_BY_DAY:{
                specificResponse.put("SuperAdminCount", data.get("dailySuperAdmin"));
                specificResponse.put("AdminCount", data.get("dailyAdmin"));
                specificResponse.put("AgentCount", data.get("dailyAgent"));
                response.add(specificResponse);
                break;
            }
            case COUNT_BY_WEEK:{
                specificResponse.put("SuperAdminCount", data.get("weeklySuperAdmin"));
                specificResponse.put("AdminCount", data.get("weeklyAdmin"));
                specificResponse.put("AgentCount", data.get("weeklyAgent"));
                response.add(specificResponse);
                break;
            }
            case COUNT_BY_MONTH:{
                specificResponse.put("SuperAdminCount", data.get("monthlySuperAdmin"));
                specificResponse.put("AdminCount", data.get("monthlyAdmin"));
                specificResponse.put("AgentCount", data.get("monthlyAgent"));
                response.add(specificResponse);
                break;
            }
            case COUNT_BY_YEAR:{
                specificResponse.put("SuperAdminCount", data.get("superAdminYearly"));
                specificResponse.put("AdminCount", data.get("adminYearly"));
                specificResponse.put("AgentCount", data.get("agentYearly"));
                response.add(specificResponse);
                break;
            }
            default:{
                userResponse.setResponseCode(400);
                userResponse.setResponseMessage("Invalid Type");
                userResponse.setResponseData("");
                return new ResponseEntity<>(userResponse,HttpStatus.BAD_REQUEST);
            }
        }


        userResponse.setResponseCode(200);
        userResponse.setResponseMessage("Successful");
        userResponse.setResponseData(response);
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

}
