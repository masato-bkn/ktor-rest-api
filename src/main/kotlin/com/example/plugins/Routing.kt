package com.example.plugins

import com.example.models.TaskRepository
import com.example.models.UserRepository
import com.example.routes.taskRoutes
import com.example.routes.userRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

/**
 * ルーティングの設定
 * 新しいリソースのルートを追加する場合はここに登録する
 */
fun Application.configureRouting(
    taskRepository: TaskRepository,
    userRepository: UserRepository,
) {
    routing {
        taskRoutes(taskRepository)
        userRoutes(userRepository)
    }
}
