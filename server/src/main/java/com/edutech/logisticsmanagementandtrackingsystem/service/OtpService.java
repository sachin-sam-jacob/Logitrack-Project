package com.edutech.logisticsmanagementandtrackingsystem.service;

import org.springframework.stereotype.Service;

// import javax.mail.MessagingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OtpService {

    // private final EmailService emailService;

    // // store OTPs temporarily (in-memory)
    // private final Map<String, String> otpStorage = new HashMap<>();

    // public OtpService(EmailService emailService) {
    //     this.emailService = emailService;
    // }

    // // generate 6-digit OTP
    // public String generateOtp(String email) throws MessagingException {
    //     String otp = String.format("%06d", new Random().nextInt(999999));
    //     otpStorage.put(email, otp);

    //     // send OTP via email
    //     String subject = "Your OTP for LogiTrack Registration";
    //     String body = "Your OTP is: " + otp + "\nIt will expire in 5 minutes.";
    //     emailService.sendOtp(email, subject, body);

    //     return otp;
    // }

    // // validate OTP
    // public boolean validateOtp(String email, String otp) {
    //     if (otpStorage.containsKey(email) && otpStorage.get(email).equals(otp)) {
    //         otpStorage.remove(email); // remove after validation
    //         return true;
    //     }
    //     return false;
    // }
}