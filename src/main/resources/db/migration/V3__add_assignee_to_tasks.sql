-- =============================================================================
-- V3: tasks に assignee_id（担当ユーザー）を追加
-- =============================================================================
-- Task と User のリレーション。1 User : N Tasks。
-- 担当者未割り当てを許容するため NULL OK。
-- User 削除時はタスクを残し assignee_id を NULL に戻す（SET NULL）。
--
-- 対応する Exposed 定義: com.example.db.Tasks.assigneeId
-- =============================================================================

ALTER TABLE tasks
    ADD COLUMN assignee_id INTEGER REFERENCES users(id) ON DELETE SET NULL;
