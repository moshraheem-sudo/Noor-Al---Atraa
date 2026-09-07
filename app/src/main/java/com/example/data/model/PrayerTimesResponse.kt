package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PrayerTimesResponse(
    @field:Json(name = "fajir")
    val fajir: String? = null,

    @field:Json(name = "sunrise")
    val sunrise: String? = null,

    @field:Json(name = "doher")
    val doher: String? = null,

    @field:Json(name = "sunset")
    val sunset: String? = null,

    @field:Json(name = "maghrib")
    val maghrib: String? = null,

    @field:Json(name = "midnight")
    val midnight: String? = null,

    @field:Json(name = "date")
    val date: String? = null,

    @field:Json(name = "powerdby")
    val poweredBy: String? = null
)
