package com.example.database.db_factory

import com.example.database.tables.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseInitializer {
    fun init() {
        // Инициализируем подключение к базе данных
        DatabaseFactory.init()

        transaction {
//            SchemaUtils.drop(Favorites, Listings, Cities, Sources, Users)
            SchemaUtils.create(Users, Sources, Cities, Listings, Favorites)
        }

    }
}

