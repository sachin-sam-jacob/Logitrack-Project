package com.edutech.logisticsmanagementandtrackingsystem.entity;
 
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
 
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
    private String status; // Status can be 'PENDING', 'IN_TRANSIT', 'DELIVERED'

    @ManyToOne
    private Business business;
 
    @ManyToOne
    private Driver driver;

    public Cargo(String content, String size, String status, Business business, Driver driver) {
        this.content = content;
        this.size = size;
        this.status = status;
        this.business = business;
        this.driver = driver;
    }
 
    public Cargo() {
    }
 
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
 