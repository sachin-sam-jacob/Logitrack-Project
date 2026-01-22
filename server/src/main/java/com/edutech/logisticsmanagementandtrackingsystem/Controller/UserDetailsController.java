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
import com.edutech.logisticsmanagementandtrackingsystem.entity.Business;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Customer;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Driver;
import com.edutech.logisticsmanagementandtrackingsystem.service.BusinessService;
import com.edutech.logisticsmanagementandtrackingsystem.service.CustomerService;
import com.edutech.logisticsmanagementandtrackingsystem.service.DriverService;

@RestController
@RequestMapping("/api/user-details")
public class UserDetailsController {

private final BusinessService businessService;
    private final DriverService driverService;
    private final CustomerService customerService;

    @Autowired
    public UserDetailsController(BusinessService businessService,
                          DriverService driverService,
                          CustomerService customerService) {
        this.businessService = businessService;
        this.driverService = driverService;
        this.customerService = customerService;
    }


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

    // NEW: Get existing details
    @GetMapping("/business")
    public ResponseEntity<?> getBusinessDetails(@RequestParam String username) {
        try {
            Business business = businessService.getBusinessDetails(username);
            return ResponseEntity.ok(business);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/driver")
    public ResponseEntity<?> getDriverDetails(@RequestParam String username) {
        try {
            Driver driver = driverService.getDriverDetails(username);
            return ResponseEntity.ok(driver);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/customer")
    public ResponseEntity<?> getCustomerDetails(@RequestParam String username) {
        try {
            Customer customer = customerService.getCustomerDetails(username);
            return ResponseEntity.ok(customer);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // NEW: Toggle driver availability
    @PostMapping("/driver/toggle-availability")
    public ResponseEntity<Map<String, Object>> toggleAvailability(@RequestParam String username) {
        try {
            Driver driver = driverService.toggleAvailability(username);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Availability updated");
            response.put("isAvailable", driver.isAvailable());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}