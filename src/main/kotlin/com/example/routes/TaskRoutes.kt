package com.example.routes

import com.example.models.CreateTaskRequest
import com.example.models.DomainError
import com.example.models.Either
import com.example.models.Task
import com.example.models.TaskRepository
import com.example.models.UpdateTaskRequest
import com.example.plugins.handleDelete
import com.example.plugins.handleGet
import com.example.plugins.handlePost
import com.example.plugins.handlePut
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * タスク管理のRESTエンドポイントを定義する
 *
 * エンドポイント一覧:
 *   GET    /tasks       - 全タスク取得
 *   GET    /tasks/{id}  - タスク1件取得
 *   POST   /tasks       - タスク作成
 *   PUT    /tasks/{id}  - タスク更新（部分更新）
 *   DELETE /tasks/{id}  - タスク削除
 */
fun Route.taskRoutes(repository: TaskRepository) {
    route("/tasks") {
        handleGet<List<Task>> {
            Either.Right(repository.all())
        }

        handleGet<Task>("{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@handleGet Either.Left(DomainError.BadRequest("Invalid ID"))

            val task =
                repository.findById(id)
                    ?: return@handleGet Either.Left(DomainError.NotFound("Task not found"))

            Either.Right(task)
        }

        handlePost<Task> {
            val request = call.receive<CreateTaskRequest>()
            if (request.title.isBlank()) {
                return@handlePost Either.Left(DomainError.BadRequest("Title is required"))
            }
            val task = repository.create(request)
            Either.Right(task)
        }

        handlePut<Task>("{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@handlePut Either.Left(DomainError.BadRequest("Invalid ID"))

            val request = call.receive<UpdateTaskRequest>()
            val updated =
                repository.update(id, request)
                    ?: return@handlePut Either.Left(DomainError.NotFound("Task not found"))

            Either.Right(updated)
        }

        handleDelete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@handleDelete Either.Left(DomainError.BadRequest("Invalid ID"))

            if (repository.delete(id)) {
                Either.Right(Unit)
            } else {
                Either.Left(DomainError.NotFound("Task not found"))
            }
        }
    }
}
