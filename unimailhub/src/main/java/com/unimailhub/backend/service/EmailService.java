package com.unimailhub.backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Welcome to UniMailHub");
        message.setText("Welcome to UniMailHub! Your account has been created successfully.");
        mailSender.send(message);
    }

    public void sendSecurityEmail(String toEmail, String approveUrl, String denyUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Security Alert: Login Attempt Detected");
        message.setText("Someone is trying to log in to your account.\n\n" +
                "If this is you, click here to approve: " + approveUrl + "\n\n" +
                "If this is not you, click here to deny: " + denyUrl + "\n\n" +
                "This link will expire in 15 minutes.");
        mailSender.send(message);
    }
}