package com.example.data.remote

import com.example.data.model.AladhanResponse
import com.example.data.model.PrayerTimesResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit

interface KafeelApiService {
    @GET("Api/init/init.php")
    suspend fun getPrayerTimes(
        @Query("v") version: String = "jsonPrayerTimes",
        @Query("timezone") timezone: String,
        @Query("long") longitude: String,
        @Query("lati") latitude: String
    ): PrayerTimesResponse
}

interface AladhanApiService {
    @GET("v1/timings")
    suspend fun getPrayerTimes(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 0 // 0 = Shia Ithna-Ashari, Leva Institute, Qum
    ): AladhanResponse
}

object ApiClient {
    private const val BASE_URL = "https://hq.alkafeel.net/"
    private const val ALADHAN_BASE_URL = "https://api.aladhan.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "HaqybatAlMomen/1.0 (Android; Mobile)")
                .header("Accept", "application/json, text/plain, */*")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val apiService: KafeelApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
            .build()
            .create(KafeelApiService::class.java)
    }

    val aladhanService: AladhanApiService by lazy {
        Retrofit.Builder()
            .baseUrl(ALADHAN_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
            .build()
            .create(AladhanApiService::class.java)
    }
}
