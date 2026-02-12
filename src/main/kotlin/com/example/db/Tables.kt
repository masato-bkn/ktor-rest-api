package com.example.db

import org.jetbrains.exposed.sql.Table

/** Tasks テーブル定義 */
object Tasks : Table("tasks") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 255)
    val description = text("description").default("")
    val completed = bool("completed").default(false)

    override val primaryKey = PrimaryKey(id)
}

/** Users テーブル定義 */
object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val email = varchar("email", 255)

    override val primaryKey = PrimaryKey(id)
}
