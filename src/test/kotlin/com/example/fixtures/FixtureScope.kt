package com.example.fixtures

import com.example.factories.UserFactory
import com.example.models.User

class FixtureScope {
    suspend fun user(
        name: String,
        email: String,
        block: suspend UserFixtureScope.() -> Unit,
    ): User {
        // `block: suspend UserFixtureScope.() -> Unit` -> UserFixtureScope のメソッドとして書かれたラムダ
        val user = UserFactory.create(name, email)
        UserFixtureScope(user).block()

        return user
    }
}
