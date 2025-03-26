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

fun Route.authRoutes(authService: AuthService) {
    post("/register") {
        try {
            val registerRequest = call.receive<RegisterRequest>()

            // Проверка обязательных полей
            if (registerRequest.firstName.isBlank() || registerRequest.lastName.isBlank()) {
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
            call.respond(HttpStatusCode.Created, RegisterResponse(status = "registered", userId = userId))
        } catch (e: IllegalArgumentException) {
            when (e.message) {
                "Email already registered" -> call.respond(HttpStatusCode.Conflict, ErrorResponse("User already exists"))
                else -> call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Registration failed"))
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse(e.message ?: "Registration failed"))
        }
    }

    post("/login") {
        try {
            val loginRequest = call.receive<LoginRequest>()
            val token = authService.login(loginRequest.email, loginRequest.password)
            call.respond(HttpStatusCode.OK, LoginResponse(token = token))
        } catch (e: IllegalArgumentException) {
            when (e.message) {
                "User not found" -> call.respond(HttpStatusCode.NotFound, ErrorResponse("User not found"))
                "Invalid credentials" -> call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid password"))
                else -> call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse(e.message ?: "Login failed"))
        }
    }
}
