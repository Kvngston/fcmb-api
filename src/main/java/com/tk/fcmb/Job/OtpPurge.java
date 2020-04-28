package com.tk.fcmb.Job;

import com.tk.fcmb.Entities.OTP;
import com.tk.fcmb.Repositories.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class OtpPurge {

    @Autowired
    private OtpRepository otpRepository;

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

}
