package com.edutech.logisticsmanagementandtrackingsystem.dto;

public class GoogleSignInRequest {
    private String credential; // Google ID token
    
    public GoogleSignInRequest() {}
    
    public String getCredential() {
        return credential;
    }
    
    public void setCredential(String credential) {
        this.credential = credential;
    }
}