package com.example.planvista.repository;

import com.example.planvista.model.entity.UserEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * ユーザーリポジトリ実装
 */
@Repository
public class UserRepositoryImpl implements UserRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public UserRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public UserEntity getById(Integer id) {
        String sql = "SELECT * FROM " + UserEntity.TABLE_NAME + " WHERE id = ?";
        try {
            List<UserEntity> results = jdbcTemplate.query(sql, new UserRowMapper(), id);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public UserEntity findByEmail(String email) {
        String sql = "SELECT * FROM " + UserEntity.TABLE_NAME + " WHERE email = ?";
        try {
            List<UserEntity> results = jdbcTemplate.query(sql, new UserRowMapper(), email);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public UserEntity findByUsername(String username) {
        String sql = "SELECT * FROM " + UserEntity.TABLE_NAME + " WHERE username = ?";
        try {
            List<UserEntity> results = jdbcTemplate.query(sql, new UserRowMapper(), username);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public UserEntity findByEmailAndPassword(String email, String password) {
        String sql = "SELECT * FROM " + UserEntity.TABLE_NAME + " WHERE email = ? AND password = ?";
        try {
            List<UserEntity> results = jdbcTemplate.query(sql, new UserRowMapper(), email, password);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public List<UserEntity> findAll() {
        String sql = "SELECT * FROM " + UserEntity.TABLE_NAME + " ORDER BY id";
        return jdbcTemplate.query(sql, new UserRowMapper());
    }
    
    @Override
    public Integer save(UserEntity user) {
        if (user.getId() == null) {
            // 新規作成
            String sql = "INSERT INTO " + UserEntity.TABLE_NAME +
                        " (username, email, password, company_id, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getCompanyId(),
                Timestamp.valueOf(user.getCreatedAt()),
                Timestamp.valueOf(user.getUpdatedAt())
            );
            
            // 自動生成されたIDを取得
            String idSql = "SELECT LAST_INSERT_ID()";
            return jdbcTemplate.queryForObject(idSql, Integer.class);
        } else {
            // 更新
            String sql = "UPDATE " + UserEntity.TABLE_NAME +
                        " SET username = ?, email = ?, password = ?, company_id = ?, updated_at = ? " +
                        "WHERE id = ?";
            jdbcTemplate.update(sql,
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getCompanyId(),
                Timestamp.valueOf(user.getUpdatedAt()),
                user.getId()
            );
            return user.getId();
        }
    }
    
    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM " + UserEntity.TABLE_NAME + " WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM " + UserEntity.TABLE_NAME + " WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }
    
    /**
     * UserEntity用のRowMapper
     */
    private static class UserRowMapper implements RowMapper<UserEntity> {
        @Override
        public UserEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
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
}