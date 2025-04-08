package com.example.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object Listings : Table("listing") {
    val id = integer("id").autoIncrement()
    val title = text("title")
    val description = text("description").nullable()
    val price = decimal("price", 10, 2)
    val address = text("address").nullable()
    // Поле url удалено
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime).nullable()
    val views = text("views").nullable()
    val publicationDate = datetime("publication_date").nullable()
    val seller = varchar("seller", 255).nullable()
    val sellerUrl = text("seller_url").nullable()

    // Внешние ключи
    val sourceId = integer("source_id").references(Sources.id)
    val cityId = integer("city_id").references(Cities.id)

    override val primaryKey = PrimaryKey(id)
}
