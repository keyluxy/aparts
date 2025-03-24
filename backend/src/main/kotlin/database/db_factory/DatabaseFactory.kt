package com.example.database.db_factory


import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun init() {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://localhost:5432/apart-db"
            driverClassName = "org.postgresql.Driver"
            username = "postgres"
            password = "admin"
            maximumPoolSize = 5
        }
        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)
    }
}
