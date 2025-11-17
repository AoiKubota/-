package com.example.planvista.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * 作業記録エンティティ
 * 実際に行った作業の記録を管理
 */
@Entity
@Table(name = "records")
public class RecordEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * スケジュールID
     * どのスケジュールの実績記録かを示す
     * データベースのカラム名: event_id
     */
    @Column(name = "event_id")
    private Long scheduleId;
    
    @Column(name = "task_id", nullable = false)
    private Long taskId;
    
    @Column(name = "task_name", nullable = false, length = 100)
    private String taskName;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    
    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    /**
     * スケジュールIDを取得
     */
    public Long getScheduleId() {
        return scheduleId;
    }
    
    /**
     * スケジュールIDを設定
     */
    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }
    
    /**
     * レコードIDを取得（idのエイリアス）
     */
    public Long getRecordId() {
        return id;
    }
    
    /**
     * @deprecated eventIdはscheduleIdに変更されました。getScheduleId()を使用してください。
     */
    @Deprecated
    public Long getEventId() {
        return scheduleId;
    }
    
    /**
     * @deprecated eventIdはscheduleIdに変更されました。setScheduleId()を使用してください。
     */
    @Deprecated
    public void setEventId(Long eventId) {
        this.scheduleId = eventId;
    }
    
    public Long getTaskId() {
        return taskId;
    }
    
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
    
    public String getTaskName() {
        return taskName;
    }
    
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public String getMemo() {
        return memo;
    }
    
    public void setMemo(String memo) {
        this.memo = memo;
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

    /**
     * 作業時間（分）を計算
     */
    public long getDurationMinutes() {
        if (startTime != null && endTime != null) {
            return Duration.between(startTime, endTime).toMinutes();
        }
        return 0;
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