package com.example

import com.example.db.Tasks
import com.example.db.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.testcontainers.containers.PostgreSQLContainer

/**
 * テスト用 DB ヘルパー
 * Testcontainers で PostgreSQL コンテナをシングルトン管理し、全テストで共有する
 */
object TestDatabaseFactory {
    private val container =
        PostgreSQLContainer("postgres:16").apply {
            withDatabaseName("test_db")
            withUsername("test")
            withPassword("test")
        }

    /** コンテナ起動 → DB接続 → テーブル作成（テストスイート全体で1回だけ呼ぶ） */
    fun init() {
        if (!container.isRunning) {
            container.start()
        }

        Database.connect(
            url = container.jdbcUrl,
            driver = "org.postgresql.Driver",
            user = container.username,
            password = container.password,
        )

        transaction {
            SchemaUtils.create(Tasks, Users)
        }
    }

    /** テーブルを DROP → CREATE してデータとシーケンスをリセットする */
    fun clean() {
        transaction {
            SchemaUtils.drop(Tasks, Users)
            SchemaUtils.create(Tasks, Users)
        }
    }
}
