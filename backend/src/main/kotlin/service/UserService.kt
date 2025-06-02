package com.example.service

import com.example.database.tables.Users
import com.example.routes.dto.UserDto
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("UserService")

class UserService {
    fun getUserInfo(userId: Int): UserDto {
        return transaction {
            Users.select { Users.id eq userId }
                .map { row ->
                    UserDto(
                        id = row[Users.id],
                        email = row[Users.email],
                        firstName = row[Users.firstName],
                        lastName = row[Users.lastName],
                        isAdmin = row[Users.isAdmin]
                    )
                }
                .singleOrNull() ?: throw Exception("User not found")
        }
    }
} 