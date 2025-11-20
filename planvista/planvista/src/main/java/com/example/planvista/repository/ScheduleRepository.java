package com.example.planvista.repository;

import com.example.planvista.model.entity.ScheduleEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * スケジュールリポジトリインターフェース
 */
public interface ScheduleRepository {

    /**
     * ユーザーIDで削除されていないスケジュールを全取得
     */
    List<ScheduleEntity> findByUserIdAndDeletedAtIsNull(Long userId);

    /**
     * ユーザーIDとスケジュールIDで削除されていないスケジュールを取得
     */
    Optional<ScheduleEntity> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    /**
     * 期間指定でユーザーのスケジュールを取得
     */
    List<ScheduleEntity> findByUserIdAndDateRange(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 手動登録スケジュールのみを取得
     */
    List<ScheduleEntity> findManualSchedulesByUserId(Long userId);

    /**
     * Google同期スケジュールのみを取得
     */
    List<ScheduleEntity> findGoogleSyncedSchedulesByUserId(Long userId);

    /**
     * GoogleEventIDでスケジュールを検索（重複チェック用）
     */
    Optional<ScheduleEntity> findByGoogleEventId(String googleEventId);

    /**
     * スケジュールを保存
     */
    ScheduleEntity save(ScheduleEntity schedule);

    /**
     * 論理削除
     */
    int logicalDeleteById(Long id, Long userId, LocalDateTime deletedAt);

    /**
     * Google同期スケジュールの論理削除（同期解除時）
     */
    int logicalDeleteAllGoogleSyncedSchedules(Long userId, LocalDateTime deletedAt);

    /**
     * 指定ユーザーの指定月のスケジュールを取得（メンバーカレンダー用）
     * @param userId ユーザーID
     * @param year 年
     * @param month 月
     * @return スケジュールリスト
     */
    List<ScheduleEntity> findByUserIdAndMonth(Integer userId, int year, int month);

    /**
     * 指定ユーザーの指定日のスケジュールを取得（メンバーカレンダー用）
     * @param userId ユーザーID
     * @param date 日付
     * @return スケジュールリスト
     */
    List<ScheduleEntity> findByUserIdAndDate(Integer userId, LocalDate date);
}