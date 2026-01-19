package com.edutech.logisticsmanagementandtrackingsystem.Controller;

import com.edutech.logisticsmanagementandtrackingsystem.dto.EmailRequest;
import com.edutech.logisticsmanagementandtrackingsystem.dto.ResetPasswordRequest;
import com.edutech.logisticsmanagementandtrackingsystem.service.ForgotPasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/forgot-password")
@CrossOrigin
public class ForgotPasswordController {

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    @PostMapping("/check")
    public ResponseEntity<Map<String, Boolean>> check(@RequestBody EmailRequest req) {
        boolean exists = forgotPasswordService.existsByEmail(req.getEmail());
        Map<String, Boolean> body = new HashMap<>();
        body.put("exists", exists);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/request")
    public ResponseEntity<Map<String, String>> request(@RequestBody EmailRequest req) {
        String username = forgotPasswordService.sendResetOtpAndReturnUsername(req.getEmail());

        Map<String, String> body = new HashMap<>();
        body.put("message", "If the email exists, reset OTP has been sent.");
        if (username != null) {
            body.put("username", username); // frontend needs this for verify/resend
        }
        return ResponseEntity.ok(body);
    }
    

@PostMapping("/reset")
public ResponseEntity<Map<String, String>> reset(@RequestBody ResetPasswordRequest req) {
    forgotPasswordService.resetPassword(req.getUsername(), req.getNewPassword());
    return ResponseEntity.ok(Map.of("message", "Password updated"));
}

}