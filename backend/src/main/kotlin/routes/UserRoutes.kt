package com.example.routes

import com.example.auth.JwtConfig
import com.example.routes.dto.ErrorResponse
import com.example.routes.dto.UserDto
import com.example.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("UserRoutes")

fun Route.userRoutes(userService: UserService) {
    route("/user") {
        authenticate("auth-jwt") {
            get("/info") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("id", Int::class) ?: run {
                    logger.error("Invalid token in user info request")
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))
                    return@get
                }

                try {
                    val userInfo = userService.getUserInfo(userId)
                    call.respond(userInfo)
                } catch (e: Exception) {
                    logger.error("Error getting user info", e)
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Failed to get user info"))
                }
            }
        }
    }
} 