package com.example.planvista.repository;

import com.example.planvista.model.entity.TeamMemberEntity;
import com.example.planvista.model.entity.UserEntity;
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
 * チームメンバーリポジトリ実装
 */
@Repository
public class TeammemberRepositoryImpl implements TeammemberRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public TeammemberRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    @Transactional
    public void create(TeamMemberEntity teamMember) {
        jdbcTemplate.update(
                "INSERT INTO " + TeamMemberEntity.TABLE_NAME +
                        " (leader_user_id, member_user_id, created_at, updated_at)" +
                        " VALUES (?, ?, ?, ?)",
                teamMember.getLeaderUserId(),
                teamMember.getMemberUserId(),
                Timestamp.valueOf(LocalDateTime.now()),
                Timestamp.valueOf(LocalDateTime.now())
        );
    }
    
    @Override
    public List<UserEntity> getMembersByLeader(Integer leaderUserId) {
        return jdbcTemplate.query(
                "SELECT u.* FROM " + UserEntity.TABLE_NAME + " u " +
                        "INNER JOIN " + TeamMemberEntity.TABLE_NAME + " tm ON u.id = tm.member_user_id " +
                        "WHERE tm.leader_user_id = ? " +
                        "ORDER BY u.username",
                new RowMapper<UserEntity>() {
                    @Override
                    public UserEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapUserEntity(rs);
                    }
                },
                leaderUserId
        );
    }
    
    @Override
    public boolean exists(Integer leaderUserId, Integer memberUserId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + TeamMemberEntity.TABLE_NAME +
                        " WHERE leader_user_id = ? AND member_user_id = ?",
                Integer.class,
                leaderUserId,
                memberUserId
        );
        return count != null && count > 0;
    }
    
    @Override
    public boolean canView(Integer leaderUserId, Integer memberUserId) {
        // 自分自身は常に閲覧可能
        if (leaderUserId.equals(memberUserId)) {
            return true;
        }
        return exists(leaderUserId, memberUserId);
    }
    
    @Override
    @Transactional
    public void delete(Integer leaderUserId, Integer memberUserId) {
        jdbcTemplate.update(
                "DELETE FROM " + TeamMemberEntity.TABLE_NAME +
                        " WHERE leader_user_id = ? AND member_user_id = ?",
                leaderUserId,
                memberUserId
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