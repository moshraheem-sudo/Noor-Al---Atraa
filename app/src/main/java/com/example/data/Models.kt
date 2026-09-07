package com.example.data

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

data class Person(
    val id: Int,
    val name: String,
    val title: String,
    val father: String,
    val mother: String,
    val birthDate: HijriDate?, // e.g. 15th Shaban 255 AH
    val deathDate: HijriDate?, // e.g. 8th Rabi' al-Awwal 260 AH
    val imamateDuration: String?, // duration in years
    val causeOfDeath: String?,
    val killer: String?,
    val childrenCount: Int?,
    val famousChildren: List<String>,
    val wives: List<String>,
    val bio: String
)

data class HijriDate(
    val day: Int,
    val month: Int, // 1 to 12
    val year: Int,
    val description: String = ""
) {
    override fun toString(): String {
        val monthName = getHijriMonthName(month)
        return "$day $monthName ${if (year > 0) "$year هـ" else ""}".trim()
    }
}

data class HijriEvent(
    val day: Int,
    val month: Int,
    val name: String,
    val isMartyrdom: Boolean,
    val historicalYear: Int? = null
)

fun getHijriMonthName(month: Int): String {
    val months = listOf(
        "المحرّم", "صفر", "ربيع الأول", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "شهر رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )
    return if (month in 1..12) months[month - 1] else ""
}
