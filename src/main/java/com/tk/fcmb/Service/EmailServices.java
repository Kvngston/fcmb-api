package com.tk.fcmb.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;

@Slf4j
@Service
public class EmailServices {


    @Autowired
    private JavaMailSender mailSender;



    public void sendMail(String subject, String msg, String email) throws MailException, MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        //helper.setFrom("no-reply@etranzact.net");
        helper.setTo(email);
        helper.setText(msg, true);
        helper.setSubject(subject);

        mailSender.send(message);
        log.info("Email successfully sent to " + email);
    }

    public void sendMail(String subject, String msg, String[] email) throws MailException, MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        //helper.setFrom("no-reply@etranzact.net");
        helper.setTo(email);
        helper.setText(msg, true);
        helper.setSubject(subject);

        mailSender.send(message);
        log.info("Email successfully sent to " + Arrays.toString(email));
    }
}
