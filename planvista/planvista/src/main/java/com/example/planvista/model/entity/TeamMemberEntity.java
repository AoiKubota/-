package com.example.planvista.model.entity;

import java.time.LocalDateTime;

/**
 * チームメンバーエンティティ
 * team_membersテーブルに対応
 */
public class TeamMemberEntity {
    
    public static final String TABLE_NAME = "team_members";
    
    private Integer id;
    private Integer leaderUserId;    // チームリーダーのユーザーID
    private Integer memberUserId;    // メンバーのユーザーID
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // コンストラクタ
    public TeamMemberEntity() {
    }
    
    public TeamMemberEntity(Integer leaderUserId, Integer memberUserId) {
        this.leaderUserId = leaderUserId;
        this.memberUserId = memberUserId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getter and Setter
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getLeaderUserId() {
        return leaderUserId;
    }
    
    public void setLeaderUserId(Integer leaderUserId) {
        this.leaderUserId = leaderUserId;
    }
    
    public Integer getMemberUserId() {
        return memberUserId;
    }
    
    public void setMemberUserId(Integer memberUserId) {
        this.memberUserId = memberUserId;
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
    
    @Override
    public String toString() {
        return "TeamMemberEntity{" +
                "id=" + id +
                ", leaderUserId=" + leaderUserId +
                ", memberUserId=" + memberUserId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}