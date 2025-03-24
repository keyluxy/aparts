// Файл: com/example/service/ListingService.kt
package com.example.service

import com.example.database.tables.Listings
import com.example.parser.ListingParserDTO
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction


object ListingService {
    fun saveListing(listing: ListingParserDTO, sourceId: Int = 1) {
        transaction {
            Listings.insert { row ->
                row[Listings.sourceId] = sourceId
                row[Listings.title] = listing.title
                row[Listings.description] = listing.description
                row[Listings.price] = listing.price.toBigDecimal()
                row[Listings.currency] = listing.currency
                row[Listings.cityId] = listing.cityId
                row[Listings.address] = listing.address
                row[Listings.rooms] = listing.rooms
                row[Listings.area] = listing.area?.toBigDecimal()
                row[Listings.floor] = listing.floor
                row[Listings.totalFloors] = listing.totalFloors
                row[Listings.url] = listing.url
            }
        }
        println("Сохранено объявление: ${listing.title} из источника $sourceId")
    }
}

