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
import org.jetbrains.exposed.sql.Database
import java.io.File
//import io.ktor.server.plugins.cors.routing.*
import io.ktor.http.HttpMethod
import io.ktor.http.HttpHeaders

private val logger = LoggerFactory.getLogger("Application")

// Конфигурация путей к изображениям
object ImageConfig {
    // Базовый путь к папке с изображениями относительно корня проекта
    const val IMAGES_BASE_PATH = "uploads"
    
    // Получение абсолютного пути к папке с изображениями
    fun getImagesAbsolutePath(): String {
        return System.getProperty("user.dir") + "/" + IMAGES_BASE_PATH
    }
}

fun main() {
    // Создаем директорию для изображений, если она не существует
    val imagesDir = File(ImageConfig.getImagesAbsolutePath())
    if (!imagesDir.exists()) {
        imagesDir.mkdirs()
        logger.info("Created images directory at: ${imagesDir.absolutePath}")
    }

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
        
        // Настройка CORS
//        install(CORS) {
//            anyHost()
//            allowMethod(HttpMethod.Get)
//            allowMethod(HttpMethod.Post)
//            allowMethod(HttpMethod.Put)
//            allowMethod(HttpMethod.Delete)
//            allowMethod(HttpMethod.Options)
//            allowHeader(HttpHeaders.Authorization)
//            allowHeader(HttpHeaders.ContentType)
//            allowCredentials = true
//        }
        logger.info("CORS configured successfully")
        
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
        
        // Публичный маршрут для получения изображений
        listingImageRoute()
        
        // Маршруты аутентификации (регистрация и вход)
        authRoutes(authService)
        
        // Маршруты, требующие аутентификации
        authenticate("auth-jwt") {
            // Маршруты для работы с объявлениями
            listingsRoutes()
            
            // Маршруты для работы с избранным
            favoritesRoutes()
            
            // Маршруты для работы с пользователями
            userRoutes(userService)
            
            // Маршруты для администраторов
            adminRoutes(adminService)
        }
    }
}


