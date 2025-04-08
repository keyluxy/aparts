package com.example.routes

import com.example.database.tables.ListingImages
import com.example.database.tables.Listings
import com.example.routes.dto.ListingResponse
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.format.DateTimeFormatter

fun Route.listingsRoutes() {
    get("/listings") {
        // Задаём форматирование даты, например ISO
        val formatter = DateTimeFormatter.ISO_DATE_TIME

        // Базовый URL сервера, его можно вынести в конфиг
        val baseUrl = "http://10.178.204.18:8080"

        // Выполняем запрос к базе и мапим каждую строку в ListingResponse
        val listings = transaction {
            Listings.selectAll().map { row ->
                val listingId = row[Listings.id]
                // Для данного объявления получаем все картинки
                val imageUrls = ListingImages.select { ListingImages.listingId eq listingId }
                    .map { imgRow ->
                        // Формируем URL для получения картинки по маршруту /listings/{listingId}/image?imageId={imageId}
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
                    imageUrls = imageUrls
                )
            }
        }

        call.respond(listings)
    }
}
