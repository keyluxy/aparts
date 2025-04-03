//package com.example.service
//
//import com.example.database.tables.Listings
//import com.example.model.ListingEntity
//import org.jetbrains.exposed.sql.insert
//import org.jetbrains.exposed.sql.transactions.transaction
//
//object ListingService {
//    fun insertListing(listing: ListingEntity) {
//        transaction {
//            Listings.insert { row ->
//                row[title] = listing.title
//                row[description] = listing.description
//                row[price] = listing.price.toBigDecimal()
//
//                row[address] = listing.address
//
//                row[url] = listing.url
//                row[createdAt] = listing.createdAt
//
//                // Новые поля
//                row[views] = listing.views
//                row[publicationDate] = listing.publicationDate
//                row[seller] = listing.seller
//                row[sellerUrl] = listing.sellerUrl
//            }
//        }
//    }
////}
