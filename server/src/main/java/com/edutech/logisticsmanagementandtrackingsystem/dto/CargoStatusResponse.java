package com.edutech.logisticsmanagementandtrackingsystem.dto;
public class CargoStatusResponse {

    private Long cargoId;
    private String status;
    
    public CargoStatusResponse(Long cargoId, String status) {
        this.cargoId = cargoId;
        this.status = status;
    }
 
    public Long getCargoId() {
        return cargoId;
    }
 
    public void setCargoId(Long cargoId) {
        this.cargoId = cargoId;
    }
 
    public String getStatus() {
        return status;
    }
 
    public void setStatus(String status) {
        this.status = status;
    }
}
 