package com.example.planvista.repository;

import com.example.planvista.model.entity.UserEntity;

import java.util.List;

/**
 * ユーザーリポジトリインターフェース
 */
public interface UserRepository {
    
    /**
     * IDでユーザーを取得
     * @param id ユーザーID
     * @return ユーザーエンティティ（見つからない場合はnull）
     */
    UserEntity getById(Integer id);
    
    /**
     * メールアドレスでユーザーを検索
     * @param email メールアドレス
     * @return ユーザーエンティティ（見つからない場合はnull）
     */
    UserEntity findByEmail(String email);
    
    /**
     * ユーザー名でユーザーを検索
     * @param username ユーザー名
     * @return ユーザーエンティティ（見つからない場合はnull）
     */
    UserEntity findByUsername(String username);
    
    /**
     * メールアドレスとパスワードでユーザーを検索（ログイン用）
     * @param email メールアドレス
     * @param password パスワード
     * @return ユーザーエンティティ（見つからない場合はnull）
     */
    UserEntity findByEmailAndPassword(String email, String password);
    
    /**
     * 全ユーザーを取得
     * @return ユーザーリスト
     */
    List<UserEntity> findAll();
    
    /**
     * ユーザーを保存
     * @param user ユーザーエンティティ
     * @return 保存されたユーザーID
     */
    Integer save(UserEntity user);
    
    /**
     * ユーザーを削除
     * @param id ユーザーID
     */
    void deleteById(Integer id);
    
    /**
     * メールアドレスの存在確認
     * @param email メールアドレス
     * @return 存在すればtrue
     */
    boolean existsByEmail(String email);
}