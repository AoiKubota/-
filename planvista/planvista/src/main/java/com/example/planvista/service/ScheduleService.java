package com.example.planvista.service;

import com.example.planvista.model.entity.ScheduleEntity;
import com.example.planvista.model.entity.TaskEntity;
import com.example.planvista.repository.ScheduleRepository;
import com.example.planvista.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;
    
    @Autowired
    private TaskRepository taskRepository;

    /**
     * 指定ユーザーの全スケジュールを取得
     */
    public List<ScheduleEntity> getAllSchedules(String userId) {
        return scheduleRepository.getAllByUserId(userId);
    }

    /**
     * スケジュールIDでスケジュールを取得
     */
    public ScheduleEntity getScheduleById(Integer id, String userId) {
        return scheduleRepository.getById(id, userId);
    }

    /**
     * 期間指定でスケジュールを取得
     */
    public List<ScheduleEntity> getSchedulesByPeriod(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        return scheduleRepository.getByPeriod(userId, startDate, endDate);
    }

    /**
     * 今後のスケジュールを取得
     */
    public List<ScheduleEntity> getUpcomingSchedules(String userId) {
        return scheduleRepository.getUpcomingSchedules(userId);
    }

    /**
     * 過去のスケジュールを取得
     */
    public List<ScheduleEntity> getPastSchedules(String userId) {
        return scheduleRepository.getPastSchedules(userId);
    }

    /**
     * スケジュール名で検索
     */
    public List<ScheduleEntity> searchByScheduleName(String userId, String scheduleName) {
        return scheduleRepository.searchByScheduleName(userId, scheduleName);
    }

    /**
     * タスク名で検索
     */
    public List<ScheduleEntity> searchByTask(String userId, String task) {
        return scheduleRepository.searchByTask(userId, task);
    }

    /**
     * スケジュールを新規作成
     */
    @Transactional
    public void createSchedule(ScheduleEntity schedule) {
        // task_timeを計算
        LocalTime taskTime = calculateTaskTime(schedule.getStartTime(), schedule.getEndTime());
        schedule.setTaskTime(taskTime);
        
        scheduleRepository.create(schedule);
    }

    /**
     * スケジュールを更新
     */
    @Transactional
    public void updateSchedule(Integer scheduleId, ScheduleEntity schedule, String userId) {
        // task_timeを再計算
        LocalTime taskTime = calculateTaskTime(schedule.getStartTime(), schedule.getEndTime());
        schedule.setTaskTime(taskTime);
        
        scheduleRepository.updateById(scheduleId, schedule, userId);
    }

    /**
     * スケジュールを削除（物理削除）
     */
    @Transactional
    public void deleteSchedule(Integer scheduleId, String userId) {
        scheduleRepository.deleteById(scheduleId, userId);
    }

    /**
     * スケジュールを削除（論理削除）
     */
    @Transactional
    public void logicalDeleteSchedule(Integer scheduleId, String userId) {
        scheduleRepository.logicalDeleteById(scheduleId, userId);
    }

    /**
     * ユーザーのタスク一覧を取得
     */
    public List<TaskEntity> getUserTasks(Long userId) {
        return taskRepository.findByUserId(userId);
    }

    /**
     * 新しいタスクを作成
     */
    @Transactional
    public TaskEntity createTask(String taskName, Long userId) {
        // 既に存在するタスク名かチェック
        if (taskRepository.existsByTaskNameAndUserId(taskName, userId)) {
            throw new IllegalArgumentException("このタスク名は既に登録されています");
        }
        
        TaskEntity task = new TaskEntity();
        task.setTaskName(taskName);
        task.setUserId(userId);
        
        return taskRepository.create(task);
    }

    /**
     * タスクの推測所要時間を取得（AI分析用）
     */
    public Map<String, String> getEstimatedTaskTime(String userId, String taskName) {
        // 過去の同じタスクの平均時間を計算
        List<ScheduleEntity> pastSchedules = scheduleRepository.searchByTask(userId, taskName);
        
        Map<String, String> result = new HashMap<>();
        
        if (pastSchedules.isEmpty()) {
            result.put("estimatedTime", "00:00");
            result.put("sampleCount", "0");
            return result;
        }
        
        long totalMinutes = 0;
        for (ScheduleEntity schedule : pastSchedules) {
            Duration duration = Duration.between(schedule.getStartTime(), schedule.getEndTime());
            totalMinutes += duration.toMinutes();
        }
        
        long avgMinutes = totalMinutes / pastSchedules.size();
        long hours = avgMinutes / 60;
        long minutes = avgMinutes % 60;
        
        result.put("estimatedTime", String.format("%02d:%02d", hours, minutes));
        result.put("sampleCount", String.valueOf(pastSchedules.size()));
        
        return result;
    }

    /**
     * 開始時刻と終了時刻から所要時間を計算
     */
    private LocalTime calculateTaskTime(LocalDateTime startTime, LocalDateTime endTime) {
        Duration duration = Duration.between(startTime, endTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        
        return LocalTime.of((int) hours, (int) minutes);
    }

    /**
     * スケジュールの検証
     */
    public boolean validateSchedule(ScheduleEntity schedule) {
        // 終了時刻が開始時刻より後かチェック
        if (schedule.getEndTime().isBefore(schedule.getStartTime())) {
            return false;
        }
        
        // 必須項目のチェック
        if (schedule.getScheduleName() == null || schedule.getScheduleName().trim().isEmpty()) {
            return false;
        }
        
        if (schedule.getTask() == null || schedule.getTask().trim().isEmpty()) {
            return false;
        }
        
        return true;
    }
}