package com.example.models

import kotlinx.serialization.Serializable

/** タスクのレスポンスモデル */
@Serializable
data class Task(
    val id: Int,
    val title: String,
    val description: String = "",
    val completed: Boolean = false,
)

/** タスク作成時のリクエストボディ（POST /tasks で使用） */
@Serializable
data class CreateTaskRequest(
    val title: String,
    val description: String = "",
)

/** タスク更新時のリクエストボディ（PUT /tasks/{id} で使用） */
@Serializable
data class UpdateTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val completed: Boolean? = null,
)

/** タスクのCRUD操作を定義するインターフェース */
interface TaskRepository {
    suspend fun all(): List<Task>

    suspend fun findById(id: Int): Task?

    suspend fun create(request: CreateTaskRequest): Task

    suspend fun update(
        id: Int,
        request: UpdateTaskRequest,
    ): Task?

    suspend fun delete(id: Int): Boolean
}
