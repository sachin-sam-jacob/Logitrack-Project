package com.edutech.logisticsmanagementandtrackingsystem.dto;

class UpdateCargoStatusRequest {
    private String status;
    private String deliveryNotes;
    private String deliveryProof; // Base64 encoded

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeliveryNotes() {
        return deliveryNotes;
    }

    public void setDeliveryNotes(String deliveryNotes) {
        this.deliveryNotes = deliveryNotes;
    }

    public String getDeliveryProof() {
        return deliveryProof;
    }

    public void setDeliveryProof(String deliveryProof) {
        this.deliveryProof = deliveryProof;
    }
}
