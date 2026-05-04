package com.example

import com.example.factories.TaskFactory
import com.example.factories.UserFactory
import com.example.models.CreateUserRequest
import com.example.models.Task
import com.example.models.UpdateUserRequest
import com.example.models.User
import com.example.plugins.ErrorResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertEquals

class UserRoutesTest : ApiTestBase() {
    // ========== 正常系テスト ==========

    @Test
    fun `GET users returns empty list initially`() =
        apiTest { client ->
            val response = client.get("/users")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(emptyList<User>(), response.body<List<User>>())
        }

    @Test
    fun `POST users creates a new user`() =
        apiTest { client ->
            val response =
                client.post("/users") {
                    contentType(ContentType.Application.Json)
                    setBody(CreateUserRequest(name = "Alice", email = "alice@example.com"))
                }

            assertEquals(HttpStatusCode.Created, response.status)
            val user = response.body<User>()
            assertEquals("Alice", user.name)
            assertEquals("alice@example.com", user.email)
        }

    @Test
    fun `GET users by id returns the user`() =
        apiTest { client ->
            val created = UserFactory.create(name = "Bob", email = "bob@example.com")

            val response = client.get("/users/${created.id}")
            assertEquals(HttpStatusCode.OK, response.status)
            val user = response.body<User>()
            assertEquals("Bob", user.name)
            assertEquals("bob@example.com", user.email)
        }

    @Test
    fun `GET users returns all users`() =
        apiTest { client ->
            UserFactory.create(name = "Alice", email = "alice@example.com")
            UserFactory.create(name = "Bob", email = "bob@example.com")

            val response = client.get("/users")
            assertEquals(HttpStatusCode.OK, response.status)
            val users = response.body<List<User>>()
            assertEquals(2, users.size)
        }

    @Test
    fun `PUT users updates the user`() =
        apiTest { client ->
            val created = UserFactory.create(name = "Alice", email = "alice@example.com")

            val response =
                client.put("/users/${created.id}") {
                    contentType(ContentType.Application.Json)
                    setBody(UpdateUserRequest(name = "Alice Updated"))
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val user = response.body<User>()
            assertEquals("Alice Updated", user.name)
            assertEquals("alice@example.com", user.email)
        }

    @Test
    fun `DELETE users removes the user`() =
        apiTest { client ->
            val created = UserFactory.create(name = "Alice", email = "alice@example.com")

            val deleteResponse = client.delete("/users/${created.id}")
            assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

            val getResponse = client.get("/users/${created.id}")
            assertEquals(HttpStatusCode.NotFound, getResponse.status)
        }

    // ========== 異常系テスト ==========

    @Test
    fun `GET users by non-numeric id returns 400`() =
        apiTest { client ->
            val response = client.get("/users/abc")
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Invalid ID", response.body<ErrorResponse>().message)
        }

    @Test
    fun `GET users by non-existent id returns 404`() =
        apiTest { client ->
            val response = client.get("/users/999")
            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals("User not found", response.body<ErrorResponse>().message)
        }

    @Test
    fun `POST users with blank name returns 400`() =
        apiTest { client ->
            val response =
                client.post("/users") {
                    contentType(ContentType.Application.Json)
                    setBody(CreateUserRequest(name = "   ", email = "test@example.com"))
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Name is required", response.body<ErrorResponse>().message)
        }

    @Test
    fun `POST users with blank email returns 400`() =
        apiTest { client ->
            val response =
                client.post("/users") {
                    contentType(ContentType.Application.Json)
                    setBody(CreateUserRequest(name = "Alice", email = "   "))
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Email is required", response.body<ErrorResponse>().message)
        }

    @Test
    fun `POST users with invalid JSON returns 500`() =
        apiTest { client ->
            val response =
                client.post("/users") {
                    contentType(ContentType.Application.Json)
                    setBody("{invalid json}")
                }

            assertEquals(HttpStatusCode.InternalServerError, response.status)
        }

    @Test
    fun `PUT users with non-numeric id returns 400`() =
        apiTest { client ->
            val response =
                client.put("/users/abc") {
                    contentType(ContentType.Application.Json)
                    setBody(UpdateUserRequest(name = "Updated"))
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Invalid ID", response.body<ErrorResponse>().message)
        }

    @Test
    fun `DELETE users with non-existent id returns 404`() =
        apiTest { client ->
            val response = client.delete("/users/999")
            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals("User not found", response.body<ErrorResponse>().message)
        }

    // ========== GET /users/{id}/tasks ==========

    @Test
    fun `GET users tasks returns tasks assigned to that user`() =
        apiTest { client ->
            val result =
                fixture {
                    user(name = "Alice", email = "a@example.com") {
                        task(title = "T1")
                        task(title = "T2")
                    }
                }
            TaskFactory.create(title = "Other")

            val response = client.get("/users/${result.user.id}/tasks")
            assertEquals(HttpStatusCode.OK, response.status)
            val tasks = response.body<List<Task>>()
            assertEquals(2, tasks.size)
            assertEquals(setOf("T1", "T2"), tasks.map { it.title }.toSet())
        }

    @Test
    fun `GET users tasks returns empty list when user has no tasks`() =
        apiTest { client ->
            val user = UserFactory.create(name = "Alice", email = "a@example.com")

            val response = client.get("/users/${user.id}/tasks")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(emptyList<Task>(), response.body<List<Task>>())
        }

    @Test
    fun `GET users tasks returns 404 when user does not exist`() =
        apiTest { client ->
            val response = client.get("/users/999/tasks")
            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals("User not found", response.body<ErrorResponse>().message)
        }

    @Test
    fun `GET users tasks with non-numeric id returns 400`() =
        apiTest { client ->
            val response = client.get("/users/abc/tasks")
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Invalid ID", response.body<ErrorResponse>().message)
        }
}
