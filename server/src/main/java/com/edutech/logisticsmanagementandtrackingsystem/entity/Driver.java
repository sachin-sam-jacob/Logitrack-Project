package com.edutech.logisticsmanagementandtrackingsystem.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import javax.persistence.*;
import java.util.List;
 
@Entity
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="name")
    private String name;
    @Column(name="email")
    private String email;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Cargo> assignedCargos;
 

    public Driver() {
    }

    public Driver(String name, String email, List<Cargo> assignedCargos) {
        this.name = name;
        this.email = email;
        this.assignedCargos = assignedCargos;
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
 
    public List<Cargo> getAssignedCargos() {
        return assignedCargos;
    }
 
    public void setAssignedCargos(List<Cargo> assignedCargos) {
        this.assignedCargos = assignedCargos;
    }
}
 