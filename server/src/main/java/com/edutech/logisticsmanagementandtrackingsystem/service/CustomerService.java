package com.edutech.logisticsmanagementandtrackingsystem.service;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.edutech.logisticsmanagementandtrackingsystem.dto.CargoStatusResponse;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Cargo;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Customer;
import com.edutech.logisticsmanagementandtrackingsystem.repository.CargoRepository;
import com.edutech.logisticsmanagementandtrackingsystem.repository.CustomerRepository;
 
 
@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;
 
    @Autowired
    private CargoRepository cargoRepository;
 
    public Customer createCustomer(Customer customer) {
         return customerRepository.save(customer);
 
    }
 
    public CargoStatusResponse viewCargoStatus(Long cargoId){
        Cargo cargo = (cargoRepository.findById(cargoId)).orElse(null);
        if (cargo != null) {
            return new CargoStatusResponse(cargo.getId(), cargo.getStatus());

        } else {
            throw new EntityNotFoundException("Cargo with this Id doesn't exists.");
        }
 
    }

}
 