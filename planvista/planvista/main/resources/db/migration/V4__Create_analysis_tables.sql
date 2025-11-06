-- レコード（実績）テーブルとタスクマスタテーブルを作成

-- タスクマスタテーブル
CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    task_name VARCHAR(100) NOT NULL COMMENT 'タスク名',
    user_id BIGINT NOT NULL COMMENT 'ユーザーID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    UNIQUE KEY unique_user_task (user_id, task_name),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB COMMENT='タスクマスタテーブル';

-- スケジュールテーブルにタスクIDを追加
ALTER TABLE events 
ADD COLUMN task_id BIGINT COMMENT 'タスクID' AFTER user_id,
ADD INDEX idx_task_id (task_id);

-- レコード（実績）テーブル
CREATE TABLE IF NOT EXISTS records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT 'ユーザーID',
    event_id BIGINT COMMENT '対応するスケジュールID',
    task_id BIGINT NOT NULL COMMENT 'タスクID',
    task_name VARCHAR(100) NOT NULL COMMENT 'タスク名',
    start_time DATETIME NOT NULL COMMENT '開始時間',
    end_time DATETIME NOT NULL COMMENT '終了時間',
    memo TEXT COMMENT 'メモ',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    INDEX idx_user_id (user_id),
    INDEX idx_event_id (event_id),
    INDEX idx_task_id (task_id),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB COMMENT='レコード（実績）テーブル';

-- デフォルトタスクをいくつか追加（サンプル）
INSERT INTO tasks (task_name, user_id) VALUES 
('会議', 1),
('メール確認', 1),
('資料作成', 1),
('日報', 1),
('その他', 1)
ON DUPLICATE KEY UPDATE task_name = task_name;