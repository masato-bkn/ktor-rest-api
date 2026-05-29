package com.example.auth

import com.auth0.jwt.JWT
import kotlin.test.Test
import kotlin.test.assertEquals

class JwtConfigTest {
    @Test
    fun `generateToken - returns new token including id`() {
        val token = JwtConfig("secret", "issuer", "audience", "realm").generateToken(1)
        val decodedToken = JWT.decode(token)
        val id = decodedToken.getClaim("id").asInt()
        assertEquals(1, id)
    }
}
