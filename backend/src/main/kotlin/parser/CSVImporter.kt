package com.example.parser

import com.example.database.db_factory.DatabaseInitializer
import com.example.database.tables.Cities
import com.example.database.tables.Listings
import com.example.database.tables.ListingImages
import com.example.database.tables.Sources
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File
import java.math.BigDecimal
import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object CSVImporter {
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    fun importCSV(filePath: String) {
        val csvData = csvReader {
            delimiter = ','
            quoteChar = '"'
        }.readAllWithHeader(File(filePath))

        transaction {
            csvData.forEachIndexed { index, row ->
                val title = row["title"] ?: return@forEachIndexed
                val description = row["description"]
                val price = row["price"]?.toBigDecimalOrNull() ?: BigDecimal.ZERO
                val address = row["address"]
                val createdAt = LocalDateTime.now()
                val views = row["views"]
                val publicationDate = row["publication_date"]?.let {
                    try {
                        LocalDateTime.parse(it, dateTimeFormatter)
                    } catch (e: Exception) {
                        null
                    }
                }
                val seller = row["seller"]
                val sellerUrl = row["seller_url"]
                val imageBasePath = row["image_data"]?.trim() ?: "" // теперь это путь до uploads

                // Обработка источника
                val sourceUrl = row["url"]?.trim() ?: "default"
                val sourceId = Sources.select { Sources.url eq sourceUrl }
                    .firstOrNull()?.get(Sources.id)
                    ?: Sources.insert {
                        it[name] = URI(sourceUrl).host
                        it[url] = sourceUrl
                    }.resultedValues?.firstOrNull()?.get(Sources.id)
                    ?: throw Exception("Не удалось вставить источник: $sourceUrl")

                // Обработка города
                val cityName = extractCityFromUrl(sourceUrl) ?: "Unknown"
                val cityId = Cities.select { Cities.name eq cityName }
                    .firstOrNull()?.get(Cities.id)
                    ?: Cities.insert {
                        it[name] = cityName
                    }.resultedValues?.firstOrNull()?.get(Cities.id)
                    ?: throw Exception("Не удалось вставить город: $cityName")

                // Вставка объявления
                val listingId = Listings.insert {
                    it[Listings.title] = title
                    it[Listings.description] = description
                    it[Listings.price] = price
                    it[Listings.address] = address
                    it[Listings.createdAt] = createdAt
                    it[Listings.views] = views
                    if (publicationDate != null) it[Listings.publicationDate] = publicationDate
                    it[Listings.seller] = seller
                    it[Listings.sellerUrl] = sellerUrl
                    it[Listings.sourceId] = sourceId
                    it[Listings.cityId] = cityId
                } get Listings.id

                // Автоматическая загрузка всех картинок из папки uploads/img_N
                val folderIndex = index + 1 // img_1 для первой строки, и т.д.
                val imageDir = File("$imageBasePath/img_$folderIndex")

                if (imageDir.exists() && imageDir.isDirectory) {
                    imageDir.listFiles { file ->
                        file.isFile && (file.extension.lowercase() in listOf("png", "jpg", "jpeg"))
                    }?.forEach { imageFile ->
                        val imageData = imageFile.readBytes()
                        ListingImages.insert {
                            it[ListingImages.listingId] = listingId
                            it[ListingImages.imageData] = imageData
                        }
                        println("✅ Загружено изображение: ${imageFile.name} -> listing_id: $listingId")
                    }
                } else {
                    println("⚠️ Папка с изображениями не найдена: ${imageDir.absolutePath}")
                }
            }
        }
    }

    private fun extractCityFromUrl(url: String): String? {
        val regex = Regex("https?://(?:www\\.)?avito\\.ru/([^/]+)/")
        return regex.find(url)?.groups?.get(1)?.value
    }
}


fun main() {
    DatabaseInitializer.init()
    val csvFilePath = "all.csv"
    CSVImporter.importCSV(csvFilePath)
}
