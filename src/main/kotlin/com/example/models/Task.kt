package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: Int,
    val title: String,
    val description: String = "",
    val completed: Boolean = false
)

@Serializable
data class CreateTaskRequest(
    val title: String,
    val description: String = ""
)

@Serializable
data class UpdateTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val completed: Boolean? = null
)

object TaskRepository {
    private val tasks = mutableListOf<Task>()
    private var nextId = 1

    fun all(): List<Task> = tasks.toList()

    fun findById(id: Int): Task? = tasks.find { it.id == id }

    fun create(request: CreateTaskRequest): Task {
        val task = Task(
            id = nextId++,
            title = request.title,
            description = request.description
        )
        tasks.add(task)
        return task
    }

    fun update(id: Int, request: UpdateTaskRequest): Task? {
        val index = tasks.indexOfFirst { it.id == id }
        if (index == -1) return null

        val existing = tasks[index]
        val updated = existing.copy(
            title = request.title ?: existing.title,
            description = request.description ?: existing.description,
            completed = request.completed ?: existing.completed
        )
        tasks[index] = updated
        return updated
    }

    fun delete(id: Int): Boolean {
        return tasks.removeAll { it.id == id }
    }

    fun clear() {
        tasks.clear()
        nextId = 1
    }
}
