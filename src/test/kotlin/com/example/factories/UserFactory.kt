package com.example.factories

import com.example.db.ExposedUserRepository
import com.example.models.CreateUserRequest
import com.example.models.User

object UserFactory {
    private val repository = ExposedUserRepository()

    suspend fun create(
        name: String = "Test User",
        email: String = "test@example.com",
    ): User = repository.create(CreateUserRequest(name, email))
}
