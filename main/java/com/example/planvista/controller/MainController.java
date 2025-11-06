package com.example.planvista.controller;

import com.example.planvista.model.entity.RecordEntity;
import com.example.planvista.model.entity.TaskEntity;
import com.example.planvista.service.RecordService;
import com.example.planvista.service.ScheduleService;
import com.example.planvista.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MainController - メインページ（/main）のコントローラー
 * レコード記録機能（タスク実行の開始・終了）を管理
 */
@Controller
@RequestMapping("/main")
public class MainController {

    @Autowired
    private RecordService recordService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private TaskService taskService;

    /**
     * メインページの表示
     */
    @GetMapping
    public String showMainPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // ユーザー情報を取得（実際の実装に応じて調整）
        Long userId = getUserIdFromUserDetails(userDetails);
        
        // 今日の日付を取得
        LocalDate today = LocalDate.now();
        
        // 進行中のレコードを取得
        RecordEntity activeRecord = recordService.getActiveRecord(userId);
        
        // タスク一覧を取得
        List<TaskEntity> tasks = taskService.getAllTasks();
        
        // 今日のレコードを取得
        List<RecordEntity> todayRecords = recordService.getRecordsByUserAndDate(userId, today);
        
        model.addAttribute("activeRecord", activeRecord);
        model.addAttribute("tasks", tasks);
        model.addAttribute("todayRecords", todayRecords);
        model.addAttribute("today", today);
        
        return "main";
    }

    /**
     * レコードの開始（Ajax）
     */
    @PostMapping("/record/start")
    @ResponseBody
    public Map<String, Object> startRecord(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long taskId,
            @RequestParam(required = false) String memo) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = getUserIdFromUserDetails(userDetails);
            
            // すでに進行中のレコードがあるかチェック
            RecordEntity activeRecord = recordService.getActiveRecord(userId);
            if (activeRecord != null) {
                response.put("success", false);
                response.put("message", "既に記録が開始されています");
                return response;
            }
            
            // レコード開始
            RecordEntity record = recordService.startRecord(userId, taskId, memo);
            
            response.put("success", true);
            response.put("recordId", record.getRecordId());
            response.put("startTime", record.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            response.put("message", "記録を開始しました");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "エラーが発生しました: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * レコードの終了（Ajax）
     */
    @PostMapping("/record/end")
    @ResponseBody
    public Map<String, Object> endRecord(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long recordId,
            @RequestParam(required = false) Long scheduleId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            RecordEntity record = recordService.endRecord(recordId, scheduleId);
            
            response.put("success", true);
            response.put("endTime", record.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            response.put("message", "記録を終了しました");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "エラーが発生しました: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 進行中のレコード情報取得（Ajax）
     */
    @GetMapping("/record/active")
    @ResponseBody
    public Map<String, Object> getActiveRecord(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = getUserIdFromUserDetails(userDetails);
            RecordEntity activeRecord = recordService.getActiveRecord(userId);
            
            if (activeRecord != null) {
                response.put("hasActiveRecord", true);
                response.put("recordId", activeRecord.getRecordId());
                response.put("taskId", activeRecord.getTaskId());
                response.put("startTime", activeRecord.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                response.put("memo", activeRecord.getMemo());
            } else {
                response.put("hasActiveRecord", false);
            }
            
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "エラーが発生しました: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 指定日のスケジュールとレコードを取得（Ajax）
     */
    @GetMapping("/day-data")
    @ResponseBody
    public Map<String, Object> getDayData(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String date) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = getUserIdFromUserDetails(userDetails);
            LocalDate targetDate = LocalDate.parse(date);
            
            List<RecordEntity> records = recordService.getRecordsByUserAndDate(userId, targetDate);
            
            response.put("success", true);
            response.put("records", records);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "エラーが発生しました: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * UserDetailsからユーザーIDを取得するヘルパーメソッド
     * 実際の実装に応じて調整が必要
     */
    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        // TODO: 実際のプロジェクトの認証方式に合わせて実装してください
        return 1L; // 仮の実装
    }
}