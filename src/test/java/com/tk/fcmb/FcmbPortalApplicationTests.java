package com.tk.fcmb;

import com.tk.fcmb.Entities.dto.Response;
import com.tk.fcmb.Entities.dto.TransactionLimitUpgradeDto;
import com.tk.fcmb.Service.EmailServices;
import com.tk.fcmb.restclients.CoreBankClients;
import com.tk.fcmb.utils.DateUtil;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.mail.MessagingException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@SpringBootTest
class FcmbPortalApplicationTests {


    @Autowired
    private EmailServices emailServices;

    @Autowired
    private CoreBankClients coreBankClients;

    @Test
    void contextLoads() {
    }

    @Test
    void sendMail() throws MessagingException {

            Random rnd = new Random();
            int number = rnd.nextInt(999999);
            String code = String.format("%06d", number);


        emailServices.sendMail("Test","I love you, and i miss you so much " , "fionaodoboh@gmail.com");
    }

    @Test
    void sendSMS(){

        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        String code = String.format("%06d", number);

        coreBankClients.sendSMS("08065121145", "Your One Time Password is "+ code, "09058547992");
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


        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://18.190.12.249:8001/FCMBProcessor/list/userTypes/{id}");

        Map<String, Integer> path = new HashMap<>();
        path.put("id", 1);

        URI uri = builder.buildAndExpand(path).toUri();
        System.out.println(uri.toString());

        TransactionLimitUpgradeDto upgradeDto = new TransactionLimitUpgradeDto();
        upgradeDto.setDailyAmountLimit(1000);
        upgradeDto.setTransactionLimit(2000);

        ResponseEntity<?> response =  restTemplate.postForEntity(uri.toString(),upgradeDto,Response.class);
        System.out.println(response);
    }

    @Test
    void LocalDateTimeCheck(){
        System.out.println(LocalDateTime.now());
        System.out.println(new Date());
    }

    @Test
    void ipAddressValidator(){
        InetAddressValidator validator = InetAddressValidator.getInstance();
        System.out.println(validator.isValid("0:0:0:0:0:0:0:1"));
    }

}
