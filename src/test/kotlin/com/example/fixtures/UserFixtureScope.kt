package com.example.fixtures

import com.example.factories.TaskFactory
import com.example.models.Task
import com.example.models.User

data class UserWithTasks(val user: User, val tasks: List<Task>)

@FixtureDsl
class UserFixtureScope(val user: User) {
    private val tasks = mutableListOf<Task>()

    suspend fun task(
        title: String,
        description: String = "",
    ): Task {
        return TaskFactory.create(title, description, this.user.id).also { tasks.add(it) }
    }

    fun build(): UserWithTasks = UserWithTasks(user, tasks.toList())
}
