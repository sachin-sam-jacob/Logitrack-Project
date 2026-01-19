package com.edutech.logisticsmanagementandtrackingsystem.dto;

public class CustomerDetailsRequest {
    private String username;
    private String contactNumber;
    private String alternativeContactNumber;
    private String address;
    private String location; // ADDED

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
}