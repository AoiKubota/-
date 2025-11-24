package com.example.planvista.controller;

import com.example.planvista.model.entity.RecordEntity;
import com.example.planvista.model.entity.ScheduleEntity;
import com.example.planvista.model.entity.TaskEntity;
import com.example.planvista.service.RecordService;
import com.example.planvista.service.ScheduleService;
import com.example.planvista.service.TaskService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MainController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private RecordService recordService;

    @Autowired
    private TaskService taskService;

    @GetMapping("/main")
    public String showMain(HttpSession session, Model model) {
        Long userId = getUserIdAsLong(session);
        if (userId == null) {
            return "redirect:/login";
        }

        System.out.println("=== メインページ表示 ===");
        System.out.println("userId: " + userId);

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        List<ScheduleEntity> schedules = scheduleService.getSchedulesByDateRange(userId, startOfDay, endOfDay);
        System.out.println("取得したスケジュール数: " + schedules.size());

        List<RecordEntity> records = recordService.getRecordsByUserAndDate(userId, today);
        System.out.println("取得したレコード数: " + records.size());

        List<Map<String, Object>> scheduleList = new ArrayList<>();
        for (ScheduleEntity schedule : schedules) {
            Map<String, Object> scheduleMap = new HashMap<>();
            scheduleMap.put("id", schedule.getId());
            scheduleMap.put("title", schedule.getTitle());
            scheduleMap.put("startHour", schedule.getStartTime().getHour());
            scheduleMap.put("startMinute", schedule.getStartTime().getMinute());
            scheduleMap.put("endHour", schedule.getEndTime().getHour());
            scheduleMap.put("endMinute", schedule.getEndTime().getMinute());
            scheduleMap.put("memo", schedule.getMemo() != null ? schedule.getMemo() : "");
            scheduleMap.put("task", schedule.getTask() != null ? schedule.getTask() : "");
            scheduleMap.put("isSyncedFromGoogle", schedule.getIsSyncedFromGoogle() != null && schedule.getIsSyncedFromGoogle());
            scheduleList.add(scheduleMap);
        }

        List<Map<String, Object>> recordList = new ArrayList<>();
        for (RecordEntity record : records) {
            Map<String, Object> recordMap = new HashMap<>();
            recordMap.put("id", record.getId());
            recordMap.put("title", record.getTaskName());
            recordMap.put("startHour", record.getStartTime().getHour());
            recordMap.put("startMinute", record.getStartTime().getMinute());
            recordMap.put("endHour", record.getEndTime().getHour());
            recordMap.put("endMinute", record.getEndTime().getMinute());
            recordMap.put("memo", record.getMemo() != null ? record.getMemo() : "");
            recordList.add(recordMap);
        }

        List<TaskEntity> tasks = scheduleService.getUserTasks(userId);
        System.out.println("取得したタスク数: " + tasks.size());

        model.addAttribute("schedules", scheduleList);
        model.addAttribute("records", recordList);
        model.addAttribute("tasks", tasks);
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("today", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        System.out.println("=== メインページ表示完了 ===");
        return "main";
    }

    @PostMapping("/record_save")
    @ResponseBody
    public Map<String, Object> saveRecord(HttpSession session,
                                         @RequestParam("taskName") String taskName,
                                         @RequestParam("startTime") String startTime,
                                         @RequestParam("endTime") String endTime,
                                         @RequestParam(value = "memo", required = false) String memo) {
        Map<String, Object> response = new HashMap<>();

        try {
            Long userId = getUserIdAsLong(session);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "ログインしてください");
                return response;
            }

            System.out.println("=== レコード保存開始 ===");
            System.out.println("userId: " + userId);
            System.out.println("taskName: " + taskName);
            System.out.println("startTime: " + startTime);
            System.out.println("endTime: " + endTime);
            System.out.println("memo: " + memo);

            LocalDateTime startDateTime = LocalDateTime.parse(startTime);
            LocalDateTime endDateTime = LocalDateTime.parse(endTime);

            TaskEntity task = taskService.getTaskByName(taskName);
            Long taskId;
            
            if (task == null) {
                task = taskService.createTask(taskName);
                taskId = task.getId();
                System.out.println("新規タスク作成: " + taskName + " (ID: " + taskId + ")");
            } else {
                taskId = task.getId();
                System.out.println("既存タスク使用: " + taskName + " (ID: " + taskId + ")");
            }

            RecordEntity record = recordService.startRecord(userId, taskId, memo);

            record.setStartTime(startDateTime);
            record.setEndTime(endDateTime);

            RecordEntity savedRecord = recordService.updateRecord(
                record.getId(),
                taskId,
                startDateTime,
                endDateTime,
                memo,
                "タイマーから保存"
            );
            
            System.out.println("保存されたレコードID: " + savedRecord.getId());
            System.out.println("=== レコード保存完了 ===");

            response.put("success", true);
            response.put("message", "レコードを保存しました");
            response.put("recordId", savedRecord.getId());

        } catch (Exception e) {
            System.err.println("レコード保存エラー: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "レコードの保存に失敗しました: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/record/start")
    @ResponseBody
    public Map<String, Object> startRecord(HttpSession session,
                                          @RequestParam("taskId") Long taskId,
                                          @RequestParam(value = "memo", required = false) String memo) {
        Map<String, Object> response = new HashMap<>();

        try {
            Long userId = getUserIdAsLong(session);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "ログインしてください");
                return response;
            }

            System.out.println("=== レコード開始 ===");
            System.out.println("userId: " + userId);
            System.out.println("taskId: " + taskId);

            RecordEntity activeRecord = recordService.getActiveRecord(userId);
            if (activeRecord != null) {
                response.put("success", false);
                response.put("message", "既に記録が開始されています");
                return response;
            }

            RecordEntity record = recordService.startRecord(userId, taskId, memo);
            System.out.println("レコード開始完了: ID=" + record.getId());

            response.put("success", true);
            response.put("recordId", record.getId());
            response.put("startTime", record.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            response.put("message", "記録を開始しました");

        } catch (Exception e) {
            System.err.println("レコード開始エラー: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "エラーが発生しました: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/record/end")
    @ResponseBody
    public Map<String, Object> endRecord(HttpSession session,
                                        @RequestParam("recordId") Long recordId,
                                        @RequestParam(value = "scheduleId", required = false) Long scheduleId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Long userId = getUserIdAsLong(session);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "ログインしてください");
                return response;
            }

            System.out.println("=== レコード終了 ===");
            System.out.println("recordId: " + recordId);
            System.out.println("scheduleId: " + scheduleId);

            RecordEntity record = recordService.endRecord(recordId, scheduleId);
            System.out.println("レコード終了完了: ID=" + record.getId());

            response.put("success", true);
            response.put("endTime", record.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            response.put("message", "記録を終了しました");

        } catch (Exception e) {
            System.err.println("レコード終了エラー: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "エラーが発生しました: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/record/active")
    @ResponseBody
    public Map<String, Object> getActiveRecord(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            Long userId = getUserIdAsLong(session);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "ログインしてください");
                return response;
            }

            RecordEntity activeRecord = recordService.getActiveRecord(userId);

            if (activeRecord != null) {
                response.put("hasActiveRecord", true);
                response.put("recordId", activeRecord.getId());
                response.put("taskId", activeRecord.getTaskId());
                response.put("startTime", activeRecord.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                response.put("memo", activeRecord.getMemo());
            } else {
                response.put("hasActiveRecord", false);
            }

            response.put("success", true);

        } catch (Exception e) {
            System.err.println("進行中レコード取得エラー: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "エラーが発生しました: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/day-data")
    @ResponseBody
    public Map<String, Object> getDayData(HttpSession session,
                                         @RequestParam("date") String date) {
        Map<String, Object> response = new HashMap<>();

        try {
            Long userId = getUserIdAsLong(session);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "ログインしてください");
                return response;
            }

            LocalDate targetDate = LocalDate.parse(date);

            List<RecordEntity> records = recordService.getRecordsByUserAndDate(userId, targetDate);

            response.put("success", true);
            response.put("records", records);

        } catch (Exception e) {
            System.err.println("日別データ取得エラー: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "エラーが発生しました: " + e.getMessage());
        }

        return response;
    }

    private Long getUserIdAsLong(HttpSession session) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return null;
        }

        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        } else {
            try {
                return Long.parseLong(userIdObj.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}