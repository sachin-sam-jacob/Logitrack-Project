package com.edutech.logisticsmanagementandtrackingsystem.Controller;

import com.edutech.logisticsmanagementandtrackingsystem.dto.CreateCargoRequest;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Cargo;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Driver;
import com.edutech.logisticsmanagementandtrackingsystem.service.CargoService;
import com.edutech.logisticsmanagementandtrackingsystem.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    @Autowired
    private CargoService cargoService;

    @Autowired
    private DriverService driverService;

    /**
     * Create cargo with customer details and locations
     */
    @PostMapping("/cargo")
    public ResponseEntity<?> addCargo(
            @RequestBody CreateCargoRequest request,
            Principal principal) {
        try {
            Cargo savedCargo = cargoService.addCargo(request, principal.getName());
            return new ResponseEntity<>(savedCargo, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get all cargos for business
     */
    @GetMapping("/cargo")
    public ResponseEntity<List<Cargo>> viewBusinessCargo(Authentication auth) {
        return ResponseEntity.ok(
            cargoService.getCargoByBusiness(auth.getName())
        );
    }

    /**
     * Get available drivers for specific source location
     */
    @GetMapping("/drivers")
    public ResponseEntity<List<Driver>> getAvailableDrivers(
            @RequestParam(required = false) String sourceLocation) {
        
        if (sourceLocation != null && !sourceLocation.isEmpty()) {
            return ResponseEntity.ok(
                cargoService.getAvailableDriversForLocation(sourceLocation)
            );
        }
        
        return ResponseEntity.ok(driverService.getAllDrivers());
    }

    /**
     * Assign cargo to driver
     */
    @PostMapping("/assign-cargo")
    public ResponseEntity<?> assignCargoToDriver(
            @RequestParam Long cargoId,
            @RequestParam Long driverId) {
        try {
            cargoService.assignCargoToDriver(cargoId, driverId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cargo assigned successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Approve or reject delivery proof
     */
    @PostMapping("/approve-delivery")
    public ResponseEntity<?> approveDeliveryProof(
            @RequestParam Long cargoId,
            @RequestParam String status,
            @RequestParam(required = false) String rejectionReason) {
        try {
            Cargo cargo = cargoService.approveDeliveryProof(cargoId, status, rejectionReason);
            return ResponseEntity.ok(cargo);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * View my cargos
     */
    @GetMapping("/my-cargos")
    public ResponseEntity<List<Cargo>> viewMyCargos(Authentication auth) {
        return ResponseEntity.ok(cargoService.getBusinessCargos(auth.getName()));
    }

    /**
     * Search cargo
     */
    @GetMapping("/search-cargo")
    public ResponseEntity<List<Cargo>> searchCargo(
            @RequestParam String keyword,
            Authentication auth) {
        return ResponseEntity.ok(
            cargoService.searchBusinessCargos(auth.getName(), keyword)
        );
    }

    /**
     * Get cargos awaiting delivery proof approval
     */
    @GetMapping("/pending-approvals")
    public ResponseEntity<List<Cargo>> getPendingApprovals(Authentication auth) {
        List<Cargo> allCargos = cargoService.getBusinessCargos(auth.getName());
        List<Cargo> pendingApprovals = allCargos.stream()
            .filter(c -> "AWAITING_APPROVAL".equals(c.getStatus()))
            .collect(Collectors.toList());   // ✅ Java 8+

        return ResponseEntity.ok(pendingApprovals);
    }
}