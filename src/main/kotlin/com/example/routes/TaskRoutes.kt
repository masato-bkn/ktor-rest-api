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
 *
 * エラー時は ErrorResponse の JSON を返す
 *
 * NOTE: return@get, return@post 等はラベル付きreturn（Ktor固有ではなくKotlinの言語機能）。
 * ラムダの中では素の return が使えないため、@ラベルで「どのラムダから抜けるか」を指定する。
 * Ruby の next に近い（ブロックの残りをスキップしてエラーレスポンスを返す）。
 */
fun Route.taskRoutes() {
    route("/tasks") {
        // GET /tasks - 全タスク取得
        // レスポンス: 200 OK + Task の JSON 配列
        get {
            call.respond(TaskRepository.all())
        }

        // GET /tasks/{id} - タスク1件取得
        // レスポンス: 200 OK + Task / 404 Not Found
        get("{id}") {
            val id =
                call.parameters["id"]?.toIntOrNull()
                    // パスパラメータが数値でない場合は 400 を返す
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid ID"))

            val task =
                TaskRepository.findById(id)
                    ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("Task not found"))

            call.respond(task)
        }

        // POST /tasks - タスク作成
        // リクエストボディ: { "title": "...", "description": "..." }
        // レスポンス: 201 Created + 作成された Task
        post {
            // call.receive<T>() は ContentNegotiation により JSON → オブジェクト自動変換
            val request = call.receive<CreateTaskRequest>()
            if (request.title.isBlank()) {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Title is required"))
            }
            val task = TaskRepository.create(request)
            call.respond(HttpStatusCode.Created, task)
        }

        // PUT /tasks/{id} - タスク更新（部分更新）
        // リクエストボディ: { "title": "...", "completed": true } （変更したいフィールドのみ）
        // レスポンス: 200 OK + 更新後の Task / 404 Not Found
        put("{id}") {
            val id =
                call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid ID"))

            val request = call.receive<UpdateTaskRequest>()
            val updated =
                TaskRepository.update(id, request)
                    ?: return@put call.respond(HttpStatusCode.NotFound, ErrorResponse("Task not found"))

            call.respond(updated)
        }

        // DELETE /tasks/{id} - タスク削除
        // レスポンス: 204 No Content（成功）/ 404 Not Found
        delete("{id}") {
            val id =
                call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid ID"))

            if (TaskRepository.delete(id)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Task not found"))
            }
        }
    }
}
