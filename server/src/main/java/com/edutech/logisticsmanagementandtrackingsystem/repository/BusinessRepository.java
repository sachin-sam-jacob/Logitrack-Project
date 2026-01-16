package com.edutech.logisticsmanagementandtrackingsystem.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edutech.logisticsmanagementandtrackingsystem.entity.Business;
import com.edutech.logisticsmanagementandtrackingsystem.entity.Cargo;

@Repository
public interface BusinessRepository extends JpaRepository<Business,Long> {
        // List<Cargo> findByBusinessId(Long businessId);
        Optional<Business> findByname(String username);

}
