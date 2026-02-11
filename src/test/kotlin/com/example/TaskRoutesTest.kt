package com.example

import com.example.models.CreateTaskRequest
import com.example.models.Task
import com.example.models.TaskRepository
import com.example.models.UpdateTaskRequest
import com.example.plugins.ErrorResponse
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

/**
 * TaskRoutes の統合テスト（Rails でいう request spec に相当）
 *
 * testApplication { } で実サーバーを起動せずに HTTP リクエスト/レスポンスをテストする。
 * 各テストは @BeforeTest でデータをリセットするため、テスト間の依存がない。
 */
class TaskRoutesTest {
    /** 各テスト前にリポジトリを初期化し、テスト間のデータ干渉を防ぐ */
    @BeforeTest
    fun setup() {
        TaskRepository.clear()
    }

    /**
     * JSON の送受信が可能なテスト用 HTTP クライアントを生成する。
     * サーバー側の ContentNegotiation と対になるクライアント側の設定。
     * Rails テストの `as: :json` を共通化しているイメージ。
     */
    private fun ApplicationTestBuilder.jsonClient() =
        createClient {
            install(ContentNegotiation) { json() }
        }

    @Test
    fun `GET tasks returns empty list initially`() =
        testApplication {
            val client = jsonClient()

            val response = client.get("/tasks")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(emptyList<Task>(), response.body<List<Task>>())
        }

    @Test
    fun `POST tasks creates a new task`() =
        testApplication {
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
            val client = jsonClient()

            // 事前データ作成（Rails の let! や before に相当）
            client.post("/tasks") {
                contentType(ContentType.Application.Json)
                setBody(CreateTaskRequest(title = "My Task"))
            }

            val response = client.get("/tasks/1")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("My Task", response.body<Task>().title)
        }

    @Test
    fun `GET tasks by invalid id returns 404`() =
        testApplication {
            val client = jsonClient()

            val response = client.get("/tasks/999")
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `PUT tasks updates the task`() =
        testApplication {
            val client = jsonClient()

            client.post("/tasks") {
                contentType(ContentType.Application.Json)
                setBody(CreateTaskRequest(title = "Original"))
            }

            val response =
                client.put("/tasks/1") {
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
            val client = jsonClient()

            client.post("/tasks") {
                contentType(ContentType.Application.Json)
                setBody(CreateTaskRequest(title = "To Delete"))
            }

            val deleteResponse = client.delete("/tasks/1")
            assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

            // 削除後に GET して 404 になることで、実際に消えたことを検証
            val getResponse = client.get("/tasks/1")
            assertEquals(HttpStatusCode.NotFound, getResponse.status)
        }

    // ========== 異常系テスト ==========

    @Test
    fun `GET tasks by non-numeric id returns 400`() =
        testApplication {
            val client = jsonClient()

            val response = client.get("/tasks/abc")
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Invalid ID", response.body<ErrorResponse>().message)
        }

    @Test
    fun `POST tasks with blank title returns 400`() =
        testApplication {
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
            val client = jsonClient()

            val response = client.delete("/tasks/abc")
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Invalid ID", response.body<ErrorResponse>().message)
        }

    @Test
    fun `DELETE tasks with non-existent id returns 404`() =
        testApplication {
            val client = jsonClient()

            val response = client.delete("/tasks/999")
            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals("Task not found", response.body<ErrorResponse>().message)
        }
}
