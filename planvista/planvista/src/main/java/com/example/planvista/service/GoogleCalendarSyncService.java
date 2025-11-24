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

@Service
public class GoogleCalendarSyncService {
    
    @Autowired
    private GoogleCalendarService googleCalendarService;
    
    @Autowired
    private ScheduleService scheduleService;
    

    @Transactional
    public int syncGoogleCalendarEvents(String accessToken, Long userId) throws Exception {
        List<Event> googleEvents = googleCalendarService.getEvents(accessToken);
        
        int syncedCount = 0;
        
        for (Event googleEvent : googleEvents) {
            try {
                ScheduleEntity schedule = convertGoogleEventToSchedule(googleEvent, userId);

                scheduleService.createGoogleSyncedSchedule(schedule, googleEvent.getId());
                
                syncedCount++;
            } catch (Exception e) {

                System.err.println("Failed to sync event: " + googleEvent.getId() + " - " + e.getMessage());
            }
        }
        
        return syncedCount;
    }

    @Transactional
    public int syncGoogleCalendarEventsByDateRange(String accessToken, Long userId, 
                                                     ZonedDateTime startDate, ZonedDateTime endDate) throws Exception {
        List<Event> googleEvents = googleCalendarService.getEventsByDateRange(accessToken, startDate, endDate);
        
        int syncedCount = 0;
        
        for (Event googleEvent : googleEvents) {
            try {
                ScheduleEntity schedule = convertGoogleEventToSchedule(googleEvent, userId);

                scheduleService.createGoogleSyncedSchedule(schedule, googleEvent.getId());
                
                syncedCount++;
            } catch (Exception e) {
                System.err.println("Failed to sync event: " + googleEvent.getId() + " - " + e.getMessage());
            }
        }
        
        return syncedCount;
    }

    @Transactional
    public int unsyncGoogleCalendar(Long userId) {
        return scheduleService.deleteAllGoogleSyncedSchedules(userId);
    }

    private ScheduleEntity convertGoogleEventToSchedule(Event googleEvent, Long userId) {
        ScheduleEntity schedule = new ScheduleEntity();
        
        schedule.setUserId(userId);
        schedule.setTitle(googleEvent.getSummary() != null ? googleEvent.getSummary() : "無題");

        EventDateTime start = googleEvent.getStart();
        LocalDateTime startTime = convertEventDateTimeToLocalDateTime(start);
        schedule.setStartTime(startTime);

        EventDateTime end = googleEvent.getEnd();
        LocalDateTime endTime = convertEventDateTimeToLocalDateTime(end);
        schedule.setEndTime(endTime);

        schedule.setMemo(googleEvent.getDescription());

        
        return schedule;
    }
    

    private LocalDateTime convertEventDateTimeToLocalDateTime(EventDateTime eventDateTime) {
        if (eventDateTime == null) {
            return LocalDateTime.now();
        }

        if (eventDateTime.getDateTime() != null) {
            long timestamp = eventDateTime.getDateTime().getValue();
            return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp), 
                ZoneId.systemDefault()
            );
        }

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