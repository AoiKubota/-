package com.example.planvista.controller;

import com.example.planvista.service.GoogleCalendarAsyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class GoogleCalendarController {
    
    @Autowired
    private GoogleCalendarAsyncService googleCalendarAsyncService;

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

            googleCalendarAsyncService.syncEventsAsync(accessToken, userId);

            redirectAttributes.addFlashAttribute("info", "Googleカレンダーの同期を開始しました。処理が完了するまでしばらくお待ちください。");
            
            return "redirect:/calendar";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "同期の開始中にエラーが発生しました: " + e.getMessage());
            return "redirect:/calendar";
        }
    }

    @GetMapping("/google_calendar_sync_status")
    public String getSyncStatus(Model model, HttpSession session) {
        return "google_calendar_sync_status";
    }
}