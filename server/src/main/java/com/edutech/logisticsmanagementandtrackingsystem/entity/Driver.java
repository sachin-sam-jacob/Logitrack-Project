package com.edutech.logisticsmanagementandtrackingsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.util.List;

@Entity
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name="name")
    private String name;
    
    @Column(name="email")
    private String email;
    
    @Column(name="license_number")
    private String licenseNumber;
    
    @Column(name="vehicle_type")
    private String vehicleType;
    
    @Column(name="vehicle_number")
    private String vehicleNumber;
    
    @Column(name="license_proof_url", length = 500)
    private String licenseProofUrl;
    
    @Column(name="vehicle_rc_url", length = 500)
    private String vehicleRcUrl;
    
    @Column(name="contact_number")
    private String contactNumber;
    
    @Column(name="base_location") // Original registration location
    private String baseLocation;
    
    @Column(name="current_location") // Updated after each delivery
    private String currentLocation;
    
    @Column(name="is_available", nullable = false)
    private boolean isAvailable = false; // Default to false until approved
    
    @Column(name="verification_status")
    private String verificationStatus = "PENDING";
    
    @Column(name="rejection_reason")
    private String rejectionReason;
    
    @Column(name="details_completed", nullable = false)
    private boolean detailsCompleted = false;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Cargo> assignedCargos;

    public Driver() {
    }

    public Driver(String name, String email, List<Cargo> assignedCargos) {
        this.name = name;
        this.email = email;
        this.assignedCargos = assignedCargos;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }
 
    public void setId(Long id) {
       this.id = id;
    }
 
    public String getName() {
        return name;
    }
 
    public void setName(String name) {
        this.name = name;
    }
 
    public String getEmail() {
        return email;
    }
 
    public void setEmail(String email) {
        this.email = email;
    }
 
    public List<Cargo> getAssignedCargos() {
        return assignedCargos;
    }
 
    public void setAssignedCargos(List<Cargo> assignedCargos) {
        this.assignedCargos = assignedCargos;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getLicenseProofUrl() {
        return licenseProofUrl;
    }

    public void setLicenseProofUrl(String licenseProofUrl) {
        this.licenseProofUrl = licenseProofUrl;
    }

    public String getVehicleRcUrl() {
        return vehicleRcUrl;
    }

    public void setVehicleRcUrl(String vehicleRcUrl) {
        this.vehicleRcUrl = vehicleRcUrl;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getBaseLocation() {
        return baseLocation;
    }

    public void setBaseLocation(String baseLocation) {
        this.baseLocation = baseLocation;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public boolean isDetailsCompleted() {
        return detailsCompleted;
    }

    public void setDetailsCompleted(boolean detailsCompleted) {
        this.detailsCompleted = detailsCompleted;
    }
}