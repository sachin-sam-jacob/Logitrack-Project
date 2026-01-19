package com.edutech.logisticsmanagementandtrackingsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Cargo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name="content")
    private String content;
    
    @Column(name="size")
    private String size;
    
    @Column(name="status")
    private String status; // CREATED, ASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED, CANCELLED
    
    @Column(name="source_location", nullable = false)
    private String sourceLocation;
    
    @Column(name="destination_location", nullable = false)
    private String destinationLocation;
    
    @Column(name="customer_name")
    private String customerName;
    
    @Column(name="customer_contact")
    private String customerContact;
    
    @Column(name="customer_address")
    private String customerAddress;
    
    @Column(name="delivery_proof_url", length = 500)
    private String deliveryProofUrl;
    
    @Column(name="delivery_proof_status")
    private String deliveryProofStatus; // PENDING, APPROVED, REJECTED
    
    @Column(name="delivery_notes", length = 1000)
    private String deliveryNotes;
    
    @Column(name="estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;
    
    @Column(name="actual_delivery_date")
    private LocalDateTime actualDeliveryDate;
    
    @Column(name="created_at")
    private LocalDateTime createdAt;
    
    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "business_id")
    private Business business;
 
    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Cargo() {
        this.status = "CREATED";
    }

    public Cargo(String content, String size, String status, Business business, Driver driver) {
        this.content = content;
        this.size = size;
        this.status = status;
        this.business = business;
        this.driver = driver;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(String sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public String getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(String destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerContact() {
        return customerContact;
    }

    public void setCustomerContact(String customerContact) {
        this.customerContact = customerContact;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getDeliveryProofUrl() {
        return deliveryProofUrl;
    }

    public void setDeliveryProofUrl(String deliveryProofUrl) {
        this.deliveryProofUrl = deliveryProofUrl;
    }

    public String getDeliveryProofStatus() {
        return deliveryProofStatus;
    }

    public void setDeliveryProofStatus(String deliveryProofStatus) {
        this.deliveryProofStatus = deliveryProofStatus;
    }

    public String getDeliveryNotes() {
        return deliveryNotes;
    }

    public void setDeliveryNotes(String deliveryNotes) {
        this.deliveryNotes = deliveryNotes;
    }

    public LocalDateTime getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(LocalDateTime estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public LocalDateTime getActualDeliveryDate() {
        return actualDeliveryDate;
    }

    public void setActualDeliveryDate(LocalDateTime actualDeliveryDate) {
        this.actualDeliveryDate = actualDeliveryDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }
}