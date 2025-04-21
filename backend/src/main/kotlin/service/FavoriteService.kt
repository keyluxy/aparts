package com.example.service

import com.example.database.tables.Favorites
import com.example.database.tables.Listings
import com.example.database.tables.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class FavoriteService {

    fun getFavorites(userId: Int): List<Int> = transaction {
        Favorites
            .slice(Favorites.listingId)
            .select { Favorites.userId eq userId }
            .map { it[Favorites.listingId] }
    }

    fun addFavorite(userId: Int, listingId: Int) = transaction {
        // Проверяем существование пользователя
        val userExists = Users.select { Users.id eq userId }.count() > 0
        if (!userExists) throw IllegalArgumentException("User not found")

        // Проверяем существование объявления
        val listingExists = Listings.select { Listings.id eq listingId }.count() > 0
        if (!listingExists) throw IllegalArgumentException("Listing not found")

        // Проверяем, не добавлено ли уже в избранное
        val exists = Favorites.select {
            (Favorites.userId eq userId) and (Favorites.listingId eq listingId)
        }.count() > 0

        if (!exists) {
            Favorites.insert {
                it[Favorites.userId] = userId
                it[Favorites.listingId] = listingId
            }
        }
    }

    fun removeFavorite(userId: Int, listingId: Int) = transaction {
        Favorites.deleteWhere {
            (Favorites.userId eq userId) and (Favorites.listingId eq listingId)
        }
    }
}
