package com.edutech.logisticsmanagementandtrackingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.edutech.logisticsmanagementandtrackingsystem.entity.EmailOtp;
import com.edutech.logisticsmanagementandtrackingsystem.entity.User;
import com.edutech.logisticsmanagementandtrackingsystem.repository.EmailOtpRepository;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OtpService {

    
private final JavaMailSender mailSender;
    private final EmailOtpRepository emailOtpRepository;

    @Autowired
    public OtpService(JavaMailSender mailSender,
                      EmailOtpRepository emailOtpRepository) {
        this.mailSender = mailSender;
        this.emailOtpRepository = emailOtpRepository;
    }


    public void generateAndSendOtp(User user) {
        String otp = String.valueOf((int)(Math.random() * 900000) + 100000); 

        EmailOtp emailOtp = emailOtpRepository.findByUser(user)
                .orElse(new EmailOtp());
        emailOtp.setUser(user);
        emailOtp.setOtp(otp);
        emailOtp.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        emailOtpRepository.save(emailOtp);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(user.getEmail());
        helper.setSubject("LogiTrack | Verify Your Email");

        String htmlContent =
            "<div style='font-family: Arial, sans-serif; background:#f4f6f8; padding:20px'>" +
            "<div style='max-width:500px; background:#ffffff; padding:30px; margin:auto; border-radius:8px'>" +

            "<h2 style='color:#2c3e50'>🔐 Email Verification</h2>" +
            "<p>Hello <b>" + user.getUsername() + "</b>,</p>" +

            "<p>Thank you for registering with <b>LogiTrack</b>.</p>" +
            "<p>Please use the OTP below to verify your email address:</p>" +

            "<div style='font-size:28px; font-weight:bold; letter-spacing:4px; color:#3498db; margin:20px 0'>" +
            otp +
            "</div>" +

            "<p>This OTP is valid for <b>10 minutes</b>.</p>" +

            "<p style='color:#888'>If you did not request this, please ignore this email.</p>" +

            "<hr/>" +
            "<p style='font-size:12px; color:#aaa'>© LogiTrack | Smart Logistics Platform</p>" +

            "</div></div>";

        helper.setText(htmlContent, true);
        mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }



    }

    public boolean validateOtp(User user, String otp) {
        EmailOtp emailOtp = emailOtpRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (emailOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        return emailOtp.getOtp().equals(otp);
    }
}