package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class FieldError(val field: String, val message: String)

sealed class DomainError(val message: String) {
    data class NotFound(val detail: String) : DomainError(detail)

    data class BadRequest(val detail: String) : DomainError(detail)

    data class ValidationFailedError(val errors: List<FieldError>) : DomainError("Validation failed")
}
