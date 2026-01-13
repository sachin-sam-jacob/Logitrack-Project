package com.edutech.logisticsmanagementandtrackingsystem.service;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
import com.edutech.logisticsmanagementandtrackingsystem.entity.Cargo;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Driver;
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
    private DriverRepository driverRepo;
 
    public Cargo addCargo(Cargo cargo) {
        return this.cargoRepo.save(cargo);
    }
 
    public Cargo getCargoById(Long cargoId) {
        return this.cargoRepo.findById(cargoId).get();
    }

    public List<Cargo> viewAllCargos() {
        return this.cargoRepo.findAll();
    }
 

    public boolean assignCargoToDriver(Long cargoId, Long driverId) {
        Cargo cargo = cargoRepo.findById(cargoId).orElseThrow(() -> new EntityNotFoundException("Cargo with ID " + cargoId + " not found!"));
        Driver driver = driverRepo.findById(driverId).orElseThrow(() -> new EntityNotFoundException("Driver with ID " + driverId + " not found!"));
        cargo.setStatus("Order Assigned");
        cargo.setDriver(driver);
        cargoRepo.save(cargo);
        return true;
    }
}