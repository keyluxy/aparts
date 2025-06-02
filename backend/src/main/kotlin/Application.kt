package com.example

import com.example.plugins.*
import com.example.service.AdminService
import com.example.service.AuthService
import com.example.service.UserService
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import com.example.database.db_factory.DatabaseInitializer
import com.example.routes.adminRoutes
import com.example.routes.authRoutes
import com.example.routes.userRoutes
import com.example.routes.listingsRoutes
import com.example.routes.listingImageRoute
import favoritesRoutes
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respondText
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Application")

fun main() {
    try {
        embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
            .start(wait = true)
    } catch (e: Exception) {
        logger.error("Failed to start server", e)
        throw e
    }
}

fun Application.module() {
    try {
        logger.info("Starting application initialization...")
        
        // Инициализация базы данных
        DatabaseInitializer.init()
        logger.info("Database initialized successfully")
        
        // Настройка безопасности
        configureSecurity()
        logger.info("Security configured successfully")
        
        // Настройка сериализации
        configureSerialization()
        logger.info("Serialization configured successfully")
        
        // Инициализация сервисов
        val authService = AuthService()
        val adminService = AdminService()
        val userService = UserService()
        logger.info("Services initialized successfully")
        
        // Настройка маршрутизации
        configureRouting(authService, adminService, userService)
        logger.info("Routing configured successfully")
        
        logger.info("Application initialized successfully")
    } catch (e: Exception) {
        logger.error("Failed to initialize application", e)
        throw e
    }
}

fun Application.configureRouting(
    authService: AuthService,
    adminService: AdminService,
    userService: UserService
) {
    routing {
        // Сначала регистрируем публичные маршруты
        get("/") {
            call.respondText("Hello World!")
        }
        
        // Маршруты аутентификации (регистрация и вход)
        authRoutes(authService)
        
        // Маршруты, требующие аутентификации
        authenticate("auth-jwt") {
            // Маршруты для работы с объявлениями
            listingsRoutes()
            
            // Маршруты для работы с избранным
            favoritesRoutes()
            
            // Маршруты для работы с изображениями
            listingImageRoute()
            
            // Маршруты для работы с пользователями
            userRoutes(userService)
            
            // Маршруты для администраторов
            adminRoutes(adminService)
        }
    }
}
