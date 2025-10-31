package com.example.planvista.repository;

import com.example.planvista.model.entity.RecordEntity;

import java.time.LocalDateTime;
import java.util.List;


public interface RecordRepository {

    RecordEntity create(RecordEntity record);

    List<RecordEntity> findByUserId(Long userId);

    List<RecordEntity> findByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    List<RecordEntity> findByTaskId(Long taskId);

    List<RecordEntity> findByUserIdAndTaskId(Long userId, Long taskId);

    RecordEntity findByEventId(Long eventId);

    void update(RecordEntity record);

    void delete(Long id);

    RecordEntity findById(Long id);

    List<RecordEntity> findAll();
}