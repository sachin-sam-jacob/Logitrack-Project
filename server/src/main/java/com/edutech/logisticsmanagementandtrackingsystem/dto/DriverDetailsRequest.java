package com.edutech.logisticsmanagementandtrackingsystem.dto;

public class DriverDetailsRequest {
    private String username;
    private String licenseNumber;
    private String vehicleType;
    private String vehicleNumber;
    private String licenseProof; // Base64 encoded
    private String vehicleRc; // Base64 encoded
    private String contactNumber;
    private String location; // NEW

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getLicenseProof() {
        return licenseProof;
    }

    public void setLicenseProof(String licenseProof) {
        this.licenseProof = licenseProof;
    }

    public String getVehicleRc() {
        return vehicleRc;
    }

    public void setVehicleRc(String vehicleRc) {
        this.vehicleRc = vehicleRc;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}