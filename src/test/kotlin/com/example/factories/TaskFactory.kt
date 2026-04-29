package com.example.factories

import com.example.db.ExposedTaskRepository
import com.example.models.CreateTaskRequest
import com.example.models.Task

object TaskFactory {
    private val repository = ExposedTaskRepository()

    suspend fun create(
        title: String = "Test Task",
        description: String = "",
        assigneeId: Int? = null,
    ): Task = repository.create(CreateTaskRequest(title, description, assigneeId))
}
