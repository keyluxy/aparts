package com.example.routes

import com.example.routes.dto.RegisterRequest
import com.example.routes.dto.LoginRequest
import com.example.routes.dto.RegisterResponse
import com.example.routes.dto.LoginResponse
import com.example.routes.dto.ErrorResponse
import com.example.service.AuthService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("AuthRoutes")

fun Route.authRoutes(authService: AuthService) {
    post("/register") {
        try {
            val registerRequest = call.receive<RegisterRequest>()
            logger.info("Received registration request for email: ${registerRequest.email}")

            // Проверка обязательных полей
            if (registerRequest.firstName.isBlank() || registerRequest.lastName.isBlank()) {
                logger.warn("Registration failed: First name or last name is blank")
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("First name and last name are required"))
                return@post
            }

            val userId = authService.register(
                registerRequest.email,
                registerRequest.password,
                registerRequest.firstName,
                registerRequest.lastName,
                registerRequest.middleName
            )
            logger.info("Registration successful for user id: $userId")
            call.respond(HttpStatusCode.Created, RegisterResponse(status = "registered", userId = userId))
        } catch (e: IllegalArgumentException) {
            when (e.message) {
                "Email already registered" -> {
                    logger.warn("Registration failed: Email already registered")
                    call.respond(HttpStatusCode.Conflict, ErrorResponse("User already exists"))
                }
                else -> {
                    logger.error("Registration failed: ${e.message}")
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Registration failed"))
                }
            }
        } catch (e: Exception) {
            logger.error("Registration failed with unexpected error", e)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse(e.message ?: "Registration failed"))
        }
    }

    post("/login") {
        try {
            val loginRequest = call.receive<LoginRequest>()
            logger.info("Received login request for email: ${loginRequest.email}")

            val token = authService.login(loginRequest.email, loginRequest.password)
            logger.info("Login successful for email: ${loginRequest.email}")
            call.respond(HttpStatusCode.OK, LoginResponse(token = token))
        } catch (e: IllegalArgumentException) {
            when (e.message) {
                "User not found" -> {
                    logger.warn("Login failed: User not found")
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("User not found"))
                }
                "Invalid credentials" -> {
                    logger.warn("Login failed: Invalid credentials")
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid password"))
                }
                else -> {
                    logger.error("Login failed: ${e.message}")
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Login failed"))
                }
            }
        } catch (e: Exception) {
            logger.error("Login failed with unexpected error", e)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse(e.message ?: "Login failed"))
        }
    }
}
