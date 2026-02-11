package com.example.routes

import com.example.models.CreateUserRequest
import com.example.models.UserRepository
import com.example.models.UpdateUserRequest
import com.example.plugins.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * ユーザー管理のRESTエンドポイントを定義する
 *
 * エンドポイント一覧:
 *   GET    /users       - 全ユーザー取得
 *   GET    /users/{id}  - ユーザー1件取得
 *   POST   /users       - ユーザー作成
 *   PUT    /users/{id}  - ユーザー更新（部分更新）
 *   DELETE /users/{id}  - ユーザー削除
 */
fun Route.userRoutes() {
    route("/users") {

        get {
            call.respond(UserRepository.all())
        }

        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid ID"))

            val user = UserRepository.findById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("User not found"))

            call.respond(user)
        }

        post {
            val request = call.receive<CreateUserRequest>()
            if (request.name.isBlank()) {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Name is required"))
            }
            if (request.email.isBlank()) {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Email is required"))
            }
            val user = UserRepository.create(request)
            call.respond(HttpStatusCode.Created, user)
        }

        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid ID"))

            val request = call.receive<UpdateUserRequest>()
            val updated = UserRepository.update(id, request)
                ?: return@put call.respond(HttpStatusCode.NotFound, ErrorResponse("User not found"))

            call.respond(updated)
        }

        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid ID"))

            if (UserRepository.delete(id)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("User not found"))
            }
        }
    }
}
