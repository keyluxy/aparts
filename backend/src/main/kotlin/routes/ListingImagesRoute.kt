package com.example.routes

import com.example.database.tables.ListingImages
import io.ktor.http.*
import io.ktor.server.application.call
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.listingImageRoute() {
    get("/listings/{listingId}/image") {
        // Извлекаем listingId из пути
        val listingIdParam = call.parameters["listingId"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing listingId")
        val listingId = listingIdParam.toIntOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid listingId")

        // Извлекаем query-параметр imageId
        val imageIdParam = call.request.queryParameters["imageId"]
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing imageId")
        val imageId = imageIdParam.toIntOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid imageId")

        // Извлекаем бинарные данные картинки из таблицы ListingImages
        val imageBytes = transaction {
            ListingImages.select { (ListingImages.listingId eq listingId) and (ListingImages.id eq imageId) }
                .firstOrNull()
                ?.get(ListingImages.imageData)
        }
        if (imageBytes == null) {
            call.respond(HttpStatusCode.NotFound, "Image not found")
        } else {
            call.respondBytes(imageBytes, contentType = ContentType.Image.Any)
        }
    }
}
