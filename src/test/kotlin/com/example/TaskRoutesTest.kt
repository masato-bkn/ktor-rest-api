package com.example

import com.example.db.ExposedTaskRepository
import com.example.db.ExposedUserRepository
import com.example.models.CreateTaskRequest
import com.example.models.Task
import com.example.models.UpdateTaskRequest
import com.example.plugins.ErrorResponse
import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import com.example.plugins.configureStatusPages
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import org.junit.BeforeClass
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TaskRoutesTest {
    companion object {
        @JvmStatic
        @BeforeClass
        fun initDatabase() {
            TestDatabaseFactory.init()
        }
    }

    @BeforeTest
    fun setup() {
        TestDatabaseFactory.clean()
    }

    private fun ApplicationTestBuilder.jsonClient() =
        createClient {
            install(ContentNegotiation) { json() }
        }

    private fun ApplicationTestBuilder.configureTestApplication() {
        application {
            configureSerialization()
            configureStatusPages()
            configureRouting(ExposedTaskRepository(), ExposedUserRepository())
        }
    }

    @Test
    fun `GET tasks returns empty list initially`() =
        testApplication {
            configureTestApplication()
            val client = jsonClient()

            val response = client.get("/tasks")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(emptyList<Task>(), response.body<List<Task>>())
        }

    @Test
    fun `POST tasks creates a new task`() =
        testApplication {
            configureTestApplication()
            val client = jsonClient()

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
        testApplication {
            configureTestApplication()
            val client = jsonClient()

            val createResponse =
                client.post("/tasks") {
                    contentType(ContentType.Application.Json)
                    setBody(CreateTaskRequest(title = "My Task"))
                }
            val created = createResponse.body<Task>()

            val response = client.get("/tasks/${created.id}")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("My Task", response.body<Task>().title)
        }

    @Test
    fun `GET tasks by invalid id returns 404`() =
        testApplication {
            configureTestApplication()
            val client = jsonClient()

            val response = client.get("/tasks/999")
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `PUT tasks updates the task`() =
        testApplication {
            configureTestApplication()
            val client = jsonClient()

            val createResponse =
                client.post("/tasks") {
                    contentType(ContentType.Application.Json)
                    setBody(CreateTaskRequest(title = "Original"))
                }
            val created = createResponse.body<Task>()

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
        testApplication {
            configureTestApplication()
            val client = jsonClient()

            val createResponse =
                client.post("/tasks") {
                    contentType(ContentType.Application.Json)
                    setBody(CreateTaskRequest(title = "To Delete"))
                }
            val created = createResponse.body<Task>()

            val deleteResponse = client.delete("/tasks/${created.id}")
            assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

            val getResponse = client.get("/tasks/${created.id}")
            assertEquals(HttpStatusCode.NotFound, getResponse.status)
        }

    // ========== 異常系テスト ==========

    @Test
    fun `GET tasks by non-numeric id returns 400`() =
        testApplication {
            configureTestApplication()
            val client = jsonClient()

            val response = client.get("/tasks/abc")
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Invalid ID", response.body<ErrorResponse>().message)
        }

    @Test
    fun `POST tasks with blank title returns 400`() =
        testApplication {
            configureTestApplication()
            val client = jsonClient()

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
        testApplication {
            configureTestApplication()
            val client = jsonClient()

            val response =
                client.post("/tasks") {
                    contentType(ContentType.Application.Json)
                    setBody("{invalid json}")
                }

            assertEquals(HttpStatusCode.InternalServerError, response.status)
        }

    @Test
    fun `PUT tasks with non-numeric id returns 400`() =
        testApplication {
            configureTestApplication()
            val client = jsonClient()

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
        testApplication {
            configureTestApplication()
            val client = jsonClient()

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
        testApplication {
            configureTestApplication()
            val client = jsonClient()

            val response = client.delete("/tasks/abc")
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Invalid ID", response.body<ErrorResponse>().message)
        }

    @Test
    fun `DELETE tasks with non-existent id returns 404`() =
        testApplication {
            configureTestApplication()
            val client = jsonClient()

            val response = client.delete("/tasks/999")
            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals("Task not found", response.body<ErrorResponse>().message)
        }
}
