package com.edutech.logisticsmanagementandtrackingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.edutech.logisticsmanagementandtrackingsystem.entity.User;
import com.edutech.logisticsmanagementandtrackingsystem.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

   
 private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public User registerUser(User user) {
        User existingUser = userRepository.findByUsername(user.getUsername());

        if (existingUser != null) {
            return null; 
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false);        
        user.setEmailVerified(false);  

        return userRepository.save(user);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public User getUserByUsername(String username) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        // FIXED: Case-sensitive username check
        if (!user.getUsername().equals(username)) {
            throw new UsernameNotFoundException("Invalid username or password");
        }

        // FIXED: Allow ADMIN to login even without email verification
        if (!"ADMIN".equals(user.getRole()) && !user.isEnabled()) {
            throw new UsernameNotFoundException(
                "Email not verified. Please verify OTP."
            );
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                AuthorityUtils.createAuthorityList(user.getRole())
        );
    }
}