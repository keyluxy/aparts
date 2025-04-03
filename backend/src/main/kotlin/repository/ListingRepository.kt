//package com.example.repository
//
//import com.example.model.ListingEntity
//import com.example.service.ExcelReaderService
//import java.time.LocalDateTime
//
//class ListingRepository {
//    private val excelReaderService = ExcelReaderService()
//
//    suspend fun getAllListings(): List<ListingEntity> {
//        return try {
//            val excelData = excelReaderService.readAvitoListings()
//            excelData.mapNotNull { data ->
//                ListingEntity(
//                    id = data["id"]?.toString()?.toLongOrNull() ?: 0L,
//                    title = data["title"]?.toString() ?: "",
//                    price = if (data["price"] is Double) data["price"] as Double
//                    else (data["price"]?.toString()?.toDoubleOrNull() ?: 0.0),
//                    url = data["url"]?.toString() ?: "",
//                    description = data["description"]?.toString(),
//                    views = data["views"]?.toString()?.toIntOrNull(),
//                    publicationDate = data["publication_date"] as? LocalDateTime,
//                    seller = data["seller"]?.toString(),
//                    address = data["address"]?.toString(),
//                    sellerUrl = data["seller_url"]?.toString()
//                )
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            emptyList()
//        }
//    }
//}
