package com.tk.fcmb.Service.impl;

import com.tk.fcmb.Entities.OTP;
import com.tk.fcmb.Entities.User;
import com.tk.fcmb.Repositories.OtpRepository;
import com.tk.fcmb.Repositories.UserRepository;
import com.tk.fcmb.Service.EmailServices;
import com.tk.fcmb.Service.OtpService;
import com.tk.fcmb.restclients.CoreBankClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private CoreBankClients coreBankClients;


    @Override
    public ResponseEntity<OTP> generateOtp(long userId, String otpSendMode) throws Exception {

        if (otpSendMode.contains("mobile")){

            OTP otp = new OTP();
            User user = userRepository.getOne(userId);

            otpRepository.findAllByUser(userRepository.getOne(userId)).forEach(
                    otp1 -> {
                        otp1.setValid(false);
                        otpRepository.save(otp1);
                    }
            );

            otp.setValid(true);
            otp.setPhoneNumber(userRepository.getOne(userId).getPhoneNumber());

            otp.setCreatedAt(LocalTime.now());
            otp.setExpiryTime(otp.getCreatedAt().plusMinutes(5));
            otp.setCode(codeGenerator());

            otp.setUser(user);

            userRepository.save(user);
            otpRepository.save(otp);

            //send the otp to the number
            coreBankClients.sendSMS(user.getPassword(), "Your One Time Password is "+ otp.getCode(), "09058547992");

            return new ResponseEntity<>(otp, HttpStatus.CREATED);

        }else if (otpSendMode.contains("email")){

            OTP otp = new OTP();
            User user = userRepository.getOne(userId);

            otpRepository.findAllByUser(userRepository.getOne(userId)).forEach(
                    otp1 -> {
                        otp1.setValid(false);
                        otpRepository.save(otp1);
                    }
            );

            otp.setValid(true);
            otp.setEmail(userRepository.getOne(userId).getEmail());

            otp.setCreatedAt(LocalTime.now());
            otp.setExpiryTime(otp.getCreatedAt().plusMinutes(5));
            otp.setCode(codeGenerator());

            otp.setUser(user);
            otpRepository.save(otp);


            userRepository.save(user);

            emailServices.sendMail("One Time Password","Your OTP code is : " + otp.getCode(), user.getEmail());

            return new ResponseEntity<>(otp, HttpStatus.CREATED);

        }else {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<OTP> reGenerateOtp(long userId, String otpSendMode) throws Exception {

        otpRepository.findAllByUser(userRepository.getOne(userId)).forEach(
                otp -> {
                    otp.setValid(false);
                    otpRepository.save(otp);
                }
        );

        return  generateOtp(userId,otpSendMode);
    }

    @Override
    public ResponseEntity<?> verifyOtp(long userId, String otp) {
        OTP userOtp = otpRepository.findByUserAndValid(userRepository.getOne(userId),true);

        if (userOtp.getCode().equals(otp)){
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
