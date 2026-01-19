package com.edutech.logisticsmanagementandtrackingsystem.service;

import com.edutech.logisticsmanagementandtrackingsystem.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.edutech.logisticsmanagementandtrackingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ForgotPasswordService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository; // field injection (as requested)

    @Autowired
    private OtpService otpService; // you already have this; takes User

    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return userRepository.existsByEmailIgnoreCase(email.trim());

        // If your username IS the email, switch to:
        // return userRepository.existsByUsernameIgnoreCase(email.trim());
    }

    /**
     * Finds the user by email, sends OTP via OtpService, and returns the username
     * (so the UI can call /api/verify-otp and /api/resend-otp with { username }).
     * Returns null if the email is not registered.
     */
    public String sendResetOtpAndReturnUsername(String email) {
        if (email == null || email.trim().isEmpty()) return null;

        // If your User has a real 'email' column:
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email.trim());

        // If username is actually the email, use this instead:
        // Optional<User> userOpt = userRepository.findByUsernameIgnoreCase(email.trim());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            otpService.generateAndSendOtp(user); // sends email with 6-digit OTP
            return user.getUsername();
        }
        return null;
    }

    // Back-compat if some callers still use this name
    public void requestReset(String email) {
        sendResetOtpAndReturnUsername(email);
    }
    
public void resetPassword(String username, String newPassword) {
    if (username == null || username.trim().isEmpty()) {
        throw new IllegalArgumentException("Username required");
    }
    var user = userRepository.findByUsername(username);
    if (user == null) throw new IllegalArgumentException("User not found");

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    // Optional: invalidate OTP record here so it can't be reused
    // emailOtpRepository.findByUser(user).ifPresent(otp -> { /* mark used/remove */ });
}

}

