package com.example.planvista.service;

import com.example.planvista.model.entity.PasswordResetTokenEntity;
import com.example.planvista.model.entity.UserEntity;
import com.example.planvista.repository.PasswordResetTokenRepository;
import com.example.planvista.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * パスワードリセットサービス
 */
@Service
public class PasswordResetService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    
    @Autowired
    private EmailService emailService;
    
    private static final int TOKEN_EXPIRATION_HOURS = 24;
    private static final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * パスワードリセットリクエストを処理
     * @param email メールアドレス
     * @return 処理が成功したかどうか
     */
    @Transactional
    public boolean requestPasswordReset(String email) {
        // ユーザーが存在するか確認
        UserEntity user = userRepository.getByEmail(email);
        if (user == null) {
            // セキュリティのため、ユーザーが存在しない場合も成功を返す
            return true;
        }
        
        // 既存の未使用トークンを無効化
        tokenRepository.invalidateTokensByEmail(email);
        
        // 新しいトークンを生成
        String token = generateSecureToken();
        
        // トークンエンティティを作成
        PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity();
        resetToken.setEmail(email);
        resetToken.setToken(token);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS));
        resetToken.setUsed(false);
        
        // トークンを保存
        tokenRepository.create(resetToken);
        
        // メールを送信
        try {
            emailService.sendPasswordResetEmail(email, token);
        } catch (Exception e) {
            System.err.println("メール送信エラー: " + e.getMessage());
            // メール送信に失敗してもトークンは作成済みなのでtrueを返す
        }
        
        return true;
    }
    
    /**
     * トークンの有効性を確認
     * @param token トークン文字列
     * @return トークンが有効な場合はtrue
     */
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        PasswordResetTokenEntity resetToken = tokenRepository.findByToken(token);
        return resetToken != null && resetToken.isValid();
    }
    
    /**
     * パスワードをリセット
     * @param token トークン文字列
     * @param newPassword 新しいパスワード
     * @return 処理が成功したかどうか
     */
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        // トークンを検索
        PasswordResetTokenEntity resetToken = tokenRepository.findByToken(token);
        if (resetToken == null || !resetToken.isValid()) {
            return false;
        }
        
        // ユーザーを検索
        UserEntity user = userRepository.getByEmail(resetToken.getEmail());
        if (user == null) {
            return false;
        }
        
        // パスワードを更新
        user.setPassword(newPassword);
        userRepository.updateById(user.getId(), user);
        
        // トークンを使用済みにする
        tokenRepository.markAsUsed(resetToken.getId());
        
        // パスワード変更完了メールを送信
        try {
            emailService.sendPasswordChangedEmail(user.getEmail());
        } catch (Exception e) {
            System.err.println("メール送信エラー: " + e.getMessage());
        }
        
        return true;
    }
    
    /**
     * 期限切れトークンを削除
     */
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens();
    }
    
    /**
     * トークンからメールアドレスを取得
     * @param token トークン文字列
     * @return メールアドレス（トークンが無効な場合はnull）
     */
    public String getEmailFromToken(String token) {
        PasswordResetTokenEntity resetToken = tokenRepository.findByToken(token);
        if (resetToken != null && resetToken.isValid()) {
            return resetToken.getEmail();
        }
        return null;
    }
    
    /**
     * 安全なランダムトークンを生成
     * @return トークン文字列
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}