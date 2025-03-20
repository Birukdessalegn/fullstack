package com.fullstackBackend.fullstack_backend_application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendOTP(String email, String otp) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(otp)) {
            throw new IllegalArgumentException("Email and OTP must not be empty");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP for Login");
        message.setText("Your OTP is: " + otp);

        try {
            javaMailSender.send(message);
            System.out.println("OTP sent successfully to " + email);
        } catch (Exception e) {
            System.err.println("Failed to send OTP: " + e.getMessage());
            e.printStackTrace();
        }
    }
}