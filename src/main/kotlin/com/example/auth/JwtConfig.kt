package com.example.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.ApplicationConfig
import java.util.Date

const val ONE_HOUR_MILLIS = 3_600_000

class JwtConfig(private val secret: String, val issuer: String, val audience: String, val realm: String) {
    fun generateToken(userId: Int, expiresAt: Date = Date(System.currentTimeMillis() + ONE_HOUR_MILLIS)): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("id", userId)
            .withExpiresAt(expiresAt)
            .sign(Algorithm.HMAC256(secret))
    }

    companion object {
        fun from(config: ApplicationConfig): JwtConfig {
            return JwtConfig(
                secret = config.property("jwt.secret").getString(),
                issuer = config.property("jwt.issuer").getString(),
                audience = config.property("jwt.audience").getString(),
                realm = config.property("jwt.realm").getString(),
            )
        }
    }
}
