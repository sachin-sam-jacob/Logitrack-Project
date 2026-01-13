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
 
    //funtion to add new cargo
    public Cargo addCargo(Cargo cargo) {
        return this.cargoRepo.save(cargo);
    }
 
    // function to get cargo by id
    public Cargo getCargoById(Long cargoId) {
        return this.cargoRepo.findById(cargoId).get();
    }
        // function to return list of all cargos
    public List<Cargo> viewAllCargos() {
        return this.cargoRepo.findAll();
    }
 
    //function to assign cargo to driver based in their IDs
    public boolean assignCargoToDriver(Long cargoId, Long driverId) {
 
        //check if the cargo exists in database based on cargoID, else throw error
        Cargo cargo = cargoRepo.findById(cargoId).orElseThrow(() -> new EntityNotFoundException("Cargo with ID " + cargoId + " not found!"));
 
        //check if the driver exists in database based on driverID, else throw error
        Driver driver = driverRepo.findById(driverId).orElseThrow(() -> new EntityNotFoundException("Driver with ID " + driverId + " not found!"));
        // if the driver and cargo are found, assign cargo to driver and return true.
        cargo.setStatus("Order Assigned");
        cargo.setDriver(driver);
        cargoRepo.save(cargo);
        return true;
    }
}