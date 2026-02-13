package com.example

import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.testcontainers.containers.PostgreSQLContainer

// =============================================================================
// TestDatabaseFactory - テスト用 DB ヘルパー
// =============================================================================
// Testcontainers で PostgreSQL コンテナをシングルトン管理し、全テストで共有する。
// Rails でいう database_cleaner + テスト用DB設定 に相当。
//
// ライフサイクル:
//   1. init()  - テストスイート開始時に1回呼ぶ（コンテナ起動 → マイグレーション → 接続）
//   2. clean() - 各テストの前に呼ぶ（Flyway clean → migrate でデータ完全リセット）
//
// clean() は flyway_schema_history を含む全テーブルを DROP してから再作成するため、
// TRUNCATE と違いシーケンス（SERIAL の連番）もリセットされる。
// =============================================================================

object TestDatabaseFactory {
    private val container =
        PostgreSQLContainer("postgres:16").apply {
            withDatabaseName("test_db")
            withUsername("test")
            withPassword("test")
        }

    // lateinit: init() で初期化される。テスト実行前に必ず init() を呼ぶ前提。
    private lateinit var flyway: Flyway

    /** コンテナ起動 → Flyway マイグレーション → DB接続（テストスイート全体で1回だけ呼ぶ） */
    fun init() {
        if (!container.isRunning) {
            container.start()
        }

        flyway =
            Flyway.configure()
                .dataSource(container.jdbcUrl, container.username, container.password)
                // Flyway 9+ ではデフォルトで clean() が無効化されている（本番誤操作防止）。
                // テスト環境では各テスト前にDBをリセットする必要があるため、明示的に有効化する。
                .cleanDisabled(false)
                .load()

        // src/main/resources/db/migration/ の SQL ファイルを適用
        flyway.migrate()

        Database.connect(
            url = container.jdbcUrl,
            driver = "org.postgresql.Driver",
            user = container.username,
            password = container.password,
        )
    }

    /**
     * Flyway clean → migrate でスキーマとデータを完全リセットする。
     * clean(): flyway_schema_history を含む全オブジェクトを DROP
     * migrate(): マイグレーション SQL を最初から再適用
     */
    fun clean() {
        flyway.clean()
        flyway.migrate()
    }
}
