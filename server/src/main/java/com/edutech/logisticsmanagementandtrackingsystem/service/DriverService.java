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
 
    // Dependency Injections
    @Autowired
    private DriverRepository driverRepository;
 
    @Autowired
    private CargoRepository cargoRepository;
 
    @Autowired
    private UserRepository userRepository;
 
    public Driver createDriver(Driver driver) {
        // adding driver to database and return driver
        return driverRepository.save(driver);
    }
 
    public List<Driver> getAllDrivers() {
        // returning list of drivers from database
        return driverRepository.findAll();
 
    }
 
    public List<Cargo> viewDriverCargos(Long driverId) {
        User user = userRepository.findById(driverId).get();
        Driver driver = driverRepository.findByName(user.getUsername());
        if(driver == null)
            throw new EntityNotFoundException("No driver with such username.");
        if(cargoRepository.findByDriverId(driver.getId()) != null)
            return cargoRepository.findByDriverId(driver.getId());
        else
            throw new EntityNotFoundException("No cargos associated with this driver");
 
    }
 
    public boolean updateCargoStatus(Long cargoId, String newStatus) {
        // update cargo status in database
        Cargo cargo = cargoRepository.findById(cargoId).orElseThrow(() -> new EntityNotFoundException(cargoId + " not found!!"));
 
        cargo.setStatus(newStatus);
        cargoRepository.save(cargo);
        return true;
 
    }
 
}