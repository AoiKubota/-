package com.example.planvista.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class GoogleCalendarService {
    
    private static final String APPLICATION_NAME = "PlanVista";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    
    /**
     * Googleカレンダーからイベントを取得する
     * @param accessToken OAuth2アクセストークン
     * @return イベントのリスト
     * @throws Exception エラー発生時
     */
    public List<Event> getEvents(String accessToken) throws Exception {
        Calendar service = getCalendarService(accessToken);
        
        // 現在時刻から1年後までのイベントを取得
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime oneYearLater = now.plusYears(1);
        
        Events events = service.events().list("primary")
                .setMaxResults(250) // 最大250件まで取得
                .setTimeMin(new com.google.api.client.util.DateTime(Date.from(now.toInstant())))
                .setTimeMax(new com.google.api.client.util.DateTime(Date.from(oneYearLater.toInstant())))
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        
        List<Event> items = events.getItems();
        return items != null ? items : new ArrayList<>();
    }
    
    /**
     * 特定の期間のイベントを取得する
     * @param accessToken OAuth2アクセストークン
     * @param startDate 開始日時
     * @param endDate 終了日時
     * @return イベントのリスト
     * @throws Exception エラー発生時
     */
    public List<Event> getEventsByDateRange(String accessToken, ZonedDateTime startDate, ZonedDateTime endDate) throws Exception {
        Calendar service = getCalendarService(accessToken);
        
        Events events = service.events().list("primary")
                .setMaxResults(250)
                .setTimeMin(new com.google.api.client.util.DateTime(Date.from(startDate.toInstant())))
                .setTimeMax(new com.google.api.client.util.DateTime(Date.from(endDate.toInstant())))
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        
        List<Event> items = events.getItems();
        return items != null ? items : new ArrayList<>();
    }
    
    /**
     * Googleカレンダーサービスインスタンスを作成
     * @param accessToken OAuth2アクセストークン
     * @return Calendarサービス
     * @throws Exception エラー発生時
     */
    private Calendar getCalendarService(String accessToken) throws Exception {
        // アクセストークンからGoogleCredentialsを作成
        GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null));
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
        
        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                requestInitializer)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    
    /**
     * イベントを作成する（オプション機能）
     * @param accessToken OAuth2アクセストークン
     * @param event 作成するイベント
     * @return 作成されたイベント
     * @throws Exception エラー発生時
     */
    public Event createEvent(String accessToken, Event event) throws Exception {
        Calendar service = getCalendarService(accessToken);
        return service.events().insert("primary", event).execute();
    }
    
    /**
     * イベントを更新する（オプション機能）
     * @param accessToken OAuth2アクセストークン
     * @param eventId イベントID
     * @param event 更新するイベント
     * @return 更新されたイベント
     * @throws Exception エラー発生時
     */
    public Event updateEvent(String accessToken, String eventId, Event event) throws Exception {
        Calendar service = getCalendarService(accessToken);
        return service.events().update("primary", eventId, event).execute();
    }
    
    /**
     * イベントを削除する（オプション機能）
     * @param accessToken OAuth2アクセストークン
     * @param eventId イベントID
     * @throws Exception エラー発生時
     */
    public void deleteEvent(String accessToken, String eventId) throws Exception {
        Calendar service = getCalendarService(accessToken);
        service.events().delete("primary", eventId).execute();
    }
}
