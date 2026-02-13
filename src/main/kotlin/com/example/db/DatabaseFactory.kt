package com.example.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

// =============================================================================
// DatabaseFactory - DB接続とマイグレーションの初期化
// =============================================================================
// Rails でいう config/database.yml + db:migrate を1つにまとめたもの。
// アプリケーション起動時に以下を順に実行する:
//   1. HikariCP でコネクションプールを作成
//   2. Flyway でマイグレーション（未適用の SQL ファイルを実行）
//   3. Exposed を DB に接続
//
// マイグレーション SQL は src/main/resources/db/migration/ に配置する。
// Flyway は flyway_schema_history テーブルで適用履歴を管理し、
// 適用済みファイルはスキップするため、起動のたびに全 SQL が再実行されることはない。
// =============================================================================

/** application.conf の database セクションから DB 接続を初期化する */
fun Application.configureDatabases() {
    val config = environment.config

    // HikariCP: DBコネクションプールの設定
    // application.conf から接続情報を読み取り、接続の使い回しを管理する
    val hikariConfig =
        HikariConfig().apply {
            jdbcUrl = config.property("database.url").getString()
            driverClassName = config.property("database.driver").getString()
            username = config.property("database.user").getString()
            password = config.property("database.password").getString()
            maximumPoolSize = 10
            // Exposed が自前でトランザクション管理するため、JDBC の自動コミットは無効にする
            isAutoCommit = false
            validate()
        }

    val dataSource = HikariDataSource(hikariConfig)

    // Flyway マイグレーション実行（Rails の db:migrate に相当）
    // src/main/resources/db/migration/V{番号}__{説明}.sql を番号順に適用する。
    // DB接続（Database.connect）より先に実行することで、
    // Exposed がテーブルを参照する時点ではスキーマが確実に存在する。
    Flyway.configure().dataSource(dataSource).load().migrate()

    // Exposed の DB接続を確立（以降、transaction {} ブロックでクエリ実行可能になる）
    Database.connect(dataSource)
}

/** Dispatchers.IO 上で Exposed トランザクションを実行するヘルパー */
suspend fun <T> suspendTransaction(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }
