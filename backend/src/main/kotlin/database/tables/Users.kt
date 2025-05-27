    package com.example.database.tables

    import org.jetbrains.exposed.sql.Table
    import org.jetbrains.exposed.sql.javatime.CurrentDateTime
    import org.jetbrains.exposed.sql.javatime.datetime

    object Users : Table("user") {
        val id = integer("id").autoIncrement()
        val email = varchar("email", 255).uniqueIndex()
        val firstName = varchar("first_name", 100)
        val lastName = varchar("last_name", 100)
        val middleName = varchar("middle_name", 100).nullable()
        val passwordHash = varchar("password_hash", 255)
        val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
        val isAdmin = bool("is_admin").default(false)
        override val primaryKey = PrimaryKey(id)
    }

