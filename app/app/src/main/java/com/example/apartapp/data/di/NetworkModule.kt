package com.example.apartapp.data.di

import com.example.apartapp.data.remote.AuthApiService
import com.example.apartapp.data.remote.FavoritesApiService
import com.example.apartapp.data.remote.ListingsApiService
import com.example.apartapp.data.remote.ParsingApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    companion object {
//        private const val BASE_URL = "http://10.0.2.2:8080/"
        private const val BASE_URL = "http://10.178.204.18:8080/"

    }

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideRetrofit(json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideParsingApiService(retrofit: Retrofit): ParsingApiService =
        retrofit.create(ParsingApiService::class.java)

    @Provides
    @Singleton
    fun provideListingsApiService(retrofit: Retrofit): ListingsApiService =
        retrofit.create(ListingsApiService::class.java)

    @Provides
    @Singleton
    fun provideFavoritesApiService(retrofit: Retrofit): FavoritesApiService =
        retrofit.create(FavoritesApiService::class.java)


}

