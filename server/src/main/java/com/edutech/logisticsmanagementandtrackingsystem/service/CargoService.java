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

    /**
     * CREATE CARGO
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
        cargo.setCustomerAddress(request.getCustomerAddress());
        cargo.setEstimatedDeliveryDate(request.getEstimatedDeliveryDate());
        cargo.setStatus("CREATED");

        return cargoRepo.save(cargo);
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

        return driverRepo.findAll()
                .stream()
                .filter(d -> "APPROVED".equals(d.getVerificationStatus()))
                .filter(Driver::isAvailable)
                .filter(d -> {
                    String driverLocation = d.getCurrentLocation() != null
                            ? d.getCurrentLocation()
                            : d.getBaseLocation();

                    return driverLocation != null &&
                            driverLocation.equalsIgnoreCase(sourceLocation);
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

        String driverLocation = driver.getCurrentLocation() != null
                ? driver.getCurrentLocation()
                : driver.getBaseLocation();

        if (!cargo.getSourceLocation().equalsIgnoreCase(driverLocation)) {
            throw new RuntimeException("Driver is not at cargo source location");
        }

        cargo.setDriver(driver);
        cargo.setStatus("ASSIGNED");

        driver.setAvailable(false);

        driverRepo.save(driver);
        cargoRepo.save(cargo);
    }

    /**
     * UPDATE CARGO STATUS BY DRIVER
     */
    public Cargo updateCargoStatus(Long cargoId,
                                  String newStatus,
                                  String deliveryNotes,
                                  String deliveryProofBase64) {

        Cargo cargo = cargoRepo.findById(cargoId)
                .orElseThrow(() -> new EntityNotFoundException("Cargo not found"));

        validateStatusTransition(cargo.getStatus(), newStatus);

        cargo.setStatus(newStatus);
        cargo.setDeliveryNotes(deliveryNotes);

        if ("DELIVERED".equals(newStatus)
                && deliveryProofBase64 != null
                && !deliveryProofBase64.isEmpty()) {

            String proofUrl = cloudinaryService.uploadBase64Image(
                    deliveryProofBase64,
                    "logitrack/delivery-proofs"
            );

            cargo.setDeliveryProofUrl(proofUrl);
            cargo.setDeliveryProofStatus("PENDING");
            cargo.setStatus("AWAITING_APPROVAL");
        }

        return cargoRepo.save(cargo);
    }

    /**
     * BUSINESS APPROVES DELIVERY PROOF
     */
    public Cargo approveDeliveryProof(Long cargoId,
                                      String status,
                                      String rejectionReason) {

        Cargo cargo = cargoRepo.findById(cargoId)
                .orElseThrow(() -> new EntityNotFoundException("Cargo not found"));

        if (!"AWAITING_APPROVAL".equals(cargo.getStatus())) {
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
     * STATUS TRANSITION VALIDATION (JAVA 8)
     */
    private void validateStatusTransition(String currentStatus, String newStatus) {

        boolean valid = false;

        switch (currentStatus) {

            case "CREATED":
                valid = "ASSIGNED".equals(newStatus) || "CANCELLED".equals(newStatus);
                break;

            case "ASSIGNED":
                valid = "PICKED_UP".equals(newStatus) || "CANCELLED".equals(newStatus);
                break;

            case "PICKED_UP":
                valid = "IN_TRANSIT".equals(newStatus);
                break;

            case "IN_TRANSIT":
                valid = "DELIVERED".equals(newStatus) || "AWAITING_APPROVAL".equals(newStatus);
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
