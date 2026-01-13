package com.edutech.logisticsmanagementandtrackingsystem.service;
 
 
import java.util.List;
import java.util.Optional;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
import com.edutech.logisticsmanagementandtrackingsystem.entity.Business;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Cargo;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Driver;
import com.edutech.logisticsmanagementandtrackingsystem.repository.BusinessRepository;
import com.edutech.logisticsmanagementandtrackingsystem.repository.CargoRepository;
import com.edutech.logisticsmanagementandtrackingsystem.repository.DriverRepository;
 
 
@Service
public class BusinessService {
    //Dependency Injection
    @Autowired
    private BusinessRepository businessRepository;
 
    public Business registerBusiness(Business business) {
        // save business to database
        return this.businessRepository.save(business);
    }
}
