package com.edutech.logisticsmanagementandtrackingsystem.service;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.edutech.logisticsmanagementandtrackingsystem.entity.Business;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Cargo;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Driver;
import com.edutech.logisticsmanagementandtrackingsystem.repository.BusinessRepository;
import com.edutech.logisticsmanagementandtrackingsystem.repository.CargoRepository;
import com.edutech.logisticsmanagementandtrackingsystem.repository.DriverRepository;
 
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
 
 
@Service
public class CargoService {

    @Autowired
    private CargoRepository cargoRepo;

    @Autowired
    private BusinessRepository businessRepo;

    @Autowired
    private DriverRepository driverRepo;

    public Cargo addCargo(Cargo cargo, String username) {

        Business business = businessRepo.findByname(username)
            .orElseThrow(() -> new EntityNotFoundException("Business not found"));

        cargo.setBusiness(business);
        cargo.setStatus("CREATED");

        return cargoRepo.save(cargo);
    }
    public List<Cargo> getCargoByBusiness(String username) {

        Business business = businessRepo.findByname(username)
            .orElseThrow(() -> new EntityNotFoundException("Business not found"));

        return cargoRepo.findByBusinessId(business.getId());
    }

    public void assignCargoToDriver(Long cargoId, Long driverId) {

        Cargo cargo = cargoRepo.findById(cargoId)
            .orElseThrow(() -> new EntityNotFoundException("Cargo not found"));

        Driver driver = driverRepo.findById(driverId)
            .orElseThrow(() -> new EntityNotFoundException("Driver not found"));

        cargo.setDriver(driver);
        cargo.setStatus("ORDER_ASSIGNED");

        cargoRepo.save(cargo);
    }


    public List<Cargo> getBusinessCargos(String username) {
        Business business = businessRepo.findByname(username)
            .orElseThrow(() -> new RuntimeException("Business not found"));

        return cargoRepo.findByBusinessId(business.getId());
    }

    public List<Cargo> searchBusinessCargos(String username, String keyword) {
        Business business = businessRepo.findByname(username)
            .orElseThrow(() -> new RuntimeException("Business not found"));
        Long cargoId=null;
        try{
            cargoId=Long.parseLong(keyword);
        }
        catch(NumberFormatException nfe){}
        return cargoRepo.searchBusinessCargo(business.getId(), keyword,cargoId);
    }
}
