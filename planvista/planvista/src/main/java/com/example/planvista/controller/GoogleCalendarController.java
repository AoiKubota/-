package com.example.planvista.controller;

import com.example.planvista.model.entity.EventEntity;
import com.example.planvista.repository.EventRepository;
import com.example.planvista.service.GoogleCalendarService;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Controller
public class GoogleCalendarController {
    
    @Autowired
    private GoogleCalendarService googleCalendarService;
    
    @Autowired
    private EventRepository eventRepository;
    
    @GetMapping("/google_calendar_synchronize")
    public String synchronize(
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                redirectAttributes.addFlashAttribute("error", "ログインが必要です");
                return "redirect:/login";
            }

            Long userId;
            if (userIdObj instanceof Integer) {
                userId = ((Integer) userIdObj).longValue();
            } else {
                userId = (Long) userIdObj;
            }
            
            String accessToken = authorizedClient.getAccessToken().getTokenValue();

            List<Event> events = googleCalendarService.getEvents(accessToken);
            
            System.out.println("取得した予定数: " + events.size());
            
            int syncedCount = 0;
            int skippedCount = 0;

            for (Event event : events) {
                try {
                    if (event.getId() != null && eventRepository.existsByGoogleEventId(event.getId())) {
                        System.out.println("既に存在する予定をスキップ: " + event.getSummary());
                        skippedCount++;
                        continue;
                    }

                    EventEntity planVistaEvent = new EventEntity();
                    planVistaEvent.setTitle(event.getSummary() != null ? event.getSummary() : "無題");
                    planVistaEvent.setDescription(event.getDescription());
                    planVistaEvent.setUserId(userId);
                    planVistaEvent.setGoogleEventId(event.getId());
                    planVistaEvent.setIsSyncedFromGoogle(true);

                    EventDateTime start = event.getStart();
                    if (start != null && start.getDateTime() != null) {
                        Instant startInstant = Instant.ofEpochMilli(start.getDateTime().getValue());
                        planVistaEvent.setStartTime(LocalDateTime.ofInstant(startInstant, ZoneId.systemDefault()));
                    } else if (start != null && start.getDate() != null) {
                        Instant startInstant = Instant.ofEpochMilli(start.getDate().getValue());
                        planVistaEvent.setStartTime(LocalDateTime.ofInstant(startInstant, ZoneId.systemDefault()));
                    }

                    EventDateTime end = event.getEnd();
                    if (end != null && end.getDateTime() != null) {
                        Instant endInstant = Instant.ofEpochMilli(end.getDateTime().getValue());
                        planVistaEvent.setEndTime(LocalDateTime.ofInstant(endInstant, ZoneId.systemDefault()));
                    } else if (end != null && end.getDate() != null) {
                        Instant endInstant = Instant.ofEpochMilli(end.getDate().getValue());
                        planVistaEvent.setEndTime(LocalDateTime.ofInstant(endInstant, ZoneId.systemDefault()));
                    }

                    if (planVistaEvent.getStartTime() != null && planVistaEvent.getEndTime() != null) {
                        eventRepository.create(planVistaEvent);
                        syncedCount++;
                        System.out.println("同期完了: " + planVistaEvent.getTitle());
                    }
                    
                } catch (Exception e) {
                    System.err.println("予定の同期中にエラー: " + event.getSummary());
                    e.printStackTrace();
                }
            }
            
            model.addAttribute("syncedCount", syncedCount);
            model.addAttribute("skippedCount", skippedCount);
            model.addAttribute("events", events);
            
            String message = syncedCount + "件の予定を同期しました";
            if (skippedCount > 0) {
                message += " (" + skippedCount + "件は既に存在)";
            }
            redirectAttributes.addFlashAttribute("success", message);
            
            return "redirect:/calendar";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "同期中にエラーが発生しました: " + e.getMessage());
            return "redirect:/calendar";
        }
    }
}
