package com.example.routes

import com.example.models.CreateTaskRequest
import com.example.models.TaskRepository
import com.example.models.UpdateTaskRequest
import com.example.plugins.ErrorResponse
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
        get {
            call.respond(repository.all())
        }

        get("{id}") {
            val id =
                call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid ID"))

            val task =
                repository.findById(id)
                    ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("Task not found"))

            call.respond(task)
        }

        post {
            val request = call.receive<CreateTaskRequest>()
            if (request.title.isBlank()) {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Title is required"))
            }
            val task = repository.create(request)
            call.respond(HttpStatusCode.Created, task)
        }

        put("{id}") {
            val id =
                call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid ID"))

            val request = call.receive<UpdateTaskRequest>()
            val updated =
                repository.update(id, request)
                    ?: return@put call.respond(HttpStatusCode.NotFound, ErrorResponse("Task not found"))

            call.respond(updated)
        }

        delete("{id}") {
            val id =
                call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid ID"))

            if (repository.delete(id)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Task not found"))
            }
        }
    }
}
