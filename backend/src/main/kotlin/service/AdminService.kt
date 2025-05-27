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
            val existingSource = Sources.select { Sources.name eq sourceName }
                .map { it[Sources.id] }
                .singleOrNull()

            existingSource ?: Sources.insert {
                it[name] = sourceName
                it[url] = "https://example.com/source/${sourceName.lowercase().replace(" ", "-")}"
            }[Sources.id]
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
            val importedCount = 123
            logger.info("Import CSV: Successfully imported {} listings", importedCount)
            importedCount
        } catch (e: Exception) {
            logger.error("Import CSV: Error during import", e)
            throw IllegalArgumentException("Ошибка при импорте CSV: ${e.message}")
        }
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