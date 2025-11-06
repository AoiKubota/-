package com.example.planvista.service;

import com.example.planvista.model.entity.ScheduleEntity;
import com.example.planvista.model.entity.TaskEntity;
import com.example.planvista.model.entity.RecordEntity;
import com.example.planvista.repository.ScheduleRepository;
import com.example.planvista.repository.TaskRepository;
import com.example.planvista.repository.RecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private RecordRepository recordRepository;
    
    // デフォルトタスク名
    private static final String DEFAULT_TASK_NAME = "その他";

    /**
     * ユーザーの全スケジュールを取得
     */
    public List<ScheduleEntity> getAllSchedules(Long userId) {
        return scheduleRepository.findByUserIdAndDeletedAtIsNull(userId);
    }

    /**
     * 期間を指定してスケジュールを取得
     */
    public List<ScheduleEntity> getSchedulesByDateRange(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return scheduleRepository.findByUserIdAndDateRange(userId, startTime, endTime);
    }

    /**
     * 手動登録スケジュールのみを取得
     */
    public List<ScheduleEntity> getManualSchedules(Long userId) {
        return scheduleRepository.findManualSchedulesByUserId(userId);
    }

    /**
     * Google同期スケジュールのみを取得
     */
    public List<ScheduleEntity> getGoogleSyncedSchedules(Long userId) {
        return scheduleRepository.findGoogleSyncedSchedulesByUserId(userId);
    }

    /**
     * スケジュールをIDで取得
     */
    public Optional<ScheduleEntity> getScheduleById(Long id, Long userId) {
        return scheduleRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId);
    }

    /**
     * Google Event IDでスケジュールを検索
     */
    public Optional<ScheduleEntity> getScheduleByGoogleEventId(String googleEventId) {
        return scheduleRepository.findByGoogleEventId(googleEventId);
    }

    /**
     * 手動スケジュールを作成
     */
    @Transactional
    public ScheduleEntity createManualSchedule(ScheduleEntity schedule) {
        schedule.setIsSyncedFromGoogle(false);
        schedule.setGoogleEventId(null);
        
        // taskがnullの場合はデフォルト値を設定
        if (schedule.getTask() == null || schedule.getTask().isEmpty()) {
            schedule.setTask(DEFAULT_TASK_NAME);
        }
        
        // task_timeがnullの場合は0を設定
        if (schedule.getTaskTime() == null) {
            schedule.setTaskTime(0);
        }
        
        return scheduleRepository.save(schedule);
    }

    /**
     * Googleカレンダー同期スケジュールを作成（重複チェック付き）
     */
    @Transactional
    public ScheduleEntity createGoogleSyncedSchedule(ScheduleEntity schedule, String googleEventId) {
        // 既に同じGoogleイベントIDが存在するかチェック
        Optional<ScheduleEntity> existing = scheduleRepository.findByGoogleEventId(googleEventId);
        if (existing.isPresent()) {
            // 既存のスケジュールを更新
            ScheduleEntity existingSchedule = existing.get();
            existingSchedule.setTitle(schedule.getTitle());
            existingSchedule.setStartTime(schedule.getStartTime());
            existingSchedule.setEndTime(schedule.getEndTime());
            existingSchedule.setMemo(schedule.getMemo());
            
            // taskがnullの場合はデフォルト値を設定
            if (existingSchedule.getTask() == null || existingSchedule.getTask().isEmpty()) {
                existingSchedule.setTask(DEFAULT_TASK_NAME);
            }
            
            // task_timeがnullの場合は0を設定
            if (existingSchedule.getTaskTime() == null) {
                existingSchedule.setTaskTime(0);
            }
            
            return scheduleRepository.save(existingSchedule);
        }

        // 新規作成
        schedule.setIsSyncedFromGoogle(true);
        schedule.setGoogleEventId(googleEventId);
        
        // taskがnullの場合はデフォルト値を設定
        if (schedule.getTask() == null || schedule.getTask().isEmpty()) {
            schedule.setTask(DEFAULT_TASK_NAME);
        }
        
        // task_timeがnullの場合は0を設定
        if (schedule.getTaskTime() == null) {
            schedule.setTaskTime(0);
        }
        
        return scheduleRepository.save(schedule);
    }

    /**
     * 手動スケジュールを更新
     */
    @Transactional
    public ScheduleEntity updateManualSchedule(ScheduleEntity schedule) {
        // Google同期スケジュールは更新不可
        if (schedule.getIsSyncedFromGoogle()) {
            throw new IllegalStateException("Google同期スケジュールは編集できません");
        }
        
        // taskがnullの場合はデフォルト値を設定
        if (schedule.getTask() == null || schedule.getTask().isEmpty()) {
            schedule.setTask(DEFAULT_TASK_NAME);
        }
        
        // task_timeがnullの場合は0を設定
        if (schedule.getTaskTime() == null) {
            schedule.setTaskTime(0);
        }
        
        return scheduleRepository.save(schedule);
    }

    /**
     * スケジュールを論理削除
     */
    @Transactional
    public boolean deleteSchedule(Long id, Long userId) {
        // Google同期スケジュールは削除不可のチェック
        Optional<ScheduleEntity> schedule = getScheduleById(id, userId);
        if (schedule.isPresent() && schedule.get().getIsSyncedFromGoogle()) {
            throw new IllegalStateException("Google同期スケジュールは削除できません");
        }

        int result = scheduleRepository.logicalDeleteById(id, userId, LocalDateTime.now());
        return result > 0;
    }

    /**
     * 全てのGoogle同期スケジュールを論理削除（同期解除時）
     */
    @Transactional
    public int deleteAllGoogleSyncedSchedules(Long userId) {
        return scheduleRepository.logicalDeleteAllGoogleSyncedSchedules(userId, LocalDateTime.now());
    }

    /**
     * スケジュールが編集可能かチェック
     */
    public boolean isEditable(Long id, Long userId) {
        Optional<ScheduleEntity> schedule = getScheduleById(id, userId);
        return schedule.isPresent() && schedule.get().isEditable();
    }

    /**
     * スケジュールが削除可能かチェック
     */
    public boolean isDeletable(Long id, Long userId) {
        Optional<ScheduleEntity> schedule = getScheduleById(id, userId);
        return schedule.isPresent() && schedule.get().isDeletable();
    }
    
    // =========================================================================
    // 以下、ScheduleControllerで使用される追加メソッド
    // =========================================================================
    
    /**
     * スケジュールのバリデーション
     * 終了時刻が開始時刻より後であることを確認
     */
    public boolean validateSchedule(ScheduleEntity schedule) {
        if (schedule.getStartTime() == null || schedule.getEndTime() == null) {
            return false;
        }
        return schedule.getEndTime().isAfter(schedule.getStartTime());
    }
    
    /**
     * ユーザーの全タスクを取得
     */
    public List<TaskEntity> getUserTasks(Long userId) {
        return taskRepository.findByUserId(userId);
    }
    
    /**
     * 新しいタスクを作成
     */
    @Transactional
    public TaskEntity createTask(String taskName, Long userId) {
        // タスク名の重複チェック
        Optional<TaskEntity> existing = taskRepository.findByTaskNameAndUserId(taskName, userId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("同じ名前のタスクが既に存在します");
        }
        
        TaskEntity task = new TaskEntity();
        task.setTaskName(taskName);
        task.setUserId(userId);
        
        return taskRepository.save(task);
    }
    
    /**
     * タスクの推測所要時間を取得
     * 過去の実績レコードから平均時間を計算
     */
    public Map<String, String> getEstimatedTaskTime(Long userId, String taskName) {
        Map<String, String> result = new HashMap<>();
        
        // 過去3ヶ月のレコードを取得
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        LocalDateTime now = LocalDateTime.now();
        
        List<RecordEntity> records = recordRepository.findByUserIdAndTaskNameAndDateRange(
            userId, taskName, threeMonthsAgo, now
        );
        
        if (records.isEmpty()) {
            result.put("estimatedTime", "データなし");
            result.put("minutes", "0");
            return result;
        }
        
        // 平均時間を計算
        long totalMinutes = records.stream()
            .mapToLong(RecordEntity::getDurationMinutes)
            .sum();
        
        long averageMinutes = totalMinutes / records.size();
        
        // 時間:分の形式に変換
        long hours = averageMinutes / 60;
        long minutes = averageMinutes % 60;
        
        result.put("estimatedTime", String.format("%02d:%02d", hours, minutes));
        result.put("minutes", String.valueOf(averageMinutes));
        result.put("recordCount", String.valueOf(records.size()));
        
        return result;
    }

    public List<ScheduleEntity> getSchedulesByUserAndDate(Long userId, LocalDate targetDate) {
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.plusDays(1).atStartOfDay();
        return scheduleRepository.findByUserIdAndDateRange(userId, startOfDay, endOfDay);
    }
}