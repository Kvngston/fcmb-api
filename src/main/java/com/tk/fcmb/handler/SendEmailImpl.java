package com.tk.fcmb.handler;

import com.tk.fcmb.Service.EmailServices;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendEmailImpl implements Runnable {
    private String subject;
    private String message;
    private String email;
    EmailServices emailServices;

    public SendEmailImpl(EmailServices emailServices, String subject, String message, String email) {
        this.email = email;
        this.emailServices = emailServices;
        this.message = message;
        this.subject = subject;
    }

    @Override
    public void run() {
        try {
            emailServices.sendMail(subject, message, email);
        } catch (Exception e) {
            log.error("Error sending Email ", e);
        }
    }
}
