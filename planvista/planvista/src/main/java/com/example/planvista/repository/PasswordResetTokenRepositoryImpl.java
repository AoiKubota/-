package com.example.planvista.repository;

import com.example.planvista.model.entity.PasswordResetTokenEntity;
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
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;


@Repository
public class PasswordResetTokenRepositoryImpl implements PasswordResetTokenRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public PasswordResetTokenRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    @Transactional
    public void create(PasswordResetTokenEntity token) {
        String sql = "INSERT INTO password_reset_tokens (email, token, expires_at, used, created_at) VALUES (?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, token.getEmail());
            ps.setString(2, token.getToken());
            ps.setTimestamp(3, Timestamp.valueOf(token.getExpiresAt()));
            ps.setBoolean(4, token.getUsed());
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, keyHolder);
        
        token.setId(keyHolder.getKey().longValue());
    }
    
    @Override
    public PasswordResetTokenEntity findByToken(String token) {
        String sql = "SELECT * FROM password_reset_tokens WHERE token = ? LIMIT 1";
        List<PasswordResetTokenEntity> results = jdbcTemplate.query(sql, new TokenRowMapper(), token);
        return results.isEmpty() ? null : results.get(0);
    }
    
    @Override
    public PasswordResetTokenEntity findValidTokenByEmail(String email) {
        String sql = "SELECT * FROM password_reset_tokens WHERE email = ? AND used = FALSE AND expires_at > ? ORDER BY created_at DESC LIMIT 1";
        List<PasswordResetTokenEntity> results = jdbcTemplate.query(sql, new TokenRowMapper(), email, Timestamp.valueOf(LocalDateTime.now()));
        return results.isEmpty() ? null : results.get(0);
    }
    
    @Override
    @Transactional
    public void markAsUsed(Long tokenId) {
        String sql = "UPDATE password_reset_tokens SET used = TRUE WHERE id = ?";
        jdbcTemplate.update(sql, tokenId);
    }
    
    @Override
    @Transactional
    public void deleteExpiredTokens() {
        String sql = "DELETE FROM password_reset_tokens WHERE expires_at < ?";
        jdbcTemplate.update(sql, Timestamp.valueOf(LocalDateTime.now()));
    }
    
    @Override
    @Transactional
    public void invalidateTokensByEmail(String email) {
        String sql = "UPDATE password_reset_tokens SET used = TRUE WHERE email = ? AND used = FALSE";
        jdbcTemplate.update(sql, email);
    }
    
    /**
     * RowMapper実装
     */
    private static class TokenRowMapper implements RowMapper<PasswordResetTokenEntity> {
        @Override
        public PasswordResetTokenEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            PasswordResetTokenEntity token = new PasswordResetTokenEntity();
            token.setId(rs.getLong("id"));
            token.setEmail(rs.getString("email"));
            token.setToken(rs.getString("token"));
            token.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
            token.setUsed(rs.getBoolean("used"));
            token.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return token;
        }
    }
}
