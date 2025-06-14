package com.example.service

import com.example.database.tables.Cities
import com.example.database.tables.Listings
import com.example.database.tables.Sources
import com.example.database.tables.Users
import com.example.database.tables.ListingImages
import com.example.parser.CSVImporter
import com.example.routes.dto.AdminListingRequest
import com.example.routes.dto.CityDto
import com.example.routes.dto.SourceDto
import com.example.routes.dto.UserDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.StringReader
import java.io.ByteArrayInputStream

class AdminService {
    private val logger: Logger = LoggerFactory.getLogger(AdminService::class.java)

    fun isAdmin(userId: Int): Boolean {
        return transaction {
            Users.select { Users.id eq userId }
                .map { it[Users.isAdmin] }
                .singleOrNull() ?: false
        }
    }

    fun getCities(): List<CityDto> {
        return transaction {
            Cities.selectAll()
                .map { CityDto(it[Cities.id], it[Cities.name]) }
        }
    }

    fun getSources(): List<SourceDto> {
        return transaction {
            Sources.selectAll()
                .map { SourceDto(it[Sources.id], it[Sources.name]) }
        }
    }

    fun findOrCreateCity(cityName: String): Int {
        return transaction {
            val existingCity = Cities.select { Cities.name eq cityName }
                .map { it[Cities.id] }
                .singleOrNull()

            existingCity ?: Cities.insert {
                it[name] = cityName
            }[Cities.id]
        }
    }

    fun findOrCreateSource(sourceName: String): Int {
        return transaction {
            val canonicalUrl = "https://example.com/source/${sourceName.lowercase().replace(" ", "-")}"

            // 1. Пытаемся найти существующий источник по каноническому URL
            val existingSourceByCanonicalUrl = Sources.select { Sources.url eq canonicalUrl }
                .firstOrNull()

            if (existingSourceByCanonicalUrl != null) {
                // Если источник с таким каноническим URL уже существует, возвращаем его ID
                logger.info("Найден существующий источник по каноническому URL: $canonicalUrl, ID: ${existingSourceByCanonicalUrl[Sources.id]}")
                return@transaction existingSourceByCanonicalUrl[Sources.id]
            }

            // 2. Если не найден по каноническому URL, пытаемся найти по sourceName
            //    и обновляем его URL на канонический, если найден.
            val existingSourceByName = Sources.select { Sources.name eq sourceName }
                .firstOrNull()

            if (existingSourceByName != null) {
                // Найден по имени, но URL был неканоническим. Обновляем его URL.
                Sources.update({ Sources.id eq existingSourceByName[Sources.id] }) {
                    it[url] = canonicalUrl
                }
                logger.warn("Найден существующий источник по имени: $sourceName, но URL был неканоническим. Обновлен на: $canonicalUrl, ID: ${existingSourceByName[Sources.id]}")
                return@transaction existingSourceByName[Sources.id]
            }

            // 3. Если ни один не найден, вставляем новый источник
            val newSourceId = Sources.insert {
                it[name] = sourceName
                it[url] = canonicalUrl
            }[Sources.id]
            logger.info("Создан новый источник: $sourceName, URL: $canonicalUrl, ID: $newSourceId")
            newSourceId
        }
    }

    fun addListingImage(listingId: Int, imageData: ByteArray): Int {
        return transaction {
            ListingImages.insert {
                it[ListingImages.listingId] = listingId
                it[ListingImages.imageData] = imageData
                it[ListingImages.createdAt] = LocalDateTime.now()
            }[ListingImages.id]
        }
    }

    fun getListingImages(listingId: Int): List<ByteArray> {
        return transaction {
            ListingImages.select { ListingImages.listingId eq listingId }
                .map { it[ListingImages.imageData] }
        }
    }

    fun createListing(userId: Int, request: AdminListingRequest): Int {
        logger.info("Creating listing for user $userId with title: ${request.title}")
        
        if (!isAdmin(userId)) {
            logger.error("User $userId is not admin")
            throw SecurityException("Требуются права администратора")
        }

        // Валидация обязательных полей
        if (request.title.isBlank()) {
            logger.error("Title is empty")
            throw IllegalArgumentException("Название объявления не может быть пустым")
        }
        if (request.price.isBlank()) {
            logger.error("Price is empty")
            throw IllegalArgumentException("Цена не может быть пустой")
        }
        if (request.cityName.isBlank()) {
            logger.error("City name is empty")
            throw IllegalArgumentException("Город не может быть пустым")
        }
        if (request.sourceName.isBlank()) {
            logger.error("Source name is empty")
            throw IllegalArgumentException("Источник не может быть пустым")
        }

        // Валидация цены
        try {
            BigDecimal(request.price)
        } catch (e: NumberFormatException) {
            logger.error("Invalid price format: ${request.price}")
            throw IllegalArgumentException("Некорректный формат цены")
        }

        // Валидация изображений
        request.images?.forEachIndexed { index, imageData ->
            try {
                // Проверяем, что строка является валидной Base64
                if (!imageData.matches(Regex("^[A-Za-z0-9+/]*={0,2}$"))) {
                    logger.error("Invalid Base64 format for image $index")
                    throw IllegalArgumentException("Некорректный формат изображения $index")
                }

                // Проверяем размер декодированного изображения (максимум 5MB)
                val bytes = Base64.getDecoder().decode(imageData)
                if (bytes.size > 5 * 1024 * 1024) {
                    logger.error("Image $index is too large: ${bytes.size} bytes")
                    throw IllegalArgumentException("Изображение $index слишком большое (максимум 5MB)")
                }
            } catch (e: IllegalArgumentException) {
                throw e
            } catch (e: Exception) {
                logger.error("Error processing image $index", e)
                throw IllegalArgumentException("Ошибка при обработке изображения $index: ${e.message}")
            }
        }

        return transaction {
            logger.info("Starting database transaction for listing creation")
            
            val cityId = findOrCreateCity(request.cityName)
            logger.info("City ID: $cityId for name: ${request.cityName}")
            
            val sourceId = findOrCreateSource(request.sourceName)
            logger.info("Source ID: $sourceId for name: ${request.sourceName}")

            val listingId = Listings.insert {
                it[Listings.title] = request.title
                it[Listings.description] = request.description
                it[Listings.price] = BigDecimal(request.price)
                it[Listings.district] = request.district
                it[Listings.rooms] = request.rooms
                it[Listings.sourceId] = sourceId
                it[Listings.cityId] = cityId
                it[Listings.publicationDate] = request.publicationDate?.let { dateStr ->
                    LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME)
                } ?: LocalDateTime.now()
                it[Listings.createdAt] = LocalDateTime.now()
            }[Listings.id]

            logger.info("Created listing with ID: $listingId")

            // Добавляем изображения, если они есть
            request.images?.forEachIndexed { index, imageData ->
                try {
                    val bytes = Base64.getDecoder().decode(imageData)
                    val imageId = addListingImage(listingId, bytes)
                    logger.info("Added image $index with ID: $imageId to listing $listingId")
                } catch (e: Exception) {
                    logger.error("Error adding image $index to listing $listingId", e)
                    throw e
                }
            }

            listingId
        }
    }

    fun importListingsFromCsv(userId: Int, csvContent: String): Int {
        if (!isAdmin(userId)) {
            logger.warn("Import CSV: User {} is not admin", userId)
            throw SecurityException("Требуются права администратора")
        }

        logger.info("Import CSV: Starting import process")
        return try {
            val csvData = csvReader {
                delimiter = ','
                quoteChar = '"'
            }.readAllWithHeader(csvContent)

            var importedCount = 0
            transaction {
                csvData.forEach { row ->
                    try {
                        val title = row["title"] ?: return@forEach
                        val description = row["description"]
                        val price = row["price"]?.toBigDecimalOrNull() ?: BigDecimal.ZERO
                        val district = row["district"]
                        val createdAt = LocalDateTime.now()
                        val publicationDate = row["publication_date"]?.let {
                            try {
                                LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
                            } catch (e: Exception) {
                                null
                            }
                        }

                        val sourceUrl = row["url"]?.trim() ?: "default"
                        val sourceName = extractSourceNameFromUrl(sourceUrl) ?: "Неизвестно"
                        val sourceId = findOrCreateSource(sourceName)

                        val cityName = extractCityFromUrl(sourceUrl) ?: "Unknown"
                        val cityId = findOrCreateCity(cityName)

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
                            it[Listings.rooms] = rooms
                        } get Listings.id

                        // Обработка изображений с использованием относительного пути из CSV
                        val relativeImagePath = row["image_data"]?.trim()?.let { path ->
                            // Если путь абсолютный, преобразуем его в относительный
                            if (path.startsWith("/")) {
                                val basePath = com.example.ImageConfig.getImagesAbsolutePath()
                                if (path.startsWith(basePath)) {
                                    path.substring(basePath.length).trimStart('/')
                                } else {
                                    path.substringAfterLast("/")
                                }
                            } else {
                                path
                            }
                        } ?: ""

                        val imageDir = java.io.File(com.example.ImageConfig.getImagesAbsolutePath(), relativeImagePath)
                        if (imageDir.exists() && imageDir.isDirectory) {
                            imageDir.listFiles { file ->
                                file.isFile && (file.extension.lowercase() in listOf("png", "jpg", "jpeg"))
                            }?.forEach { imageFile ->
                                try {
                                    val imageDataBytes = imageFile.readBytes()
                                    ListingImages.insert {
                                        it[ListingImages.listingId] = listingId
                                        it[ListingImages.imageData] = imageDataBytes
                                        it[ListingImages.createdAt] = createdAt
                                    }
                                } catch (e: Exception) {
                                    logger.warn("Failed to import image from file ${imageFile.name} for listing $listingId: ${e.message}")
                                }
                            }
                        } else {
                            logger.warn("Image directory not found for listing $listingId: ${imageDir.absolutePath}")
                        }

                        importedCount++
                        logger.info("Successfully imported listing: $title")
                    } catch (e: Exception) {
                        logger.error("Failed to import row: ${e.message}")
                        // Продолжаем импорт других строк даже если одна не удалась
                    }
                }
            }

            logger.info("Import CSV: Successfully imported {} listings", importedCount)
            importedCount
        } catch (e: Exception) {
            logger.error("Import CSV: Error during import", e)
            throw IllegalArgumentException("Ошибка при импорте CSV: ${e.message}")
        }
    }

    private fun extractSourceNameFromUrl(url: String): String? {
        return when {
            url.contains("avito") -> "Avito"
            url.contains("cian") -> "Циан"
            url.contains("domclick") -> "Домклик"
            else -> try {
                java.net.URI(url).host
            } catch (e: Exception) {
                null
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

        return null
    }

    private fun extractRoomsFromTitle(title: String): Int? {
        val regex = Regex("""(\d+)\s*к(?:\.| квартира)?""", RegexOption.IGNORE_CASE)
        val match = regex.find(title)
        return match?.groups?.get(1)?.value?.toIntOrNull()
    }

    fun getUserInfo(userId: Int): UserDto {
        return transaction {
            Users.select { Users.id eq userId }
                .map { row ->
                    UserDto(
                        id = row[Users.id],
                        email = row[Users.email],
                        firstName = row[Users.firstName],
                        lastName = row[Users.lastName],
                        isAdmin = row[Users.isAdmin]
                    )
                }
                .singleOrNull() ?: throw Exception("User not found")
        }
    }
}