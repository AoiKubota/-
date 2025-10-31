package com.example.planvista.repository;

import com.example.planvista.model.entity.TaskEntity;
import java.util.List;

public interface TaskRepository {
    
    /**
     * ユーザーIDでタスク一覧を取得
     */
    List<TaskEntity> findByUserId(Long userId);
    
    /**
     * タスクIDでタスクを取得
     */
    TaskEntity findById(Long id);
    
    /**
     * タスク名とユーザーIDでタスクを検索
     */
    TaskEntity findByTaskNameAndUserId(String taskName, Long userId);
    
    /**
     * タスク名とユーザーIDの組み合わせが存在するかチェック
     */
    boolean existsByTaskNameAndUserId(String taskName, Long userId);
    
    /**
     * タスクを作成
     */
    TaskEntity create(TaskEntity task);
    
    /**
     * タスクを更新
     */
    void update(TaskEntity task);
    
    /**
     * タスクを削除
     */
    void delete(Long id);
}