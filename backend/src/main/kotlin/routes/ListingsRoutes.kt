package com.example.routes

import com.example.database.tables.Cities
import com.example.database.tables.ListingImages
import com.example.database.tables.Listings
import com.example.database.tables.Sources
import com.example.routes.dto.ListingResponse
import io.ktor.http.*
import io.ktor.server.application.call
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.format.DateTimeFormatter

//const val baseUrl = "http://192.168.31.138:8080/"
//private const val baseUrl = "https://cd20c175-22a4-4f7d-bc62-ef3bb2948d58.tunnel4.com"
private const val baseUrl = "http://10.0.2.2:8080/"

fun Route.listingsRoutes() {
    route("/listings") {
        authenticate("auth-jwt") {
            get {
                val formatter = DateTimeFormatter.ISO_DATE_TIME

                val listings = transaction {
                    (Listings innerJoin Sources innerJoin Cities)
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
                        .selectAll()
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
                call.respond(listings)
            }
        }
    }
}
