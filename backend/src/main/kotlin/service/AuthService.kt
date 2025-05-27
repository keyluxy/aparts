package com.example.service

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.auth.JwtConfig
import com.example.database.tables.Users
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("AuthService")

class AuthService {

    fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        middleName: String?
    ): Int {
        logger.info("Attempting to register user with email: $email")
        val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        return transaction {
            val exists = Users.select { Users.email eq email }.count() > 0
            if (exists) {
                logger.warn("Registration failed: Email already registered: $email")
                throw IllegalArgumentException("Email already registered")
            }

            try {
                val userId = Users.insert {
                    it[Users.email] = email
                    it[Users.passwordHash] = hashedPassword
                    it[Users.firstName] = firstName
                    it[Users.lastName] = lastName
                    it[Users.middleName] = middleName
                }[Users.id]
                logger.info("Successfully registered user with id: $userId")
                userId
            } catch (e: Exception) {
                logger.error("Registration failed for email: $email", e)
                throw e
            }
        }
    }

    fun login(email: String, password: String): String {
        logger.info("Attempting login for email: $email")
        return transaction {
            val userRow = Users.select { Users.email eq email }.singleOrNull()
            if (userRow == null) {
                logger.warn("Login failed: User not found for email: $email")
                throw IllegalArgumentException("User not found")
            }

            val storedHash = userRow[Users.passwordHash]
            val verifyResult = BCrypt.verifyer().verify(password.toCharArray(), storedHash)
            if (!verifyResult.verified) {
                logger.warn("Login failed: Invalid credentials for email: $email")
                throw IllegalArgumentException("Invalid credentials")
            }

            val userId = userRow[Users.id]
            val token = JwtConfig.makeToken(userId)
            logger.info("Successfully logged in user with id: $userId")
            token
        }
    }
}
