package com.example.planvista.controller;

import com.example.planvista.model.entity.ScheduleEntity;
import com.example.planvista.model.entity.TaskEntity;
import com.example.planvista.service.ScheduleService;
import com.example.planvista.service.UserActivityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Controller
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;
    
    @Autowired
    private UserActivityService userActivityService;

    @GetMapping("/schedule_add")
    public String showScheduleAddForm(HttpSession session, Model model,
                                     @RequestParam(required = false) String date) {
        Long userId = getUserIdAsLong(session);
        if (userId == null) {
            return "redirect:/login";
        }

        List<TaskEntity> tasks = scheduleService.getUserTasks(userId);
        model.addAttribute("tasks", tasks);

        if (date != null && !date.isEmpty()) {
            model.addAttribute("selectedDate", date);
        } else {
            model.addAttribute("selectedDate", LocalDate.now().toString());
        }

        return "schedule_add";
    }

    @PostMapping("/schedule_add")
    public String addSchedule(HttpSession session,
                            HttpServletRequest request,
                            @RequestParam("scheduleName") String scheduleName,
                            @RequestParam("date") String date,
                            @RequestParam("startTime") String startTime,
                            @RequestParam("endTime") String endTime,
                            @RequestParam("task") String task,
                            @RequestParam(value = "memo", required = false) String memo,
                            RedirectAttributes redirectAttributes) {
        try {
            Long userId = getUserIdAsLong(session);
            if (userId == null) {
                return "redirect:/login";
            }

            System.out.println("=== スケジュール登録開始 ===");
            System.out.println("userId: " + userId);
            System.out.println("scheduleName: " + scheduleName);
            System.out.println("date: " + date);
            System.out.println("startTime: " + startTime);
            System.out.println("endTime: " + endTime);
            System.out.println("task: " + task);
            System.out.println("memo: " + memo);

            if (scheduleName == null || scheduleName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "スケジュール名を入力してください");
                return "redirect:/schedule_add?date=" + date;
            }

            ScheduleEntity schedule = new ScheduleEntity();
            schedule.setUserId(userId);
            schedule.setTitle(scheduleName.trim());
            schedule.setTask(task);

            LocalDateTime startDateTime = LocalDateTime.parse(date + "T" + startTime);
            LocalDateTime endDateTime = LocalDateTime.parse(date + "T" + endTime);
            
            schedule.setStartTime(startDateTime);
            schedule.setEndTime(endDateTime);
            schedule.setMemo(memo != null ? memo.trim() : null);

            if (!scheduleService.validateSchedule(schedule)) {
                redirectAttributes.addFlashAttribute("error", "入力内容に誤りがあります。終了時刻は開始時刻より後に設定してください。");
                return "redirect:/schedule_add?date=" + date;
            }

            ScheduleEntity savedSchedule = scheduleService.createManualSchedule(schedule);
            System.out.println("保存されたスケジュールID: " + savedSchedule.getId());
            System.out.println("=== スケジュール登録完了 ===");

            userActivityService.logScheduleCreate(userId.intValue(), scheduleName.trim(), request);

            redirectAttributes.addFlashAttribute("success", "スケジュールを登録しました");
            return "redirect:/calendar";

        } catch (Exception e) {
            System.err.println("スケジュール登録エラー: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "スケジュールの登録に失敗しました: " + e.getMessage());
            return "redirect:/schedule_add";
        }
    }

    @GetMapping("/schedule_update")
    public String showScheduleUpdateForm(HttpSession session,
                                        @RequestParam("id") Long scheduleId,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        Long userId = getUserIdAsLong(session);
        if (userId == null) {
            return "redirect:/login";
        }

        System.out.println("=== スケジュール編集画面表示 ===");
        System.out.println("scheduleId: " + scheduleId);
        System.out.println("userId: " + userId);

        Optional<ScheduleEntity> scheduleOpt = scheduleService.getScheduleById(scheduleId, userId);
        if (!scheduleOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "スケジュールが見つかりません");
            return "redirect:/calendar";
        }
        
        ScheduleEntity schedule = scheduleOpt.get();

        if (schedule.getIsSyncedFromGoogle() != null && schedule.getIsSyncedFromGoogle()) {
            redirectAttributes.addFlashAttribute("error", "Googleカレンダーから同期されたスケジュールは編集できません");
            return "redirect:/calendar";
        }

        model.addAttribute("schedule", schedule);

        List<TaskEntity> tasks = scheduleService.getUserTasks(userId);
        model.addAttribute("tasks", tasks);

        model.addAttribute("date", schedule.getStartTime().toLocalDate().toString());
        model.addAttribute("startTime", schedule.getStartTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        model.addAttribute("endTime", schedule.getEndTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));

        if (schedule.getTask() != null && !schedule.getTask().isEmpty()) {
            Map<String, String> estimatedTime = scheduleService.getEstimatedTaskTime(userId, schedule.getTask());
            model.addAttribute("estimatedTime", estimatedTime.get("estimatedTime"));
        } else {
            model.addAttribute("estimatedTime", "00:30");
        }

        return "schedule_update";
    }

    @PostMapping("/schedule_update")
    public String updateSchedule(HttpSession session,
                                HttpServletRequest request,
                                @RequestParam("scheduleId") Long scheduleId,
                                @RequestParam("scheduleName") String scheduleName,
                                @RequestParam("date") String date,
                                @RequestParam("startTime") String startTime,
                                @RequestParam("endTime") String endTime,
                                @RequestParam("task") String task,
                                @RequestParam(value = "memo", required = false) String memo,
                                RedirectAttributes redirectAttributes) {
        try {
            Long userId = getUserIdAsLong(session);
            if (userId == null) {
                return "redirect:/login";
            }

            System.out.println("=== スケジュール更新開始 ===");
            System.out.println("scheduleId: " + scheduleId);
            System.out.println("scheduleName: " + scheduleName);

            if (scheduleName == null || scheduleName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "スケジュール名を入力してください");
                return "redirect:/schedule_update?id=" + scheduleId;
            }

            Optional<ScheduleEntity> scheduleOpt = scheduleService.getScheduleById(scheduleId, userId);
            if (!scheduleOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "スケジュールが見つかりません");
                return "redirect:/calendar";
            }

            ScheduleEntity schedule = scheduleOpt.get();

            schedule.setTitle(scheduleName.trim());
            schedule.setTask(task);

            LocalDateTime startDateTime = LocalDateTime.parse(date + "T" + startTime);
            LocalDateTime endDateTime = LocalDateTime.parse(date + "T" + endTime);
            
            schedule.setStartTime(startDateTime);
            schedule.setEndTime(endDateTime);
            schedule.setMemo(memo != null ? memo.trim() : null);

            if (!scheduleService.validateSchedule(schedule)) {
                redirectAttributes.addFlashAttribute("error", "入力内容に誤りがあります。終了時刻は開始時刻より後に設定してください。");
                return "redirect:/schedule_update?id=" + scheduleId;
            }

            scheduleService.updateManualSchedule(schedule);
            System.out.println("=== スケジュール更新完了 ===");

            userActivityService.logScheduleUpdate(userId.intValue(), scheduleName.trim(), request);

            redirectAttributes.addFlashAttribute("success", "スケジュールを更新しました");
            return "redirect:/calendar";

        } catch (IllegalStateException e) {
            System.err.println("スケジュール更新エラー: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/calendar";
        } catch (Exception e) {
            System.err.println("スケジュール更新エラー: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "スケジュールの更新に失敗しました: " + e.getMessage());
            return "redirect:/schedule_update?id=" + scheduleId;
        }
    }

    @PostMapping("/schedule_delete")
    public String deleteSchedule(HttpSession session,
                               HttpServletRequest request,
                               @RequestParam("scheduleId") Long scheduleId,
                               RedirectAttributes redirectAttributes) {
        try {
            Long userId = getUserIdAsLong(session);
            if (userId == null) {
                return "redirect:/login";
            }

            System.out.println("=== スケジュール削除開始 ===");
            System.out.println("scheduleId: " + scheduleId);
            System.out.println("userId: " + userId);

            Optional<ScheduleEntity> scheduleOpt = scheduleService.getScheduleById(scheduleId, userId);
            String scheduleName = scheduleOpt.map(ScheduleEntity::getTitle).orElse("不明");

            boolean deleted = scheduleService.deleteSchedule(scheduleId, userId);
            
            if (deleted) {
                System.out.println("スケジュール削除完了: ID=" + scheduleId);

                userActivityService.logScheduleDelete(userId.intValue(), scheduleName, request);
                
                redirectAttributes.addFlashAttribute("success", "スケジュールを削除しました");
            } else {
                System.err.println("スケジュール削除失敗: ID=" + scheduleId);
                redirectAttributes.addFlashAttribute("error", "スケジュールの削除に失敗しました");
            }
            
            return "redirect:/calendar";

        } catch (IllegalStateException e) {
            System.err.println("スケジュール削除エラー: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/calendar";
        } catch (Exception e) {
            System.err.println("スケジュール削除エラー: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "スケジュールの削除に失敗しました: " + e.getMessage());
            return "redirect:/calendar";
        }
    }

    @PostMapping("/schedule_delete/{scheduleId}")
    @ResponseBody
    public Map<String, Object> deleteScheduleAjax(HttpSession session,
                                                  HttpServletRequest request,
                                                  @PathVariable("scheduleId") Long scheduleId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = getUserIdAsLong(session);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "ログインしてください");
                return response;
            }

            System.out.println("=== スケジュール削除開始(Ajax) ===");
            System.out.println("scheduleId: " + scheduleId);
            System.out.println("userId: " + userId);

            Optional<ScheduleEntity> scheduleOpt = scheduleService.getScheduleById(scheduleId, userId);
            String scheduleName = scheduleOpt.map(ScheduleEntity::getTitle).orElse("不明");

            boolean deleted = scheduleService.deleteSchedule(scheduleId, userId);
            
            if (deleted) {
                System.out.println("スケジュール削除完了: ID=" + scheduleId);

                userActivityService.logScheduleDelete(userId.intValue(), scheduleName, request);
                
                response.put("success", true);
                response.put("message", "スケジュールを削除しました");
            } else {
                System.err.println("スケジュール削除失敗: ID=" + scheduleId);
                response.put("success", false);
                response.put("message", "スケジュールの削除に失敗しました");
            }

        } catch (IllegalStateException e) {
            // Google同期スケジュールの削除を試みた場合
            System.err.println("スケジュール削除エラー: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            System.err.println("スケジュール削除エラー: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "スケジュールの削除に失敗しました");
        }
        
        return response;
    }

    @PostMapping("/task_add")
    @ResponseBody
    public Map<String, Object> addTask(HttpSession session,
                                      HttpServletRequest request,
                                      @RequestParam("taskName") String taskName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = getUserIdAsLong(session);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "ログインしてください");
                return response;
            }
            
            if (taskName == null || taskName.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "タスク名を入力してください");
                return response;
            }
            
            TaskEntity task = scheduleService.createTask(taskName.trim(), userId);

            userActivityService.logTaskCreate(userId.intValue(), taskName.trim(), request);

            response.put("success", true);
            response.put("task", task);
            response.put("message", "タスクを追加しました");

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            System.err.println("タスク追加エラー: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "タスクの追加に失敗しました");
        }

        return response;
    }

    @GetMapping("/api/estimated_time")
    @ResponseBody
    public Map<String, String> getEstimatedTime(HttpSession session,
                                               @RequestParam("taskName") String taskName) {
        Long userId = getUserIdAsLong(session);
        if (userId == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "ログインしてください");
            return error;
        }

        return scheduleService.getEstimatedTaskTime(userId, taskName);
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