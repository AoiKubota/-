package com.example.planvista.repository;

import com.example.planvista.model.entity.PasswordResetTokenEntity;



public interface PasswordResetTokenRepository {
    

    void create(PasswordResetTokenEntity token);

    PasswordResetTokenEntity findByToken(String token);

    PasswordResetTokenEntity findValidTokenByEmail(String email);

    void markAsUsed(Long tokenId);

    void deleteExpiredTokens();

    void invalidateTokensByEmail(String email);
}
