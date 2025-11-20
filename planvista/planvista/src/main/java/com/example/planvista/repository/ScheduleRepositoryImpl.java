package com.example.planvista.repository;

import com.example.planvista.model.entity.ScheduleEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * スケジュールリポジトリ実装クラス
 */
@Repository
public class ScheduleRepositoryImpl implements ScheduleRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String TABLE_NAME = "schedules";

    /**
     * ユーザーIDで削除されていないスケジュールを全取得
     */
    @Override
    public List<ScheduleEntity> findByUserIdAndDeletedAtIsNull(Long userId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE user_id = ? AND deleted_at IS NULL ORDER BY start_time";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ScheduleEntity.class), userId);
    }

    /**
     * ユーザーIDとスケジュールIDで削除されていないスケジュールを取得
     */
    @Override
    public Optional<ScheduleEntity> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ? AND user_id = ? AND deleted_at IS NULL";
        List<ScheduleEntity> results = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ScheduleEntity.class), id, userId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * 期間指定でユーザーのスケジュールを取得
     */
    @Override
    public List<ScheduleEntity> findByUserIdAndDateRange(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        String sql = "SELECT * FROM " + TABLE_NAME + " " +
                     "WHERE user_id = ? AND deleted_at IS NULL " +
                     "AND start_time <= ? AND end_time >= ? " +
                     "ORDER BY start_time";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ScheduleEntity.class), userId, endTime, startTime);
    }

    /**
     * 手動登録スケジュールのみを取得
     */
    @Override
    public List<ScheduleEntity> findManualSchedulesByUserId(Long userId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " " +
                     "WHERE user_id = ? AND deleted_at IS NULL AND is_synced_from_google = false " +
                     "ORDER BY start_time";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ScheduleEntity.class), userId);
    }

    /**
     * Google同期スケジュールのみを取得
     */
    @Override
    public List<ScheduleEntity> findGoogleSyncedSchedulesByUserId(Long userId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " " +
                     "WHERE user_id = ? AND deleted_at IS NULL AND is_synced_from_google = true " +
                     "ORDER BY start_time";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ScheduleEntity.class), userId);
    }

    /**
     * GoogleEventIDでスケジュールを検索（重複チェック用）
     */
    @Override
    public Optional<ScheduleEntity> findByGoogleEventId(String googleEventId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE google_event_id = ? AND deleted_at IS NULL";
        List<ScheduleEntity> results = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ScheduleEntity.class), googleEventId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * スケジュールを保存（新規作成または更新）
     */
    @Override
    public ScheduleEntity save(ScheduleEntity schedule) {
        if (schedule.getId() == null) {
            // 新規作成
            return insert(schedule);
        } else {
            // 更新
            return update(schedule);
        }
    }

    /**
     * スケジュールを新規作成
     */
    private ScheduleEntity insert(ScheduleEntity schedule) {
        String sql = "INSERT INTO " + TABLE_NAME + " " +
                     "(user_id, title, start_time, end_time, task, task_time, memo, " +
                     "is_synced_from_google, google_event_id, created_at, updated_at) " +
                     "VALUES (:userId, :title, :startTime, :endTime, :task, :taskTime, :memo, " +
                     ":isSyncedFromGoogle, :googleEventId, :createdAt, :updatedAt)";

        // タイムスタンプを設定
        LocalDateTime now = LocalDateTime.now();
        schedule.setCreatedAt(now);
        schedule.setUpdatedAt(now);

        SqlParameterSource param = new BeanPropertySqlParameterSource(schedule);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        namedParameterJdbcTemplate.update(sql, param, keyHolder);

        // 生成されたIDを設定
        schedule.setId(keyHolder.getKey().longValue());

        return schedule;
    }

    /**
     * スケジュールを更新
     */
    private ScheduleEntity update(ScheduleEntity schedule) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                     "title = :title, start_time = :startTime, end_time = :endTime, " +
                     "task = :task, task_time = :taskTime, memo = :memo, " +
                     "is_synced_from_google = :isSyncedFromGoogle, google_event_id = :googleEventId, " +
                     "updated_at = :updatedAt " +
                     "WHERE id = :id";

        // 更新タイムスタンプを設定
        schedule.setUpdatedAt(LocalDateTime.now());

        SqlParameterSource param = new BeanPropertySqlParameterSource(schedule);
        namedParameterJdbcTemplate.update(sql, param);

        return schedule;
    }

    /**
     * 論理削除
     */
    @Override
    public int logicalDeleteById(Long id, Long userId, LocalDateTime deletedAt) {
        String sql = "UPDATE " + TABLE_NAME + " SET deleted_at = ? WHERE id = ? AND user_id = ?";
        return jdbcTemplate.update(sql, deletedAt, id, userId);
    }

    /**
     * Google同期スケジュールの論理削除（同期解除時）
     */
    @Override
    public int logicalDeleteAllGoogleSyncedSchedules(Long userId, LocalDateTime deletedAt) {
        String sql = "UPDATE " + TABLE_NAME + " SET deleted_at = ? WHERE user_id = ? AND is_synced_from_google = true";
        return jdbcTemplate.update(sql, deletedAt, userId);
    }

    /**
     * 指定ユーザーの指定月のスケジュールを取得（メンバーカレンダー用）
     */
    @Override
    public List<ScheduleEntity> findByUserIdAndMonth(Integer userId, int year, int month) {
        // 月の最初の日と最後の日を取得
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        String sql = "SELECT * FROM " + TABLE_NAME + 
                     " WHERE user_id = ? " +
                     " AND DATE(start_time) >= ? " +
                     " AND DATE(start_time) <= ? " +
                     " AND deleted_at IS NULL " +
                     " ORDER BY start_time";
        
        return jdbcTemplate.query(sql, 
                new BeanPropertyRowMapper<>(ScheduleEntity.class),
                userId,
                startDate,
                endDate);
    }

    /**
     * 指定ユーザーの指定日のスケジュールを取得（メンバーカレンダー用）
     */
    @Override
    public List<ScheduleEntity> findByUserIdAndDate(Integer userId, LocalDate date) {
        String sql = "SELECT * FROM " + TABLE_NAME + 
                     " WHERE user_id = ? " +
                     " AND DATE(start_time) = ? " +
                     " AND deleted_at IS NULL " +
                     " ORDER BY start_time";
        
        return jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(ScheduleEntity.class),
                userId,
                date);
    }
}