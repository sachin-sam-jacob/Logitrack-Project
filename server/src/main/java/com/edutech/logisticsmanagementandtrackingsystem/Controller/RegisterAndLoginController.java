package com.edutech.logisticsmanagementandtrackingsystem.Controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.edutech.logisticsmanagementandtrackingsystem.dto.LoginRequest;
import com.edutech.logisticsmanagementandtrackingsystem.dto.LoginResponse;
import com.edutech.logisticsmanagementandtrackingsystem.dto.OtpRequest;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Business;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Customer;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Driver;
import com.edutech.logisticsmanagementandtrackingsystem.entity.User;
import com.edutech.logisticsmanagementandtrackingsystem.jwt.JwtUtil;
import com.edutech.logisticsmanagementandtrackingsystem.service.BusinessService;
import com.edutech.logisticsmanagementandtrackingsystem.service.CustomerService;
import com.edutech.logisticsmanagementandtrackingsystem.service.DriverService;
import com.edutech.logisticsmanagementandtrackingsystem.service.OtpService;
import com.edutech.logisticsmanagementandtrackingsystem.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RegisterAndLoginController {

    @Autowired  
    private UserService userService;  

    @Autowired  
    private OtpService otpService;  

    @Autowired  
    private BusinessService businessService;  

    @Autowired  
    private CustomerService customerService;  

    @Autowired  
    private DriverService driverService;  

    @Autowired  
    private AuthenticationManager authenticationManager;  

    @Autowired  
    private JwtUtil jwtUtil;  

    // REGISTER - FIXED
    @PostMapping("/register")  
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody User user) {  
        try {
            User registeredUser = userService.registerUser(user);  
            if (registeredUser == null) {  
                Map<String, String> error = new HashMap<>();  
                error.put("message", "User already exists");  
                return ResponseEntity.badRequest().body(error);  
            }  

            otpService.generateAndSendOtp(registeredUser);  
            Map<String, String> success = new HashMap<>();  
            success.put("message", "OTP sent to email");  
            return ResponseEntity.ok(success);  
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();  
            error.put("message", "Registration failed: " + e.getMessage());  
            return ResponseEntity.internalServerError().body(error);
        }
    }  

    // VERIFY OTP  
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestBody OtpRequest request) {
        try {
            User user = userService.getUserByUsername(request.getUsername());  
            if (!otpService.validateOtp(user, request.getOtp())) {  
                Map<String, String> error = new HashMap<>();  
                error.put("message", "Invalid or expired OTP");  
                return ResponseEntity.badRequest().body(error);  
            }  

            user.setEnabled(true);  
            user.setEmailVerified(true);  
            userService.updateUser(user);  

            // Create role-based entity ONLY ONCE  
            if (user.getRole().equals("BUSINESS")) {  
                Business business = new Business();  
                business.setName(user.getUsername());  
                business.setEmail(user.getEmail());  
                businessService.registerBusiness(business);  
            } else if (user.getRole().equals("CUSTOMER")) {  
                Customer customer = new Customer();  
                customer.setName(user.getUsername());  
                customer.setEmail(user.getEmail());  
                customerService.createCustomer(customer);  
            } else {  
                Driver driver = new Driver();  
                driver.setName(user.getUsername());  
                driver.setEmail(user.getEmail());  
                driverService.createDriver(driver);  
            }  

            Map<String, String> success = new HashMap<>();  
            success.put("message", "Email verified successfully");  
            return ResponseEntity.ok(success);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();  
            error.put("message", "Verification failed: " + e.getMessage());  
            return ResponseEntity.badRequest().body(error);
        }
    }

    // LOGIN 
    @PostMapping("/login")  
    public ResponseEntity<LoginResponse> loginUser(@RequestBody LoginRequest loginRequest) {  
        User user = userService.getUserByUsername(loginRequest.getUsername());  
        if (!user.isEnabled()) {  
            throw new ResponseStatusException(  
                HttpStatus.FORBIDDEN,  
                "Please verify your email first"  
            );  
        }  
        authenticationManager.authenticate(  
            new UsernamePasswordAuthenticationToken(  
                loginRequest.getUsername(),  
                loginRequest.getPassword()  
            )  
        );  
        final UserDetails userDetails = userService.loadUserByUsername(loginRequest.getUsername());  
        final String token = jwtUtil.generateToken(userDetails.getUsername());  
        return ResponseEntity.ok(  
            new LoginResponse(  
                token,  
                user.getUsername(),  
                user.getEmail(),  
                user.getRole(),  
                user.getId()  
            )  
        );  
    }

    //RESEND OTP
    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, String>> resendOtp(@RequestBody Map<String, String> request) {

        String username = request.get("username");
        User user = userService.getUserByUsername(username);

        if (user.isEnabled()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "User already verified"));
        }

        otpService.generateAndSendOtp(user);

        return ResponseEntity.ok(
                Map.of("message", "OTP resent successfully")
        );
    }
}