-- ユーザーアクティビティテーブル
CREATE TABLE IF NOT EXISTS user_activities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id INT NOT NULL COMMENT 'ユーザーID',
    activity_type VARCHAR(50) NOT NULL COMMENT 'アクティビティタイプ（ログイン、ログアウト、スケジュール変更等）',
    activity_description VARCHAR(255) COMMENT 'アクティビティの詳細説明',
    ip_address VARCHAR(45) COMMENT 'IPアドレス',
    user_agent TEXT COMMENT 'ユーザーエージェント',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    INDEX idx_user_id (user_id),
    INDEX idx_activity_type (activity_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB COMMENT='ユーザーアクティビティログテーブル';