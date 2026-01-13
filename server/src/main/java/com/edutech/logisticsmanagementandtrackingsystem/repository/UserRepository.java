package com.edutech.logisticsmanagementandtrackingsystem.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edutech.logisticsmanagementandtrackingsystem.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    // extend jpa repository and add custom methods if needed
    User findByUsername(String username);
}
