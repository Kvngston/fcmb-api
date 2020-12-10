package com.tk.fcmb.Service.impl;

import com.tk.fcmb.Entities.OTP;
import com.tk.fcmb.Entities.User;
import com.tk.fcmb.Entities.dto.Response;
import com.tk.fcmb.Enums.LoginFlag;
import com.tk.fcmb.Repositories.OtpRepository;
import com.tk.fcmb.Repositories.UserRepository;
import com.tk.fcmb.Service.EmailServices;
import com.tk.fcmb.Service.OtpService;
import com.tk.fcmb.restclients.CoreBankClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Random;

@Service
public class OtpServiceImpl implements OtpService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private EmailServices emailServices;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private CoreBankClients coreBankClients;


    @Override
    public ResponseEntity<?> generateOtp(String email, String otpSendMode) throws Exception {

        Response response = new Response();

        if (otpSendMode.equals("sms".toLowerCase())){

            OTP otp = new OTP();
            User user = userRepository.findByEmail(email);

            otpRepository.findAllByUserAndValid(userRepository.findByEmail(email), true).forEach(
                    otp1 -> {
                        otp1.setValid(false);
                        otpRepository.save(otp1);
                    }
            );

            otp.setValid(true);
            otp.setPhoneNumber(userRepository.findByEmail(email).getPhoneNumber());

            otp.setCreatedAt(LocalTime.now());
            otp.setExpiryTime(otp.getCreatedAt().plusMinutes(5));

            String generatedOtp = codeGenerator();

            otp.setCode(passwordEncoder.encode(generatedOtp));

            otp.setUser(user);

            user.setLoginFlag(LoginFlag.VERIFY_OTP_FLAG);

            userRepository.save(user);
            otpRepository.save(otp);

            //send the otp to the number

            coreBankClients.sendSMS(!user.getPhoneNumber().startsWith("234") ? user.getPhoneNumber().replaceFirst("0", "234") : user.getPhoneNumber(),
                    "Your One Time Password is "+ generatedOtp, "FCMB BETA",user.getId());

//            !user.getPhoneNumber().startsWith("234") ? user.getPhoneNumber().replaceFirst("0", "234") : user.getPhoneNumber()
            response.setResponseCode(200);
            response.setResponseMessage("Successful");
            response.setResponseData(otp);
            return new ResponseEntity<>(response, HttpStatus.OK);

        }else if (otpSendMode.equals("email".toLowerCase())){

            OTP otp = new OTP();
            User user = userRepository.findByEmail(email);

            otpRepository.findAllByUserAndValid(userRepository.findByEmail(email), true).forEach(
                    otp1 -> {
                        otp1.setValid(false);
                        otpRepository.save(otp1);
                    }
            );

            otp.setValid(true);
            otp.setEmail(userRepository.findByEmail(email).getEmail());

            otp.setCreatedAt(LocalTime.now());
            otp.setExpiryTime(otp.getCreatedAt().plusMinutes(5));

            String generatedOtp = codeGenerator();

            otp.setCode(passwordEncoder.encode(generatedOtp));

            otp.setUser(user);
            otpRepository.save(otp);

            user.setLoginFlag(LoginFlag.VERIFY_OTP_FLAG);

            userRepository.save(user);

            emailServices.sendMail("One Time Password","Your OTP code is : " + generatedOtp, user.getEmail());


            response.setResponseCode(200);
            response.setResponseMessage("Successful");
            response.setResponseData(otp);
            return new ResponseEntity<>(response, HttpStatus.OK);

        }else {
            response.setResponseCode(200);
            response.setResponseMessage("An Error occurred");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> reGenerateOtp(String email, String otpSendMode) throws Exception {

        return generateOtp(email,otpSendMode);
    }

    @Override
    public ResponseEntity<?> verifyOtp(long userId, String otp) {
        OTP userOtp = otpRepository.findByUserAndValid(userRepository.findById(userId),true);
        Response response = new Response();
        if (userOtp == null){
            response.setResponseCode(400);
            response.setResponseMessage("Otp might has Expired");
            response.setResponseData("");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (passwordEncoder.matches(otp,userOtp.getCode())){
            if (userOtp.isValid()){
                return ResponseEntity.accepted().body(true);
            }else
                return ResponseEntity.badRequest().body(false);

        }else {
            return ResponseEntity.badRequest().body(false);
        }
    }

    public String codeGenerator() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        return String.format("%06d", number);
    }
}
