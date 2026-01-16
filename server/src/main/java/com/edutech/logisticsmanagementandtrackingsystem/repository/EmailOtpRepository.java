
package com.edutech.logisticsmanagementandtrackingsystem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edutech.logisticsmanagementandtrackingsystem.entity.EmailOtp;
import com.edutech.logisticsmanagementandtrackingsystem.entity.User;

@Repository
public interface EmailOtpRepository extends JpaRepository<EmailOtp,Long>{

    Optional<EmailOtp> findByUser(User user);
}