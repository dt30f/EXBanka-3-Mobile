package com.exbanka.mobile.data.network

import com.exbanka.mobile.BuildConfig
import com.exbanka.mobile.data.local.SessionStore
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    fun createApi(sessionStore: SessionStore): BankingApiService {
        val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder().apply {
                sessionStore.getAccessToken()?.takeIf { it.isNotBlank() }?.let { token ->
                    header("Authorization", "Bearer $token")
                }
            }.build()
            chain.proceed(request)
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BankingApiService::class.java)
    }
}
