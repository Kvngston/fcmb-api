package com.tk.fcmb.handler;

import com.tk.fcmb.Entities.SmsLog;
import com.tk.fcmb.Repositories.SmsLogRepository;
import com.tk.fcmb.utils.AES;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.URLDecoder;
import java.util.Date;

@Slf4j
public class LogSMS implements Runnable {

    private SmsLogRepository smsLogRepository;
    private String senderMobile;
    private String destinationMobile;
    private String message;
    private String responseCode;
    private Long userId;

    public LogSMS(SmsLogRepository smsLogRepository, String senderMobile, String destinationMobile, String message, String responseCode,Long userId) {
        this.smsLogRepository = smsLogRepository;
        this.senderMobile = senderMobile;
        this.destinationMobile = destinationMobile;
        this.message = message;
        this.responseCode = responseCode;
        this.userId = userId;
    }

    @Override
    public void run() {
        String newMessage;
        try {
            newMessage = URLDecoder.decode(message, "UTF-8");
        } catch (Exception e) {
            newMessage = message;
        }
        SmsLog smsLog = new SmsLog();
        smsLog.setDestinationMobile(destinationMobile);
        smsLog.setMessage(StringUtils.isNotBlank(newMessage) ? AES.encrypt(newMessage) : "");
        smsLog.setResponseCode(responseCode);
        smsLog.setSenderMobile(senderMobile);
        smsLog.setCreatedAt(new Date());
        smsLog.setUserId(userId);
        try {
            smsLogRepository.save(smsLog);
        } catch (Exception e) {
            log.error("Error Logging SMS ", e);
        }
    }
}
