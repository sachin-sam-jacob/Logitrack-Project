package com.edutech.logisticsmanagementandtrackingsystem.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.edutech.logisticsmanagementandtrackingsystem.entity.Cargo;
import com.edutech.logisticsmanagementandtrackingsystem.service.CargoService;
import com.edutech.logisticsmanagementandtrackingsystem.service.DriverService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/driver")
public class DriverController {

    @Autowired
    private DriverService driverService;

    @Autowired
    private CargoService cargoService;

    /**
     * Get assigned cargos for driver
     */
    @GetMapping("/cargo")
    public ResponseEntity<?> viewAssignedCargos(@RequestParam Long driverId) {
        try {
            List<Cargo> cargos = driverService.viewDriverCargos(driverId);
            return new ResponseEntity<>(cargos, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch cargos: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Update cargo status with optional delivery proof
     */
    @PutMapping("/update-cargo-status")
    public ResponseEntity<?> updateCargoStatus(
            @RequestParam Long cargoId, 
            @RequestParam String newStatus,
            @RequestParam(required = false) String deliveryNotes,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String deliveryProof = body != null ? body.get("deliveryProof") : null;
            
            Cargo updatedCargo = cargoService.updateCargoStatus(
                cargoId, 
                newStatus, 
                deliveryNotes,
                deliveryProof
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cargo status updated successfully");
            response.put("cargo", updatedCargo);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to update cargo status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Simple status update (backward compatibility)
     */
    @PutMapping("/update-status")
    public ResponseEntity<?> simpleStatusUpdate(
            @RequestParam Long cargoId,
            @RequestParam String newStatus) {
        try {
            cargoService.updateCargoStatus(cargoId, newStatus, null, null);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cargo status updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Check driver verification status
     */
    @GetMapping("/verification-status")
    public ResponseEntity<?> getVerificationStatus(@RequestParam String username) {
        try {
            var driver = driverService.getDriverDetails(username);
            Map<String, Object> response = new HashMap<>();
            response.put("verificationStatus", driver.getVerificationStatus());
            response.put("isAvailable", driver.isAvailable());
            response.put("rejectionReason", driver.getRejectionReason());
            response.put("currentLocation", driver.getCurrentLocation());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}