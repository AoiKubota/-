package com.example.planvista.repository;

import com.example.planvista.model.entity.RecordEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * RecordRepositoryの実装クラス
 * V1のrecordsテーブル構造(task, start_time, end_time, memo)に対応
 * task_id, task_name, schedule_idカラムがない古いテーブルでも動作
 */
@Repository
public class RecordRepositoryImpl implements RecordRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public RecordEntity create(RecordEntity record) {
        // V1のrecordsテーブル構造に対応 - task_timeカラムを含める
        String sql = "INSERT INTO records (user_id, task, start_time, end_time, task_time, memo, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, record.getUserId());
            // task_nameをtaskカラムに保存
            ps.setString(2, record.getTaskName());
            ps.setTimestamp(3, Timestamp.valueOf(record.getStartTime()));
            ps.setTimestamp(4, Timestamp.valueOf(record.getEndTime()));
            
            // task_timeを計算 (end_time - start_time)
            long durationMinutes = java.time.Duration.between(record.getStartTime(), record.getEndTime()).toMinutes();
            long hours = durationMinutes / 60;
            long minutes = durationMinutes % 60;
            String taskTime = String.format("%02d:%02d:00", hours, minutes);
            ps.setString(5, taskTime);  // TIME型として保存
            
            ps.setString(6, record.getMemo());
            return ps;
        }, keyHolder);
        
        record.setId(keyHolder.getKey().longValue());
        return record;
    }

    @Override
    public List<RecordEntity> findByUserId(Long userId) {
        String sql = "SELECT id, user_id, task as task_name, start_time, end_time, memo, created_at, updated_at " +
                     "FROM records WHERE user_id = ? ORDER BY start_time DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(RecordEntity.class), userId);
    }

    @Override
    public List<RecordEntity> findByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "SELECT id, user_id, task as task_name, start_time, end_time, memo, created_at, updated_at " +
                     "FROM records WHERE user_id = ? AND start_time >= ? AND start_time < ? ORDER BY start_time ASC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(RecordEntity.class), 
                userId, Timestamp.valueOf(startDate), Timestamp.valueOf(endDate));
    }
    
    @Override
    public List<RecordEntity> findByUserIdAndTaskNameAndDateRange(Long userId, String taskName, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "SELECT id, user_id, task as task_name, start_time, end_time, memo, created_at, updated_at " +
                     "FROM records WHERE user_id = ? AND task = ? AND start_time >= ? AND start_time < ? ORDER BY start_time ASC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(RecordEntity.class), 
                userId, taskName, Timestamp.valueOf(startDate), Timestamp.valueOf(endDate));
    }

    @Override
    public List<RecordEntity> findByTaskId(Long taskId) {
        // V1のテーブルにはtask_idカラムがないため、空リストを返す
        System.out.println("Warning: task_id column does not exist in V1 records table");
        return List.of();
    }

    @Override
    public List<RecordEntity> findByUserIdAndTaskId(Long userId, Long taskId) {
        // V1のテーブルにはtask_idカラムがないため、空リストを返す
        System.out.println("Warning: task_id column does not exist in V1 records table");
        return List.of();
    }

    @Override
    public RecordEntity findByScheduleId(Long scheduleId) {
        // V1のテーブルにはschedule_idカラムがないため、nullを返す
        System.out.println("Warning: schedule_id column does not exist in V1 records table");
        return null;
    }

    @Override
    public void update(RecordEntity record) {
        // task_timeを計算
        long durationMinutes = java.time.Duration.between(record.getStartTime(), record.getEndTime()).toMinutes();
        long hours = durationMinutes / 60;
        long minutes = durationMinutes % 60;
        String taskTime = String.format("%02d:%02d:00", hours, minutes);
        
        String sql = "UPDATE records SET task = ?, start_time = ?, end_time = ?, task_time = ?, memo = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        jdbcTemplate.update(sql, 
                record.getTaskName(),  // task_nameをtaskカラムに保存
                Timestamp.valueOf(record.getStartTime()),
                Timestamp.valueOf(record.getEndTime()),
                taskTime,
                record.getMemo(),
                record.getId());
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM records WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public RecordEntity findById(Long id) {
        String sql = "SELECT id, user_id, task as task_name, start_time, end_time, memo, created_at, updated_at " +
                     "FROM records WHERE id = ? LIMIT 1";
        List<RecordEntity> results = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(RecordEntity.class), id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<RecordEntity> findAll() {
        String sql = "SELECT id, user_id, task as task_name, start_time, end_time, memo, created_at, updated_at " +
                     "FROM records ORDER BY start_time DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(RecordEntity.class));
    }
    
    @Override
    public RecordEntity save(RecordEntity record) {
        if (record.getId() == null) {
            return create(record);
        } else {
            update(record);
            return record;
        }
    }
    
    @Override
    public boolean existsById(Long recordId) {
        String sql = "SELECT COUNT(*) FROM records WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, recordId);
        return count != null && count > 0;
    }
}