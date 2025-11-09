package com.example.planvista.service;

import com.example.planvista.model.entity.TeamMemberEntity;
import com.example.planvista.model.entity.UserEntity;
import com.example.planvista.repository.TeammemberRepository;
import com.example.planvista.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * チームメンバー管理サービス
 */
@Service
public class TeamMemberService {
    
    private final TeammemberRepository teammemberRepository;
    private final UserRepository userRepository;
    
    public TeamMemberService(TeammemberRepository teammemberRepository,
                            UserRepository userRepository) {
        this.teammemberRepository = teammemberRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * メンバー一覧を取得
     * @param leaderUserId チームリーダーのユーザーID
     * @return メンバーリスト
     */
    public List<UserEntity> getMembers(Integer leaderUserId) {
        return teammemberRepository.getMembersByLeader(leaderUserId);
    }
    
    /**
     * メンバーを追加
     * @param leaderUserId チームリーダーのユーザーID
     * @param email 追加するメンバーのメールアドレス
     * @param username 追加するメンバーのユーザー名（任意）
     * @return 成功したらtrue
     */
    @Transactional
    public boolean addMember(Integer leaderUserId, String email, String username) {
        // メールアドレスでユーザーを検索
        UserEntity targetUser = userRepository.findByEmail(email);
        
        // メールアドレスで見つからない場合は、ユーザー名でも検索を試みる
        if (targetUser == null && username != null && !username.trim().isEmpty()) {
            targetUser = userRepository.findByUsername(username);
        }
        
        if (targetUser == null) {
            return false;
        }
        
        // 自分自身を追加しようとしていないかチェック
        if (targetUser.getId().equals(leaderUserId)) {
            throw new IllegalArgumentException("自分自身をメンバーに追加することはできません");
        }
        
        // 既に追加済みかチェック
        boolean alreadyExists = teammemberRepository.exists(leaderUserId, targetUser.getId());
        if (alreadyExists) {
            throw new IllegalStateException("このユーザーは既にメンバーに追加されています");
        }
        
        // チームメンバーとして追加
        TeamMemberEntity teamMember = new TeamMemberEntity();
        teamMember.setLeaderUserId(leaderUserId);
        teamMember.setMemberUserId(targetUser.getId());
        
        teammemberRepository.create(teamMember);
        
        return true;
    }
    
    /**
     * メンバーを削除
     * @param leaderUserId チームリーダーのユーザーID
     * @param memberId 削除するメンバーのユーザーID
     */
    @Transactional
    public void removeMember(Integer leaderUserId, Integer memberId) {
        teammemberRepository.delete(leaderUserId, memberId);
    }
    
    /**
     * メンバーのカレンダーを閲覧する権限があるかチェック
     * @param leaderUserId チームリーダーのユーザーID
     * @param memberId 閲覧対象のメンバーID
     * @return 権限があればtrue
     */
    public boolean canViewMember(Integer leaderUserId, Integer memberId) {
        return teammemberRepository.canView(leaderUserId, memberId);
    }
}