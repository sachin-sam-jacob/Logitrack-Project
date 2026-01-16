package com.edutech.logisticsmanagementandtrackingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.edutech.logisticsmanagementandtrackingsystem.entity.EmailOtp;
import com.edutech.logisticsmanagementandtrackingsystem.entity.User;
import com.edutech.logisticsmanagementandtrackingsystem.repository.EmailOtpRepository;

import javax.mail.MessagingException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailOtpRepository emailOtpRepository;

    public void generateAndSendOtp(User user) {
        String otp = String.valueOf((int)(Math.random() * 900000) + 100000); 

        EmailOtp emailOtp = emailOtpRepository.findByUser(user)
                .orElse(new EmailOtp());
        emailOtp.setUser(user);
        emailOtp.setOtp(otp);
        emailOtp.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        emailOtpRepository.save(emailOtp);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Your OTP for registration");
        message.setText("Your OTP is: " + otp + "\nIt expires in 10 minutes.");
        mailSender.send(message);
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