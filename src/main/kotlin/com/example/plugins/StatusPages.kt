package com.example.plugins

import com.example.models.DomainError
import com.example.models.FieldError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val message: String)

@Serializable
data class ValidationErrorResponse(val errors: List<FieldError>)

/** 例外ハンドラは具体的な型 → 汎用の順に定義する（順序を逆にすると Throwable で全て捕まえてしまう） */
fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(cause.message ?: "Bad request"))
        }
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(cause.message ?: "Internal server error"),
            )
        }
    }
}

suspend fun ApplicationCall.handleDomainError(error: DomainError) {
    when (error) {
        is DomainError.NotFound -> respond(HttpStatusCode.NotFound, ErrorResponse(error.message))
        is DomainError.BadRequest -> respond(HttpStatusCode.BadRequest, ErrorResponse(error.message))
        is DomainError.ValidationFailedError -> respond(HttpStatusCode.BadRequest, ValidationErrorResponse(error.errors))
    }
}

suspend fun ApplicationCall.handleThrowable(error: Throwable) {
    respond(
        HttpStatusCode.InternalServerError,
        ErrorResponse(error.message ?: "Internal server error"),
    )
}
