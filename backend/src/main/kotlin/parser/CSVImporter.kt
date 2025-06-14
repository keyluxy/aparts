package com.example.parser

import com.example.database.db_factory.DatabaseInitializer
import com.example.database.tables.Cities
import com.example.database.tables.ListingImages
import com.example.database.tables.Listings
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
import com.example.model.ImageConfig

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
                val district = row["district"]
                val createdAt = LocalDateTime.now()
                val publicationDate = row["publication_date"]?.let {
                    try {
                        LocalDateTime.parse(it, dateTimeFormatter)
                    } catch (e: Exception) {
                        null
                    }
                }

                // Получаем относительный путь к изображениям из CSV
                val relativeImagePath = row["image_data"]?.trim()?.let { path ->
                    // Если путь абсолютный, преобразуем его в относительный
                    if (path.startsWith("/")) {
                        val basePath = ImageConfig.getImagesAbsolutePath()
                        if (path.startsWith(basePath)) {
                            path.substring(basePath.length).trimStart('/')
                        } else {
                            // Если путь не начинается с базового пути, используем только имя файла/папки
                            path.substringAfterLast("/")
                        }
                    } else {
                        path
                    }
                } ?: ""

                val sourceUrl = row["url"]?.trim() ?: "default"
                val sourceName = extractSourceNameFromUrl(sourceUrl) ?: "Неизвестно"

                val sourceId = Sources.select { Sources.url eq sourceUrl }
                    .firstOrNull()?.get(Sources.id)
                    ?: Sources.insert {
                        it[name] = sourceName
                        it[url] = sourceUrl
                    }.resultedValues?.firstOrNull()?.get(Sources.id)
                    ?: throw Exception("Не удалось вставить источник: $sourceUrl")

                val cityName = extractCityFromUrl(sourceUrl) ?: "Unknown"
                val cityId = Cities.select { Cities.name eq cityName }
                    .firstOrNull()?.get(Cities.id)
                    ?: Cities.insert {
                        it[name] = cityName
                    }.resultedValues?.firstOrNull()?.get(Cities.id)
                    ?: throw Exception("Не удалось вставить город: $cityName")

                // Извлекаем количество комнат из заголовка
                val rooms = extractRoomsFromTitle(title)

                val listingId = Listings.insert {
                    it[Listings.title] = title
                    it[Listings.description] = description
                    it[Listings.price] = price
                    it[Listings.district] = district
                    it[Listings.createdAt] = createdAt
                    if (publicationDate != null) it[Listings.publicationDate] = publicationDate

                    it[Listings.sourceId] = sourceId
                    it[Listings.cityId] = cityId
                    it[Listings.rooms] = rooms  // Записываем комнаты
                } get Listings.id

                // Обработка изображений с использованием относительного пути
                val imageDir = File(ImageConfig.getImagesAbsolutePath(), relativeImagePath)
                if (imageDir.exists() && imageDir.isDirectory) {
                    imageDir.listFiles { file ->
                        file.isFile && (file.extension.lowercase() in listOf("png", "jpg", "jpeg"))
                    }?.forEach { imageFile ->
                        try {
                            val imageData = imageFile.readBytes()
                            ListingImages.insert {
                                it[ListingImages.listingId] = listingId
                                it[ListingImages.imageData] = imageData
                            }
                            println("✅ Загружено изображение: ${imageFile.name} -> listing_id: $listingId")
                        } catch (e: Exception) {
                            println("⚠️ Ошибка при загрузке изображения ${imageFile.name}: ${e.message}")
                        }
                    }
                } else {
                    println("⚠️ Папка с изображениями не найдена: ${imageDir.absolutePath}")
                }
            }
        }
    }

    private fun extractCityFromUrl(url: String): String? {
        // Сначала пытаемся извлечь город из поддомена (например, "kazan" в "kazan.cian.ru")
        val regexSubdomain = Regex("""https?://([a-zA-Z0-9\-]+)\.(?:cian\.ru|avito\.ru|domclick\.ru)""")
        val matchSubdomain = regexSubdomain.find(url)
        if (matchSubdomain != null) {
            return matchSubdomain.groups[1]?.value
        }

        // Если не подходит под поддомен, пытаемся извлечь город из пути (например, avito.ru/moscow/...)
        val regexPath = Regex("""https?://(?:www\.)?avito\.ru/([^/]+)/""")
        val matchPath = regexPath.find(url)
        if (matchPath != null) {
            return matchPath.groups[1]?.value
        }

        // Если ничего не подошло, возвращаем null
        return null
    }

    private fun extractSourceNameFromUrl(url: String): String? {
        return when {
            url.contains("avito") -> "Avito"
            url.contains("cian") -> "Циан"
            url.contains("domclick") -> "Домклик"
            else -> URI(url).host
        }
    }

    private fun extractRoomsFromTitle(title: String): Int? {
        // Ищет шаблон: число + "к" или "к." (например, "1к", "2к.", "3к")
        val regex = Regex("""(\d+)\s*к(?:\.| квартира)?""", RegexOption.IGNORE_CASE)
        val match = regex.find(title)
        return match?.groups?.get(1)?.value?.toIntOrNull()
    }
}

fun main() {
    DatabaseInitializer.init()
    val csvFilePath = "all.csv"
    CSVImporter.importCSV(csvFilePath)
}