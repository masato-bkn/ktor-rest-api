package com.example.db

import com.example.models.CreateTaskRequest
import com.example.models.Task
import com.example.models.TaskRepository
import com.example.models.UpdateTaskRequest
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class ExposedTaskRepository : TaskRepository {
    private fun resultRowToTask(row: ResultRow) =
        Task(
            id = row[Tasks.id],
            title = row[Tasks.title],
            description = row[Tasks.description],
            completed = row[Tasks.completed],
        )

    override suspend fun all(): List<Task> =
        suspendTransaction {
            Tasks.selectAll().map(::resultRowToTask)
        }

    override suspend fun findById(id: Int): Task? =
        suspendTransaction {
            Tasks.selectAll().where { Tasks.id eq id }
                .map(::resultRowToTask)
                .singleOrNull()
        }

    override suspend fun create(request: CreateTaskRequest): Task =
        suspendTransaction {
            val id =
                Tasks.insert {
                    it[title] = request.title
                    it[description] = request.description
                } get Tasks.id

            Task(
                id = id,
                title = request.title,
                description = request.description,
            )
        }

    override suspend fun update(
        id: Int,
        request: UpdateTaskRequest,
    ): Task? =
        suspendTransaction {
            val existing =
                Tasks.selectAll().where { Tasks.id eq id }
                    .map(::resultRowToTask)
                    .singleOrNull() ?: return@suspendTransaction null

            Tasks.update({ Tasks.id eq id }) {
                it[title] = request.title ?: existing.title
                it[description] = request.description ?: existing.description
                it[completed] = request.completed ?: existing.completed
            }

            Tasks.selectAll().where { Tasks.id eq id }
                .map(::resultRowToTask)
                .single()
        }

    override suspend fun delete(id: Int): Boolean =
        suspendTransaction {
            Tasks.deleteWhere { Tasks.id eq id } > 0
        }
}
