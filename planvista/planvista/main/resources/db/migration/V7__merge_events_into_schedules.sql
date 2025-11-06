-- V7: eventsテーブルをschedulesテーブルに統合

-- ステップ1: schedulesテーブルに新しいカラムを追加
ALTER TABLE schedules
ADD COLUMN is_synced_from_google BOOLEAN DEFAULT FALSE COMMENT 'Googleカレンダーから同期されたスケジュールかどうか',
ADD COLUMN google_event_id VARCHAR(255) DEFAULT NULL COMMENT 'GoogleカレンダーのイベントID（同期時のみ）';

-- ステップ2: インデックスを追加
ALTER TABLE schedules
ADD INDEX idx_google_event_id (google_event_id);

-- ステップ3: schedule_nameをtitleに変更
ALTER TABLE schedules
CHANGE COLUMN schedule_name title VARCHAR(255) NOT NULL COMMENT 'スケジュールのタイトル';

-- ステップ4: eventsテーブルのデータをschedulesテーブルに移行
INSERT INTO schedules (
    user_id,
    title,
    start_time,
    end_time,
    task,
    memo,
    is_synced_from_google,
    google_event_id,
    created_at,
    updated_at
)
SELECT
    user_id,
    title,
    start_time,
    end_time,
    NULL AS task,
    description AS memo,
    TRUE AS is_synced_from_google,
    google_event_id,
    created_at,
    updated_at
FROM events;

-- ステップ5: eventsテーブルを削除
DROP TABLE IF EXISTS events;

-- ステップ6: コメントを更新
ALTER TABLE schedules COMMENT = 'スケジュール管理テーブル（手動登録とGoogle同期を統合）';