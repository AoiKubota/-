package com.example.planvista.repository;

import com.example.planvista.model.entity.RecordEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作業記録リポジトリインターフェース
 */
public interface RecordRepository {

    /**
     * レコードを作成
     */
    RecordEntity create(RecordEntity record);

    /**
     * ユーザーIDで全レコードを取得
     */
    List<RecordEntity> findByUserId(Long userId);

    /**
     * ユーザーIDと期間でレコードを取得
     */
    List<RecordEntity> findByUserIdAndDateRange(Long userId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * ユーザーID、タスク名、期間でレコードを取得
     * 推測所要時間の計算に使用
     */
    List<RecordEntity> findByUserIdAndTaskNameAndDateRange(Long userId, String taskName, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * タスクIDでレコードを取得
     */
    List<RecordEntity> findByTaskId(Long taskId);

    /**
     * ユーザーIDとタスクIDでレコードを取得
     */
    List<RecordEntity> findByUserIdAndTaskId(Long userId, Long taskId);

    /**
     * スケジュールIDでレコードを取得
     */
    RecordEntity findByScheduleId(Long scheduleId);

    /**
     * レコードを更新
     */
    void update(RecordEntity record);

    /**
     * レコードを削除
     */
    void delete(Long id);

    /**
     * IDでレコードを取得
     */
    RecordEntity findById(Long id);

    /**
     * 全レコードを取得
     */
    List<RecordEntity> findAll();
    
    /**
     * レコードを保存（新規作成または更新）
     */
    RecordEntity save(RecordEntity record);

    /**
     * レコードが存在するかチェック
     */
    boolean existsById(Long recordId);
}