package com.example.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

/** application.conf の database セクションから DB 接続を初期化する */
fun Application.configureDatabases() {
    val config = environment.config
    val hikariConfig =
        HikariConfig().apply {
            jdbcUrl = config.property("database.url").getString()
            driverClassName = config.property("database.driver").getString()
            username = config.property("database.user").getString()
            password = config.property("database.password").getString()
            maximumPoolSize = 10
            isAutoCommit = false
            validate()
        }

    val dataSource = HikariDataSource(hikariConfig)
    Database.connect(dataSource)

    transaction {
        SchemaUtils.create(Tasks, Users)
    }
}

/** Dispatchers.IO 上で Exposed トランザクションを実行するヘルパー */
suspend fun <T> suspendTransaction(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }
