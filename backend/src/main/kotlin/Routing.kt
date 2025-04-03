package com.example

import com.example.routes.authRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.example.service.AuthService

fun Application.configureRouting() {
    routing {
        authRoutes(AuthService())
        get("/") {
            call.respondText("Hello World!")
        }
    }
}
