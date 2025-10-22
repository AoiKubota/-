package com.example.planvista.repository;

import com.example.planvista.model.entity.UserEntity;
import org.springframework.jdbc.core.DataClassRowMapper;
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
public class UserRepositoryImpl implements UserRepository {
    

    private final JdbcTemplate jdbcTemplate;
    
    public UserRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public List<UserEntity> getAll() {
        return jdbcTemplate.query(
                "SELECT * FROM " + UserEntity.TABLE_NAME,
                new DataClassRowMapper<>(UserEntity.class)
        );
    }
    
    @Override
    public UserEntity getById(Integer id) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM " + UserEntity.TABLE_NAME + " WHERE id = ?",
                new RowMapper<UserEntity>() {
                    @Override
                    public UserEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapUserEntity(rs);
                    }
                },
                id
        );
    }
    
    @Override
    public UserEntity getByEmail(String email) {
        List<UserEntity> results = jdbcTemplate.query(
                "SELECT * FROM " + UserEntity.TABLE_NAME + " WHERE email = ?",
                new RowMapper<UserEntity>() {
                    @Override
                    public UserEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapUserEntity(rs);
                    }
                },
                email
        );
        return results.isEmpty() ? null : results.get(0);
    }
    
    @Override
    public UserEntity getByUsername(String username) {
        List<UserEntity> results = jdbcTemplate.query(
                "SELECT * FROM " + UserEntity.TABLE_NAME + " WHERE username = ?",
                new RowMapper<UserEntity>() {
                    @Override
                    public UserEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapUserEntity(rs);
                    }
                },
                username
        );
        return results.isEmpty() ? null : results.get(0);
    }
    
    @Override
    public List<UserEntity> getByCompanyId(String companyId) {
        return jdbcTemplate.query(
                "SELECT * FROM " + UserEntity.TABLE_NAME + " WHERE company_id = ?",
                new RowMapper<UserEntity>() {
                    @Override
                    public UserEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapUserEntity(rs);
                    }
                },
                companyId
        );
    }
    
    @Override
    public UserEntity getByEmailAndPassword(String email, String password) {
        List<UserEntity> results = jdbcTemplate.query(
                "SELECT * FROM " + UserEntity.TABLE_NAME + " WHERE email = ? AND password = ?",
                new RowMapper<UserEntity>() {
                    @Override
                    public UserEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapUserEntity(rs);
                    }
                },
                email,
                password
        );
        return results.isEmpty() ? null : results.get(0);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + UserEntity.TABLE_NAME + " WHERE email = ?",
                Integer.class,
                email
        );
        return count != null && count > 0;
    }
    
    @Override
    public boolean existsByUsername(String username) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + UserEntity.TABLE_NAME + " WHERE username = ?",
                Integer.class,
                username
        );
        return count != null && count > 0;
    }
    
    @Override
    @Transactional
    public void create(UserEntity user) {
        jdbcTemplate.update(
                "INSERT INTO " + UserEntity.TABLE_NAME +
                        " (username, email, password, company_id, created_at, updated_at)" +
                        " VALUES (?, ?, ?, ?, ?, ?)",
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getCompanyId(),
                Timestamp.valueOf(LocalDateTime.now()),
                Timestamp.valueOf(LocalDateTime.now())
        );
    }
    
    @Override
    @Transactional
    public void updateById(Integer userId, UserEntity user) {
        jdbcTemplate.update(
                "UPDATE " + UserEntity.TABLE_NAME +
                        " SET username = ?, email = ?, password = ?, company_id = ?, updated_at = ?" +
                        " WHERE id = ?",
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getCompanyId(),
                Timestamp.valueOf(LocalDateTime.now()),
                userId
        );
    }
    
    @Override
    @Transactional
    public void deleteById(Integer userId) {
        jdbcTemplate.update(
                "DELETE FROM " + UserEntity.TABLE_NAME + " WHERE id = ?",
                userId
        );
    }
    
    @Override
    public List<UserEntity> searchByCompanyIdAndUsername(String companyId, String username) {
        return jdbcTemplate.query(
                "SELECT * FROM " + UserEntity.TABLE_NAME +
                        " WHERE company_id = ? AND username LIKE ?" +
                        " ORDER BY created_at DESC",
                new RowMapper<UserEntity>() {
                    @Override
                    public UserEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapUserEntity(rs);
                    }
                },
                companyId,
                "%" + username + "%"
        );
    }
    
    private UserEntity mapUserEntity(ResultSet rs) throws SQLException {
        UserEntity user = new UserEntity();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setCompanyId(rs.getString("company_id"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return user;
    }
}
