package com.edutech.logisticsmanagementandtrackingsystem.service;
 
 
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
 
import com.edutech.logisticsmanagementandtrackingsystem.entity.User;

import com.edutech.logisticsmanagementandtrackingsystem.repository.UserRepository;
 
import java.util.ArrayList;

import java.util.List;
 
 
@Service

public class UserService implements UserDetailsService {
 
    // Dependency Injections

    @Autowired

    private UserRepository userRepository;
 
    @Autowired

    private PasswordEncoder passwordEncoder;
 
    // If username already present in DB then it will return null or Else save in DB

    public User registerUser(User user) {

        User user1 = userRepository.findByUsername(user.getUsername());

        if (user1 == null) {

            user.setPassword(passwordEncoder.encode(user.getPassword()));

            return userRepository.save(user);

        } else {

            return null;

        }

    }
 
    // Getting User by taking username

    public User getUserByUsername(String username) {

        return userRepository.findByUsername(username);

    }
 
    @Override

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username);

        if (user == null) {

            throw new UsernameNotFoundException("User not found");

        }

        return new org.springframework.security.core.userdetails.User(

                user.getUsername(),

                user.getPassword(),

                new ArrayList<>());

    }

}
 