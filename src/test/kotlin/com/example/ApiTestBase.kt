package com.example

import com.example.db.ExposedTaskRepository
import com.example.db.ExposedUserRepository
import com.example.models.CreateTaskRequest
import com.example.models.CreateUserRequest
import com.example.models.Task
import com.example.models.User
import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import com.example.plugins.configureStatusPages
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import org.junit.BeforeClass
import kotlin.test.BeforeTest

abstract class ApiTestBase {
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

    protected fun apiTest(block: suspend (HttpClient) -> Unit) =
        testApplication {
            application {
                configureSerialization()
                configureStatusPages()
                configureRouting(ExposedTaskRepository(), ExposedUserRepository())
            }
            block(
                createClient {
                    install(ContentNegotiation) { json() }
                },
            )
        }

    protected suspend fun HttpClient.createTask(
        title: String,
        description: String = "",
        assigneeId: Int? = null,
    ): Task =
        post("/tasks") {
            contentType(ContentType.Application.Json)
            setBody(CreateTaskRequest(title = title, description = description, assigneeId = assigneeId))
        }.body<Task>()

    protected suspend fun HttpClient.createUser(
        name: String,
        email: String,
    ): User =
        post("/users") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest(name = name, email = email))
        }.body<User>()
}
