package com.example.planvista.controller;

import com.example.planvista.model.entity.TaskEntity;
import com.example.planvista.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TaskController - タスク管理のコントローラー
 */
@Controller
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * 新規タスク追加（Ajax）
     */
    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> addTask(@RequestParam String taskName) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (taskName == null || taskName.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "タスク名を入力してください");
                return response;
            }

            TaskEntity task = taskService.createTask(taskName.trim());

            response.put("success", true);
            response.put("taskId", task.getId());
            response.put("taskName", task.getTaskName());
            response.put("message", "タスクを追加しました");

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "タスクの追加に失敗しました");
        }

        return response;
    }

    /**
     * タスク一覧取得（Ajax）
     */
    @GetMapping("/list")
    @ResponseBody
    public Map<String, Object> getTaskList() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<TaskEntity> tasks = taskService.getAllTasks();

            response.put("success", true);
            response.put("tasks", tasks);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "タスクの取得に失敗しました");
        }

        return response;
    }

    /**
     * タスク更新（Ajax）
     */
    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> updateTask(
            @RequestParam Long taskId,
            @RequestParam String taskName) {
        
        Map<String, Object> response = new HashMap<>();

        try {
            if (taskName == null || taskName.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "タスク名を入力してください");
                return response;
            }

            TaskEntity task = taskService.updateTask(taskId, taskName.trim());

            response.put("success", true);
            response.put("task", task);
            response.put("message", "タスクを更新しました");

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "タスクの更新に失敗しました");
        }

        return response;
    }

    /**
     * タスク削除（Ajax）
     */
    @PostMapping("/delete")
    @ResponseBody
    public Map<String, Object> deleteTask(@RequestParam Long taskId) {
        Map<String, Object> response = new HashMap<>();

        try {
            taskService.deleteTask(taskId);

            response.put("success", true);
            response.put("message", "タスクを削除しました");

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "タスクの削除に失敗しました");
        }

        return response;
    }

    /**
     * タスク詳細取得（Ajax）
     */
    @GetMapping("/{taskId}")
    @ResponseBody
    public Map<String, Object> getTask(@PathVariable Long taskId) {
        Map<String, Object> response = new HashMap<>();

        try {
            TaskEntity task = taskService.getTaskById(taskId);

            response.put("success", true);
            response.put("task", task);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "タスクの取得に失敗しました");
        }

        return response;
    }
}