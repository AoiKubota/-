package com.example.planvista.service;

import com.example.planvista.model.entity.TaskEntity;
import com.example.planvista.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TaskService - タスク管理のビジネスロジック
 */
@Service
@Transactional
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    /**
     * 全タスクを取得
     * @return タスクのリスト
     */
    public List<TaskEntity> getAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * タスクIDでタスクを取得
     * @param taskId タスクID
     * @return TaskEntity
     */
    public TaskEntity getTaskById(Long taskId) {
        TaskEntity task = taskRepository.findById(taskId);
        if (task == null) {
            throw new RuntimeException("Task not found with id: " + taskId);
        }
        return task;
    }

    /**
     * タスク名でタスクを取得
     * @param taskName タスク名
     * @return TaskEntity（存在しない場合はnull）
     */
    public TaskEntity getTaskByName(String taskName) {
        return taskRepository.findByTaskName(taskName);
    }

    /**
     * 新規タスクを作成
     * @param taskName タスク名
     * @return 作成されたTaskEntity
     */
    public TaskEntity createTask(String taskName) {
        // 既に同じ名前のタスクが存在するかチェック
        TaskEntity existingTask = taskRepository.findByTaskName(taskName);
        if (existingTask != null) {
            throw new RuntimeException("Task with name '" + taskName + "' already exists");
        }

        TaskEntity task = new TaskEntity();
        task.setTaskName(taskName);
        task.setUserId(1L); // TODO: 実際のユーザーIDを設定

        return taskRepository.save(task);
    }

    /**
     * タスクを更新
     * @param taskId タスクID
     * @param taskName 新しいタスク名
     * @return 更新されたTaskEntity
     */
    public TaskEntity updateTask(Long taskId, String taskName) {
        TaskEntity task = getTaskById(taskId);

        // 他のタスクと名前が重複しないかチェック
        TaskEntity existingTask = taskRepository.findByTaskName(taskName);
        if (existingTask != null && !existingTask.getId().equals(taskId)) {
            throw new RuntimeException("Task with name '" + taskName + "' already exists");
        }

        task.setTaskName(taskName);
        task.setUpdatedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    /**
     * タスクを削除
     * @param taskId タスクID
     */
    public void deleteTask(Long taskId) {
        if (!existsById(taskId)) {
            throw new RuntimeException("Task not found with id: " + taskId);
        }
        taskRepository.delete(taskId);
    }

    /**
     * タスクが存在するかチェック
     * @param taskId タスクID
     * @return 存在する場合true
     */
    public boolean existsById(Long taskId) {
        return taskRepository.findById(taskId) != null;
    }

    /**
     * タスク名が存在するかチェック
     * @param taskName タスク名
     * @return 存在する場合true
     */
    public boolean existsByName(String taskName) {
        return taskRepository.findByTaskName(taskName) != null;
    }

    /**
     * タスク数を取得
     * @return タスクの総数
     */
    public long countTasks() {
        return taskRepository.findAll().size();
    }
}