package com.example.planvista.repository;

import com.example.planvista.model.entity.ScheduleEntity;
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


@Repository
public class ScheduleRepositoryImpl implements ScheduleRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public ScheduleRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public List<ScheduleEntity> getAllByUserId(String userId) {
        return jdbcTemplate.query(
                "SELECT * FROM " + ScheduleEntity.TABLE_NAME +
                        " WHERE user_id = ? AND deleted_at IS NULL" +
                        " ORDER BY start_time ASC",
                new RowMapper<ScheduleEntity>() {
                    @Override
                    public ScheduleEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapScheduleEntity(rs);
                    }
                },
                userId
        );
    }
    
    @Override
    public ScheduleEntity getById(Integer id, String userId) {
        List<ScheduleEntity> results = jdbcTemplate.query(
                "SELECT * FROM " + ScheduleEntity.TABLE_NAME +
                        " WHERE id = ? AND user_id = ? AND deleted_at IS NULL",
                new RowMapper<ScheduleEntity>() {
                    @Override
                    public ScheduleEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapScheduleEntity(rs);
                    }
                },
                id,
                userId
        );
        return results.isEmpty() ? null : results.get(0);
    }
    
    @Override
    public List<ScheduleEntity> getByScheduleName(String userId, String scheduleName) {
        return jdbcTemplate.query(
                "SELECT * FROM " + ScheduleEntity.TABLE_NAME +
                        " WHERE user_id = ? AND schedule_name = ? AND deleted_at IS NULL" +
                        " ORDER BY start_time ASC",
                new RowMapper<ScheduleEntity>() {
                    @Override
                    public ScheduleEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapScheduleEntity(rs);
                    }
                },
                userId,
                scheduleName
        );
    }
    
    @Override
    public List<ScheduleEntity> getByPeriod(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        return jdbcTemplate.query(
                "SELECT * FROM " + ScheduleEntity.TABLE_NAME +
                        " WHERE user_id = ?" +
                        " AND start_time >= ?" +
                        " AND end_time <= ?" +
                        " AND deleted_at IS NULL" +
                        " ORDER BY start_time ASC",
                new RowMapper<ScheduleEntity>() {
                    @Override
                    public ScheduleEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapScheduleEntity(rs);
                    }
                },
                userId,
                Timestamp.valueOf(startDate),
                Timestamp.valueOf(endDate)
        );
    }
    
    @Override
    public List<ScheduleEntity> getUpcomingSchedules(String userId) {
        return jdbcTemplate.query(
                "SELECT * FROM " + ScheduleEntity.TABLE_NAME +
                        " WHERE user_id = ?" +
                        " AND start_time >= ?" +
                        " AND deleted_at IS NULL" +
                        " ORDER BY start_time ASC",
                new RowMapper<ScheduleEntity>() {
                    @Override
                    public ScheduleEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapScheduleEntity(rs);
                    }
                },
                userId,
                Timestamp.valueOf(LocalDateTime.now())
        );
    }
    
    @Override
    public List<ScheduleEntity> getPastSchedules(String userId) {
        return jdbcTemplate.query(
                "SELECT * FROM " + ScheduleEntity.TABLE_NAME +
                        " WHERE user_id = ?" +
                        " AND end_time < ?" +
                        " AND deleted_at IS NULL" +
                        " ORDER BY end_time DESC",
                new RowMapper<ScheduleEntity>() {
                    @Override
                    public ScheduleEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapScheduleEntity(rs);
                    }
                },
                userId,
                Timestamp.valueOf(LocalDateTime.now())
        );
    }
    
    @Override
    @Transactional
    public void create(ScheduleEntity schedule) {
        jdbcTemplate.update(
                "INSERT INTO " + ScheduleEntity.TABLE_NAME +
                        " (user_id, schedule_name, task, start_time, end_time, task_time, memo, created_at, updated_at)" +
                        " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                schedule.getUserId(),
                schedule.getScheduleName(),
                schedule.getTask(),
                Timestamp.valueOf(schedule.getStartTime()),
                Timestamp.valueOf(schedule.getEndTime()),
                schedule.getTaskTime(),
                schedule.getMemo(),
                Timestamp.valueOf(LocalDateTime.now()),
                Timestamp.valueOf(LocalDateTime.now())
        );
    }
    
    @Override
    @Transactional
    public void updateById(Integer scheduleId, ScheduleEntity schedule, String userId) {
        jdbcTemplate.update(
                "UPDATE " + ScheduleEntity.TABLE_NAME +
                        " SET schedule_name = ?, task = ?, start_time = ?, end_time = ?, task_time = ?, memo = ?, updated_at = ?" +
                        " WHERE id = ? AND user_id = ?",
                schedule.getScheduleName(),
                schedule.getTask(),
                Timestamp.valueOf(schedule.getStartTime()),
                Timestamp.valueOf(schedule.getEndTime()),
                schedule.getTaskTime(),
                schedule.getMemo(),
                Timestamp.valueOf(LocalDateTime.now()),
                scheduleId,
                userId
        );
    }
    
    @Override
    @Transactional
    public void deleteById(Integer scheduleId, String userId) {
        jdbcTemplate.update(
                "DELETE FROM " + ScheduleEntity.TABLE_NAME +
                        " WHERE id = ? AND user_id = ?",
                scheduleId,
                userId
        );
    }
    
    @Override
    @Transactional
    public void logicalDeleteById(Integer scheduleId, String userId) {
        jdbcTemplate.update(
                "UPDATE " + ScheduleEntity.TABLE_NAME +
                        " SET deleted_at = ?" +
                        " WHERE id = ? AND user_id = ?",
                Timestamp.valueOf(LocalDateTime.now()),
                scheduleId,
                userId
        );
    }
    
    @Override
    public List<ScheduleEntity> searchByScheduleName(String userId, String scheduleName) {
        return jdbcTemplate.query(
                "SELECT * FROM " + ScheduleEntity.TABLE_NAME +
                        " WHERE user_id = ? AND schedule_name LIKE ? AND deleted_at IS NULL" +
                        " ORDER BY start_time ASC",
                new RowMapper<ScheduleEntity>() {
                    @Override
                    public ScheduleEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapScheduleEntity(rs);
                    }
                },
                userId,
                "%" + scheduleName + "%"
        );
    }
    
    @Override
    public List<ScheduleEntity> searchByTask(String userId, String task) {
        return jdbcTemplate.query(
                "SELECT * FROM " + ScheduleEntity.TABLE_NAME +
                        " WHERE user_id = ? AND task LIKE ? AND deleted_at IS NULL" +
                        " ORDER BY start_time ASC",
                new RowMapper<ScheduleEntity>() {
                    @Override
                    public ScheduleEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapScheduleEntity(rs);
                    }
                },
                userId,
                "%" + task + "%"
        );
    }
    
    private ScheduleEntity mapScheduleEntity(ResultSet rs) throws SQLException {
        ScheduleEntity schedule = new ScheduleEntity();
        schedule.setId(rs.getInt("id"));
        schedule.setUserId(rs.getString("user_id"));
        schedule.setScheduleName(rs.getString("schedule_name"));
        schedule.setTask(rs.getString("task"));
        schedule.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        schedule.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        schedule.setTaskTime(rs.getTime("task_time").toLocalTime());
        schedule.setMemo(rs.getString("memo"));
        schedule.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        schedule.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        
        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        if (deletedAt != null) {
            schedule.setDeletedAt(deletedAt.toLocalDateTime());
        }
        
        return schedule;
    }
}
