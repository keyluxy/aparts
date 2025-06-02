package com.example.apartapp.data.di

import android.content.Context
import com.example.apartapp.data.remote.AuthApiService
import com.example.apartapp.data.remote.FavoritesApiService
import com.example.apartapp.data.remote.ListingsApiService
import com.example.apartapp.data.remote.ParsingApiService
import com.example.apartapp.data.remote.AdminApiService
import com.example.apartapp.data.remote.UserApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    companion object {
        // Для эмулятора
//         private const val BASE_URL = "http://10.178.204.18:8080/"
        private const val BASE_URL = "http://10.0.2.2:8080/"
        // Для реального устройства в локальной сети
//        private const val BASE_URL = "http://192.168.31.138:8080/"
        // Для внешнего доступа
        // private const val BASE_URL = "https://cd20c175-22a4-4f7d-bc62-ef3bb2948d58.tunnel4.com"
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(@ApplicationContext context: Context): Interceptor {
        return object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .getString("auth_token", null)
                
                val request = chain.request().newBuilder().apply {
                    token?.let { addHeader("Authorization", "Bearer $it") }
                }.build()
                
                return chain.proceed(request)
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor { chain ->
                val request = chain.request()
                android.util.Log.d("NetworkModule", "Request: ${request.method} ${request.url}")
                
                val response = chain.proceed(request)
                val responseBody = response.body
                val responseBodyString = responseBody?.string()
                
                android.util.Log.d("NetworkModule", "Response: ${response.code} ${response.message}")
                android.util.Log.d("NetworkModule", "Response body: $responseBodyString")
                
                // Создаем новый response с тем же телом, так как оригинальное было прочитано
                response.newBuilder()
                    .body(responseBodyString?.let { ResponseBody.create(responseBody.contentType(), it) })
                    .build()
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
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

    @Provides
    @Singleton
    fun provideAdminApiService(retrofit: Retrofit): AdminApiService =
        retrofit.create(AdminApiService::class.java)

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService =
        retrofit.create(UserApiService::class.java)
}

