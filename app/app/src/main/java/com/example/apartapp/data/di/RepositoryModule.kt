package com.example.apartapp.data.di

import com.example.apartapp.data.repository.AuthRepositoryImpl
import com.example.apartapp.data.repository.ListingsRepositoryImpl
import com.example.apartapp.domain.repository.AuthRepository
import com.example.apartapp.domain.repository.ListingsRepository
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
}
