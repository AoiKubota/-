package com.example.planvista.repository;

import com.example.planvista.model.entity.RecordEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


public interface RecordRepository {

    List<RecordEntity> getAllByUserId(String userId);

    RecordEntity getById(Integer id, String userId);

    List<RecordEntity> getByTask(String userId, String task);

    List<RecordEntity> getByPeriod(String userId, LocalDateTime startDate, LocalDateTime endDate);

    List<RecordEntity> getTodayRecords(String userId);

    List<RecordEntity> getThisWeekRecords(String userId);

    List<RecordEntity> getThisMonthRecords(String userId);

    Map<String, Long> getTotalTimeByTask(String userId, LocalDateTime startDate, LocalDateTime endDate);

    void create(RecordEntity record);

    void updateById(Integer recordId, RecordEntity record, String userId);

    void deleteById(Integer recordId, String userId);

    void logicalDeleteById(Integer recordId, String userId);

    List<RecordEntity> searchByTask(String userId, String task);
}
