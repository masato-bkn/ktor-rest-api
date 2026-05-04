package com.example.fixtures

import com.example.factories.UserFactory

@FixtureDsl
class FixtureScope {
    suspend fun user(
        name: String,
        email: String,
        block: suspend UserFixtureScope.() -> Unit,
    ): UserWithTasks {
        // `block: suspend UserFixtureScope.() -> Unit` -> UserFixtureScope のメソッドとして書かれたラムダ
        val user = UserFactory.create(name, email)
        val scope = UserFixtureScope(user)
        scope.block()

        return scope.build()
    }
}
