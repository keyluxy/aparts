package com.example.routes

import com.example.auth.JwtConfig
import com.example.routes.dto.AdminListingRequest
import com.example.routes.dto.CityDto
import com.example.routes.dto.ErrorResponse
import com.example.routes.dto.UserDto
import com.example.routes.dto.CsvImportRequest
import com.example.routes.dto.CsvImportResponse
import com.example.service.AdminService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("AdminRoutes")

fun Route.adminRoutes(adminService: AdminService) {
    route("/admin") {
        authenticate("auth-jwt") {
            // Получение информации о пользователе
            get("/user") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("id", Int::class) ?: run {
                    logger.error("Invalid token in user info request")
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))
                    return@get
                }

                try {
                    val userInfo = adminService.getUserInfo(userId)
                    call.respond(userInfo)
                } catch (e: Exception) {
                    logger.error("Error getting user info", e)
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Failed to get user info"))
                }
            }

            // Проверка прав админа
            get("/check") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("id", Int::class) ?: run {
                    logger.error("Invalid token in admin check")
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))
                    return@get
                }

                logger.info("Checking admin status for user $userId")
                val isAdmin = adminService.isAdmin(userId)
                logger.info("Admin status for user $userId: $isAdmin")
                call.respond(mapOf("isAdmin" to isAdmin))
            }

            // Получение списка городов
            get("/cities") {
                try {
                    val cities = adminService.getCities()
                    call.respond(cities)
                } catch (e: Exception) {
                    logger.error("Error getting cities", e)
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Failed to get cities"))
                }
            }

            // Получение списка источников
            get("/sources") {
                try {
                    val sources = adminService.getSources()
                    call.respond(sources)
                } catch (e: Exception) {
                    logger.error("Error getting sources", e)
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Failed to get sources"))
                }
            }

            // Создание одного объявления
            post("/listings") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("id", Int::class) ?: run {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))
                    return@post
                }

                try {
                    val request = call.receive<AdminListingRequest>()
                    val listingId = adminService.createListing(userId, request)
                    call.respond(HttpStatusCode.Created, mapOf("listingId" to listingId))
                } catch (e: SecurityException) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse(e.message ?: "Access denied"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Invalid request"))
                }
            }

            // Импорт объявлений из CSV
            post("/listings/import") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.getClaim("id", Int::class) ?: run {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid token"))
                    return@post
                }

                try {
                    val request = call.receive<CsvImportRequest>()
                    val importedCount = adminService.importListingsFromCsv(userId, request.content)
                    call.respond(HttpStatusCode.OK, CsvImportResponse(importedCount = importedCount))
                } catch (e: SecurityException) {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse(e.message ?: "Access denied"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Invalid CSV format"))
                }
            }
        }
    }
} 