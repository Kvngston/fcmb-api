package com.tk.fcmb.Job;

import com.tk.fcmb.Entities.OTP;
import com.tk.fcmb.Entities.dto.Response;
import com.tk.fcmb.Enums.TransactionGraphCountType;
import com.tk.fcmb.Repositories.OtpRepository;
import com.tk.fcmb.utils.GraphDbPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import static com.tk.fcmb.Enums.TransactionGraphCountType.COUNT_BY_DAY;

@Service
public class OtpPurge {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private GraphDbPopulator dbPopulator;

    @Async
    @Scheduled(cron = "* * * 1/1 * ?")
    public void expireOtp(){

        //reduce to only valid ones
        List<OTP> purgeList = otpRepository.findAllByValid(true);

        purgeList.forEach(otp -> {
            if (LocalTime.now().toString().contains(otp.getExpiryTime().toString())){
                otp.setValid(false);
                otpRepository.save(otp);
            }
        });
    }

    @Async
    @Scheduled(cron = "0 0 0/1 1/1 * ?")
    public void updateGraphContents(){
        dbPopulator.dailyGraphPopulator();
        dbPopulator.monthlyGraphPopulator();
        dbPopulator.weeklyGraphPopulator();
        dbPopulator.yearlyGraphPopulator();
        dbPopulator.userDailyGraphPopulator();
        dbPopulator.usersMonthlyGraphPopulator();
        dbPopulator.usersYearlyGraphPopulator();
        dbPopulator.userWeeklyGraphPopulator();
        dbPopulator.usersDaysInWeeksGraphPopulator();
    }

}
