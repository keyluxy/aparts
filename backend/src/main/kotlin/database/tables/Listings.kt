package com.example.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object Listings : Table("listing") {
    val id = integer("id").autoIncrement()
    val title = text("title")
    val description = text("description").nullable()
    val price = decimal("price", 10, 2)
    val district = text("district").nullable() // <-- новое поле "район" вместо address
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime).nullable()
    val publicationDate = datetime("publication_date").nullable()
    val rooms = integer("rooms").nullable()

    // Внешние ключи
    val sourceId = integer("source_id").references(Sources.id)
    val cityId = integer("city_id").references(Cities.id)

    override val primaryKey = PrimaryKey(id)
}

