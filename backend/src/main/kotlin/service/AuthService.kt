package com.example.service

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.auth.JwtConfig
import com.example.database.tables.Users
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class AuthService {

    fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        middleName: String?
    ): Int {
        val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        return transaction {
            val exists = Users.select { Users.email eq email }.count() > 0
            if (exists) throw IllegalArgumentException("Email already registered")

            Users.insert {
                it[Users.email] = email
                it[Users.passwordHash] = hashedPassword
                it[Users.firstName] = firstName
                it[Users.lastName] = lastName
                it[Users.middleName] = middleName
            }[Users.id]
        }
    }

    fun login(email: String, password: String): String {
        return transaction {
            val userRow = Users.select { Users.email eq email }.singleOrNull()
                ?: throw IllegalArgumentException("User not found")

            val storedHash = userRow[Users.passwordHash]
            val verifyResult = BCrypt.verifyer().verify(password.toCharArray(), storedHash)
            if (!verifyResult.verified) throw IllegalArgumentException("Invalid credentials")

            JwtConfig.makeToken(userRow[Users.id])
        }
    }
}
