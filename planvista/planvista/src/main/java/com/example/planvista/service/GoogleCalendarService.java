package com.example.planvista.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.Event;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoogleCalendarService {
    
    public void getEvents(String accessToken) {
        // Googleカレンダーから予定を取得
    }
    
    public void syncToGoogleCalendar(Event event, String accessToken) {
        // PlanVistaの予定をGoogleカレンダーに同期
    }
}
