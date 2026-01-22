package com.edutech.logisticsmanagementandtrackingsystem.service;
 
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
import com.edutech.logisticsmanagementandtrackingsystem.dto.CargoStatusResponse;
import com.edutech.logisticsmanagementandtrackingsystem.dto.CustomerDetailsRequest;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Cargo;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Customer;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Driver;
import com.edutech.logisticsmanagementandtrackingsystem.entity.User;
import com.edutech.logisticsmanagementandtrackingsystem.repository.CargoRepository;
import com.edutech.logisticsmanagementandtrackingsystem.repository.CustomerRepository;
import com.edutech.logisticsmanagementandtrackingsystem.repository.DriverRepository;
import com.edutech.logisticsmanagementandtrackingsystem.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
 
@Service
public class CustomerService {
    

private final CustomerRepository customerRepository;
    private final CargoRepository cargoRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final DriverRepository driverRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository,
                           CargoRepository cargoRepository,
                           UserRepository userRepository,
                           EmailService emailService,
                           DriverRepository driverRepository) {
        this.customerRepository = customerRepository;
        this.cargoRepository = cargoRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.driverRepository = driverRepository;
    }

 
    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }
 
    public CargoStatusResponse viewCargoStatus(Long cargoId) {
        Cargo cargo = cargoRepository.findById(cargoId)
                .orElseThrow(() -> new EntityNotFoundException("Cargo not found"));

        String driverName = cargo.getDriver() != null
                ? cargo.getDriver().getName()
                : "Not Assigned";

        return new CargoStatusResponse(
                cargo.getId(),
                cargo.getContent(),
                cargo.getSize(),
                cargo.getStatus(),
                driverName
        );
    }

    public List<Cargo> getCargosByCustomerEmail(String email) {
        return cargoRepository.findAll().stream()
                .filter(c -> email.equalsIgnoreCase(c.getCustomerEmail()))
                .collect(Collectors.toList());
    }

    public List<Cargo> searchCustomerCargos(String email, String keyword) {
        if (email == null || email.trim().isEmpty()) {
            return cargoRepository.findAll().stream()
                    .filter(c -> {
                        String lowerKeyword = keyword.toLowerCase();
                        return (c.getTrackingNumber() != null && c.getTrackingNumber().toLowerCase().contains(lowerKeyword));
                    })
                    .collect(Collectors.toList());
        }
        
        return cargoRepository.findAll().stream()
                .filter(c -> email.equalsIgnoreCase(c.getCustomerEmail()))
                .filter(c -> {
                    String lowerKeyword = keyword.toLowerCase();
                    return (c.getTrackingNumber() != null && c.getTrackingNumber().toLowerCase().contains(lowerKeyword)) ||
                           (c.getContent() != null && c.getContent().toLowerCase().contains(lowerKeyword)) ||
                           (c.getStatus() != null && c.getStatus().toLowerCase().contains(lowerKeyword));
                })
                .collect(Collectors.toList());
    }

    public Cargo getCargoByTrackingNumber(String trackingNumber) {
        return cargoRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new EntityNotFoundException("Cargo not found with tracking number: " + trackingNumber));
    }

    /**
     * UPDATED: Verify delivery OTP and notify business
     */
    public boolean verifyDeliveryOtp(Long cargoId, String otp) {
    Cargo cargo = cargoRepository.findById(cargoId)
            .orElseThrow(() -> new EntityNotFoundException("Cargo not found"));

    if (cargo.getDeliveryOtp() != null && cargo.getDeliveryOtp().equals(otp)) {
        cargo.setOtpVerified(true);
        cargo.setStatus("DELIVERED");
        cargo.setActualDeliveryDate(LocalDateTime.now());
        
        // Make driver available again after successful delivery
        Driver driver = cargo.getDriver();
        if (driver != null) {
            driver.setCurrentLocation(cargo.getDestinationLocation());
            driver.setAvailable(true); // Driver becomes available after delivery
            driverRepository.save(driver);
        }
        
        cargoRepository.save(cargo);
        
        // Send delivery completion email to business
        try {
            emailService.sendDeliveryCompletionToBusiness(cargo);
        } catch (Exception e) {
            System.err.println("Failed to send delivery completion email to business: " + e.getMessage());
        }
        
        return true;
    }
    return false;
}

    public Customer updateCustomerDetails(CustomerDetailsRequest request) {
        User user = userRepository.findByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Customer customer = customerRepository.findByName(request.getUsername());
        if (customer == null) {
            throw new RuntimeException("Customer not found");
        }

        customer.setContactNumber(request.getContactNumber());
        customer.setAlternativeContactNumber(request.getAlternativeContactNumber());
        customer.setAddress(request.getAddress());
        customer.setLocation(request.getLocation());
        customer.setDetailsCompleted(true);

        return customerRepository.save(customer);
    }

    public boolean isDetailsCompleted(String username) {
        Customer customer = customerRepository.findByName(username);
        if (customer == null) {
            throw new RuntimeException("Customer not found");
        }
        return customer.isDetailsCompleted();
    }

    public Customer getCustomerDetails(String username) {
        Customer customer = customerRepository.findByName(username);
        if (customer == null) {
            throw new RuntimeException("Customer not found");
        }
        return customer;
    }
}