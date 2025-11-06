package com.example.planvista.repository;

import com.example.planvista.model.entity.TaskEntity;

import java.util.List;
import java.util.Optional;

/**
 * タスクリポジトリインターフェース
 */
public interface TaskRepository {

    /**
     * ユーザーIDで全タスクを取得
     */
    List<TaskEntity> findByUserId(Long userId);

    /**
     * IDでタスクを取得
     */
    TaskEntity findById(Long id);

    /**
     * タスク名とユーザーIDでタスクを検索
     * タスク重複チェックに使用
     */
    Optional<TaskEntity> findByTaskNameAndUserId(String taskName, Long userId);

    /**
     * タスク名とユーザーIDで存在チェック
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
    
    /**
     * タスクを保存（新規作成または更新）
     */
    TaskEntity save(TaskEntity task);

    /**
     * 全タスクを取得
     */
    List<TaskEntity> findAll();

    /**
     * タスク名でタスクを検索
     */
    TaskEntity findByTaskName(String taskName);
}