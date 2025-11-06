-- パスワードリセットトークンテーブル
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    email VARCHAR(30) NOT NULL COMMENT 'メールアドレス',
    token VARCHAR(255) NOT NULL COMMENT 'リセットトークン',
    expires_at DATETIME NOT NULL COMMENT '有効期限',
    used BOOLEAN DEFAULT FALSE COMMENT '使用済みフラグ',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    INDEX idx_email (email),
    INDEX idx_token (token),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB COMMENT='パスワードリセットトークンテーブル';