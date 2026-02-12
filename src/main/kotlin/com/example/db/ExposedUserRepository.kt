package com.example.db

import com.example.models.CreateUserRequest
import com.example.models.UpdateUserRequest
import com.example.models.User
import com.example.models.UserRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class ExposedUserRepository : UserRepository {
    private fun resultRowToUser(row: ResultRow) =
        User(
            id = row[Users.id],
            name = row[Users.name],
            email = row[Users.email],
        )

    override suspend fun all(): List<User> =
        suspendTransaction {
            Users.selectAll().map(::resultRowToUser)
        }

    override suspend fun findById(id: Int): User? =
        suspendTransaction {
            Users.selectAll().where { Users.id eq id }
                .map(::resultRowToUser)
                .singleOrNull()
        }

    override suspend fun create(request: CreateUserRequest): User =
        suspendTransaction {
            val id =
                Users.insert {
                    it[name] = request.name
                    it[email] = request.email
                } get Users.id

            User(
                id = id,
                name = request.name,
                email = request.email,
            )
        }

    override suspend fun update(
        id: Int,
        request: UpdateUserRequest,
    ): User? =
        suspendTransaction {
            val existing =
                Users.selectAll().where { Users.id eq id }
                    .map(::resultRowToUser)
                    .singleOrNull() ?: return@suspendTransaction null

            Users.update({ Users.id eq id }) {
                it[name] = request.name ?: existing.name
                it[email] = request.email ?: existing.email
            }

            Users.selectAll().where { Users.id eq id }
                .map(::resultRowToUser)
                .single()
        }

    override suspend fun delete(id: Int): Boolean =
        suspendTransaction {
            Users.deleteWhere { Users.id eq id } > 0
        }
}
