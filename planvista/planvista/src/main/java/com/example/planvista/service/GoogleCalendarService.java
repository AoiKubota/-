package com.example.planvista.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.Event;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class GoogleCalendarService {
    
    private static final String APPLICATION_NAME = "PlanVista";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public List<Event> getEvents(String accessToken) throws GeneralSecurityException, IOException {
        Calendar service = getCalendarService(accessToken);

        Events events = service.events().list("primary")
                .setMaxResults(100)
                .setTimeMin(new com.google.api.client.util.DateTime(System.currentTimeMillis()))
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        
        return events.getItems();
    }

    public void syncToGoogleCalendar(Event event, String accessToken) throws GeneralSecurityException, IOException {
        Calendar service = getCalendarService(accessToken);

        service.events().insert("primary", event).execute();
    }

    private Calendar getCalendarService(String accessToken) throws GeneralSecurityException, IOException {

        GoogleCredentials credentials = GoogleCredentials.create(
            new AccessToken(accessToken, Date.from(Instant.now().plusSeconds(3600)))
        );
        
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
        
        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                requestInitializer)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}