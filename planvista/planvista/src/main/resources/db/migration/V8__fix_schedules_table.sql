-- schedulesテーブルのdeleted_at問題を修正
-- deleted_atはデフォルトでNULLであるべき（論理削除用）

ALTER TABLE schedules 
MODIFY COLUMN deleted_at DATETIME DEFAULT NULL COMMENT '削除日時';

-- task_timeカラムがNULLを許可するように変更
ALTER TABLE schedules 
MODIFY COLUMN task_time INT DEFAULT NULL COMMENT '所要時間（分）';

-- taskカラムもNULLを許可（Google同期の場合はNULL可能）
ALTER TABLE schedules 
MODIFY COLUMN task VARCHAR(255) DEFAULT NULL COMMENT 'タスク名';