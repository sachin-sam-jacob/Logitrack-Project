package com.edutech.logisticsmanagementandtrackingsystem.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edutech.logisticsmanagementandtrackingsystem.entity.Cargo;

import java.util.List;


@Repository
public interface CargoRepository extends JpaRepository<Cargo,Long>{
     List<Cargo> findByBusinessId(Long businessId);

    // give a list of assigned cargo based on driver id
    List<Cargo> findByDriverId(Long driverId);
}
