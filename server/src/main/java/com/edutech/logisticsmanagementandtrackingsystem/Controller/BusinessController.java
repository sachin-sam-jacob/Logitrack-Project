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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    private CargoService cargoService;
    private DriverService driverService;
    private BusinessService businessService;

    //Dependency Injections
    @Autowired
    public void setCargoService(CargoService cargoService) {
        this.cargoService = cargoService;
    }

    @Autowired
    public void setDriverService(DriverService driverService) {
        this.driverService = driverService;
    }

    @Autowired
    public void setBusinessService(BusinessService businessService) {
        this.businessService = businessService;
    }

    public CargoService getCargoService() {
        return cargoService;
    }

    public DriverService getDriverService() {
        return driverService;
    }

    public BusinessService getBusinessService() {
        return businessService;
    }

    @PostMapping("/cargo")
    @PreAuthorize("hasAuthority('BUSINESS')")
    public ResponseEntity<Cargo> addCargo(@RequestBody Cargo cargo) {

        // add cargo and return the added cargo with status code 200
        return new ResponseEntity<Cargo>(this.cargoService.addCargo(cargo), HttpStatus.OK);
    }

    @GetMapping("/cargo-id")
    @PreAuthorize("hasAuthority('BUSINESS')")
    public ResponseEntity<Cargo> getCargoById(@RequestParam Long cargoId) {
        // return list of drivers
        return new ResponseEntity<Cargo>(this.cargoService.getCargoById(cargoId), HttpStatus.OK);
    }

    @GetMapping("/drivers")
    @PreAuthorize("hasAuthority('BUSINESS')")
    public ResponseEntity<List<Driver>> getAllDrivers() {
        // return list of drivers
        return new ResponseEntity<List<Driver>>(this.driverService.getAllDrivers(), HttpStatus.OK);
    }

    @GetMapping("/cargo")
    @PreAuthorize("hasAuthority('BUSINESS')")
    public ResponseEntity<List<Cargo>> viewAllCargos() {
        // return all cargos with status code 200
        return new ResponseEntity<List<Cargo>>(this.cargoService.viewAllCargos(), HttpStatus.OK);
    }

    @PostMapping("/assign-cargo")
    @PreAuthorize("hasAuthority('BUSINESS')")
    public ResponseEntity<String> assignCargoToDriver(@RequestParam Long cargoId, @RequestParam Long driverId) {
        // assign cargo to a driver
        System.out.println("Assigned");
        boolean assignmentSuccess = this.cargoService.assignCargoToDriver(cargoId, driverId);
        if (assignmentSuccess) {
            // if assignment is sucess return with this message
            return ResponseEntity.ok().body("{\"message\": \"Cargo assigned successfully.\"}");
        } else {
            // if assignemnt failed return with this message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to assign cargo.");
        }
    }
}
