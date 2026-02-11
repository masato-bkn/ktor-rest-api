package com.example

import com.example.models.CreateUserRequest
import com.example.models.User
import com.example.models.UserRepository
import com.example.models.UpdateUserRequest
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

class UserRoutesTest {

    @BeforeTest
    fun setup() {
        UserRepository.clear()
    }

    private fun ApplicationTestBuilder.jsonClient() = createClient {
        install(ContentNegotiation) { json() }
    }

    // ========== 正常系テスト ==========

    @Test
    fun `GET users returns empty list initially`() = testApplication {
        val client = jsonClient()

        val response = client.get("/users")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(emptyList<User>(), response.body<List<User>>())
    }

    @Test
    fun `POST users creates a new user`() = testApplication {
        val client = jsonClient()

        val response = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(name = "Alice", email = "alice@example.com"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val user = response.body<User>()
        assertEquals("Alice", user.name)
        assertEquals("alice@example.com", user.email)
    }

    @Test
    fun `GET users by id returns the user`() = testApplication {
        val client = jsonClient()

        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(name = "Bob", email = "bob@example.com"))
        }

        val response = client.get("/users/1")
        assertEquals(HttpStatusCode.OK, response.status)
        val user = response.body<User>()
        assertEquals("Bob", user.name)
        assertEquals("bob@example.com", user.email)
    }

    @Test
    fun `GET users returns all users`() = testApplication {
        val client = jsonClient()

        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(name = "Alice", email = "alice@example.com"))
        }
        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(name = "Bob", email = "bob@example.com"))
        }

        val response = client.get("/users")
        assertEquals(HttpStatusCode.OK, response.status)
        val users = response.body<List<User>>()
        assertEquals(2, users.size)
    }

    @Test
    fun `PUT users updates the user`() = testApplication {
        val client = jsonClient()

        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(name = "Alice", email = "alice@example.com"))
        }

        val response = client.put("/users/1") {
            contentType(ContentType.Application.Json)
            setBody(UpdateUserRequest(name = "Alice Updated"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val user = response.body<User>()
        assertEquals("Alice Updated", user.name)
        assertEquals("alice@example.com", user.email) // emailは変更されない
    }

    @Test
    fun `DELETE users removes the user`() = testApplication {
        val client = jsonClient()

        client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(name = "Alice", email = "alice@example.com"))
        }

        val deleteResponse = client.delete("/users/1")
        assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

        val getResponse = client.get("/users/1")
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }

    // ========== 異常系テスト ==========

    @Test
    fun `GET users by non-numeric id returns 400`() = testApplication {
        val client = jsonClient()

        val response = client.get("/users/abc")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Invalid ID", response.body<ErrorResponse>().message)
    }

    @Test
    fun `GET users by non-existent id returns 404`() = testApplication {
        val client = jsonClient()

        val response = client.get("/users/999")
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals("User not found", response.body<ErrorResponse>().message)
    }

    @Test
    fun `POST users with blank name returns 400`() = testApplication {
        val client = jsonClient()

        val response = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(name = "   ", email = "test@example.com"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Name is required", response.body<ErrorResponse>().message)
    }

    @Test
    fun `POST users with blank email returns 400`() = testApplication {
        val client = jsonClient()

        val response = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(name = "Alice", email = "   "))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Email is required", response.body<ErrorResponse>().message)
    }

    @Test
    fun `POST users with invalid JSON returns 500`() = testApplication {
        val client = jsonClient()

        val response = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody("{invalid json}")
        }

        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }

    @Test
    fun `PUT users with non-numeric id returns 400`() = testApplication {
        val client = jsonClient()

        val response = client.put("/users/abc") {
            contentType(ContentType.Application.Json)
            setBody(UpdateUserRequest(name = "Updated"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Invalid ID", response.body<ErrorResponse>().message)
    }

    @Test
    fun `DELETE users with non-existent id returns 404`() = testApplication {
        val client = jsonClient()

        val response = client.delete("/users/999")
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals("User not found", response.body<ErrorResponse>().message)
    }
}
