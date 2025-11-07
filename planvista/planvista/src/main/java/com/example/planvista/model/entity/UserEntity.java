package com.example.planvista.model.entity;

import java.time.LocalDateTime;

/**
 * ユーザーエンティティ
 */
public class UserEntity {
    
    public static final String TABLE_NAME = "users";
    
    private Integer id;
    private String username;
    private String email;
    private String password;
    private String companyId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // コンストラクタ
    public UserEntity() {
    }
    
    // Getter/Setter
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getCompanyId() {
        return companyId;
    }
    
    public void setCompanyId(String companyId) {
        this.companyId = companyId;
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
}