package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class FieldError(val field: String, val message: String)

sealed class DomainError(val message: String) {
    // 親の val message と衝突するため、サブクラス側は detail という名前で受けて親に渡す
    data class NotFound(val detail: String) : DomainError(detail)

    data class BadRequest(val detail: String) : DomainError(detail)

    data class ValidationFailedError(val errors: List<FieldError>) : DomainError("Validation failed")
}
