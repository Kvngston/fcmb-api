package com.tk.fcmb.Service;

import com.tk.fcmb.Entities.OTP;
import org.springframework.http.ResponseEntity;

public interface OtpService {

    ResponseEntity<?> generateOtp(String email, String otpSendMode) throws Exception;
    ResponseEntity<?> reGenerateOtp(String email, String otpSendMode) throws Exception;
    ResponseEntity<?> verifyOtp(long userId, String otp);

}
