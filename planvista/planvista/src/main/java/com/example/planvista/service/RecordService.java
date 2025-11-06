package com.example.planvista.service;

import com.example.planvista.model.entity.RecordEntity;
import com.example.planvista.model.entity.TaskEntity;
import com.example.planvista.repository.RecordRepository;
import com.example.planvista.repository.ScheduleRepository;
import com.example.planvista.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * RecordService - レコード（実績記録）機能のビジネスロジック
 * メインページでのタスク実行記録の開始・終了を管理
 */
@Service
@Transactional
public class RecordService {

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private TaskRepository taskRepository;

    /**
     * レコードの開始（記録開始）
     * @param userId ユーザーID
     * @param taskId タスクID
     * @param memo メモ
     * @return 作成されたRecordEntity
     */
    public RecordEntity startRecord(Long userId, Long taskId, String memo) {
        // タスク情報を取得
        TaskEntity task = taskRepository.findById(taskId);
        if (task == null) {
            throw new RuntimeException("Task not found with id: " + taskId);
        }
        
        RecordEntity record = new RecordEntity();
        record.setUserId(userId);
        record.setTaskId(taskId);
        record.setTaskName(task.getTaskName());
        record.setStartTime(LocalDateTime.now());
        record.setEndTime(LocalDateTime.now()); // 仮の値を設定
        record.setMemo(memo);
        
        return recordRepository.save(record);
    }

    /**
     * レコードの終了（記録終了）
     * @param recordId レコードID
     * @param scheduleId 対応するスケジュールID（オプション）
     * @return 更新されたRecordEntity
     */
    public RecordEntity endRecord(Long recordId, Long scheduleId) {
        RecordEntity record = recordRepository.findById(recordId);
        
        if (record == null) {
            throw new RuntimeException("Record not found with id: " + recordId);
        }
        
        record.setEndTime(LocalDateTime.now());
        record.setScheduleId(scheduleId);
        
        return recordRepository.save(record);
    }

    /**
     * 指定ユーザーの進行中のレコードを取得
     * @param userId ユーザーID
     * @return 進行中のRecordEntity（存在しない場合はnull）
     */
    public RecordEntity getActiveRecord(Long userId) {
        List<RecordEntity> records = recordRepository.findByUserId(userId);
        
        // 終了時刻が開始時刻と同じ（または非常に近い）レコードを探す
        for (RecordEntity record : records) {
            if (record.getStartTime().equals(record.getEndTime()) || 
                record.getEndTime().isBefore(record.getStartTime().plusSeconds(1))) {
                return record;
            }
        }
        
        return null;
    }

    /**
     * 指定ユーザーの指定日のレコード一覧を取得
     * @param userId ユーザーID
     * @param date 日付
     * @return レコードのリスト
     */
    public List<RecordEntity> getRecordsByUserAndDate(Long userId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return recordRepository.findByUserIdAndDateRange(userId, startOfDay, endOfDay);
    }

    /**
     * 指定ユーザーの全レコードを取得
     * @param userId ユーザーID
     * @return レコードのリスト
     */
    public List<RecordEntity> getAllRecordsByUser(Long userId) {
        return recordRepository.findByUserId(userId);
    }

    /**
     * レコードIDでレコードを取得
     * @param recordId レコードID
     * @return RecordEntity
     */
    public RecordEntity getRecordById(Long recordId) {
        RecordEntity record = recordRepository.findById(recordId);
        if (record == null) {
            throw new RuntimeException("Record not found with id: " + recordId);
        }
        return record;
    }

    /**
     * レコードの更新
     * @param recordId レコードID
     * @param taskId タスクID
     * @param startTime 開始時刻
     * @param endTime 終了時刻
     * @param memo メモ
     * @param changeReason 変更理由
     * @return 更新されたRecordEntity
     */
    public RecordEntity updateRecord(Long recordId, Long taskId, LocalDateTime startTime, 
                                    LocalDateTime endTime, String memo, String changeReason) {
        RecordEntity record = getRecordById(recordId);
        
        // タスク情報を取得
        TaskEntity task = taskRepository.findById(taskId);
        if (task == null) {
            throw new RuntimeException("Task not found with id: " + taskId);
        }
        
        record.setTaskId(taskId);
        record.setTaskName(task.getTaskName());
        record.setStartTime(startTime);
        record.setEndTime(endTime);
        record.setMemo(memo);
        
        return recordRepository.save(record);
    }

    /**
     * レコードの削除
     * @param recordId レコードID
     */
    public void deleteRecord(Long recordId) {
        if (!recordRepository.existsById(recordId)) {
            throw new RuntimeException("Record not found with id: " + recordId);
        }
        recordRepository.delete(recordId);
    }

    /**
     * スケジュールIDに紐づくレコードを取得
     * @param scheduleId スケジュールID
     * @return RecordEntity（存在しない場合はnull）
     */
    public RecordEntity getRecordByScheduleId(Long scheduleId) {
        return recordRepository.findByScheduleId(scheduleId);
    }

    /**
     * 指定期間のレコードを取得
     * @param userId ユーザーID
     * @param startDate 開始日
     * @param endDate 終了日
     * @return レコードのリスト
     */
    public List<RecordEntity> getRecordsByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        return recordRepository.findByUserIdAndDateRange(userId, start, end);
    }

    /**
     * タスクIDに紐づくレコードを取得
     * @param taskId タスクID
     * @return レコードのリスト
     */
    public List<RecordEntity> getRecordsByTaskId(Long taskId) {
        return recordRepository.findByTaskId(taskId);
    }

    /**
     * メモを更新
     * @param recordId レコードID
     * @param memo メモ
     * @return 更新されたRecordEntity
     */
    public RecordEntity updateMemo(Long recordId, String memo) {
        RecordEntity record = getRecordById(recordId);
        record.setMemo(memo);
        return recordRepository.save(record);
    }
}