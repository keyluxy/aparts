package com.example.database.tables

import org.jetbrains.exposed.sql.Table

object Sources : Table("source") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val url = text("url").uniqueIndex()
    override val primaryKey = PrimaryKey(id)
}