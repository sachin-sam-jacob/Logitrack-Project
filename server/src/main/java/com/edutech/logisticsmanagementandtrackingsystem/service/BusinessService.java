package com.edutech.logisticsmanagementandtrackingsystem.service;
 
 
import java.util.List;
import java.util.Optional;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.edutech.logisticsmanagementandtrackingsystem.dto.BusinessDetailsRequest;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Business;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Cargo;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Driver;
import com.edutech.logisticsmanagementandtrackingsystem.entity.User;
import com.edutech.logisticsmanagementandtrackingsystem.repository.BusinessRepository;
import com.edutech.logisticsmanagementandtrackingsystem.repository.CargoRepository;
import com.edutech.logisticsmanagementandtrackingsystem.repository.DriverRepository;
import com.edutech.logisticsmanagementandtrackingsystem.repository.UserRepository;
 
 
@Service
public class BusinessService {
    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private UserRepository userRepository;
 
    public Business registerBusiness(Business business) {
        return this.businessRepository.save(business);
    }

    public Business updateBusinessDetails(BusinessDetailsRequest request) {
        // Find user
        User user = userRepository.findByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Find business by username
        Business business = businessRepository.findByname(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Business not found"));

        // Update details
        business.setBusinessName(request.getBusinessName());
        business.setLocation(request.getLocation());
        business.setContactNumber(request.getContactNumber());
        business.setBusinessType(request.getBusinessType());
        business.setDetailsCompleted(true);

        return businessRepository.save(business);
    }

    public boolean isDetailsCompleted(String username) {
        Business business = businessRepository.findByname(username)
                .orElseThrow(() -> new RuntimeException("Business not found"));
        return business.isDetailsCompleted();
    }
}
