package com.example.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object Listings : Table("listing") {
    val id = integer("id").autoIncrement()
    val sourceId = integer("source_id").references(Sources.id)
    val title = text("title")
    val description = text("description").nullable()
    val price = decimal("price", 10, 2)
    val currency = varchar("currency", 10).default("RUB")
    val cityId = integer("city_id").references(Cities.id)
    val address = text("address").nullable()
    val rooms = integer("rooms").nullable()
    val area = decimal("area", 10, 2).nullable()
    val floor = integer("floor").nullable()
    val totalFloors = integer("total_floors").nullable()
    val url = text("url").uniqueIndex()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val userId = integer("user_id").references(Users.id)
    override val primaryKey = PrimaryKey(id)


}
