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
    
    /**
     * Googleカレンダー同期を非同期で開始する
     */
    @GetMapping("/google_calendar_synchronize")
    public String synchronize(
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            // ユーザーIDの取得
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
            
            // アクセストークンの取得
            String accessToken = authorizedClient.getAccessToken().getTokenValue();

            // 非同期同期を開始（すぐにリターン）
            googleCalendarAsyncService.syncEventsAsync(accessToken, userId);
            
            // ユーザーにはすぐにフィードバックを返す
            redirectAttributes.addFlashAttribute("info", "Googleカレンダーの同期を開始しました。処理が完了するまでしばらくお待ちください。");
            
            return "redirect:/calendar";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "同期の開始中にエラーが発生しました: " + e.getMessage());
            return "redirect:/calendar";
        }
    }
    
    /**
     * 同期ステータスを確認するエンドポイント（オプション）
     * WebSocketやポーリングで利用可能
     */
    @GetMapping("/google_calendar_sync_status")
    public String getSyncStatus(Model model, HttpSession session) {
        // 必要に応じて同期ステータスを返す実装を追加
        return "google_calendar_sync_status";
    }
}