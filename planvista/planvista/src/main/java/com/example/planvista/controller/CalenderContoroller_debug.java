package com.example.planvista.controller;

import com.example.planvista.model.entity.ScheduleEntity;
import com.example.planvista.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * カレンダー表示コントローラー
 * スケジュール（手動登録+Google同期）を統合表示
 */
@Controller
public class CalendarController {
    
    @Autowired
    private ScheduleService scheduleService;
    
    @GetMapping("/calendar")
    public String calendar(Model model, HttpSession session) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return "redirect:/login";
        }

        Long userIdLong;
        
        // userIdの型を統一的に処理
        if (userIdObj instanceof Integer) {
            userIdLong = ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            userIdLong = (Long) userIdObj;
        } else {
            userIdLong = Long.parseLong(userIdObj.toString());
        }

        System.out.println("=== カレンダー表示処理開始 ===");
        System.out.println("ユーザーID: " + userIdLong);

        LocalDate today = LocalDate.now();

        // 3ヶ月前から3ヶ月後までの範囲を設定
        LocalDate threeMonthsAgo = today.minusMonths(3);
        LocalDateTime startDate = threeMonthsAgo.withDayOfMonth(1).atStartOfDay();

        LocalDate threeMonthsLater = today.plusMonths(3);
        LocalDateTime endDate = threeMonthsLater.withDayOfMonth(threeMonthsLater.lengthOfMonth()).atTime(23, 59, 59);

        System.out.println("取得期間: " + startDate + " ～ " + endDate);

        // 全てのスケジュール（手動登録 + Google同期）を取得
        List<ScheduleEntity> schedules = scheduleService.getSchedulesByDateRange(userIdLong, startDate, endDate);

        System.out.println("取得したスケジュール総数: " + schedules.size());
        System.out.println("  - 手動登録: " + schedules.stream().filter(s -> !s.getIsSyncedFromGoogle()).count());
        System.out.println("  - Google同期: " + schedules.stream().filter(s -> s.getIsSyncedFromGoogle()).count());

        // スケジュールをカレンダー表示用のマップに変換
        List<Map<String, Object>> eventList = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (ScheduleEntity schedule : schedules) {
            System.out.println("処理中: " + schedule.getTitle() + 
                             " (Google: " + schedule.getIsSyncedFromGoogle() + 
                             ", Date: " + schedule.getStartTime().format(dateFormatter) + ")");
            
            Map<String, Object> scheduleMap = new HashMap<>();
            scheduleMap.put("id", schedule.getId());
            scheduleMap.put("title", schedule.getTitle());
            scheduleMap.put("description", schedule.getMemo());
            scheduleMap.put("date", schedule.getStartTime().format(dateFormatter));
            scheduleMap.put("startHour", schedule.getStartTime().getHour());
            scheduleMap.put("startMinute", schedule.getStartTime().getMinute());
            scheduleMap.put("endHour", schedule.getEndTime().getHour());
            scheduleMap.put("endMinute", schedule.getEndTime().getMinute());
            scheduleMap.put("memo", schedule.getMemo() != null ? schedule.getMemo() : "");
            scheduleMap.put("task", schedule.getTask() != null ? schedule.getTask() : "");
            scheduleMap.put("taskTime", schedule.getTaskTime() != null ? schedule.getTaskTime() : 0);
            
            // Google同期かどうかを判定
            scheduleMap.put("isSyncedFromGoogle", schedule.getIsSyncedFromGoogle());
            
            // タイプを設定（UIでの区別用）
            if (schedule.getIsSyncedFromGoogle()) {
                scheduleMap.put("type", "google");  // Google同期スケジュール
                scheduleMap.put("editable", false);  // 編集不可
                scheduleMap.put("deletable", false); // 削除不可
                System.out.println("  → type: google");
            } else {
                scheduleMap.put("type", "schedule"); // 手動登録スケジュール
                scheduleMap.put("editable", true);   // 編集可能
                scheduleMap.put("deletable", true);  // 削除可能
                System.out.println("  → type: schedule");
            }
            
            eventList.add(scheduleMap);
        }
        
        // 開始時刻でソート
        eventList.sort((a, b) -> {
            String dateA = (String) a.get("date");
            String dateB = (String) b.get("date");
            int dateCompare = dateA.compareTo(dateB);
            if (dateCompare != 0) return dateCompare;
            
            int hourCompare = Integer.compare((Integer) a.get("startHour"), (Integer) b.get("startHour"));
            if (hourCompare != 0) return hourCompare;
            
            return Integer.compare((Integer) a.get("startMinute"), (Integer) b.get("startMinute"));
        });
        
        System.out.println("最終的にmodelに渡すイベント数: " + eventList.size());
        System.out.println("=== カレンダー表示処理終了 ===");
        
        model.addAttribute("events", eventList);
        model.addAttribute("username", session.getAttribute("username"));
        
        return "calendar";
    }
}