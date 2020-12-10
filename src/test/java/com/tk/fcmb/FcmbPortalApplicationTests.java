package com.tk.fcmb;

import com.tk.fcmb.Entities.AuditModel;
import com.tk.fcmb.Entities.Role;
import com.tk.fcmb.Entities.User;
import com.tk.fcmb.Entities.dto.Response;
import com.tk.fcmb.Enums.RoleType;
import com.tk.fcmb.Enums.TransactionGraphCountType;
import com.tk.fcmb.Repositories.RoleRepository;
import com.tk.fcmb.Repositories.UserRepository;
import com.tk.fcmb.Service.EmailServices;
import com.tk.fcmb.Service.UserService;
import com.tk.fcmb.restclients.CoreBankClients;
import com.tk.fcmb.utils.ConvertToPageable;
import com.tk.fcmb.utils.GraphDbPopulator;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.mail.MessagingException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest
class FcmbPortalApplicationTests {


    @Autowired
    private EmailServices emailServices;

    @Autowired
    private UserService userService;

    @Autowired
    private CoreBankClients coreBankClients;

    @Value("${fcmb.processor.base.url}")
    String baseUrl;

    @Test
    void contextLoads() {
    }

    @Test
    void sendMail() throws MessagingException {

            Random rnd = new Random();
            int number = rnd.nextInt(999999);
            String code = String.format("%06d", number);


    }

    @Test
    void sendSMS(){

        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        String code = String.format("%06d", number);
        long num = 1;

        coreBankClients.sendSMS("23409058547992",
                "Your One Time Password is "+ code, "FCMB BETA",num);
    }

    @Test
    void restTemplateTests(){

//        String url = "http://18.190.12.249:8001/FCMBProcessor";
        RestTemplate restTemplate = new RestTemplate();



//        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://18.190.12.249:8001/FCMBProcessor/management/changeMobileUserType");
//        HttpHeaders headers = new HttpHeaders();
//        HttpEntity<?> entity = new HttpEntity<>(headers);
//
//        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//        params.add("mobileNumber", "09058547992");
//        params.add("userTypeId", "1");
//
//
//        builder.queryParams(params);
//
//        ResponseEntity<?> response =  restTemplate.exchange(builder.toUriString(),
//                HttpMethod.PUT,
//                entity,
//                Response.class);
//
//        System.out.println(response);


//        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://18.190.12.249:8001/FCMBProcessor/list/userTypes/{id}");
//
//        Map<String, Integer> path = new HashMap<>();
//        path.put("id", 1);
//
//        URI uri = builder.buildAndExpand(path).toUri();
//        System.out.println(uri.toString());
//
//        TransactionLimitUpgradeDto upgradeDto = new TransactionLimitUpgradeDto();
//        upgradeDto.setDailyAmountLimit(1000);
//        upgradeDto.setTransactionLimit(2000);
//
//        ResponseEntity<?> response =  restTemplate.postForEntity(uri.toString(),upgradeDto,Response.class);
//        System.out.println(response);


//        HttpHeaders headers = new HttpHeaders();
//        HttpEntity<?> entity = new HttpEntity<>(headers);
//
//        String url =  "http://18.190.12.249:8001/FCMBProcessor/list/activityLog";
//
//        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
//        params.add("accountNumber", "094922823232");
//        params.add("action", "ihbdksb");
//        params.add("date", "023030823");
//        params.add("mobileNumber", "3927392392");
//        params.add("status", "29u32323");
//        params.add("size", 3);
//        params.add("page", 2);
//        params.add("orderAscending", true);
//
//        Response response = restTemplate.postForObject(url,params,Response.class);
//
//        System.out.println(response);

//        GraphDbPopulator graphDbPopulator = new GraphDbPopulator();
////        graphDbPopulator.dailyGraphPopulator();
//        graphDbPopulator.usersDaysInWeeksGraphPopulator();
//
//        System.out.println(baseUrl+"/transactions/logs");

//        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl+"/analyseTransactions");
//
//        HttpHeaders headers = new HttpHeaders();
//        HttpEntity<?> entity = new HttpEntity<>(headers);
//
//        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//        params.add("type", "COUNT_BY_WEEK");
//        builder.queryParams(params);
//
//        ResponseEntity<Response> response = restTemplate.exchange(builder.toUriString(),
//                HttpMethod.GET,
//                entity,
//                Response.class);
//
//        System.out.println(Objects.requireNonNull(response.getBody()).getResponseData().getClass().getName());
//
//        LinkedHashMap hashMap = (LinkedHashMap) Objects.requireNonNull(response.getBody()).getResponseData();
//        System.out.println(hashMap);
//
//        int[] weeklySuccessFul = new int[5];
//        int[] weeklyFailed = new int[5];
//
//        if ((int) hashMap.get("week1Count") > 0 ){
//            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("week1");
//            list.forEach(list1 -> {
//                if ( (list1.get("responseCode").equals("0"))){
//                    weeklySuccessFul[0]++;
//                }else if (list1.get("responseCode").equals("1")){
//                    weeklyFailed[0]++;
//                }
//            });
//        }
//
//        System.out.println(Arrays.toString(weeklyFailed));
//        System.out.println(Arrays.toString(weeklySuccessFul));

    }

    @Test
    void LocalDateTimeCheck(){
        LocalDate date = LocalDate.of(2020,5,31);

//        date.

        System.out.println(date.get(ChronoField.ALIGNED_WEEK_OF_MONTH));
    }

    @Test
    void ipAddressValidator(){
        InetAddressValidator validator = InetAddressValidator.getInstance();
        System.out.println(validator.isValid("0:0:0:0:0:0:0:1"));
    }

    @Test
    void convertToPageTest(){
        List<String> strings = new ArrayList<>();
        strings.add("hello");
        strings.add("hello");
        strings.add("hello");
        strings.add("hello");
        strings.add("hello");

        ConvertToPageable convertToPageable = new ConvertToPageable(0, 20);
        System.out.println(convertToPageable.convertListToPage(strings));

        System.out.println(TransactionGraphCountType.COUNT_BY_DAY.toString().toUpperCase());
    }

    @Test
    void minorTests(){
        List<Integer> list = new ArrayList<>();
        list.add(3);
        list.add(4);
        list.add(5);

        System.out.println(list.stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void usersGraphTest(){
        System.out.println(userService.userAnalysis());
    }

}
