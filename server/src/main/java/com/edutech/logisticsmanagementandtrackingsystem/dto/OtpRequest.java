package com.edutech.logisticsmanagementandtrackingsystem.dto;

public class OtpRequest {
    private String username;
    private String otp;
    private String purpose;
    public String getPurpose() {
        return purpose;
    }
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getOtp() {
        return otp;
    }
    public void setOtp(String otp) {
        this.otp = otp;
    }

    
}
