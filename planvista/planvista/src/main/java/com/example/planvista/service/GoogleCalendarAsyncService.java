package com.example.planvista.service;

import com.example.planvista.model.entity.ScheduleEntity;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Googleカレンダー非同期同期サービス
 * GoogleCalendarSyncServiceを非同期で実行
 */
@Service
public class GoogleCalendarAsyncService {
    
    @Autowired
    private GoogleCalendarService googleCalendarService;
    
    @Autowired
    private ScheduleService scheduleService;
    
    /**
     * Googleカレンダーからイベントを非同期で同期する
     * @param accessToken Google OAuth2アクセストークン
     * @param userId ユーザーID
     * @return 同期結果（同期件数、スキップ件数）
     */
    @Async("googleCalendarTaskExecutor")
    public CompletableFuture<SyncResult> syncEventsAsync(String accessToken, Long userId) {
        try {
            System.out.println("非同期同期開始 - ユーザーID: " + userId + " スレッド: " + Thread.currentThread().getName());
            
            List<Event> events = googleCalendarService.getEvents(accessToken);
            
            System.out.println("取得した予定数: " + events.size());
            
            int syncedCount = 0;
            int skippedCount = 0;

            for (Event event : events) {
                try {
                    // GoogleイベントIDで既存チェック
                    if (event.getId() != null && scheduleService.getScheduleByGoogleEventId(event.getId()).isPresent()) {
                        System.out.println("既に存在する予定をスキップ: " + event.getSummary());
                        skippedCount++;
                        continue;
                    }

                    // ScheduleEntityの作成
                    ScheduleEntity schedule = new ScheduleEntity();
                    schedule.setTitle(event.getSummary() != null ? event.getSummary() : "無題");
                    schedule.setMemo(event.getDescription());
                    schedule.setUserId(userId);
                    schedule.setIsSyncedFromGoogle(true);

                    // 開始時刻の設定
                    EventDateTime start = event.getStart();
                    if (start != null && start.getDateTime() != null) {
                        Instant startInstant = Instant.ofEpochMilli(start.getDateTime().getValue());
                        schedule.setStartTime(LocalDateTime.ofInstant(startInstant, ZoneId.systemDefault()));
                    } else if (start != null && start.getDate() != null) {
                        Instant startInstant = Instant.ofEpochMilli(start.getDate().getValue());
                        schedule.setStartTime(LocalDateTime.ofInstant(startInstant, ZoneId.systemDefault()));
                    }

                    // 終了時刻の設定
                    EventDateTime end = event.getEnd();
                    if (end != null && end.getDateTime() != null) {
                        Instant endInstant = Instant.ofEpochMilli(end.getDateTime().getValue());
                        schedule.setEndTime(LocalDateTime.ofInstant(endInstant, ZoneId.systemDefault()));
                    } else if (end != null && end.getDate() != null) {
                        Instant endInstant = Instant.ofEpochMilli(end.getDate().getValue());
                        schedule.setEndTime(LocalDateTime.ofInstant(endInstant, ZoneId.systemDefault()));
                    }

                    // 保存（重複チェック付き）
                    if (schedule.getStartTime() != null && schedule.getEndTime() != null) {
                        scheduleService.createGoogleSyncedSchedule(schedule, event.getId());
                        syncedCount++;
                        System.out.println("同期完了: " + schedule.getTitle());
                    }
                    
                } catch (Exception e) {
                    System.err.println("予定の同期中にエラー: " + event.getSummary());
                    e.printStackTrace();
                }
            }
            
            System.out.println("非同期同期完了 - 同期: " + syncedCount + "件, スキップ: " + skippedCount + "件");
            
            return CompletableFuture.completedFuture(new SyncResult(syncedCount, skippedCount, true, null));
            
        } catch (Exception e) {
            System.err.println("非同期同期中にエラー発生: " + e.getMessage());
            e.printStackTrace();
            return CompletableFuture.completedFuture(new SyncResult(0, 0, false, e.getMessage()));
        }
    }
    
    /**
     * 同期結果を格納するクラス
     */
    public static class SyncResult {
        private final int syncedCount;
        private final int skippedCount;
        private final boolean success;
        private final String errorMessage;
        
        public SyncResult(int syncedCount, int skippedCount, boolean success, String errorMessage) {
            this.syncedCount = syncedCount;
            this.skippedCount = skippedCount;
            this.success = success;
            this.errorMessage = errorMessage;
        }
        
        public int getSyncedCount() {
            return syncedCount;
        }
        
        public int getSkippedCount() {
            return skippedCount;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}