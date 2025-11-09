package com.example.planvista.repository;

import com.example.planvista.model.entity.TeamMemberEntity;
import com.example.planvista.model.entity.UserEntity;

import java.util.List;

/**
 * チームメンバーリポジトリインターフェース
 */
public interface TeammemberRepository {
    
    /**
     * チームメンバーを作成
     * @param teamMember チームメンバーエンティティ
     */
    void create(TeamMemberEntity teamMember);
    
    /**
     * リーダーのチームメンバーを取得
     * @param leaderUserId リーダーのユーザーID
     * @return メンバーのユーザーエンティティリスト
     */
    List<UserEntity> getMembersByLeader(Integer leaderUserId);
    
    /**
     * チームメンバーの存在確認
     * @param leaderUserId リーダーのユーザーID
     * @param memberUserId メンバーのユーザーID
     * @return 存在すればtrue
     */
    boolean exists(Integer leaderUserId, Integer memberUserId);
    
    /**
     * メンバーのカレンダーを閲覧できるか確認
     * @param leaderUserId リーダーのユーザーID
     * @param memberUserId メンバーのユーザーID
     * @return 閲覧可能であればtrue
     */
    boolean canView(Integer leaderUserId, Integer memberUserId);
    
    /**
     * チームメンバーを削除
     * @param leaderUserId リーダーのユーザーID
     * @param memberUserId メンバーのユーザーID
     */
    void delete(Integer leaderUserId, Integer memberUserId);
}