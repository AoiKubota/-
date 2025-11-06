package com.example.planvista.service;

import com.example.planvista.model.entity.ScheduleEntity;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Googleカレンダー同期サービス
 * GoogleカレンダーのイベントをScheduleEntityに変換して保存
 */
@Service
public class GoogleCalendarSyncService {
    
    @Autowired
    private GoogleCalendarService googleCalendarService;
    
    @Autowired
    private ScheduleService scheduleService;
    
    /**
     * Googleカレンダーからイベントを取得してスケジュールとして保存
     * @param accessToken OAuth2アクセストークン
     * @param userId ユーザーID
     * @return 同期したスケジュール数
     * @throws Exception エラー発生時
     */
    @Transactional
    public int syncGoogleCalendarEvents(String accessToken, Long userId) throws Exception {
        // Googleカレンダーからイベントを取得
        List<Event> googleEvents = googleCalendarService.getEvents(accessToken);
        
        int syncedCount = 0;
        
        for (Event googleEvent : googleEvents) {
            try {
                // GoogleイベントをScheduleEntityに変換
                ScheduleEntity schedule = convertGoogleEventToSchedule(googleEvent, userId);
                
                // スケジュールを保存（重複チェック付き）
                scheduleService.createGoogleSyncedSchedule(schedule, googleEvent.getId());
                
                syncedCount++;
            } catch (Exception e) {
                // 個別のイベント同期失敗時はスキップして続行
                System.err.println("Failed to sync event: " + googleEvent.getId() + " - " + e.getMessage());
            }
        }
        
        return syncedCount;
    }
    
    /**
     * 期間を指定してGoogleカレンダーからイベントを同期
     * @param accessToken OAuth2アクセストークン
     * @param userId ユーザーID
     * @param startDate 開始日時
     * @param endDate 終了日時
     * @return 同期したスケジュール数
     * @throws Exception エラー発生時
     */
    @Transactional
    public int syncGoogleCalendarEventsByDateRange(String accessToken, Long userId, 
                                                     ZonedDateTime startDate, ZonedDateTime endDate) throws Exception {
        // Googleカレンダーから期間指定でイベントを取得
        List<Event> googleEvents = googleCalendarService.getEventsByDateRange(accessToken, startDate, endDate);
        
        int syncedCount = 0;
        
        for (Event googleEvent : googleEvents) {
            try {
                // GoogleイベントをScheduleEntityに変換
                ScheduleEntity schedule = convertGoogleEventToSchedule(googleEvent, userId);
                
                // スケジュールを保存（重複チェック付き）
                scheduleService.createGoogleSyncedSchedule(schedule, googleEvent.getId());
                
                syncedCount++;
            } catch (Exception e) {
                // 個別のイベント同期失敗時はスキップして続行
                System.err.println("Failed to sync event: " + googleEvent.getId() + " - " + e.getMessage());
            }
        }
        
        return syncedCount;
    }
    
    /**
     * Google同期スケジュールを全て削除（同期解除）
     * @param userId ユーザーID
     * @return 削除したスケジュール数
     */
    @Transactional
    public int unsyncGoogleCalendar(Long userId) {
        return scheduleService.deleteAllGoogleSyncedSchedules(userId);
    }
    
    /**
     * GoogleイベントをScheduleEntityに変換
     * @param googleEvent Googleカレンダーのイベント
     * @param userId ユーザーID
     * @return ScheduleEntity
     */
    private ScheduleEntity convertGoogleEventToSchedule(Event googleEvent, Long userId) {
        ScheduleEntity schedule = new ScheduleEntity();
        
        schedule.setUserId(userId);
        schedule.setTitle(googleEvent.getSummary() != null ? googleEvent.getSummary() : "無題");
        
        // 開始時刻を変換
        EventDateTime start = googleEvent.getStart();
        LocalDateTime startTime = convertEventDateTimeToLocalDateTime(start);
        schedule.setStartTime(startTime);
        
        // 終了時刻を変換
        EventDateTime end = googleEvent.getEnd();
        LocalDateTime endTime = convertEventDateTimeToLocalDateTime(end);
        schedule.setEndTime(endTime);
        
        // 説明をmemoに設定
        schedule.setMemo(googleEvent.getDescription());
        
        // Google同期フラグとイベントIDは後でScheduleServiceで設定される
        
        return schedule;
    }
    
    /**
     * GoogleのEventDateTimeをLocalDateTimeに変換
     * @param eventDateTime GoogleのEventDateTime
     * @return LocalDateTime
     */
    private LocalDateTime convertEventDateTimeToLocalDateTime(EventDateTime eventDateTime) {
        if (eventDateTime == null) {
            return LocalDateTime.now();
        }
        
        // dateTime（時刻あり）の場合
        if (eventDateTime.getDateTime() != null) {
            long timestamp = eventDateTime.getDateTime().getValue();
            return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp), 
                ZoneId.systemDefault()
            );
        }
        
        // date（終日イベント）の場合
        if (eventDateTime.getDate() != null) {
            long timestamp = eventDateTime.getDate().getValue();
            return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp), 
                ZoneId.systemDefault()
            ).withHour(0).withMinute(0).withSecond(0);
        }
        
        return LocalDateTime.now();
    }
}