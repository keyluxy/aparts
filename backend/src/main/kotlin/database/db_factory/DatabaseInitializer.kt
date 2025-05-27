package com.example.database.db_factory

import com.example.database.migration.AddAdminField
import com.example.database.tables.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseInitializer {
    fun init() {
        // Инициализируем подключение к базе данных
        DatabaseFactory.init()

        transaction {
            // Удаляем старые таблицы, если они существуют (обратите внимание на порядок)
//            SchemaUtils.drop(ListingImages, Favorites, Listings, Cities, Sources, Users)
            // Создаем новые таблицы – сначала базовые, затем зависимости
            SchemaUtils.create(Users, Sources, Cities, Listings, Favorites, ListingImages)
            
            // Применяем миграции
            AddAdminField.migrate()
        }
    }
}
