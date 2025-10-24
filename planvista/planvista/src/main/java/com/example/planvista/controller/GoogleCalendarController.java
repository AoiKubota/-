package com.example.planvista.controller;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GoogleCalendarController {
    
    @GetMapping("/google_calendar_synchronize")
    public String synchronize(
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
            Model model) {
        
        @SuppressWarnings("unused")
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        
        // Googleカレンダーから予定を取得して同期
        
        return "google_calendar_synchronize";
    }
}
