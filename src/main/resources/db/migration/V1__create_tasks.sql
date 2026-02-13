-- =============================================================================
-- V1: tasks テーブル作成
-- =============================================================================
-- タスク管理の基本テーブル。
-- Exposed の Tables.kt (Tasks オブジェクト) と対応する。
-- カラムの型・制約は Tables.kt の定義と一致させる必要がある。
--
-- 対応する Exposed 定義: com.example.db.Tasks
-- =============================================================================

CREATE TABLE tasks (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    completed BOOLEAN NOT NULL DEFAULT FALSE
);
