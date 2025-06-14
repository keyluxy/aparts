// В файле routes/FavoritesRoutes.kt

import com.example.database.tables.Cities
import com.example.database.tables.Favorites
import com.example.database.tables.ListingImages
import com.example.database.tables.Listings
import com.example.database.tables.Sources
import com.example.database.tables.Users
import com.example.routes.dto.ListingResponse
import com.example.routes.mappers.buildListingResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.format.DateTimeFormatter

//const val baseUrl = "http://192.168.18.138:8080"
//private const val baseUrl = "https://cd20c175-22a4-4f7d-bc62-ef3bb2948d58.tunnel4.com"

private const val baseUrl = "http://10.0.2.2:8080"

fun Route.favoritesRoutes() {
    route("/favorites") {
        authenticate("auth-jwt") {
            get("/{userId}") {
        val userId = call.parameters["userId"]?.toIntOrNull()
        if (userId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
            return@get
        }

                // Проверяем существование пользователя
                val userExists = transaction {
                    Users.select { Users.id eq userId }.count() > 0
                }
                if (!userExists) {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                    return@get
                }

                val formatter = DateTimeFormatter.ISO_DATE_TIME

                val favorites = transaction {
                    (Favorites innerJoin Listings innerJoin Sources innerJoin Cities)
                        .slice(
                            Listings.id,
                            Listings.title,
                            Listings.description,
                            Listings.price,
                            Listings.district,
                            Listings.createdAt,
                            Listings.publicationDate,
                            Listings.sourceId,
                            Listings.cityId,
                            Listings.rooms,
                            Sources.name,
                            Sources.url,
                            Cities.name
                        )
                        .select { Favorites.userId eq userId }
                        .map { row ->
                            val listingId = row[Listings.id]
                            val imageUrls = ListingImages.select { ListingImages.listingId eq listingId }
                                .map { imgRow ->
                                    "$baseUrl/listings/$listingId/image?imageId=${imgRow[ListingImages.id]}"
                                }
                            ListingResponse(
                                id = listingId,
                                title = row[Listings.title],
                                description = row[Listings.description],
                                price = row[Listings.price].toString(),
                                district = row[Listings.district],
                                createdAt = row[Listings.createdAt]?.format(formatter),
                                publicationDate = row[Listings.publicationDate]?.format(formatter),
                                sourceId = row[Listings.sourceId],
                                cityId = row[Listings.cityId],
                                rooms = row[Listings.rooms],
                                sourceName = row[Sources.name],
                                sourceUrl = row[Sources.url],
                                cityName = row[Cities.name],
                                imageUrls = imageUrls
                            )
            }
        }
        call.respond(favorites)
    }

            post("/{userId}/{listingId}") {
                val userId = call.parameters["userId"]?.toIntOrNull()
        val listingId = call.parameters["listingId"]?.toIntOrNull()

        if (userId == null || listingId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid user ID or listing ID")
            return@post
        }

                // Проверяем существование пользователя и объявления
                val exists = transaction {
                    val userExists = Users.select { Users.id eq userId }.count() > 0
                    val listingExists = Listings.select { Listings.id eq listingId }.count() > 0
                    userExists && listingExists
                }

                if (!exists) {
                    call.respond(HttpStatusCode.NotFound, "User or listing not found")
                return@post
            }

                // Проверяем, не добавлено ли уже объявление в избранное
                val alreadyFavorite = transaction {
                    Favorites.select { (Favorites.userId eq userId) and (Favorites.listingId eq listingId) }.count() > 0
                }

                if (alreadyFavorite) {
                    call.respond(HttpStatusCode.Conflict, "Listing already in favorites")
                return@post
            }

                transaction {
                    Favorites.insert {
                        it[Favorites.userId] = userId
                        it[Favorites.listingId] = listingId
                    }
                }

                call.respond(HttpStatusCode.Created, "Added to favorites")
            }

            delete("/{userId}/{listingId}") {
        val userId = call.parameters["userId"]?.toIntOrNull()
        val listingId = call.parameters["listingId"]?.toIntOrNull()

        if (userId == null || listingId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid user ID or listing ID")
            return@delete
        }

                val deleted = transaction {
                    Favorites.deleteWhere { (Favorites.userId eq userId) and (Favorites.listingId eq listingId) }
                }

                if (deleted > 0) {
                    call.respond(HttpStatusCode.OK, "Removed from favorites")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Favorite not found")
        }
    }
        }
    }
}


