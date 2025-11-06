package com.example.planvista.repository;

import com.example.planvista.model.entity.EventEntity;
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
public class EventRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public EventEntity create(EventEntity event) {
        String sql = "INSERT INTO events (title, description, start_time, end_time, user_id, google_event_id, is_synced_from_google) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(event.getStartTime()));
            ps.setTimestamp(4, Timestamp.valueOf(event.getEndTime()));
            ps.setLong(5, event.getUserId());
            ps.setString(6, event.getGoogleEventId());
            ps.setBoolean(7, event.getIsSyncedFromGoogle() != null ? event.getIsSyncedFromGoogle() : false);
            return ps;
        }, keyHolder);
        
        event.setId(keyHolder.getKey().longValue());
        return event;
    }

    public List<EventEntity> findByUserId(Long userId) {
        String sql = "SELECT * FROM events WHERE user_id = ? ORDER BY start_time ASC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(EventEntity.class), userId);
    }

    public EventEntity findByGoogleEventId(String googleEventId) {
        String sql = "SELECT * FROM events WHERE google_event_id = ? LIMIT 1";
        List<EventEntity> results = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(EventEntity.class), googleEventId);
        return results.isEmpty() ? null : results.get(0);
    }

    public boolean existsByGoogleEventId(String googleEventId) {
        String sql = "SELECT COUNT(*) FROM events WHERE google_event_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, googleEventId);
        return count != null && count > 0;
    }

    public List<EventEntity> findByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "SELECT * FROM events WHERE user_id = ? AND start_time >= ? AND start_time < ? ORDER BY start_time ASC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(EventEntity.class), 
                userId, Timestamp.valueOf(startDate), Timestamp.valueOf(endDate));
    }

    public void update(EventEntity event) {
        String sql = "UPDATE events SET title = ?, description = ?, start_time = ?, end_time = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        jdbcTemplate.update(sql, 
                event.getTitle(), 
                event.getDescription(), 
                Timestamp.valueOf(event.getStartTime()),
                Timestamp.valueOf(event.getEndTime()),
                event.getId());
    }

    public void delete(Long id) {
        String sql = "DELETE FROM events WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void deleteGoogleSyncedEventsByUserId(Long userId) {
        String sql = "DELETE FROM events WHERE user_id = ? AND is_synced_from_google = true";
        jdbcTemplate.update(sql, userId);
    }
}
