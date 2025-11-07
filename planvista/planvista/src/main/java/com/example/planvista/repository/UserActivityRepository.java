package com.example.planvista.repository;

import com.example.planvista.model.entity.UserActivityEntity;

import java.util.List;

/**
 * ユーザーアクティビティリポジトリ
 */
public interface UserActivityRepository {
    
    /**
     * アクティビティを記録
     */
    void create(UserActivityEntity activity);
    
    /**
     * 指定ユーザーのアクティビティを取得（新しい順）
     */
    List<UserActivityEntity> getByUserId(Integer userId);
    
    /**
     * 指定ユーザーのアクティビティを取得（件数制限付き）
     */
    List<UserActivityEntity> getByUserIdWithLimit(Integer userId, int limit);
    
    /**
     * すべてのアクティビティを取得（新しい順）
     */
    List<UserActivityEntity> getAll();
}