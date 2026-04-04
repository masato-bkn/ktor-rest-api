package com.example.models

sealed class Either<L, R> {
    data class Left<L> (val value : L):  Either<L, Nothing>()
    data class Right<R> (val value : R) : Either<Nothing, R>()
}
