package com.example.planvista.repository;

import com.example.planvista.model.entity.CompanyEntity;
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
public class CompanyRepositoryImpl implements CompanyRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public CompanyRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public List<CompanyEntity> getAll() {
        return jdbcTemplate.query(
                "SELECT * FROM " + CompanyEntity.TABLE_NAME + " WHERE deleted_at IS NULL",
                new RowMapper<CompanyEntity>() {
                    @Override
                    public CompanyEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapCompanyEntity(rs);
                    }
                }
        );
    }
    
    @Override
    public CompanyEntity getById(Integer id) {
        List<CompanyEntity> results = jdbcTemplate.query(
                "SELECT * FROM " + CompanyEntity.TABLE_NAME + " WHERE id = ? AND deleted_at IS NULL",
                new RowMapper<CompanyEntity>() {
                    @Override
                    public CompanyEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapCompanyEntity(rs);
                    }
                },
                id
        );
        return results.isEmpty() ? null : results.get(0);
    }
    
    @Override
    public CompanyEntity getByName(String name) {
        List<CompanyEntity> results = jdbcTemplate.query(
                "SELECT * FROM " + CompanyEntity.TABLE_NAME + " WHERE name = ? AND deleted_at IS NULL",
                new RowMapper<CompanyEntity>() {
                    @Override
                    public CompanyEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapCompanyEntity(rs);
                    }
                },
                name
        );
        return results.isEmpty() ? null : results.get(0);
    }
    
    @Override
    public boolean existsByName(String name) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + CompanyEntity.TABLE_NAME + 
                        " WHERE name = ? AND deleted_at IS NULL",
                Integer.class,
                name
        );
        return count != null && count > 0;
    }
    
    @Override
    @Transactional
    public void create(CompanyEntity company) {
        jdbcTemplate.update(
                "INSERT INTO " + CompanyEntity.TABLE_NAME +
                        " (name, created_at)" +
                        " VALUES (?, ?)",
                company.getName(),
                Timestamp.valueOf(LocalDateTime.now())
        );
    }
    
    @Override
    @Transactional
    public void updateById(Integer companyId, CompanyEntity company) {
        jdbcTemplate.update(
                "UPDATE " + CompanyEntity.TABLE_NAME +
                        " SET name = ?" +
                        " WHERE id = ? AND deleted_at IS NULL",
                company.getName(),
                companyId
        );
    }
    
    @Override
    @Transactional
    public void deleteById(Integer companyId) {
        jdbcTemplate.update(
                "DELETE FROM " + CompanyEntity.TABLE_NAME + " WHERE id = ?",
                companyId
        );
    }
    
    @Override
    @Transactional
    public void logicalDeleteById(Integer companyId) {
        jdbcTemplate.update(
                "UPDATE " + CompanyEntity.TABLE_NAME +
                        " SET deleted_at = ?" +
                        " WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.now()),
                companyId
        );
    }
    
    @Override
    public List<CompanyEntity> searchByName(String name) {
        return jdbcTemplate.query(
                "SELECT * FROM " + CompanyEntity.TABLE_NAME +
                        " WHERE name LIKE ? AND deleted_at IS NULL" +
                        " ORDER BY created_at DESC",
                new RowMapper<CompanyEntity>() {
                    @Override
                    public CompanyEntity mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                        return mapCompanyEntity(rs);
                    }
                },
                "%" + name + "%"
        );
    }
    

    private CompanyEntity mapCompanyEntity(ResultSet rs) throws SQLException {
        CompanyEntity company = new CompanyEntity();
        company.setId(rs.getInt("id"));
        company.setName(rs.getString("name"));
        company.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        if (deletedAt != null) {
            company.setDeletedAt(deletedAt.toLocalDateTime());
        }
        
        return company;
    }
}
