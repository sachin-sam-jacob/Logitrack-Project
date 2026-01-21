package com.edutech.logisticsmanagementandtrackingsystem.service;

import com.edutech.logisticsmanagementandtrackingsystem.dto.CreateCargoRequest;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Business;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Cargo;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Driver;
import com.edutech.logisticsmanagementandtrackingsystem.repository.BusinessRepository;
import com.edutech.logisticsmanagementandtrackingsystem.repository.CargoRepository;
import com.edutech.logisticsmanagementandtrackingsystem.repository.DriverRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class CargoService {

    @Autowired
    private CargoRepository cargoRepo;

    @Autowired
    private BusinessRepository businessRepo;

    @Autowired
    private DriverRepository driverRepo;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private EmailService emailService;

    /**
     * CREATE CARGO with email notification
     */
    public Cargo addCargo(CreateCargoRequest request, String username) {

        Business business = businessRepo.findByname(username)
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));

        Cargo cargo = new Cargo();
        cargo.setBusiness(business);
        cargo.setContent(request.getContent());
        cargo.setSize(request.getSize());
        cargo.setSourceLocation(request.getSourceLocation());
        cargo.setDestinationLocation(request.getDestinationLocation());
        cargo.setCustomerName(request.getCustomerName());
        cargo.setCustomerContact(request.getCustomerContact());
        cargo.setCustomerEmail(request.getCustomerEmail());
        cargo.setCustomerAddress(request.getCustomerAddress());
        cargo.setEstimatedDeliveryDate(request.getEstimatedDeliveryDate());
        cargo.setStatus("CREATED");

        Cargo savedCargo = cargoRepo.save(cargo);

        if (request.getCustomerEmail() != null && !request.getCustomerEmail().trim().isEmpty()) {
            try {
                emailService.sendTrackingEmail(savedCargo);
            } catch (Exception e) {
                System.err.println("Failed to send tracking email: " + e.getMessage());
            }
        }

        return savedCargo;
    }

    /**
     * GET CARGOS BY BUSINESS
     */
    public List<Cargo> getCargoByBusiness(String username) {
        Business business = businessRepo.findByname(username)
                .orElseThrow(() -> new EntityNotFoundException("Business not found"));
        return cargoRepo.findByBusinessId(business.getId());
    }

    /**
     * GET AVAILABLE DRIVERS FOR SOURCE LOCATION
     */
    public List<Driver> getAvailableDriversForLocation(String sourceLocation) {
        if (sourceLocation == null || sourceLocation.trim().isEmpty()) {
            return driverRepo.findAll()
                    .stream()
                    .filter(d -> "APPROVED".equals(d.getVerificationStatus()))
                    .filter(Driver::isAvailable)
                    .collect(Collectors.toList());
        }

        return driverRepo.findAll()
                .stream()
                .filter(d -> "APPROVED".equals(d.getVerificationStatus()))
                .filter(Driver::isAvailable)
                .filter(d -> {
                    String driverLocation = d.getCurrentLocation() != null && !d.getCurrentLocation().trim().isEmpty()
                            ? d.getCurrentLocation().trim()
                            : d.getBaseLocation() != null ? d.getBaseLocation().trim() : "";
                    return driverLocation.equalsIgnoreCase(sourceLocation.trim());
                })
                .collect(Collectors.toList());
    }

    /**
     * ASSIGN CARGO TO DRIVER
     */
    public void assignCargoToDriver(Long cargoId, Long driverId) {
        Cargo cargo = cargoRepo.findById(cargoId)
                .orElseThrow(() -> new EntityNotFoundException("Cargo not found"));

        Driver driver = driverRepo.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found"));

        if (!"APPROVED".equals(driver.getVerificationStatus())) {
            throw new RuntimeException("Driver is not approved");
        }

        if (!driver.isAvailable()) {
            throw new RuntimeException("Driver is not available");
        }

        String driverLocation = driver.getCurrentLocation() != null && !driver.getCurrentLocation().trim().isEmpty()
                ? driver.getCurrentLocation().trim()
                : driver.getBaseLocation() != null ? driver.getBaseLocation().trim() : "";

        String cargoSource = cargo.getSourceLocation() != null ? cargo.getSourceLocation().trim() : "";

        if (!driverLocation.equalsIgnoreCase(cargoSource)) {
            throw new RuntimeException(
                "Driver is not at cargo source location. Driver location: " + driverLocation + 
                ", Cargo source: " + cargoSource
            );
        }

        cargo.setDriver(driver);
        cargo.setStatus("ASSIGNED");
        cargo.setRejectionReason(null);

        cargoRepo.save(cargo);
        
        try {
            emailService.sendCargoAssignmentEmail(cargo, driver);
        } catch (Exception e) {
            System.err.println("Failed to send assignment email: " + e.getMessage());
        }
    }

    /**
     * ACCEPT CARGO BY DRIVER
     */
    public Cargo acceptCargoByDriver(Long cargoId) {
    Cargo cargo = cargoRepo.findById(cargoId)
            .orElseThrow(() -> new EntityNotFoundException("Cargo not found"));

    if (!"ASSIGNED".equals(cargo.getStatus())) {
        throw new RuntimeException("Cargo is not in ASSIGNED status");
    }

    cargo.setStatus("ACCEPTED");
    
    Driver driver = cargo.getDriver();
    if (driver != null) {
        driver.setAvailable(false); // Set to BUSY when accepting cargo
        driverRepo.save(driver);
    }

    Cargo savedCargo = cargoRepo.save(cargo);
    
    try {
        emailService.sendCargoAcceptedEmail(savedCargo);
    } catch (Exception e) {
        System.err.println("Failed to send accepted email: " + e.getMessage());
    }
    
    return savedCargo;
}

    /**
     * REJECT CARGO BY DRIVER
     */
    public void rejectCargoByDriver(Long cargoId, String reason) {
        Cargo cargo = cargoRepo.findById(cargoId)
                .orElseThrow(() -> new EntityNotFoundException("Cargo not found"));

        if (!"ASSIGNED".equals(cargo.getStatus())) {
            throw new RuntimeException("Cargo is not in ASSIGNED status");
        }

        cargo.setStatus("CREATED");
        cargo.setRejectionReason(reason != null ? reason : "Driver rejected the assignment");
        cargo.setDriver(null);

        cargoRepo.save(cargo);
        
        try {
            emailService.sendCargoRejectedEmail(cargo, reason);
        } catch (Exception e) {
            System.err.println("Failed to send rejection email: " + e.getMessage());
        }
    }

    /**
     * UPDATE CARGO STATUS BY DRIVER - FIXED with OTP generation
     */
    public Cargo updateCargoStatus(Long cargoId,
                              String newStatus,
                              String deliveryNotes,
                              String deliveryProofBase64) {

    Cargo cargo = cargoRepo.findById(cargoId)
            .orElseThrow(() -> new EntityNotFoundException("Cargo not found"));

    validateStatusTransition(cargo.getStatus(), newStatus);

    // Generate OTP when driver marks as IN_TRANSIT
    if ("IN_TRANSIT".equals(newStatus) && cargo.getDeliveryOtp() == null) {
        String otp = generateOTP();
        cargo.setDeliveryOtp(otp);
        
        // Send OTP to customer
        try {
            emailService.sendDeliveryOtpEmail(cargo, otp);
        } catch (Exception e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
        }
    }

    cargo.setStatus(newStatus);
    cargo.setDeliveryNotes(deliveryNotes);

    // When driver uploads delivery proof, set status to AWAITING_OTP (NOT DELIVERED)
    if ("DELIVERED".equals(newStatus)
            && deliveryProofBase64 != null
            && !deliveryProofBase64.isEmpty()) {

        String proofUrl = cloudinaryService.uploadBase64Image(
                deliveryProofBase64,
                "logitrack/delivery-proofs"
        );

        cargo.setDeliveryProofUrl(proofUrl);
        cargo.setStatus("AWAITING_OTP"); // Wait for driver to enter OTP
        
        // Don't make driver available yet - wait for OTP verification
    }

    Cargo savedCargo = cargoRepo.save(cargo);
    
    try {
        emailService.sendStatusUpdateEmail(savedCargo);
    } catch (Exception e) {
        System.err.println("Failed to send status update email: " + e.getMessage());
    }
    
    return savedCargo;
}
    /**
     * Generate 6-digit OTP
     */
    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * BUSINESS APPROVES DELIVERY PROOF
     */
    public Cargo approveDeliveryProof(Long cargoId,
                                      String status,
                                      String rejectionReason) {

        Cargo cargo = cargoRepo.findById(cargoId)
                .orElseThrow(() -> new EntityNotFoundException("Cargo not found"));

        if (!"AWAITING_APPROVAL".equals(cargo.getStatus()) && !"AWAITING_OTP".equals(cargo.getStatus())) {
            throw new RuntimeException("Cargo is not awaiting approval");
        }

        cargo.setDeliveryProofStatus(status);

        if ("APPROVED".equals(status)) {
            cargo.setStatus("DELIVERED");
            cargo.setActualDeliveryDate(LocalDateTime.now());

            Driver driver = cargo.getDriver();
            if (driver != null) {
                driver.setCurrentLocation(cargo.getDestinationLocation());
                driver.setAvailable(true);
                driverRepo.save(driver);
            }
        } else if ("REJECTED".equals(status)) {
            cargo.setStatus("IN_TRANSIT");
            cargo.setDeliveryNotes(
                    (cargo.getDeliveryNotes() != null ? cargo.getDeliveryNotes() + "\n" : "")
                            + "Delivery proof rejected: " + rejectionReason
            );
        }

        return cargoRepo.save(cargo);
    }

    /**
     * STATUS TRANSITION VALIDATION - UPDATED
     */
    private void validateStatusTransition(String currentStatus, String newStatus) {
        boolean valid = false;

        switch (currentStatus) {
            case "CREATED":
                valid = "ASSIGNED".equals(newStatus) || "CANCELLED".equals(newStatus);
                break;
            case "ASSIGNED":
                valid = "ACCEPTED".equals(newStatus) || "REJECTED".equals(newStatus) || "CANCELLED".equals(newStatus);
                break;
            case "ACCEPTED":
                valid = "PICKED_UP".equals(newStatus) || "CANCELLED".equals(newStatus);
                break;
            case "PICKED_UP":
                valid = "IN_TRANSIT".equals(newStatus);
                break;
            case "IN_TRANSIT":
                valid = "DELIVERED".equals(newStatus) || "AWAITING_OTP".equals(newStatus);
                break;
            case "AWAITING_OTP":
                valid = "DELIVERED".equals(newStatus) || "IN_TRANSIT".equals(newStatus);
                break;
            case "AWAITING_APPROVAL":
                valid = "DELIVERED".equals(newStatus) || "IN_TRANSIT".equals(newStatus);
                break;
            default:
                valid = false;
        }

        if (!valid) {
            throw new RuntimeException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus
            );
        }
    }

    /**
     * SEARCH BUSINESS CARGOS
     */
    public List<Cargo> searchBusinessCargos(String username, String keyword) {
        Business business = businessRepo.findByname(username)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        Long cargoId = null;
        try {
            cargoId = Long.parseLong(keyword);
        } catch (NumberFormatException ignored) {
        }

        return cargoRepo.searchBusinessCargo(
                business.getId(),
                keyword,
                cargoId
        );
    }

    /**
     * GET BUSINESS CARGOS
     */
    public List<Cargo> getBusinessCargos(String username) {
        Business business = businessRepo.findByname(username)
                .orElseThrow(() -> new RuntimeException("Business not found"));
        return cargoRepo.findByBusinessId(business.getId());
    }
}