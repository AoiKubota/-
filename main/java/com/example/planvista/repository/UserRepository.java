package com.example.planvista.repository;

import com.example.planvista.model.entity.UserEntity;

import java.util.List;

public interface UserRepository {
    

    List<UserEntity> getAll();

    UserEntity getById(Integer id);

    UserEntity getByEmail(String email);

    UserEntity getByUsername(String username);

    List<UserEntity> getByCompanyId(String companyId);

    UserEntity getByEmailAndPassword(String email, String password);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    void create(UserEntity user);

    void updateById(Integer userId, UserEntity user);

    void deleteById(Integer userId);

    List<UserEntity> searchByCompanyIdAndUsername(String companyId, String username);
}
