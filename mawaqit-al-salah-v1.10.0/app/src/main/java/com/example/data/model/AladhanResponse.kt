package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AladhanResponse(
    @field:Json(name = "code") val code: Int? = null,
    @field:Json(name = "status") val status: String? = null,
    @field:Json(name = "data") val data: AladhanData? = null
)

@JsonClass(generateAdapter = true)
data class AladhanData(
    @field:Json(name = "timings") val timings: AladhanTimings? = null,
    @field:Json(name = "date") val date: AladhanDate? = null
)

@JsonClass(generateAdapter = true)
data class AladhanTimings(
    @field:Json(name = "Fajr") val fajr: String? = null,
    @field:Json(name = "Sunrise") val sunrise: String? = null,
    @field:Json(name = "Dhuhr") val dhuhr: String? = null,
    @field:Json(name = "Sunset") val sunset: String? = null,
    @field:Json(name = "Maghrib") val maghrib: String? = null,
    @field:Json(name = "Isha") val isha: String? = null,
    @field:Json(name = "Midnight") val midnight: String? = null
)

@JsonClass(generateAdapter = true)
data class AladhanDate(
    @field:Json(name = "readable") val readable: String? = null,
    @field:Json(name = "hijri") val hijri: AladhanHijri? = null
)

@JsonClass(generateAdapter = true)
data class AladhanHijri(
    @field:Json(name = "date") val date: String? = null,
    @field:Json(name = "day") val day: String? = null,
    @field:Json(name = "month") val month: AladhanMonth? = null,
    @field:Json(name = "year") val year: String? = null
)

@JsonClass(generateAdapter = true)
data class AladhanMonth(
    @field:Json(name = "number") val number: Int? = null,
    @field:Json(name = "en") val en: String? = null,
    @field:Json(name = "ar") val ar: String? = null
)
