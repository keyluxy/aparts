package com.example.routes.mappers

import com.example.database.tables.Cities
import com.example.database.tables.Sources
import com.example.database.tables.ListingImages
import com.example.database.tables.Listings
import com.example.routes.dto.ListingResponse
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.format.DateTimeFormatter

fun buildListingResponse(listing: ResultRow, baseUrl: String): ListingResponse {
    val formatter = DateTimeFormatter.ISO_DATE_TIME

    val imageUrls = transaction {
        ListingImages.select { ListingImages.listingId eq listing[Listings.id] }
            .map { imgRow ->
                "$baseUrl/listings/${listing[Listings.id]}/image?imageId=${imgRow[ListingImages.id]}"
            }
    }

    val source = Sources.select { Sources.id eq listing[Listings.sourceId] }.singleOrNull()
    val city = Cities.select { Cities.id eq listing[Listings.cityId] }.singleOrNull()

    return ListingResponse(
        id = listing[Listings.id],
        title = listing[Listings.title],
        description = listing[Listings.description],
        price = listing[Listings.price].toString(),
        district = listing[Listings.district],
        createdAt = listing[Listings.createdAt]?.format(formatter),
        publicationDate = listing[Listings.publicationDate]?.format(formatter),
        sourceId = listing[Listings.sourceId],
        cityId = listing[Listings.cityId],
        sourceName = source?.get(Sources.name),
        sourceUrl = source?.get(Sources.url),
        cityName = city?.get(Cities.name),
        imageUrls = imageUrls,
        rooms = listing[Listings.rooms]
    )
}
