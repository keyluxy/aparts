package com.example.apartapp.data.di

import com.example.apartapp.data.repository.AuthRepositoryImpl
import com.example.apartapp.data.repository.ParsingRepositoryImpl
import com.example.apartapp.domain.repository.AuthRepository
import com.example.apartapp.domain.repository.ParsingRepository
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
    abstract fun bindParsingRepository(parsingRepositoryImpl: ParsingRepositoryImpl): ParsingRepository
}
