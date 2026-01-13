package com.edutech.logisticsmanagementandtrackingsystem.service;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
import com.edutech.logisticsmanagementandtrackingsystem.entity.Cargo;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Driver;
import com.edutech.logisticsmanagementandtrackingsystem.entity.User;
 
import com.edutech.logisticsmanagementandtrackingsystem.repository.CargoRepository;
import com.edutech.logisticsmanagementandtrackingsystem.repository.DriverRepository;
import com.edutech.logisticsmanagementandtrackingsystem.repository.UserRepository;
 
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
 
 
@Service
public class DriverService {
    @Autowired
    private DriverRepository driverRepository;
 
    @Autowired
    private CargoRepository cargoRepository;
 
    @Autowired
    private UserRepository userRepository;
 
    public Driver createDriver(Driver driver) {
        return driverRepository.save(driver);
    }
 
    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
 
    }
 
    public List<Cargo> viewDriverCargos(Long driverId) {
         return cargoRepository.findByDriverId(driverId);
 
    }
 
    public boolean updateCargoStatus(Long cargoId, String newStatus) {
        Cargo cargo = cargoRepository.findById(cargoId).orElseThrow(() -> new EntityNotFoundException(cargoId + " not found!!"));
 
        cargo.setStatus(newStatus);
        cargoRepository.save(cargo);
        return true;
 
    }
 
}