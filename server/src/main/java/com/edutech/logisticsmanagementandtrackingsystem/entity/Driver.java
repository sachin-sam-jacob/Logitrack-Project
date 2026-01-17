package com.edutech.logisticsmanagementandtrackingsystem.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import javax.persistence.*;
import java.util.List;
 
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    
    // NEW FIELDS
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
    
    @Column(name="details_completed", nullable = false)
    private boolean detailsCompleted = false;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Cargo> assignedCargos;

    // Constructors
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

    public boolean isDetailsCompleted() {
        return detailsCompleted;
    }

    public void setDetailsCompleted(boolean detailsCompleted) {
        this.detailsCompleted = detailsCompleted;
    }
}