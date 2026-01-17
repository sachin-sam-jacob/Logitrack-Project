package com.edutech.logisticsmanagementandtrackingsystem.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.edutech.logisticsmanagementandtrackingsystem.dto.BusinessDetailsRequest;
import com.edutech.logisticsmanagementandtrackingsystem.dto.CustomerDetailsRequest;
import com.edutech.logisticsmanagementandtrackingsystem.dto.DriverDetailsRequest;
import com.edutech.logisticsmanagementandtrackingsystem.service.BusinessService;
import com.edutech.logisticsmanagementandtrackingsystem.service.CustomerService;
import com.edutech.logisticsmanagementandtrackingsystem.service.DriverService;

@RestController
@RequestMapping("/api/user-details")
public class UserDetailsController {

    @Autowired
    private BusinessService businessService;

    @Autowired
    private DriverService driverService;

    @Autowired
    private CustomerService customerService;

    @PostMapping("/business")
    public ResponseEntity<Map<String, String>> updateBusinessDetails(
            @RequestBody BusinessDetailsRequest request) {
        try {
            businessService.updateBusinessDetails(request);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Business details updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to update details: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/driver")
    public ResponseEntity<Map<String, String>> updateDriverDetails(
            @RequestBody DriverDetailsRequest request) {
        try {
            driverService.updateDriverDetails(request);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Driver details updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to update details: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/customer")
    public ResponseEntity<Map<String, String>> updateCustomerDetails(
            @RequestBody CustomerDetailsRequest request) {
        try {
            customerService.updateCustomerDetails(request);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Customer details updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to update details: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/check-completion")
    public ResponseEntity<Map<String, Boolean>> checkDetailsCompletion(
            @RequestParam String username,
            @RequestParam String role) {
        try {
            boolean completed = false;
            
            if (role.equals("BUSINESS")) {
                completed = businessService.isDetailsCompleted(username);
            } else if (role.equals("DRIVER")) {
                completed = driverService.isDetailsCompleted(username);
            } else if (role.equals("CUSTOMER")) {
                completed = customerService.isDetailsCompleted(username);
            }

            Map<String, Boolean> response = new HashMap<>();
            response.put("detailsCompleted", completed);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Boolean> error = new HashMap<>();
            error.put("detailsCompleted", false);
            return ResponseEntity.ok(error);
        }
    }
}