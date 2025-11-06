package com.example.planvista.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * スケジュールエンティティ
 * 手動登録スケジュールとGoogleカレンダー同期スケジュールの両方を管理
 */
@Entity
@Table(name = "schedules")
public class ScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "task", length = 255)
    private String task;

    @Column(name = "task_time")
    private Integer taskTime;

    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    /**
     * Googleカレンダーから同期されたスケジュールかどうか
     * true: Google同期（読み取り専用）
     * false: 手動登録（編集・削除可能）
     */
    @Column(name = "is_synced_from_google", nullable = false)
    private Boolean isSyncedFromGoogle = false;

    /**
     * GoogleカレンダーのイベントID
     * Google同期の場合のみ値が入る
     */
    @Column(name = "google_event_id", length = 255)
    private String googleEventId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // コンストラクタ
    public ScheduleEntity() {
        this.isSyncedFromGoogle = false;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public Integer getTaskTime() {
        return taskTime;
    }

    public void setTaskTime(Integer taskTime) {
        this.taskTime = taskTime;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Boolean getIsSyncedFromGoogle() {
        return isSyncedFromGoogle;
    }

    public void setIsSyncedFromGoogle(Boolean isSyncedFromGoogle) {
        this.isSyncedFromGoogle = isSyncedFromGoogle;
    }

    public String getGoogleEventId() {
        return googleEventId;
    }

    public void setGoogleEventId(String googleEventId) {
        this.googleEventId = googleEventId;
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

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    /**
     * 編集可能かどうかを判定
     * Google同期スケジュールは編集不可
     */
    public boolean isEditable() {
        return !this.isSyncedFromGoogle;
    }

    /**
     * 削除可能かどうかを判定
     * Google同期スケジュールは削除不可
     */
    public boolean isDeletable() {
        return !this.isSyncedFromGoogle;
    }
}