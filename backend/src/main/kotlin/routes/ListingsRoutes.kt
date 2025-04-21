package com.example.routes

import com.example.database.tables.Cities
import com.example.database.tables.ListingImages
import com.example.database.tables.Listings
import com.example.database.tables.Sources
import com.example.routes.dto.ListingResponse
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.format.DateTimeFormatter

fun Route.listingsRoutes() {
    get("/listings") {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val baseUrl = "http://0.0.0.0:8080"

        val listings = transaction {
            (Listings innerJoin Sources innerJoin Cities).slice(
                Listings.id,
                Listings.title,
                Listings.description,
                Listings.price,
                Listings.address,
                Listings.createdAt,
                Listings.views,
                Listings.publicationDate,
                Listings.seller,
                Listings.sellerUrl,
                Listings.sourceId,
                Listings.cityId,
                Sources.name,
                Sources.url,
                Cities.name  // название города
            ).selectAll().map { row ->
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
                    address = row[Listings.address],
                    createdAt = row[Listings.createdAt]?.format(formatter),
                    views = row[Listings.views],
                    publicationDate = row[Listings.publicationDate]?.format(formatter),
                    seller = row[Listings.seller],
                    sellerUrl = row[Listings.sellerUrl],
                    sourceId = row[Listings.sourceId],
                    cityId = row[Listings.cityId],
                    sourceName = row[Sources.name],
                    sourceUrl = row[Sources.url],
                    cityName = row[Cities.name],  // добавлено
                    imageUrls = imageUrls
                )
            }
        }
        call.respond(listings)
    }
}
