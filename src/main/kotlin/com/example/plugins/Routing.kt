package com.example.plugins

import com.example.routes.taskRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

/**
 * ルーティングの設定
 * 新しいリソースのルートを追加する場合はここに登録する
 */
fun Application.configureRouting() {
    routing {
        taskRoutes()  // /tasks 以下のエンドポイント
    }
}
