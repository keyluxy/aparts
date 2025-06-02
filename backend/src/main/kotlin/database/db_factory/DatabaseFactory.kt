package com.example.database.db_factory

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("DatabaseFactory")

object DatabaseFactory {
    fun init() {
        try {
            logger.info("Initializing database connection...")
            val config = HikariConfig().apply {
                jdbcUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/apart-db"
                driverClassName = "org.postgresql.Driver"
                username = System.getenv("DATABASE_USER") ?: "postgres"
                password = System.getenv("DATABASE_PASSWORD") ?: "admin"
                maximumPoolSize = 5
                isAutoCommit = true
                connectionTimeout = 30000
                validationTimeout = 5000
                idleTimeout = 600000
                maxLifetime = 1800000
            }
            
            val dataSource = HikariDataSource(config)
            Database.connect(dataSource)
            
            // Проверяем соединение
            dataSource.connection.use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeQuery("SELECT 1").use { resultSet ->
                        if (resultSet.next()) {
                            logger.info("Database connection test successful")
                        } else {
                            throw Exception("Database connection test failed")
                        }
                    }
                }
            }
            
            logger.info("Database connection initialized successfully")
        } catch (e: Exception) {
            logger.error("Failed to initialize database connection", e)
            throw e
        }
    }
}
