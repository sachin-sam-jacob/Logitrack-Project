package com.edutech.logisticsmanagementandtrackingsystem.Controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.edutech.logisticsmanagementandtrackingsystem.dto.CargoStatusResponse;
import com.edutech.logisticsmanagementandtrackingsystem.service.CustomerService;


@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/cargo-status")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<CargoStatusResponse> viewCargoStatus(@RequestParam Long cargoId) {

        // get cargo status from customer service and return it with status code 200
        // if cargo status is not found, return 404 status c

        CargoStatusResponse cargoStatusResponse = customerService.viewCargoStatus(cargoId);
        if (cargoStatusResponse != null) {
            return new ResponseEntity<CargoStatusResponse>(customerService.viewCargoStatus(cargoId), HttpStatus.OK);
        } else {
            return new ResponseEntity<CargoStatusResponse>(HttpStatus.NOT_FOUND);
        }

    }

}