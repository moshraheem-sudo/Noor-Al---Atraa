package com.example.data

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.Json

data class IpApiResponse(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val city: String? = null,
    val country: String? = null
)

data class AladhanResponse(
    val code: Int,
    val status: String,
    val data: AladhanData
)

data class AladhanData(
    val timings: Timings,
    val date: DateInfo
)

data class Timings(
    val Fajr: String,
    val Sunrise: String,
    val Dhuhr: String,
    val Asr: String,
    val Sunset: String,
    val Maghrib: String,
    val Isha: String,
    val Imsak: String,
    val Midnight: String
)

data class DateInfo(
    val readable: String,
    val timestamp: String,
    val hijri: HijriInfo,
    val gregorian: GregorianInfo
)

data class HijriInfo(
    val date: String,
    val format: String,
    val day: String,
    @field:Json(name = "weekday") val weekday: HijriWeekday,
    val month: HijriMonth,
    val year: String,
    val designation: Designation,
    val holidays: List<String>
)

data class GregorianInfo(
    val date: String,
    val format: String,
    val day: String,
    @field:Json(name = "weekday") val weekday: GregorianWeekday,
    val month: GregorianMonth,
    val year: String,
    val designation: Designation
)

data class HijriWeekday(
    val en: String,
    val ar: String
)

data class GregorianWeekday(
    val en: String
)

data class HijriMonth(
    val number: Int,
    val en: String,
    val ar: String
)

data class GregorianMonth(
    val number: Int,
    val en: String
)

data class Designation(
    val abbreviated: String,
    val expanded: String
)

interface ApiService {
    @GET("https://ipapi.co/json/")
    suspend fun getLocation(): IpApiResponse

    @GET("https://api.aladhan.com/v1/timings")
    suspend fun getTimings(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 4, // 4 is Shia Ithna-Ashari
        @Query("shafaq") shafaq: String = "general"
    ): AladhanResponse
}

object ApiClient {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.aladhan.com/") // Base URL doesn't matter much if we use absolute URLs in @GET
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
