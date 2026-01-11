package com.edutech.logisticsmanagementandtrackingsystem.entity;
 
 
import javax.persistence.*;
@Entity
public class Customer {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
   @Column(name="name",nullable=false)
   private String name;
   @Column(name="email",nullable=false)
   private String email;
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
}