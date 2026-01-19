package com.edutech.logisticsmanagementandtrackingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.edutech.logisticsmanagementandtrackingsystem.dto.DriverDetailsRequest;
import com.edutech.logisticsmanagementandtrackingsystem.dto.DriverVerificationRequest;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Cargo;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Driver;
import com.edutech.logisticsmanagementandtrackingsystem.entity.User;
import com.edutech.logisticsmanagementandtrackingsystem.repository.CargoRepository;
import com.edutech.logisticsmanagementandtrackingsystem.repository.DriverRepository;
import com.edutech.logisticsmanagementandtrackingsystem.repository.UserRepository;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DriverService {
 
    @Autowired
    private DriverRepository driverRepository;
 
    @Autowired
    private CargoRepository cargoRepository;
 
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudinaryService cloudinaryService;
 
    public Driver createDriver(Driver driver) {
        return driverRepository.save(driver);
    }
 
    /**
     * Get only approved and available drivers
     */
    public List<Driver> getAllDrivers() {
        return driverRepository.findAll().stream()
                .filter(d -> "APPROVED".equals(d.getVerificationStatus()) && d.isAvailable())
                .collect(Collectors.toList());
    }

    /**
     * Get drivers by specific location (current location)
     */
    public List<Driver> getDriversByLocation(String location) {
        return driverRepository.findAll().stream()
                .filter(d -> "APPROVED".equals(d.getVerificationStatus()))
                .filter(Driver::isAvailable)
                .filter(d -> {
                    String driverLocation = d.getCurrentLocation() != null 
                        ? d.getCurrentLocation() 
                        : d.getBaseLocation();
                    return driverLocation != null && 
                           driverLocation.equalsIgnoreCase(location);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get all drivers (for admin)
     */
    public List<Driver> getAllDriversForAdmin() {
        return driverRepository.findAll();
    }

    /**
     * Get pending drivers for verification
     */
    public List<Driver> getPendingDrivers() {
        return driverRepository.findAll().stream()
                .filter(d -> "PENDING".equals(d.getVerificationStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Verify driver (approve/reject) - ONLY ADMIN
     */
    public Driver verifyDriver(DriverVerificationRequest request) {
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        driver.setVerificationStatus(request.getStatus());
        
        if ("REJECTED".equals(request.getStatus())) {
            driver.setRejectionReason(request.getRejectionReason());
            driver.setAvailable(false);
        } else if ("APPROVED".equals(request.getStatus())) {
            // Set current location to base location initially
            if (driver.getCurrentLocation() == null) {
                driver.setCurrentLocation(driver.getBaseLocation());
            }
            driver.setAvailable(true);
            driver.setRejectionReason(null);
        }

        return driverRepository.save(driver);
    }
 
    /**
     * Get cargos assigned to driver
     */
    public List<Cargo> viewDriverCargos(Long driverId) {
        User user = userRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        Driver driver = driverRepository.findByName(user.getUsername());
        if (driver == null) {
            throw new EntityNotFoundException("No driver with such username.");
        }

        // Check if driver is approved
        if (!"APPROVED".equals(driver.getVerificationStatus())) {
            throw new RuntimeException("Driver is not approved yet. Please wait for admin approval.");
        }

        List<Cargo> cargos = cargoRepository.findByDriverId(driver.getId());
        if (cargos == null || cargos.isEmpty()) {
            throw new EntityNotFoundException("No cargos associated with this driver");
        }

        return cargos;
    }
 
    /**
     * Update cargo status - called by driver
     */
    public boolean updateCargoStatus(Long cargoId, String newStatus) {
        Cargo cargo = cargoRepository.findById(cargoId)
                .orElseThrow(() -> new EntityNotFoundException(cargoId + " not found!!"));
 
        // Verify driver is approved
        Driver driver = cargo.getDriver();
        if (driver != null && !"APPROVED".equals(driver.getVerificationStatus())) {
            throw new RuntimeException("Driver is not approved. Cannot update cargo status.");
        }

        cargo.setStatus(newStatus);
        cargoRepository.save(cargo);
        return true;
    }

    /**
     * Update driver details
     */
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
        driver.setBaseLocation(request.getLocation());
        
        // Set current location to base location if not already set
        if (driver.getCurrentLocation() == null) {
            driver.setCurrentLocation(request.getLocation());
        }
        
        // Upload files
        if (request.getLicenseProof() != null && !request.getLicenseProof().isEmpty()) {
            if (driver.getLicenseProofUrl() != null && !driver.getLicenseProofUrl().isEmpty()) {
                try {
                    cloudinaryService.deleteImage(driver.getLicenseProofUrl());
                } catch (Exception e) {
                    System.err.println("Failed to delete old license image: " + e.getMessage());
                }
            }
            
            String licenseUrl = cloudinaryService.uploadBase64Image(
                request.getLicenseProof(), 
                "logitrack/driver-licenses"
            );
            driver.setLicenseProofUrl(licenseUrl);
        }

        if (request.getVehicleRc() != null && !request.getVehicleRc().isEmpty()) {
            if (driver.getVehicleRcUrl() != null && !driver.getVehicleRcUrl().isEmpty()) {
                try {
                    cloudinaryService.deleteImage(driver.getVehicleRcUrl());
                } catch (Exception e) {
                    System.err.println("Failed to delete old RC image: " + e.getMessage());
                }
            }

            String rcUrl = cloudinaryService.uploadBase64Image(
                request.getVehicleRc(), 
                "logitrack/vehicle-rc"
            );
            driver.setVehicleRcUrl(rcUrl);
        }
        
        // Mark as completed if documents are uploaded
        if ((request.getLicenseProof() != null && !request.getLicenseProof().isEmpty()) ||
            (driver.getLicenseProofUrl() != null && !driver.getLicenseProofUrl().isEmpty())) {
            driver.setDetailsCompleted(true);
            
            // Set to PENDING for admin verification (reset if previously rejected)
            if ("REJECTED".equals(driver.getVerificationStatus()) || 
                driver.getVerificationStatus() == null || 
                driver.getVerificationStatus().isEmpty()) {
                driver.setVerificationStatus("PENDING");
                driver.setAvailable(false); // Not available until approved
            }
        }

        return driverRepository.save(driver);
    }

    public boolean isDetailsCompleted(String username) {
        Driver driver = driverRepository.findByName(username);
        if (driver == null) {
            throw new RuntimeException("Driver not found");
        }
        return driver.isDetailsCompleted();
    }

    public Driver getDriverDetails(String username) {
        Driver driver = driverRepository.findByName(username);
        if (driver == null) {
            throw new RuntimeException("Driver not found");
        }
        return driver;
    }

    /**
     * Toggle availability - ONLY for approved drivers
     */
    public Driver toggleAvailability(String username) {
        Driver driver = driverRepository.findByName(username);
        if (driver == null) {
            throw new RuntimeException("Driver not found");
        }

        if (!"APPROVED".equals(driver.getVerificationStatus())) {
            throw new RuntimeException("Only approved drivers can change availability");
        }

        driver.setAvailable(!driver.isAvailable());
        return driverRepository.save(driver);
    }
}