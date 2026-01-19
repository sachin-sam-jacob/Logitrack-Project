package com.edutech.logisticsmanagementandtrackingsystem.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.edutech.logisticsmanagementandtrackingsystem.dto.DriverVerificationRequest;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Driver;
import com.edutech.logisticsmanagementandtrackingsystem.service.BusinessService;
import com.edutech.logisticsmanagementandtrackingsystem.service.CargoService;
import com.edutech.logisticsmanagementandtrackingsystem.service.DriverService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private DriverService driverService;

    @Autowired
    private BusinessService businessService;

    @Autowired
    private CargoService cargoService;

    // Get all pending drivers for verification
    @GetMapping("/pending-drivers")
    public ResponseEntity<List<Driver>> getPendingDrivers() {
        return ResponseEntity.ok(driverService.getPendingDrivers());
    }

    // Get all drivers
    @GetMapping("/all-drivers")
    public ResponseEntity<List<Driver>> getAllDrivers() {
        return ResponseEntity.ok(driverService.getAllDriversForAdmin());
    }

    // Verify driver (approve/reject)
    @PostMapping("/verify-driver")
    public ResponseEntity<Map<String, String>> verifyDriver(
            @RequestBody DriverVerificationRequest request) {
        try {
            driverService.verifyDriver(request);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Driver " + request.getStatus().toLowerCase() + " successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to verify driver: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Dashboard statistics
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<Driver> allDrivers = driverService.getAllDriversForAdmin();
        long pendingDrivers = allDrivers.stream()
                .filter(d -> "PENDING".equals(d.getVerificationStatus()))
                .count();
        long approvedDrivers = allDrivers.stream()
                .filter(d -> "APPROVED".equals(d.getVerificationStatus()))
                .count();
        long rejectedDrivers = allDrivers.stream()
                .filter(d -> "REJECTED".equals(d.getVerificationStatus()))
                .count();
        
        stats.put("totalDrivers", allDrivers.size());
        stats.put("pendingDrivers", pendingDrivers);
        stats.put("approvedDrivers", approvedDrivers);
        stats.put("rejectedDrivers", rejectedDrivers);
        
        return ResponseEntity.ok(stats);
    }
}
