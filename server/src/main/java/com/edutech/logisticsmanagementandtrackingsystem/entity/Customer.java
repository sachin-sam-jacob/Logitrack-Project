package com.edutech.logisticsmanagementandtrackingsystem.entity;
import javax.persistence.*;

@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name="name")
    private String name;
    
    @Column(name="email")
    private String email;
    
    @Column(name="contact_number")
    private String contactNumber;
    
    @Column(name="alternative_contact_number")
    private String alternativeContactNumber;
    
    @Column(name="address")
    private String address;
    
    @Column(name="location") // ADDED - Fixed missing field
    private String location;
    
    @Column(name="details_completed", nullable = false)
    private boolean detailsCompleted = false;
 
    public Customer(String name, String email) {
        this.name = name;
        this.email = email;
    }
 
    public Customer() {
    }

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

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getAlternativeContactNumber() {
        return alternativeContactNumber;
    }

    public void setAlternativeContactNumber(String alternativeContactNumber) {
        this.alternativeContactNumber = alternativeContactNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isDetailsCompleted() {
        return detailsCompleted;
    }

    public void setDetailsCompleted(boolean detailsCompleted) {
        this.detailsCompleted = detailsCompleted;
    }
}