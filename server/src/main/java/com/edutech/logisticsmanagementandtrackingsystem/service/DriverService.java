package com.edutech.logisticsmanagementandtrackingsystem.service;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.edutech.logisticsmanagementandtrackingsystem.dto.DriverDetailsRequest;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Cargo;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Driver;
import com.edutech.logisticsmanagementandtrackingsystem.entity.User;
 
import com.edutech.logisticsmanagementandtrackingsystem.repository.CargoRepository;
import com.edutech.logisticsmanagementandtrackingsystem.repository.DriverRepository;
import com.edutech.logisticsmanagementandtrackingsystem.repository.UserRepository;
 
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
 
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class DriverService {
 
    @Autowired
    private DriverRepository driverRepository;
 
    @Autowired
    private CargoRepository cargoRepository;
 
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudinaryService cloudinaryService; // NEW
 
    public Driver createDriver(Driver driver) {
        return driverRepository.save(driver);
    }
 
    public List<Driver> getAllDrivers() {
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
        Cargo cargo = cargoRepository.findById(cargoId)
                .orElseThrow(() -> new EntityNotFoundException(cargoId + " not found!!"));
 
        cargo.setStatus(newStatus);
        cargoRepository.save(cargo);
        return true;
    }

    // UPDATED METHOD - Upload to Cloudinary
    public Driver updateDriverDetails(DriverDetailsRequest request) {
        User user = userRepository.findByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Driver driver = driverRepository.findByName(request.getUsername());
        if (driver == null) {
            throw new RuntimeException("Driver not found");
        }

        // Update basic details
        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setVehicleType(request.getVehicleType());
        driver.setVehicleNumber(request.getVehicleNumber());
        driver.setContactNumber(request.getContactNumber());
        
        // Upload License Proof to Cloudinary
        if (request.getLicenseProof() != null && !request.getLicenseProof().isEmpty()) {
            // Delete old image from Cloudinary if exists
            if (driver.getLicenseProofUrl() != null && !driver.getLicenseProofUrl().isEmpty()) {
                try {
                    cloudinaryService.deleteImage(driver.getLicenseProofUrl());
                } catch (Exception e) {
                    System.err.println("Failed to delete old license image: " + e.getMessage());
                }
            }
            
            // Upload new image
            String licenseUrl = cloudinaryService.uploadBase64Image(
                request.getLicenseProof(), 
                "logitrack/driver-licenses"
            );
            driver.setLicenseProofUrl(licenseUrl);
        }

        // Upload Vehicle RC to Cloudinary
        if (request.getVehicleRc() != null && !request.getVehicleRc().isEmpty()) {
            // Delete old image from Cloudinary if exists
            if (driver.getVehicleRcUrl() != null && !driver.getVehicleRcUrl().isEmpty()) {
                try {
                    cloudinaryService.deleteImage(driver.getVehicleRcUrl());
                } catch (Exception e) {
                    System.err.println("Failed to delete old RC image: " + e.getMessage());
                }
            }

            // Upload new image
            String rcUrl = cloudinaryService.uploadBase64Image(
                request.getVehicleRc(), 
                "logitrack/vehicle-rc"
            );
            driver.setVehicleRcUrl(rcUrl);
        }
        
        driver.setDetailsCompleted(true);

        return driverRepository.save(driver);
    }

    public boolean isDetailsCompleted(String username) {
        Driver driver = driverRepository.findByName(username);
        if (driver == null) {
            throw new RuntimeException("Driver not found");
        }
        return driver.isDetailsCompleted();
    }
}