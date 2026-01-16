package com.edutech.logisticsmanagementandtrackingsystem.Controller;


import com.edutech.logisticsmanagementandtrackingsystem.entity.Cargo;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Driver;
import com.edutech.logisticsmanagementandtrackingsystem.service.BusinessService;
import com.edutech.logisticsmanagementandtrackingsystem.service.CargoService;
import com.edutech.logisticsmanagementandtrackingsystem.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    @Autowired
    private CargoService cargoService;

    @Autowired
    private DriverService driverService;

    @PostMapping("/cargo")
    public ResponseEntity<Cargo> addCargo(
            @RequestBody Cargo cargo,
            Principal principal) {

        Cargo savedCargo = cargoService.addCargo(cargo, principal.getName());
        return new ResponseEntity<>(savedCargo, HttpStatus.CREATED);
    }

    @GetMapping("/cargo")
    public ResponseEntity<List<Cargo>> viewBusinessCargo(Principal principal) {
        return ResponseEntity.ok(
            cargoService.getCargoByBusiness(principal.getName())
        );
    }

    @GetMapping("/drivers")
    public ResponseEntity<List<Driver>> getAllDrivers() {
        return ResponseEntity.ok(driverService.getAllDrivers());
    }

    @PostMapping("/assign-cargo")
    public ResponseEntity<String> assignCargoToDriver(
            @RequestParam Long cargoId,
            @RequestParam Long driverId) {

        cargoService.assignCargoToDriver(cargoId, driverId);
        return ResponseEntity.ok("{\"message\":\"Cargo assigned successfully\"}");
    }


    @GetMapping("/my-cargos")
public ResponseEntity<List<Cargo>> viewMyCargos(Authentication auth) {
    return ResponseEntity.ok(cargoService.getBusinessCargos(auth.getName()));
}

@GetMapping("/search-cargo")
public ResponseEntity<List<Cargo>> searchCargo(
        @RequestParam String keyword,
        Authentication auth) {

    return ResponseEntity.ok(
        cargoService.searchBusinessCargos(auth.getName(), keyword)
    );
}
}

