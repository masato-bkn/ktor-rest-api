package com.example.plugins

import com.example.models.DomainError
import com.example.models.Either
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.routing.Route
import io.ktor.util.pipeline.PipelineContext
import io.ktor.server.response.respond
import io.ktor.server.application.call

inline fun <reified T: Any> createHandler (
    successHttpStatusCode: HttpStatusCode,
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Either<DomainError, T>
): Route.() -> Unit = {
    handle {
        when (val result = body.invoke(this, Unit)) {
            is Either.Left -> call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(result.value.message)
            )
            is Either.Right -> call.respond(successHttpStatusCode, result.value)
        }
    }
}
