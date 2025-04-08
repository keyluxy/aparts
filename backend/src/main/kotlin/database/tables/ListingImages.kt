package com.example.database.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object ListingImages : Table("listing_images") {
    val id = integer("id").autoIncrement()
    // Внешний ключ, ссылающийся на объявление из таблицы Listings
    val listingId = integer("listing_id").references(Listings.id, onDelete = ReferenceOption.CASCADE)
    // Бинарное поле для хранения изображения (ограничено 10 MB)
    val imageData = binary("image_data", length = 10 * 1024 * 1024)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
