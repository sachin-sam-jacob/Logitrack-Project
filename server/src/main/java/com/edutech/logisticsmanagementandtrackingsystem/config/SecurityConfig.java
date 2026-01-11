package com.edutech.logisticsmanagementandtrackingsystem.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.edutech.logisticsmanagementandtrackingsystem.jwt.JwtRequestFilter;


public class SecurityConfig extends WebSecurityConfigurerAdapter {
   

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // complete these method to configure the security of the application

        // /api/register and /api/login should be permitted to all
        // /api/business/cargo should be permitted to users with BUSINESS role
        // /api/business/assign-cargo should be permitted to users with BUSINESS role
        // /api/driver/cargo should be permitted to users with DRIVER role
        // /api/driver/update-cargo-status should be permitted to users with DRIVER role
        // /api/customer/cargo-status should be permitted to users with CUSTOMER role
        // all other requests should be authenticated

        // configure jwtRequestFilter to be executed before UsernamePasswordAuthenticationFilter
    }

}