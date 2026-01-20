package com.edutech.logisticsmanagementandtrackingsystem.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edutech.logisticsmanagementandtrackingsystem.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
       User findByUsername(String username);

           User findByEmail(String email);


}
