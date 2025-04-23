// В файле routes/FavoritesRoutes.kt

import com.example.database.tables.Favorites
import com.example.database.tables.Listings
import com.example.routes.dto.ListingResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.favoritesRoutes() {
    // Получить избранные объявления пользователя
    get("/favorites/{userId}") {
        val userId = call.parameters["userId"]?.toIntOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid userId")

        val baseUrl = "http://0.0.0.0:8080"
        val formatter = java.time.format.DateTimeFormatter.ISO_DATE_TIME

        val favorites = transaction {
            (Favorites innerJoin Listings).select { Favorites.userId eq userId }.map { favRow ->
                val listing = Listings.select { Listings.id eq favRow[Favorites.listingId] }.single()
                val images = transaction {
                    com.example.database.tables.ListingImages.select { com.example.database.tables.ListingImages.listingId eq listing[Listings.id] }
                        .map { imgRow -> "$baseUrl/listings/${listing[Listings.id]}/image?imageId=${imgRow[com.example.database.tables.ListingImages.id]}" }
                }

                ListingResponse(
                    id = listing[Listings.id],
                    title = listing[Listings.title],
                    description = listing[Listings.description],
                    price = listing[Listings.price].toString(),
                    address = listing[Listings.address],
                    createdAt = listing[Listings.createdAt]?.format(formatter),
                    views = listing[Listings.views],
                    publicationDate = listing[Listings.publicationDate]?.format(formatter),
                    seller = listing[Listings.seller],
                    sellerUrl = listing[Listings.sellerUrl],
                    sourceId = listing[Listings.sourceId],
                    cityId = listing[Listings.cityId],
                    sourceName = null, // Можно добавить join с Sources, если нужно
                    sourceUrl = null,  // Аналогично
                    cityName = null,   // Аналогично
                    imageUrls = images
                )
            }
        }

        call.respond(favorites)
    }

    // Добавить объявление в избранное
    post("/favorites/{userId}/{listingId}") {
        val userId = call.parameters["userId"]?.toIntOrNull()
            ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid userId")
        val listingId = call.parameters["listingId"]?.toIntOrNull()
            ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid listingId")

        transaction {
            // Проверяем, что запись не существует
            val exists = Favorites.select {
                (Favorites.userId eq userId) and (Favorites.listingId eq listingId)
            }.count() > 0

            if (!exists) {
                Favorites.insert {
                    it[Favorites.userId] = userId
                    it[Favorites.listingId] = listingId
                }
            }
        }
        call.respond(HttpStatusCode.OK)
    }

    // Удалить объявление из избранного
    delete("/favorites/{userId}/{listingId}") {
        val userId = call.parameters["userId"]?.toIntOrNull()
            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid userId")
        val listingId = call.parameters["listingId"]?.toIntOrNull()
            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid listingId")

        transaction {
            Favorites.deleteWhere {
                (Favorites.userId eq userId) and (Favorites.listingId eq listingId)
            }
        }
        call.respond(HttpStatusCode.OK)
    }
}
