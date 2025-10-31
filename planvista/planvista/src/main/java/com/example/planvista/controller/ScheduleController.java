package com.example.planvista.controller;

import com.example.planvista.model.entity.ScheduleEntity;
import com.example.planvista.model.entity.TaskEntity;
import com.example.planvista.service.ScheduleService;
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

@Controller
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * スケジュール登録画面を表示
     */
    @GetMapping("/schedule_add")
    public String showScheduleAddForm(HttpSession session, Model model,
                                     @RequestParam(required = false) String date) {
        // ログインチェック
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        // ユーザーIDをLongに変換してタスク一覧を取得
        Long userIdLong = Long.parseLong(userId);
        List<TaskEntity> tasks = scheduleService.getUserTasks(userIdLong);
        model.addAttribute("tasks", tasks);

        // 日付が指定されている場合はセット
        if (date != null && !date.isEmpty()) {
            model.addAttribute("selectedDate", date);
        } else {
            model.addAttribute("selectedDate", LocalDate.now().toString());
        }

        return "schedule_add";
    }

    /**
     * スケジュールを登録
     */
    @PostMapping("/schedule_add")
    public String addSchedule(HttpSession session,
                            @RequestParam("scheduleName") String scheduleName,
                            @RequestParam("date") String date,
                            @RequestParam("startTime") String startTime,
                            @RequestParam("endTime") String endTime,
                            @RequestParam("task") String task,
                            @RequestParam(value = "memo", required = false) String memo,
                            RedirectAttributes redirectAttributes) {
        try {
            // ログインチェック
            String userId = (String) session.getAttribute("userId");
            if (userId == null) {
                return "redirect:/login";
            }

            // スケジュールエンティティを作成
            ScheduleEntity schedule = new ScheduleEntity();
            schedule.setUserId(userId);
            schedule.setScheduleName(scheduleName);
            schedule.setTask(task);
            
            // 日時を解析
            LocalDateTime startDateTime = LocalDateTime.parse(date + "T" + startTime);
            LocalDateTime endDateTime = LocalDateTime.parse(date + "T" + endTime);
            
            schedule.setStartTime(startDateTime);
            schedule.setEndTime(endDateTime);
            schedule.setMemo(memo);

            // バリデーション
            if (!scheduleService.validateSchedule(schedule)) {
                redirectAttributes.addFlashAttribute("error", "入力内容に誤りがあります。終了時刻は開始時刻より後に設定してください。");
                return "redirect:/schedule_add?date=" + date;
            }

            // スケジュールを保存
            scheduleService.createSchedule(schedule);

            redirectAttributes.addFlashAttribute("success", "スケジュールを登録しました");
            return "redirect:/calendar";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "スケジュールの登録に失敗しました: " + e.getMessage());
            return "redirect:/schedule_add";
        }
    }

    /**
     * スケジュール編集画面を表示
     */
    @GetMapping("/schedule_update")
    public String showScheduleUpdateForm(HttpSession session,
                                        @RequestParam("id") Integer scheduleId,
                                        Model model) {
        // ログインチェック
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        // スケジュールを取得
        ScheduleEntity schedule = scheduleService.getScheduleById(scheduleId, userId);
        if (schedule == null) {
            return "redirect:/calendar";
        }

        model.addAttribute("schedule", schedule);

        // タスク一覧を取得
        Long userIdLong = Long.parseLong(userId);
        List<TaskEntity> tasks = scheduleService.getUserTasks(userIdLong);
        model.addAttribute("tasks", tasks);

        // 日付と時刻を分割してモデルに追加
        model.addAttribute("date", schedule.getStartTime().toLocalDate().toString());
        model.addAttribute("startTime", schedule.getStartTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        model.addAttribute("endTime", schedule.getEndTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));

        // 推測所要時間を取得
        Map<String, String> estimatedTime = scheduleService.getEstimatedTaskTime(userId, schedule.getTask());
        model.addAttribute("estimatedTime", estimatedTime.get("estimatedTime"));

        return "schedule_update";
    }

    /**
     * スケジュールを更新
     */
    @PostMapping("/schedule_update")
    public String updateSchedule(HttpSession session,
                               @RequestParam("scheduleId") Integer scheduleId,
                               @RequestParam("scheduleName") String scheduleName,
                               @RequestParam("date") String date,
                               @RequestParam("startTime") String startTime,
                               @RequestParam("endTime") String endTime,
                               @RequestParam("task") String task,
                               @RequestParam(value = "memo", required = false) String memo,
                               RedirectAttributes redirectAttributes) {
        try {
            // ログインチェック
            String userId = (String) session.getAttribute("userId");
            if (userId == null) {
                return "redirect:/login";
            }

            // スケジュールエンティティを作成
            ScheduleEntity schedule = new ScheduleEntity();
            schedule.setScheduleName(scheduleName);
            schedule.setTask(task);
            
            // 日時を解析
            LocalDateTime startDateTime = LocalDateTime.parse(date + "T" + startTime);
            LocalDateTime endDateTime = LocalDateTime.parse(date + "T" + endTime);
            
            schedule.setStartTime(startDateTime);
            schedule.setEndTime(endDateTime);
            schedule.setMemo(memo);

            // バリデーション
            if (!scheduleService.validateSchedule(schedule)) {
                redirectAttributes.addFlashAttribute("error", "入力内容に誤りがあります。終了時刻は開始時刻より後に設定してください。");
                return "redirect:/schedule_update?id=" + scheduleId;
            }

            // スケジュールを更新
            scheduleService.updateSchedule(scheduleId, schedule, userId);

            redirectAttributes.addFlashAttribute("success", "スケジュールを更新しました");
            return "redirect:/calendar";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "スケジュールの更新に失敗しました: " + e.getMessage());
            return "redirect:/schedule_update?id=" + scheduleId;
        }
    }

    /**
     * スケジュールを削除
     */
    @PostMapping("/schedule_delete")
    public String deleteSchedule(HttpSession session,
                               @RequestParam("scheduleId") Integer scheduleId,
                               RedirectAttributes redirectAttributes) {
        try {
            // ログインチェック
            String userId = (String) session.getAttribute("userId");
            if (userId == null) {
                return "redirect:/login";
            }

            // 論理削除を実行
            scheduleService.logicalDeleteSchedule(scheduleId, userId);

            redirectAttributes.addFlashAttribute("success", "スケジュールを削除しました");
            return "redirect:/calendar";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "スケジュールの削除に失敗しました: " + e.getMessage());
            return "redirect:/calendar";
        }
    }

    /**
     * 新しいタスクを作成（Ajax用）
     */
    @PostMapping("/task_add")
    @ResponseBody
    public Map<String, Object> addTask(HttpSession session,
                                      @RequestParam("taskName") String taskName) {
        Map<String, Object> response = new java.util.HashMap<>();
        
        try {
            // ログインチェック
            String userId = (String) session.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "ログインしてください");
                return response;
            }

            Long userIdLong = Long.parseLong(userId);
            TaskEntity task = scheduleService.createTask(taskName, userIdLong);

            response.put("success", true);
            response.put("task", task);
            response.put("message", "タスクを追加しました");

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "タスクの追加に失敗しました");
        }

        return response;
    }

    /**
     * タスクの推測時間を取得（Ajax用）
     */
    @GetMapping("/api/estimated_time")
    @ResponseBody
    public Map<String, String> getEstimatedTime(HttpSession session,
                                               @RequestParam("taskName") String taskName) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "ログインしてください");
            return error;
        }

        return scheduleService.getEstimatedTaskTime(userId, taskName);
    }
}