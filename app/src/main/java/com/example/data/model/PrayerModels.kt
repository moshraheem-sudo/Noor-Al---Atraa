package com.example.data.model

enum class PrayerType(val arName: String, val enName: String) {
    FAJR("صلاة الفجر", "Fajr"),
    SUNRISE("الشروق", "Sunrise"),
    DHUHR("صلاة الظهر", "Dhuhr"),
    ASR("صلاة العصر", "Asr"),
    SUNSET("الغروب", "Sunset"),
    MAGHRIB("صلاة المغرب", "Maghrib"),
    ISHA("صلاة العشاء", "Isha"),
    MIDNIGHT("منتصف الليل", "Midnight")
}

data class PrayerTimeDisplay(
    val type: PrayerType,
    val rawTime: String,
    val formatted12h: String,
    val isNext: Boolean = false,
    val isPassed: Boolean = false,
    val isNotificationEnabled: Boolean = true
)

data class PrayerTimesData(
    val fajir: String,
    val sunrise: String,
    val doher: String,
    val asr: String = "",
    val sunset: String,
    val maghrib: String,
    val isha: String = "",
    val midnight: String,
    val hijriDate: String,
    val gregorianDate: String,
    val city: CityLocation,
    val poweredBy: String,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val items: List<PrayerTimeDisplay>
        get() = listOfNotNull(
            PrayerTimeDisplay(PrayerType.FAJR, fajir, fajir),
            PrayerTimeDisplay(PrayerType.SUNRISE, sunrise, sunrise),
            PrayerTimeDisplay(PrayerType.DHUHR, doher, doher),
            if (asr.isNotBlank()) PrayerTimeDisplay(PrayerType.ASR, asr, asr) else null,
            PrayerTimeDisplay(PrayerType.SUNSET, sunset, sunset),
            PrayerTimeDisplay(PrayerType.MAGHRIB, maghrib, maghrib),
            if (isha.isNotBlank()) PrayerTimeDisplay(PrayerType.ISHA, isha, isha) else null,
            PrayerTimeDisplay(PrayerType.MIDNIGHT, midnight, midnight)
        )
}

data class NextPrayerInfo(
    val prayerType: PrayerType,
    val targetTimeStr: String,
    val remainingMillis: Long,
    val remainingFormatted: String,
    val progress: Float // 0f to 1f
)
