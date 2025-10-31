-- schedulesテーブルにtask_idカラムを追加

ALTER TABLE schedules 
ADD COLUMN task_id BIGINT COMMENT 'タスクID' AFTER user_id,
ADD INDEX idx_task_id (task_id);

-- user_idの型をVARCHAR(10)からBIGINTに変更（将来的な互換性のため）
-- ※既存データがある場合は、事前にバックアップを取ってから実行してください
-- ALTER TABLE schedules MODIFY COLUMN user_id BIGINT NOT NULL COMMENT 'ユーザーID';