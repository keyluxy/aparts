package com.example.service

import com.example.auth.JwtConfig
import com.example.database.tables.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("AuthService")

class AuthService {
    fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        middleName: String?
    ): Pair<Int, String> {
        // Проверяем, не зарегистрирован ли уже пользователь с таким email
        val existingUser = transaction {
            Users.select { Users.email eq email }.singleOrNull()
        }
        if (existingUser != null) {
            throw IllegalArgumentException("Email already registered")
        }

        // Хешируем пароль
        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())

        // Создаем пользователя
        val userId = transaction {
            Users.insert {
                it[Users.email] = email
                it[Users.passwordHash] = hashedPassword
                it[Users.firstName] = firstName
                it[Users.lastName] = lastName
                it[Users.middleName] = middleName
                it[Users.isAdmin] = false
            } get Users.id
        }

        // Генерируем токен
        val token = JwtConfig.makeToken(userId)

        logger.info("User registered successfully: $email")
        return Pair(userId, token)
    }

    fun login(email: String, password: String): String {
        val user = transaction {
            Users.select { Users.email eq email }.singleOrNull()
        } ?: throw IllegalArgumentException("User not found")

        val hashedPassword = user[Users.passwordHash]
        if (!BCrypt.checkpw(password, hashedPassword)) {
            throw IllegalArgumentException("Invalid credentials")
        }

        val userId = user[Users.id]
        val token = JwtConfig.makeToken(userId)

        logger.info("User logged in successfully: $email")
        return token
    }
}
