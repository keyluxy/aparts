// favoritesRoutes.kt
package com.example.routes

import com.example.service.FavoriteService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.favoritesRoutes(favoriteService: FavoriteService) {
    route("/users/{userId}/favorites") {
        authenticate {
            get {
                val userId = call.parameters["userId"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid user ID")

                try {
                    val favorites = favoriteService.getFavorites(userId)
                    call.respond(HttpStatusCode.OK, favorites)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Error")
                }
            }

            post {
                val userId = call.parameters["userId"]?.toIntOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid user ID")

                val listingId = call.request.queryParameters["listingId"]?.toIntOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing listingId")

                try {
                    favoriteService.addFavorite(userId, listingId)
                    call.respond(HttpStatusCode.Created)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Error")
                }
            }

            delete {
                val userId = call.parameters["userId"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid user ID")

                val listingId = call.request.queryParameters["listingId"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing listingId")

                try {
                    favoriteService.removeFavorite(userId, listingId)
                    call.respond(HttpStatusCode.OK)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Error")
                }
            }
        }
    }
}
