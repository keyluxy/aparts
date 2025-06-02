package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.auth.JwtConfig
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {
    authentication {
        jwt("auth-jwt") {
            verifier(
                JWT
                    .require(Algorithm.HMAC256(JwtConfig.secret))
                    .withAudience(JwtConfig.audience)
                    .withIssuer(JwtConfig.issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(JwtConfig.audience)) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
} 