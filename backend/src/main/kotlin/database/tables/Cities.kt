package com.example.database.tables

import org.jetbrains.exposed.sql.Table

object Cities : Table("city") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255).uniqueIndex()
    override val primaryKey = PrimaryKey(id)
}
