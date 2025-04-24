// В файле routes/FavoritesRoutes.kt

import com.example.database.tables.Favorites
import com.example.database.tables.Listings
import com.example.database.tables.Users
import com.example.routes.dto.ListingResponse
import com.example.routes.mappers.buildListingResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.format.DateTimeFormatter

//const val baseUrl = "http://0.0.0.0:8080"
const val baseUrl = "http://10.178.204.18:8080/"

fun Route.favoritesRoutes() {

    suspend fun respondWithError(call: ApplicationCall, status: HttpStatusCode, message: String) {
        call.respond(status, message)
    }

    get("/favorites/{userId}") {
        val userId = call.parameters["userId"]?.toIntOrNull()
        if (userId == null) {
            respondWithError(call, HttpStatusCode.BadRequest, "Invalid userId")
            return@get
        }


        val formatter = java.time.format.DateTimeFormatter.ISO_DATE_TIME

        val favorites = withContext(Dispatchers.IO) {
            transaction {
                (Favorites innerJoin Listings)
                    .select { Favorites.userId eq userId }
                    .map { favRow ->
                        val listing = Listings.select { Listings.id eq favRow[Favorites.listingId] }.single()
                        buildListingResponse(listing, baseUrl)
                    }

            }
        }
        call.respond(favorites)
    }

    post("/favorites/{userId}/{listingId}") {
        val userId    = call.parameters["userId"]?.toIntOrNull()
        val listingId = call.parameters["listingId"]?.toIntOrNull()

        if (userId == null || listingId == null) {
            respondWithError(call, HttpStatusCode.BadRequest, "userId or listingId is invalid")
            return@post
        }

        try {
            // Валидация наличия пользователя и объявления вне транзакции
            val userExists    = transaction { Users.select { Users.id eq userId }.count() > 0 }
            val listingExists = transaction { Listings.select { Listings.id eq listingId }.count() > 0 }

            if (!userExists) {
                respondWithError(call, HttpStatusCode.NotFound, "User $userId not found")
                return@post
            }
            if (!listingExists) {
                respondWithError(call, HttpStatusCode.NotFound, "Listing $listingId not found")
                return@post
            }

            // Ставим insert в собственную транзакцию и возвращаем 201 Created
            val inserted = transaction {
                // проверим, есть ли уже
                val exists = Favorites.select {
                    (Favorites.userId eq userId) and (Favorites.listingId eq listingId)
                }.any()

                if (!exists) {
                    Favorites.insert {
                        it[Favorites.userId]    = userId
                        it[Favorites.listingId] = listingId
                    }
                    true
                } else false
            }

            call.respond(
                status = if (inserted) HttpStatusCode.Created else HttpStatusCode.OK,
                message = mapOf("added" to inserted)
            )
        } catch (e: Exception) {
            application.log.error("Failed to add favorite for user=$userId, listing=$listingId", e)
            respondWithError(call, HttpStatusCode.InternalServerError, "Internal error: ${e.localizedMessage}")
        }
    }

    delete("/favorites/{userId}/{listingId}") {
        val userId = call.parameters["userId"]?.toIntOrNull()
        val listingId = call.parameters["listingId"]?.toIntOrNull()

        if (userId == null || listingId == null) {
            respondWithError(call, HttpStatusCode.BadRequest, "Invalid userId or listingId")
            return@delete
        }

        try {
            withContext(Dispatchers.IO) {
                transaction {
                    Favorites.deleteWhere {
                        (Favorites.userId eq userId) and (Favorites.listingId eq listingId)
                    }
                }
            }
            call.respond(HttpStatusCode.OK)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Database error: ${e.message}")
        }
    }

}


