package com.example.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtConfig {
    private const val secret = "secret" // в реальном проекте следует хранить в конфигурации
    private const val issuer = "com.example"
    private const val audience = "com.example.audience"
    private const val validityInMs = 36_000_00 * 10 // 10 часов

    private val algorithm = Algorithm.HMAC256(secret)

    fun makeToken(userId: Int): String {
        return JWT.create()
            .withSubject("Authentication")
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("id", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
            .sign(algorithm)
    }
}
