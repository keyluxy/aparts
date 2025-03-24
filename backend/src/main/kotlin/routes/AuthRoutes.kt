package com.example.routes

import com.example.routes.dto.RegisterRequest
import com.example.routes.dto.LoginRequest
import com.example.routes.dto.RegisterResponse
import com.example.routes.dto.LoginResponse
import com.example.routes.dto.ErrorResponse
import com.example.service.AuthService
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: AuthService) {
    post("/register") {
        try {
            val registerRequest = call.receive<RegisterRequest>()

            // Проверка обязательных полей
            if (registerRequest.firstName.isBlank() || registerRequest.lastName.isBlank()) {
                call.respond(ErrorResponse("First name and last name are required"))
                return@post
            }

            val userId = authService.register(
                registerRequest.email,
                registerRequest.password,
                registerRequest.firstName,
                registerRequest.lastName,
                registerRequest.middleName
            )
            call.respond(RegisterResponse(status = "registered", userId = userId))
        } catch (e: Exception) {
            call.respond(ErrorResponse(e.message ?: "Registration failed"))
        }
    }

    post("/login") {
        try {
            val loginRequest = call.receive<LoginRequest>()
            val token = authService.login(loginRequest.email, loginRequest.password)
            call.respond(LoginResponse(token = token))
        } catch (e: Exception) {
            call.respond(ErrorResponse(e.message ?: "Login failed"))
        }
    }
}
