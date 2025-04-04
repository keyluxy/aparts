package com.example.routes

import com.example.database.tables.Listings
import com.example.routes.dto.ListingResponse
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.format.DateTimeFormatter

fun Route.listingsRoutes() {
    get("/listings") {
        // Используем ISO формат для дат
        val formatter = DateTimeFormatter.ISO_DATE_TIME

        // Выполняем запрос к базе данных и мапим результат в список DTO
        val listings = transaction {
            Listings.selectAll().map { row ->
                ListingResponse(
                    id = row[Listings.id],
                    title = row[Listings.title],
                    description = row[Listings.description],
                    price = row[Listings.price].toString(),
                    address = row[Listings.address],
                    url = row[Listings.url],
                    createdAt = row[Listings.createdAt]?.format(formatter),
                    views = row[Listings.views],
                    publicationDate = row[Listings.publicationDate]?.format(formatter),
                    seller = row[Listings.seller],
                    sellerUrl = row[Listings.sellerUrl],
                    sourceId = row[Listings.sourceId],
                    cityId = row[Listings.cityId],
                    userId = row[Listings.userId]
                )
            }
        }

        call.respond(listings)
    }
}
