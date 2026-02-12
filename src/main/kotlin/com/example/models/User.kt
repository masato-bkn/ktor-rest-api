package com.example.models

import kotlinx.serialization.Serializable

/** ユーザーのレスポンスモデル */
@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String,
)

/** ユーザー作成時のリクエストボディ（POST /users で使用） */
@Serializable
data class CreateUserRequest(
    val name: String,
    val email: String,
)

/** ユーザー更新時のリクエストボディ（PUT /users/{id} で使用） */
@Serializable
data class UpdateUserRequest(
    val name: String? = null,
    val email: String? = null,
)

/** ユーザーのCRUD操作を定義するインターフェース */
interface UserRepository {
    suspend fun all(): List<User>

    suspend fun findById(id: Int): User?

    suspend fun create(request: CreateUserRequest): User

    suspend fun update(
        id: Int,
        request: UpdateUserRequest,
    ): User?

    suspend fun delete(id: Int): Boolean
}
