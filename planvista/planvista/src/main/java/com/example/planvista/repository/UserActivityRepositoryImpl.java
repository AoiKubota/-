package com.example.planvista.repository;

import com.example.planvista.model.entity.UserActivityEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ユーザーアクティビティリポジトリ実装
 */
@Repository
public class UserActivityRepositoryImpl implements UserActivityRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public UserActivityRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    @Transactional
    public void create(UserActivityEntity activity) {
        jdbcTemplate.update(
                "INSERT INTO " + UserActivityEntity.TABLE_NAME +
                        " (user_id, activity_type, activity_description, ip_address, user_agent, created_at)" +
                        " VALUES (?, ?, ?, ?, ?, ?)",
                activity.getUserId(),
                activity.getActivityType(),
                activity.getActivityDescription(),
                activity.getIpAddress(),
                activity.getUserAgent(),
                Timestamp.valueOf(LocalDateTime.now())
        );
    }
    
    @Override
    public List<UserActivityEntity> getByUserId(Integer userId) {
        return jdbcTemplate.query(
                "SELECT * FROM " + UserActivityEntity.TABLE_NAME +
                        " WHERE user_id = ?" +
                        " ORDER BY created_at DESC",
                new RowMapper<UserActivityEntity>() {
                    @Override
                    public UserActivityEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapUserActivityEntity(rs);
                    }
                },
                userId
        );
    }
    
    @Override
    public List<UserActivityEntity> getByUserIdWithLimit(Integer userId, int limit) {
        return jdbcTemplate.query(
                "SELECT * FROM " + UserActivityEntity.TABLE_NAME +
                        " WHERE user_id = ?" +
                        " ORDER BY created_at DESC" +
                        " LIMIT ?",
                new RowMapper<UserActivityEntity>() {
                    @Override
                    public UserActivityEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapUserActivityEntity(rs);
                    }
                },
                userId,
                limit
        );
    }
    
    @Override
    public List<UserActivityEntity> getAll() {
        return jdbcTemplate.query(
                "SELECT * FROM " + UserActivityEntity.TABLE_NAME +
                        " ORDER BY created_at DESC",
                new RowMapper<UserActivityEntity>() {
                    @Override
                    public UserActivityEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapUserActivityEntity(rs);
                    }
                }
        );
    }
    
    private UserActivityEntity mapUserActivityEntity(ResultSet rs) throws SQLException {
        UserActivityEntity activity = new UserActivityEntity();
        activity.setId(rs.getLong("id"));
        activity.setUserId(rs.getInt("user_id"));
        activity.setActivityType(rs.getString("activity_type"));
        activity.setActivityDescription(rs.getString("activity_description"));
        activity.setIpAddress(rs.getString("ip_address"));
        activity.setUserAgent(rs.getString("user_agent"));
        activity.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return activity;
    }
}