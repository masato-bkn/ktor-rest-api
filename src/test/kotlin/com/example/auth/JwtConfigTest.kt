package com.example.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import kotlin.test.Test
import kotlin.test.assertEquals

class JwtConfigTest {
    private fun decodedToken(userId: Int = 1, issuer: String = "issuer", audience: String = "audience"): DecodedJWT {
        val token = JwtConfig("secret", issuer, audience, "realm").generateToken(userId)
        return JWT.decode(token)
    }

    @Test
    fun `generateToken - returns new token including id`() {
        val expectedUserId = 100
        val id = decodedToken(userId = expectedUserId).getClaim("id").asInt()
        assertEquals(expectedUserId, id)
    }

    @Test
    fun `generateToken - returns new token including issuer`() {
        val expectedIssuer = "expectedIssuer"
        val issuer = decodedToken(issuer = expectedIssuer).issuer
        assertEquals(expectedIssuer, issuer)
    }

    @Test
    fun `generateToken - returns new token including audience`() {
        val expectedAudience = "expectedAudience"
        val audience = decodedToken(audience = expectedAudience).audience
        assertEquals(listOf(expectedAudience), audience)
    }
}
