package com.example.apartapp.data.di

import com.example.apartapp.data.repository.AdminRepositoryImpl
import com.example.apartapp.data.repository.AuthRepositoryImpl
import com.example.apartapp.data.repository.FavoritesRepositoryImpl
import com.example.apartapp.data.repository.ListingsRepositoryImpl
import com.example.apartapp.data.repository.UserRepositoryImpl
import com.example.apartapp.domain.repository.AdminRepository
import com.example.apartapp.domain.repository.AuthRepository
import com.example.apartapp.domain.repository.FavoritesRepository
import com.example.apartapp.domain.repository.ListingsRepository
import com.example.apartapp.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindListingsRepository(listingsRepositoryImpl: ListingsRepositoryImpl): ListingsRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(
        favoritesRepositoryImpl: FavoritesRepositoryImpl
    ): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindAdminRepository(
        adminRepositoryImpl: AdminRepositoryImpl
    ): AdminRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}
