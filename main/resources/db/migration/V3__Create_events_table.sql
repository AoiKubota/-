-- Googleカレンダー同期用のeventsテーブルを作成

CREATE TABLE IF NOT EXISTS events(
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    title VARCHAR(255) NOT NULL COMMENT 'イベントタイトル',
    description TEXT COMMENT 'イベント詳細',
    start_time DATETIME NOT NULL COMMENT '開始時間',
    end_time DATETIME NOT NULL COMMENT '終了時間',
    user_id BIGINT NOT NULL COMMENT 'ユーザーID',
    google_event_id VARCHAR(255) COMMENT 'GoogleカレンダーイベントID',
    is_synced_from_google BOOLEAN DEFAULT FALSE COMMENT 'Google同期フラグ',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    INDEX idx_user_id (user_id),
    INDEX idx_google_event_id (google_event_id),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB COMMENT='Googleカレンダー同期イベントテーブル';