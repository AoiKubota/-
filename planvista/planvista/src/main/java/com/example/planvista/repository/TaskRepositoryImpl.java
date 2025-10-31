package com.example.planvista.repository;

import com.example.planvista.model.entity.TaskEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class TaskRepositoryImpl implements TaskRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public TaskRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public List<TaskEntity> findByUserId(Long userId) {
        return jdbcTemplate.query(
                "SELECT * FROM " + TaskEntity.TABLE_NAME +
                        " WHERE user_id = ?" +
                        " ORDER BY task_name ASC",
                new TaskRowMapper(),
                userId
        );
    }
    
    @Override
    public TaskEntity findById(Long id) {
        List<TaskEntity> results = jdbcTemplate.query(
                "SELECT * FROM " + TaskEntity.TABLE_NAME +
                        " WHERE id = ?",
                new TaskRowMapper(),
                id
        );
        return results.isEmpty() ? null : results.get(0);
    }
    
    @Override
    public TaskEntity findByTaskNameAndUserId(String taskName, Long userId) {
        List<TaskEntity> results = jdbcTemplate.query(
                "SELECT * FROM " + TaskEntity.TABLE_NAME +
                        " WHERE task_name = ? AND user_id = ?",
                new TaskRowMapper(),
                taskName,
                userId
        );
        return results.isEmpty() ? null : results.get(0);
    }
    
    @Override
    public boolean existsByTaskNameAndUserId(String taskName, Long userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + TaskEntity.TABLE_NAME +
                        " WHERE task_name = ? AND user_id = ?",
                Integer.class,
                taskName,
                userId
        );
        return count != null && count > 0;
    }
    
    @Override
    @Transactional
    public TaskEntity create(TaskEntity task) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO " + TaskEntity.TABLE_NAME +
                            " (task_name, user_id, created_at, updated_at)" +
                            " VALUES (?, ?, ?, ?)",
                    new String[]{"id"}
            );
            ps.setString(1, task.getTaskName());
            ps.setLong(2, task.getUserId());
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, keyHolder);
        
        task.setId(keyHolder.getKey().longValue());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        
        return task;
    }
    
    @Override
    @Transactional
    public void update(TaskEntity task) {
        jdbcTemplate.update(
                "UPDATE " + TaskEntity.TABLE_NAME +
                        " SET task_name = ?, updated_at = ?" +
                        " WHERE id = ?",
                task.getTaskName(),
                Timestamp.valueOf(LocalDateTime.now()),
                task.getId()
        );
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        jdbcTemplate.update(
                "DELETE FROM " + TaskEntity.TABLE_NAME +
                        " WHERE id = ?",
                id
        );
    }
    
    private static class TaskRowMapper implements RowMapper<TaskEntity> {
        @Override
        public TaskEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            TaskEntity task = new TaskEntity();
            task.setId(rs.getLong("id"));
            task.setTaskName(rs.getString("task_name"));
            task.setUserId(rs.getLong("user_id"));
            task.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            task.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return task;
        }
    }
}
