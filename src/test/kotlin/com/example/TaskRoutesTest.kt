package com.example

import com.example.models.CreateTaskRequest
import com.example.models.Task
import com.example.models.TaskRepository
import com.example.models.UpdateTaskRequest
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TaskRoutesTest {

    @BeforeTest
    fun setup() {
        TaskRepository.clear()
    }

    private fun ApplicationTestBuilder.jsonClient() = createClient {
        install(ContentNegotiation) { json() }
    }

    @Test
    fun `GET tasks returns empty list initially`() = testApplication {
        val client = jsonClient()

        val response = client.get("/tasks")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(emptyList<Task>(), response.body<List<Task>>())
    }

    @Test
    fun `POST tasks creates a new task`() = testApplication {
        val client = jsonClient()

        val response = client.post("/tasks") {
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
    fun `GET tasks by id returns the task`() = testApplication {
        val client = jsonClient()

        client.post("/tasks") {
            contentType(ContentType.Application.Json)
            setBody(CreateTaskRequest(title = "My Task"))
        }

        val response = client.get("/tasks/1")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("My Task", response.body<Task>().title)
    }

    @Test
    fun `GET tasks by invalid id returns 404`() = testApplication {
        val client = jsonClient()

        val response = client.get("/tasks/999")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `PUT tasks updates the task`() = testApplication {
        val client = jsonClient()

        client.post("/tasks") {
            contentType(ContentType.Application.Json)
            setBody(CreateTaskRequest(title = "Original"))
        }

        val response = client.put("/tasks/1") {
            contentType(ContentType.Application.Json)
            setBody(UpdateTaskRequest(title = "Updated", completed = true))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val task = response.body<Task>()
        assertEquals("Updated", task.title)
        assertTrue(task.completed)
    }

    @Test
    fun `DELETE tasks removes the task`() = testApplication {
        val client = jsonClient()

        client.post("/tasks") {
            contentType(ContentType.Application.Json)
            setBody(CreateTaskRequest(title = "To Delete"))
        }

        val deleteResponse = client.delete("/tasks/1")
        assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

        val getResponse = client.get("/tasks/1")
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }
}
