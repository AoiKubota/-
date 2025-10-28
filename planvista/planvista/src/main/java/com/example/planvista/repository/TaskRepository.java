package com.example.planvista.repository;

import com.example.planvista.model.entity.TaskEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class TaskRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public TaskEntity create(TaskEntity task) {
        String sql = "INSERT INTO tasks (task_name, user_id) VALUES (?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, task.getTaskName());
            ps.setLong(2, task.getUserId());
            return ps;
        }, keyHolder);
        
        task.setId(keyHolder.getKey().longValue());
        return task;
    }

    public List<TaskEntity> findByUserId(Long userId) {
        String sql = "SELECT * FROM tasks WHERE user_id = ? ORDER BY task_name ASC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(TaskEntity.class), userId);
    }

    public TaskEntity findById(Long id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        List<TaskEntity> results = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(TaskEntity.class), id);
        return results.isEmpty() ? null : results.get(0);
    }

    public TaskEntity findByTaskNameAndUserId(String taskName, Long userId) {
        String sql = "SELECT * FROM tasks WHERE task_name = ? AND user_id = ?";
        List<TaskEntity> results = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(TaskEntity.class), taskName, userId);
        return results.isEmpty() ? null : results.get(0);
    }

    public boolean existsByTaskNameAndUserId(String taskName, Long userId) {
        String sql = "SELECT COUNT(*) FROM tasks WHERE task_name = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, taskName, userId);
        return count != null && count > 0;
    }

    public void delete(Long id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
