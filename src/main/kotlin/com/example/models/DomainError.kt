package com.example.models

sealed class DomainError(val message: String) {
    data class NotFound(val detail: String): DomainError(detail)
    data class BadRequest(val detail: String): DomainError(detail)
}
