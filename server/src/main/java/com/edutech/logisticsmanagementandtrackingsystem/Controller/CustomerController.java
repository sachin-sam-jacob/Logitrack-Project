package com.edutech.logisticsmanagementandtrackingsystem.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.edutech.logisticsmanagementandtrackingsystem.dto.CargoStatusResponse;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Cargo;
import com.edutech.logisticsmanagementandtrackingsystem.service.CustomerService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {


private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * View cargo status by ID
     */
    @GetMapping("/cargo-status")
    public ResponseEntity<CargoStatusResponse> viewCargoStatus(@RequestParam Long cargoId) {
        CargoStatusResponse cargoStatusResponse = customerService.viewCargoStatus(cargoId);
        if (cargoStatusResponse != null) {
            return new ResponseEntity<>(cargoStatusResponse, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Get all cargos for customer by email
     */
    @GetMapping("/my-cargos")
    public ResponseEntity<?> getMyCargos(@RequestParam String email) {
        try {
            List<Cargo> cargos = customerService.getCargosByCustomerEmail(email);
            return ResponseEntity.ok(cargos);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Search cargos by tracking number or content
     */
    @GetMapping("/search-cargo")
    public ResponseEntity<?> searchCargo(
            @RequestParam(required = false) String email,
            @RequestParam String keyword) {
        try {
            List<Cargo> cargos = customerService.searchCustomerCargos(email, keyword);
            return ResponseEntity.ok(cargos);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * NEW: Search cargo by tracking number only (no email required)
     */
    @GetMapping("/track-by-number")
    public ResponseEntity<?> trackByNumber(@RequestParam String trackingNumber) {
        try {
            Cargo cargo = customerService.getCargoByTrackingNumber(trackingNumber);
            return ResponseEntity.ok(cargo);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * NEW: Verify delivery OTP
     */
    @PostMapping("/verify-delivery-otp")
    public ResponseEntity<?> verifyDeliveryOtp(
            @RequestBody Map<String, String> request) {
        try {
            Long cargoId = Long.parseLong(request.get("cargoId"));
            String otp = request.get("otp");
            
            boolean verified = customerService.verifyDeliveryOtp(cargoId, otp);
            
            if (verified) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Delivery verified successfully");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Invalid OTP");
                return ResponseEntity.badRequest().body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}