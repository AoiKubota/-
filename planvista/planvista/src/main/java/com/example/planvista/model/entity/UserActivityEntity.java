package com.example.planvista.model.entity;

import java.time.LocalDateTime;

/**
 * ユーザーアクティビティエンティティ
 */
public class UserActivityEntity {
    
    public static final String TABLE_NAME = "user_activities";
    
    private Long id;
    private Integer userId;
    private String activityType;
    private String activityDescription;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
    
    // コンストラクタ
    public UserActivityEntity() {
    }
    
    public UserActivityEntity(Integer userId, String activityType, String activityDescription) {
        this.userId = userId;
        this.activityType = activityType;
        this.activityDescription = activityDescription;
    }
    
    // Getter/Setter
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getActivityType() {
        return activityType;
    }
    
    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }
    
    public String getActivityDescription() {
        return activityDescription;
    }
    
    public void setActivityDescription(String activityDescription) {
        this.activityDescription = activityDescription;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}