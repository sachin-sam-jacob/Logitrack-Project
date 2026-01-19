package com.edutech.logisticsmanagementandtrackingsystem.service;
 
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
import com.edutech.logisticsmanagementandtrackingsystem.dto.CargoStatusResponse;
import com.edutech.logisticsmanagementandtrackingsystem.dto.CustomerDetailsRequest;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Cargo;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Customer;
import com.edutech.logisticsmanagementandtrackingsystem.entity.User;
import com.edutech.logisticsmanagementandtrackingsystem.repository.CargoRepository;
import com.edutech.logisticsmanagementandtrackingsystem.repository.CustomerRepository;
import com.edutech.logisticsmanagementandtrackingsystem.repository.UserRepository;
 
@Service
public class CustomerService {
    
    @Autowired
    private CustomerRepository customerRepository;
 
    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private UserRepository userRepository;
 
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
        customer.setLocation(request.getLocation()); // ADDED
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