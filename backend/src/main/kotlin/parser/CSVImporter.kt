package com.example.parser

import com.example.database.db_factory.DatabaseInitializer
import com.example.database.tables.Cities
import com.example.database.tables.Listings
import com.example.database.tables.Sources
import com.example.database.tables.Users
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File
import java.math.BigDecimal
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
            csvData.forEach { row ->
                // Обработка города
                val cityName = row["city"]?.trim() ?: "Unknown"
                val cityId = Cities.select { Cities.name eq cityName }
                    .firstOrNull()?.get(Cities.id) ?: Cities.insertIgnore {
                    it[Cities.name] = cityName
                }.resultedValues?.firstOrNull()?.get(Cities.id) ?: throw Exception("Ошибка при вставке города")

                // Обработка источника
                val sourceUrlRaw = row["url"]?.trim() ?: ""
                val sourceId = if (sourceUrlRaw.contains("avito", ignoreCase = true)) {
                    val sourceName = row["name  "]?.trim() ?: "Avito"
                    Sources.select { Sources.url eq sourceUrlRaw }
                        .firstOrNull()?.get(Sources.id) ?: Sources.insertIgnore {
                        it[Sources.name] = sourceName
                        it[Sources.url] = sourceUrlRaw
                    }.resultedValues?.firstOrNull()?.get(Sources.id) ?: throw Exception("Ошибка при вставке источника")
                } else {
                    val defaultSourceUrl = "default"
                    val defaultSourceName = "Non-Avito"
                    Sources.select { Sources.url eq defaultSourceUrl }
                        .firstOrNull()?.get(Sources.id) ?: Sources.insertIgnore {
                        it[Sources.name] = defaultSourceName
                        it[Sources.url] = defaultSourceUrl
                    }.resultedValues?.firstOrNull()?.get(Sources.id) ?: throw Exception("Ошибка при вставке дефолтного источника")
                }

                // Обработка пользователя
                val userId = if (row.containsKey("user_email") && row["user_email"]!!.isNotBlank()) {
                    val email = row["user_email"]!!.trim()
                    Users.select { Users.email eq email }
                        .firstOrNull()?.get(Users.id) ?: Users.insertIgnore {
                        it[Users.email] = email
                        it[Users.passwordHash] = "defaultHash"
                        it[Users.firstName] = row["user_first_name"]?.trim() ?: "FirstName"
                        it[Users.lastName] = row["user_last_name"]?.trim() ?: "LastName"
                        it[Users.middleName] = row["user_middle_name"]?.trim()
                    }.resultedValues?.firstOrNull()?.get(Users.id) ?: throw Exception("Ошибка при вставке пользователя")
                } else {
                    1
                }

                // Обработка объявления
                val title = row["title"] ?: ""
                val description = row["description"]
                val price = row["price"]?.toBigDecimalOrNull() ?: BigDecimal.ZERO
                val address = row["address"]
                val listingUrl = row["url"] ?: ""
                val createdAt = row["created_at"]?.let {
                    try {
                        LocalDateTime.parse(it, dateTimeFormatter)
                    } catch (e: Exception) {
                        null
                    }
                }
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

                // Вставка объявления с внешними ключами на источники, город и пользователя
                Listings.insertIgnore {
                    it[Listings.title] = title
                    it[Listings.description] = description
                    it[Listings.price] = price
                    it[Listings.address] = address
                    it[Listings.url] = listingUrl
                    if (createdAt != null) it[Listings.createdAt] = createdAt
                    it[Listings.views] = views
                    if (publicationDate != null) it[Listings.publicationDate] = publicationDate
                    it[Listings.seller] = seller
                    it[Listings.sellerUrl] = sellerUrl
                    it[Listings.sourceId] = sourceId
                    it[Listings.cityId] = cityId
                    it[Listings.userId] = userId
                }
            }
        }
    }
}

fun main() {
    DatabaseInitializer.init()
    val csvFilePath = "all.csv"
    CSVImporter.importCSV(csvFilePath)
}
