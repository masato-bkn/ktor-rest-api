package com.example

import com.example.db.ExposedTaskRepository
import com.example.db.ExposedUserRepository
import com.example.fixtures.FixtureScope
import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import com.example.plugins.configureStatusPages
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
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

    protected suspend fun <T> fixture(block: suspend FixtureScope.() -> T): T {
        return FixtureScope().block()
    }
}
