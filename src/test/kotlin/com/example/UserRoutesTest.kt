package com.example

import com.example.db.ExposedTaskRepository
import com.example.db.ExposedUserRepository
import com.example.models.CreateUserRequest
import com.example.models.UpdateUserRequest
import com.example.models.User
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

class UserRoutesTest {
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

    // ========== 正常系テスト ==========

    @Test
    fun `GET users returns empty list initially`() =
        testApplication {
            configureTestApplication()
            val client = jsonClient()

            val response = client.get("/users")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(emptyList<User>(), response.body<List<User>>())
        }

    @Test
    fun `POST users creates a new user`() =
        testApplication {
            configureTestApplication()
            val client = jsonClient()

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
        testApplication {
            configureTestApplication()
            val client = jsonClient()

            val createResponse =
                client.post("/users") {
                    contentType(ContentType.Application.Json)
                    setBody(CreateUserRequest(name = "Bob", email = "bob@example.com"))
                }
            val created = createResponse.body<User>()

            val response = client.get("/users/${created.id}")
            assertEquals(HttpStatusCode.OK, response.status)
            val user = response.body<User>()
            assertEquals("Bob", user.name)
            assertEquals("bob@example.com", user.email)
        }

    @Test
    fun `GET users returns all users`() =
        testApplication {
            configureTestApplication()
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
    fun `PUT users updates the user`() =
        testApplication {
            configureTestApplication()
            val client = jsonClient()

            val createResponse =
                client.post("/users") {
                    contentType(ContentType.Application.Json)
                    setBody(CreateUserRequest(name = "Alice", email = "alice@example.com"))
                }
            val created = createResponse.body<User>()

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
        testApplication {
            configureTestApplication()
            val client = jsonClient()

            val createResponse =
                client.post("/users") {
                    contentType(ContentType.Application.Json)
                    setBody(CreateUserRequest(name = "Alice", email = "alice@example.com"))
                }
            val created = createResponse.body<User>()

            val deleteResponse = client.delete("/users/${created.id}")
            assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

            val getResponse = client.get("/users/${created.id}")
            assertEquals(HttpStatusCode.NotFound, getResponse.status)
        }

    // ========== 異常系テスト ==========

    @Test
    fun `GET users by non-numeric id returns 400`() =
        testApplication {
            configureTestApplication()
            val client = jsonClient()

            val response = client.get("/users/abc")
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("Invalid ID", response.body<ErrorResponse>().message)
        }

    @Test
    fun `GET users by non-existent id returns 404`() =
        testApplication {
            configureTestApplication()
            val client = jsonClient()

            val response = client.get("/users/999")
            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals("User not found", response.body<ErrorResponse>().message)
        }

    @Test
    fun `POST users with blank name returns 400`() =
        testApplication {
            configureTestApplication()
            val client = jsonClient()

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
        testApplication {
            configureTestApplication()
            val client = jsonClient()

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
        testApplication {
            configureTestApplication()
            val client = jsonClient()

            val response =
                client.post("/users") {
                    contentType(ContentType.Application.Json)
                    setBody("{invalid json}")
                }

            assertEquals(HttpStatusCode.InternalServerError, response.status)
        }

    @Test
    fun `PUT users with non-numeric id returns 400`() =
        testApplication {
            configureTestApplication()
            val client = jsonClient()

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
        testApplication {
            configureTestApplication()
            val client = jsonClient()

            val response = client.delete("/users/999")
            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals("User not found", response.body<ErrorResponse>().message)
        }
}
