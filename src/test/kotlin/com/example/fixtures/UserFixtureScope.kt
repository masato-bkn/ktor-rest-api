package com.example.fixtures

import com.example.factories.TaskFactory
import com.example.models.Task
import com.example.models.User

class UserFixtureScope(val user: User) {
    suspend fun task(
        title: String,
        description: String = "",
    ): Task {
        return TaskFactory.create(title, description, this.user.id)
    }
}
