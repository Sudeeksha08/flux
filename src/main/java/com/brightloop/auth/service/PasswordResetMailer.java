package com.brightloop.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetMailer {
    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String resetUrl;

    public PasswordResetMailer(JavaMailSender mailSender,
                               @Value("${app.mail.from:no-reply@brightloop.local}") String fromAddress,
                               @Value("${app.auth.password-reset-url:http://localhost:5173/reset-password}") String resetUrl) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.resetUrl = resetUrl;
    }

    public void send(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("Reset your Brightloop password");
        message.setText("Use the following one-time link to reset your password: " + resetUrl
                + "?token=" + token + "\nThis link expires in 30 minutes. If you did not request it, ignore this email.");
        mailSender.send(message);
    }
}
