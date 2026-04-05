package com.example.plugins

import com.example.models.DomainError
import com.example.models.Either
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.routing.Route
import io.ktor.util.pipeline.PipelineContext
import io.ktor.server.response.respond
import io.ktor.server.application.call
import io.ktor.server.routing.route

inline fun <reified T: Any> createHandler (
    successHttpStatusCode: HttpStatusCode,
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Either<DomainError, T>
): Route.() -> Unit = {
    handle {
        try {
            when (val result = body.invoke(this, Unit)) {
                is Either.Left -> call.handleDomainError(result.value)
                is Either.Right -> call.respond(successHttpStatusCode, result.value)
            }
        }
        catch (e: Throwable) {
            call.handleThrowable(e)
        }
    }
}

inline fun <reified T: Any> Route.handleGet(
    path: String = "",
    successHttpStatusCode: HttpStatusCode = HttpStatusCode.OK,
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Either<DomainError, T>
): Route {
    return route(path, HttpMethod.Get, createHandler<T>(successHttpStatusCode, body))
}
