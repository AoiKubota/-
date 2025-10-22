package com.example.planvista.repository;

import com.example.planvista.model.entity.RecordEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RecordRepositoryImpl implements RecordRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public RecordRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public List<RecordEntity> getAllByUserId(String userId) {
        return jdbcTemplate.query(
                "SELECT * FROM " + RecordEntity.TABLE_NAME +
                        " WHERE user_id = ? AND deleted_at IS NULL" +
                        " ORDER BY start_time DESC",
                new RowMapper<RecordEntity>() {
                    @Override
                    public RecordEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapRecordEntity(rs);
                    }
                },
                userId
        );
    }
    
    @Override
    public RecordEntity getById(Integer id, String userId) {
        List<RecordEntity> results = jdbcTemplate.query(
                "SELECT * FROM " + RecordEntity.TABLE_NAME +
                        " WHERE id = ? AND user_id = ? AND deleted_at IS NULL",
                new RowMapper<RecordEntity>() {
                    @Override
                    public RecordEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapRecordEntity(rs);
                    }
                },
                id,
                userId
        );
        return results.isEmpty() ? null : results.get(0);
    }
    
    @Override
    public List<RecordEntity> getByTask(String userId, String task) {
        return jdbcTemplate.query(
                "SELECT * FROM " + RecordEntity.TABLE_NAME +
                        " WHERE user_id = ? AND task = ? AND deleted_at IS NULL" +
                        " ORDER BY start_time DESC",
                new RowMapper<RecordEntity>() {
                    @Override
                    public RecordEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapRecordEntity(rs);
                    }
                },
                userId,
                task
        );
    }
    
    @Override
    public List<RecordEntity> getByPeriod(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        return jdbcTemplate.query(
                "SELECT * FROM " + RecordEntity.TABLE_NAME +
                        " WHERE user_id = ?" +
                        " AND start_time >= ?" +
                        " AND end_time <= ?" +
                        " AND deleted_at IS NULL" +
                        " ORDER BY start_time ASC",
                new RowMapper<RecordEntity>() {
                    @Override
                    public RecordEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapRecordEntity(rs);
                    }
                },
                userId,
                Timestamp.valueOf(startDate),
                Timestamp.valueOf(endDate)
        );
    }
    
    @Override
    public List<RecordEntity> getTodayRecords(String userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return getByPeriod(userId, startOfDay, endOfDay);
    }
    
    @Override
    public List<RecordEntity> getThisWeekRecords(String userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfWeek = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .atStartOfDay();
        LocalDateTime endOfWeek = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))
                .atTime(LocalTime.MAX);
        return getByPeriod(userId, startOfWeek, endOfWeek);
    }
    
    @Override
    public List<RecordEntity> getThisMonthRecords(String userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        LocalDateTime endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
        return getByPeriod(userId, startOfMonth, endOfMonth);
    }
    
    @Override
    public Map<String, Long> getTotalTimeByTask(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT task, SUM(TIMESTAMPDIFF(SECOND, start_time, end_time)) AS total_seconds ");
        sql.append("FROM ").append(RecordEntity.TABLE_NAME).append(" ");
        sql.append("WHERE user_id = ? AND deleted_at IS NULL ");
        
        List<Object> params = new ArrayList<>();
        params.add(userId);
        
        if (startDate != null) {
            sql.append("AND start_time >= ? ");
            params.add(Timestamp.valueOf(startDate));
        }
        if (endDate != null) {
            sql.append("AND end_time <= ? ");
            params.add(Timestamp.valueOf(endDate));
        }
        
        sql.append("GROUP BY task");
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        
        Map<String, Long> taskTimeMap = new HashMap<>();
        for (Map<String, Object> row : results) {
            String task = (String) row.get("task");
            Long totalSeconds = row.get("total_seconds") != null ? 
                    ((Number) row.get("total_seconds")).longValue() : 0L;
            taskTimeMap.put(task, totalSeconds);
        }
        
        return taskTimeMap;
    }
    
    @Override
    @Transactional
    public void create(RecordEntity record) {
        jdbcTemplate.update(
                "INSERT INTO " + RecordEntity.TABLE_NAME +
                        " (user_id, task, start_time, end_time, task_time, memo, created_at, updated_at)" +
                        " VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                record.getUserId(),
                record.getTask(),
                Timestamp.valueOf(record.getStartTime()),
                Timestamp.valueOf(record.getEndTime()),
                record.getTaskTime(),
                record.getMemo(),
                Timestamp.valueOf(LocalDateTime.now()),
                Timestamp.valueOf(LocalDateTime.now())
        );
    }
    
    @Override
    @Transactional
    public void updateById(Integer recordId, RecordEntity record, String userId) {
        jdbcTemplate.update(
                "UPDATE " + RecordEntity.TABLE_NAME +
                        " SET task = ?, start_time = ?, end_time = ?, task_time = ?, memo = ?, updated_at = ?" +
                        " WHERE id = ? AND user_id = ?",
                record.getTask(),
                Timestamp.valueOf(record.getStartTime()),
                Timestamp.valueOf(record.getEndTime()),
                record.getTaskTime(),
                record.getMemo(),
                Timestamp.valueOf(LocalDateTime.now()),
                recordId,
                userId
        );
    }
    
    @Override
    @Transactional
    public void deleteById(Integer recordId, String userId) {
        jdbcTemplate.update(
                "DELETE FROM " + RecordEntity.TABLE_NAME +
                        " WHERE id = ? AND user_id = ?",
                recordId,
                userId
        );
    }
    
    @Override
    @Transactional
    public void logicalDeleteById(Integer recordId, String userId) {
        jdbcTemplate.update(
                "UPDATE " + RecordEntity.TABLE_NAME +
                        " SET deleted_at = ?" +
                        " WHERE id = ? AND user_id = ?",
                Timestamp.valueOf(LocalDateTime.now()),
                recordId,
                userId
        );
    }
    
    @Override
    public List<RecordEntity> searchByTask(String userId, String task) {
        return jdbcTemplate.query(
                "SELECT * FROM " + RecordEntity.TABLE_NAME +
                        " WHERE user_id = ? AND task LIKE ? AND deleted_at IS NULL" +
                        " ORDER BY start_time DESC",
                new RowMapper<RecordEntity>() {
                    @Override
                    public RecordEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapRecordEntity(rs);
                    }
                },
                userId,
                "%" + task + "%"
        );
    }
    
    private RecordEntity mapRecordEntity(ResultSet rs) throws SQLException {
        RecordEntity record = new RecordEntity();
        record.setId(rs.getInt("id"));
        record.setUserId(rs.getString("user_id"));
        record.setTask(rs.getString("task"));
        record.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        record.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        record.setTaskTime(rs.getTime("task_time").toLocalTime());
        record.setMemo(rs.getString("memo"));
        record.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        record.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        
        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        if (deletedAt != null) {
            record.setDeletedAt(deletedAt.toLocalDateTime());
        }
        
        return record;
    }
}
