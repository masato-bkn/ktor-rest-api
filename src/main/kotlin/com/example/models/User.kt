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

/**
 * ユーザー更新時のリクエストボディ（PUT /users/{id} で使用）
 * 全フィールドがnullable → 送信されたフィールドのみ更新する（部分更新）
 */
@Serializable
data class UpdateUserRequest(
    val name: String? = null,
    val email: String? = null,
)

/** ユーザーのCRUD操作を提供するインメモリリポジトリ */
object UserRepository {
    private val users = mutableListOf<User>()
    private var nextId = 1

    fun all(): List<User> = users.toList()

    fun findById(id: Int): User? = users.find { it.id == id }

    fun create(request: CreateUserRequest): User {
        val user =
            User(
                id = nextId++,
                name = request.name,
                email = request.email,
            )
        users.add(user)
        return user
    }

    fun update(
        id: Int,
        request: UpdateUserRequest,
    ): User? {
        val index = users.indexOfFirst { it.id == id }
        if (index == -1) return null

        val existing = users[index]
        val updated =
            existing.copy(
                name = request.name ?: existing.name,
                email = request.email ?: existing.email,
            )
        users[index] = updated
        return updated
    }

    fun delete(id: Int): Boolean {
        return users.removeAll { it.id == id }
    }

    fun clear() {
        users.clear()
        nextId = 1
    }
}
