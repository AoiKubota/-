package com.example.planvista.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * パスワードリセットトークンエンティティ
 */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetTokenEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "email", nullable = false, length = 30)
    private String email;
    
    @Column(name = "token", nullable = false, length = 255)
    private String token;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "used", nullable = false)
    private Boolean used = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public Boolean getUsed() {
        return used;
    }
    
    public void setUsed(Boolean used) {
        this.used = used;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    /**
     * トークンが有効期限内かどうかをチェック
     */
    public boolean isValid() {
        return !used && LocalDateTime.now().isBefore(expiresAt);
    }
}