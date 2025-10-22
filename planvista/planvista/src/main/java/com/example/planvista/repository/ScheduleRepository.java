package com.example.planvista.repository;

import com.example.planvista.model.entity.ScheduleEntity;

import java.time.LocalDateTime;
import java.util.List;


public interface ScheduleRepository {

    List<ScheduleEntity> getAllByUserId(String userId);
    

    ScheduleEntity getById(Integer id, String userId);

    List<ScheduleEntity> getByScheduleName(String userId, String scheduleName);

    List<ScheduleEntity> getByPeriod(String userId, LocalDateTime startDate, LocalDateTime endDate);

    List<ScheduleEntity> getUpcomingSchedules(String userId);

    List<ScheduleEntity> getPastSchedules(String userId);

    void create(ScheduleEntity schedule);

    void updateById(Integer scheduleId, ScheduleEntity schedule, String userId);

    void deleteById(Integer scheduleId, String userId);

    void logicalDeleteById(Integer scheduleId, String userId);

    List<ScheduleEntity> searchByScheduleName(String userId, String scheduleName);
    
    List<ScheduleEntity> searchByTask(String userId, String task);
}
