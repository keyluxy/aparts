package com.example

import com.example.routes.authRoutes
import com.example.routes.listingsRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.example.service.AuthService

fun Application.configureRouting() {
    routing {
        authRoutes(AuthService())
        listingsRoutes()
        get("/") {
            call.respondText("Hello World!")
        }
    }
}
