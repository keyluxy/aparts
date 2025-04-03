package com.example.database.db_factory

import com.example.database.tables.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseInitializer {
    fun init() {
        // Инициализируем подключение к базе данных
        DatabaseFactory.init()

        transaction {
            // Удаляем только зависимые таблицы, оставляя таблицу Sources нетронутой
            SchemaUtils.drop(Favorites, Listings, Cities, Users)
            // Создаем все таблицы, если их еще нет; таблица Sources уже существует
            SchemaUtils.create(Users, Sources, Cities, Listings, Favorites)
        }


    }
}
