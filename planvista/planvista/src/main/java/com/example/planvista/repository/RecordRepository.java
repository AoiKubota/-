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

@Repository
public class RecordRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public RecordEntity create(RecordEntity record) {
        String sql = "INSERT INTO records (user_id, event_id, task_id, task_name, start_time, end_time, memo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, record.getUserId());
            ps.setObject(2, record.getEventId());
            ps.setLong(3, record.getTaskId());
            ps.setString(4, record.getTaskName());
            ps.setTimestamp(5, Timestamp.valueOf(record.getStartTime()));
            ps.setTimestamp(6, Timestamp.valueOf(record.getEndTime()));
            ps.setString(7, record.getMemo());
            return ps;
        }, keyHolder);
        
        record.setId(keyHolder.getKey().longValue());
        return record;
    }

    public List<RecordEntity> findByUserId(Long userId) {
        String sql = "SELECT * FROM records WHERE user_id = ? ORDER BY start_time DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(RecordEntity.class), userId);
    }

    public List<RecordEntity> findByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "SELECT * FROM records WHERE user_id = ? AND start_time >= ? AND start_time < ? ORDER BY start_time ASC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(RecordEntity.class), 
                userId, Timestamp.valueOf(startDate), Timestamp.valueOf(endDate));
    }

    public List<RecordEntity> findByTaskId(Long taskId) {
        String sql = "SELECT * FROM records WHERE task_id = ? ORDER BY start_time DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(RecordEntity.class), taskId);
    }

    public List<RecordEntity> findByUserIdAndTaskId(Long userId, Long taskId) {
        String sql = "SELECT * FROM records WHERE user_id = ? AND task_id = ? ORDER BY start_time DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(RecordEntity.class), userId, taskId);
    }

    public RecordEntity findByEventId(Long eventId) {
        String sql = "SELECT * FROM records WHERE event_id = ? LIMIT 1";
        List<RecordEntity> results = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(RecordEntity.class), eventId);
        return results.isEmpty() ? null : results.get(0);
    }

    public void update(RecordEntity record) {
        String sql = "UPDATE records SET task_id = ?, task_name = ?, start_time = ?, end_time = ?, memo = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        jdbcTemplate.update(sql, 
                record.getTaskId(),
                record.getTaskName(),
                Timestamp.valueOf(record.getStartTime()),
                Timestamp.valueOf(record.getEndTime()),
                record.getMemo(),
                record.getId());
    }

    public void delete(Long id) {
        String sql = "DELETE FROM records WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}