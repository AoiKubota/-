package com.example.planvista.repository;

import com.example.planvista.model.entity.CompanyEntity;

import java.util.List;

public interface CompanyRepository {

    List<CompanyEntity> getAll();

    CompanyEntity getById(Integer id);

    CompanyEntity getByName(String name);

    boolean existsByName(String name);

    void create(CompanyEntity company);

    void updateById(Integer companyId, CompanyEntity company);

    void deleteById(Integer companyId);

    void logicalDeleteById(Integer companyId);

    List<CompanyEntity> searchByName(String name);
}


