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
import com.edutech.logisticsmanagementandtrackingsystem.entity.Business;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Customer;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Driver;
import com.edutech.logisticsmanagementandtrackingsystem.entity.User;
import com.edutech.logisticsmanagementandtrackingsystem.jwt.JwtUtil;
import com.edutech.logisticsmanagementandtrackingsystem.service.BusinessService;
import com.edutech.logisticsmanagementandtrackingsystem.service.CustomerService;
import com.edutech.logisticsmanagementandtrackingsystem.service.DriverService;
import com.edutech.logisticsmanagementandtrackingsystem.service.UserService;

@RestController
@RequestMapping("/api")
public class RegisterAndLoginController {

    @Autowired
    private UserService userService;

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

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        User registeredUser = userService.registerUser(user);
        if (registeredUser == null) {
            return new ResponseEntity<HttpStatus>(HttpStatus.BAD_REQUEST);
        } else {
            System.out.println("Role: "+registeredUser.getRole());
            if (registeredUser.getRole().equals("BUSINESS")) {
                Business business = new Business();
                business.setName(registeredUser.getUsername());
                business.setEmail(user.getEmail());
                businessService.registerBusiness(business);
                return ResponseEntity.ok(business);
            } else if (registeredUser.getRole().equals("CUSTOMER")) {
                Customer customer = new Customer();
                customer.setName(registeredUser.getUsername());
                customer.setEmail(user.getEmail());
                return ResponseEntity.ok(customerService.createCustomer(customer));
            } else {
                Driver driver = new Driver();
                driver.setName(registeredUser.getUsername());
                driver.setEmail(user.getEmail());
                return ResponseEntity.ok(driverService.createDriver(driver));
            }
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password", e);
        }

        final UserDetails userDetails = userService.loadUserByUsername(loginRequest.getUsername());
        final String token = jwtUtil.generateToken(userDetails.getUsername());

        User user = userService.getUserByUsername(loginRequest.getUsername());
        // if(user.getRole() == "DRIVER"){
        // Driver driver = driverService.getDriver(loginRequest.getUsername());
        // user.setId(driver.getId());
        // }

        return ResponseEntity
                .ok(new LoginResponse(token, user.getUsername(), user.getEmail(), user.getRole(), user.getId()));
    }

}