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

fun Route.taskRoutes() {
    route("/tasks") {
        // GET /tasks - 全タスク取得
        get {
            call.respond(TaskRepository.all())
        }

        // GET /tasks/{id} - タスク1件取得
        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid ID"))

            val task = TaskRepository.findById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("Task not found"))

            call.respond(task)
        }

        // POST /tasks - タスク作成
        post {
            val request = call.receive<CreateTaskRequest>()
            if (request.title.isBlank()) {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Title is required"))
            }
            val task = TaskRepository.create(request)
            call.respond(HttpStatusCode.Created, task)
        }

        // PUT /tasks/{id} - タスク更新
        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid ID"))

            val request = call.receive<UpdateTaskRequest>()
            val updated = TaskRepository.update(id, request)
                ?: return@put call.respond(HttpStatusCode.NotFound, ErrorResponse("Task not found"))

            call.respond(updated)
        }

        // DELETE /tasks/{id} - タスク削除
        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid ID"))

            if (TaskRepository.delete(id)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Task not found"))
            }
        }
    }
}
