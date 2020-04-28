package com.tk.fcmb.Service;

import com.tk.fcmb.Entities.OTP;
import org.springframework.http.ResponseEntity;

public interface OtpService {

    ResponseEntity<OTP> generateOtp(long userId, String otpSendMode) throws Exception;
    ResponseEntity<OTP> reGenerateOtp(long userId, String otpSendMode) throws Exception;
    ResponseEntity<?> verifyOtp(long userId, String otp);

}
