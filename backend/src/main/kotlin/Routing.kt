package com.example

import com.example.routes.adminRoutes
import com.example.routes.authRoutes
import com.example.routes.listingImageRoute
import com.example.routes.listingsRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.example.service.AuthService
import com.example.service.AdminService
import favoritesRoutes

fun Application.configureRouting() {
    routing {
        authRoutes(AuthService())
        adminRoutes(AdminService())
        listingsRoutes()
        listingImageRoute() // Регистрируем маршрут для получения картинок
        favoritesRoutes() // Добавьте эту строку

        get("/") {
            call.respondText("Hello World!")
        }
    }
}
