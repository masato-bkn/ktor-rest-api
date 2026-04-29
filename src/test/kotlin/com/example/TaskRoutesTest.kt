package com.example

import com.example.factories.TaskFactory
import com.example.factories.UserFactory
import com.example.models.CreateTaskRequest
import com.example.models.Task
import com.example.models.UpdateTaskRequest
import com.example.plugins.ErrorResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TaskRoutesTest : ApiTestBase() {
    @Test
    fun `GET tasks returns empty list initially`() =
        apiTest { client ->
            val response = client.get("/tasks")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(emptyList<Task>(), response.body<List<Task>>())
        }

    @Test
    fun `POST tasks creates a new task`() =
        apiTest { client ->
            val response =
                client.post("/tasks") {
                    contentType(ContentType.Application.Json)
                    setBody(CreateTaskRequest(title = "Test Task", description = "A test"))
                }

            assertEquals(HttpStatusCode.Created, response.status)
            val task = response.body<Task>()
            assertEquals("Test Task", task.title)
            assertEquals("A test", task.description)
            assertEquals(false, task.completed)
        }

    @Test
    fun `GET tasks by id returns the task`() =
        apiTest { client ->
            val created = TaskFactory.create(title = "My Task")

            val response = client.get("/tasks/${created.id}")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("My Task", response.body<Task>().title)
        }

    @Test
    fun `GET tasks by invalid id returns 404`() =
        apiTest { client ->
            val response = client.get("/tasks/999")
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `PUT tasks updates the task`() =
        apiTest { client ->
            val created = TaskFactory.create(title = "Original")

            val response =
                client.put("/tasks/${created.id}") {
                    contentType(ContentType.Application.Json)
                    setBody(UpdateTaskRequest(title = "Updated", completed = true))
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val task = response.body<Task>()
            assertEquals("Updated", task.title)
            assertTrue(task.completed)
        }

    @Test
    fun `DELETE tasks removes the task`() =
        apiTest { client ->
            val created = TaskFactory.create()

            val deleteResponse = client.delete("/tasks/${created.id}")
            assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

            val getResponse = client.get("/tasks/${created.id}")
            assertEquals(HttpStatusCode.NotFound, getResponse.status)
        }

    // ========== 異常系テスト ==========

    @Test
    fun `GET tasks by non-numeric id returns 400`() =
        apiTest { client ->
            val response = client.get("/tasks/abc")
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Invalid ID", response.body<ErrorResponse>().message)
        }

    @Test
    fun `POST tasks with blank title returns 400`() =
        apiTest { client ->
            val response =
                client.post("/tasks") {
                    contentType(ContentType.Application.Json)
                    setBody(CreateTaskRequest(title = "   "))
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Title is required", response.body<ErrorResponse>().message)
        }

    @Test
    fun `POST tasks with invalid JSON returns 500`() =
        apiTest { client ->
            val response =
                client.post("/tasks") {
                    contentType(ContentType.Application.Json)
                    setBody("{invalid json}")
                }

            assertEquals(HttpStatusCode.InternalServerError, response.status)
        }

    @Test
    fun `PUT tasks with non-numeric id returns 400`() =
        apiTest { client ->
            val response =
                client.put("/tasks/abc") {
                    contentType(ContentType.Application.Json)
                    setBody(UpdateTaskRequest(title = "Updated"))
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Invalid ID", response.body<ErrorResponse>().message)
        }

    @Test
    fun `PUT tasks with non-existent id returns 404`() =
        apiTest { client ->
            val response =
                client.put("/tasks/999") {
                    contentType(ContentType.Application.Json)
                    setBody(UpdateTaskRequest(title = "Updated"))
                }

            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals("Task not found", response.body<ErrorResponse>().message)
        }

    @Test
    fun `DELETE tasks with non-numeric id returns 400`() =
        apiTest { client ->
            val response = client.delete("/tasks/abc")
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Invalid ID", response.body<ErrorResponse>().message)
        }

    @Test
    fun `DELETE tasks with non-existent id returns 404`() =
        apiTest { client ->
            val response = client.delete("/tasks/999")
            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals("Task not found", response.body<ErrorResponse>().message)
        }

    // ========== assignee リレーション ==========

    @Test
    fun `POST tasks with valid assigneeId stores assignee`() =
        apiTest { client ->
            val user = UserFactory.create(name = "Alice", email = "alice@example.com")

            val response =
                client.post("/tasks") {
                    contentType(ContentType.Application.Json)
                    setBody(CreateTaskRequest(title = "Assigned Task", assigneeId = user.id))
                }

            assertEquals(HttpStatusCode.Created, response.status)
            assertEquals(user.id, response.body<Task>().assigneeId)
        }

    @Test
    fun `POST tasks without assigneeId stores null`() =
        apiTest { client ->
            val response =
                client.post("/tasks") {
                    contentType(ContentType.Application.Json)
                    setBody(CreateTaskRequest(title = "Unassigned"))
                }

            assertEquals(HttpStatusCode.Created, response.status)
            assertEquals(null, response.body<Task>().assigneeId)
        }

    @Test
    fun `POST tasks with non-existent assigneeId returns 400`() =
        apiTest { client ->
            val response =
                client.post("/tasks") {
                    contentType(ContentType.Application.Json)
                    setBody(CreateTaskRequest(title = "x", assigneeId = 999))
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Assignee not found: 999", response.body<ErrorResponse>().message)
        }

    @Test
    fun `PUT tasks with valid assigneeId updates assignee`() =
        apiTest { client ->
            val user = UserFactory.create(name = "Bob", email = "bob@example.com")
            val task = TaskFactory.create()

            val response =
                client.put("/tasks/${task.id}") {
                    contentType(ContentType.Application.Json)
                    setBody(UpdateTaskRequest(assigneeId = user.id))
                }

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(user.id, response.body<Task>().assigneeId)
        }

    @Test
    fun `PUT tasks with non-existent assigneeId returns 400`() =
        apiTest { client ->
            val task = TaskFactory.create()

            val response =
                client.put("/tasks/${task.id}") {
                    contentType(ContentType.Application.Json)
                    setBody(UpdateTaskRequest(assigneeId = 999))
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Assignee not found: 999", response.body<ErrorResponse>().message)
        }

    @Test
    fun `DELETE user sets assigneeId of their tasks to null`() =
        apiTest { client ->
            val user = UserFactory.create(name = "Carol", email = "c@example.com")
            val task = TaskFactory.create(assigneeId = user.id)
            assertEquals(user.id, task.assigneeId)

            client.delete("/users/${user.id}")

            val refetched = client.get("/tasks/${task.id}").body<Task>()
            assertEquals(null, refetched.assigneeId)
        }
}
