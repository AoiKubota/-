package com.example.planvista.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;


@Entity
@Table(name = "users")

public class UserEntity {

    public static final String TABLE_NAME = "users";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "username", nullable = false, length = 10)
    private String username;

    @Column(name = "email", nullable = false, length = 30)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "company_id", nullable = false, length = 10)
    private String companyId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    public UserEntity() {}

    
    public Integer getId(){
        return id;
    }
    public void setId(Integer id){
        this.id = id;
    }

    public String getUsername(){ 
        return username;
    }
    public void setUsername(String username){ 
        this.username = username; 
    }

    public String getEmail(){ 
        return email; 
    }
    public void setEmail(String email){ 
        this.email = email;
    }

    public String getPassword(){ 
        return password; 
    }
    public void setPassword(String password){ 
        this.password = password;
    }

    public String getCompanyId() { 
        return companyId; 
    }
    public void setCompanyId(String companyId) {
         this.companyId = companyId; 
        }

    public LocalDateTime getCreatedAt(){
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt(){
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
