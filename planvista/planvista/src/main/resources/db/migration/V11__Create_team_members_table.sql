-- チームメンバー管理テーブル（シンプル版）

-- チームメンバーテーブル
CREATE TABLE IF NOT EXISTS team_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    leader_user_id INT NOT NULL COMMENT 'チームリーダーID（追加した人）',
    member_user_id INT NOT NULL COMMENT 'チームメンバーID（追加された人）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',
    UNIQUE KEY unique_leader_member (leader_user_id, member_user_id),
    INDEX idx_leader_user_id (leader_user_id),
    INDEX idx_member_user_id (member_user_id)
) ENGINE=InnoDB COMMENT='チームメンバーテーブル';