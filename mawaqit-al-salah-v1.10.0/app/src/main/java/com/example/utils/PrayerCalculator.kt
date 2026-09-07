package com.example.utils

import com.example.data.model.AppLanguage
import com.example.data.model.CalculationMethod
import com.example.data.model.CityLocation
import com.example.data.model.MidnightMethod
import com.example.data.model.NextPrayerInfo
import com.example.data.model.PrayerTimesData
import com.example.data.model.PrayerType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

object PrayerCalculator {

    private const val KAABA_LATITUDE = 21.422487
    private const val KAABA_LONGITUDE = 39.826206

    /**
     * Parses timezone string (e.g., "+3", "+3.5", "-4") to double
     */
    fun parseTimezoneOffset(tzStr: String): Double {
        val clean = tzStr.replace("+", "").trim()
        return clean.toDoubleOrNull() ?: 3.0
    }

    /**
     * Astronomical Shia prayer times calculation offline for any coordinates and date
     * Follows the official Al-Kafeel & Shia Ithna Ashari standards:
     * - Fajr: Sun 18.0° below horizon (or based on CalculationMethod)
     * - Sunrise: 0.833° (apparent)
     * - Dhuhr: Solar noon (+1 min precaution)
     * - Asr: Shadow factor = 1.0 (or Shia فضيلة العصر)
     * - Sunset: 0.833° (apparent)
     * - Maghrib: Sun 4.0° below horizon (Disappearance of Eastern Redness / ذهاب الحمرة المشرقية)
     * - Isha: Sun 14.0° below horizon (Disappearance of Western Twilight / سقوط الشفق الأحمر)
     * - Midnight: Midpoint between Sunset and Fajr (or Sunrise)
     */
    fun calculateAstronomicalPrayerTimes(
        latitude: Double,
        longitude: Double,
        timezoneOffset: Double,
        calendar: Calendar = Calendar.getInstance(),
        calculationMethod: CalculationMethod = CalculationMethod.JAFARI_KAFEEL,
        midnightMethod: MidnightMethod = MidnightMethod.SUNSET_TO_FAJR
    ): Map<PrayerType, String> {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Julian Date Calculation
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        val jd = day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045.5

        val d = jd - 2451545.0 // Days since J2000.0

        // Mean Anomaly & Longitude
        val g = Math.toRadians(fixAngle(357.529 + 0.98560028 * d))
        val q = Math.toRadians(fixAngle(280.459 + 0.98564736 * d))
        val l = Math.toRadians(fixAngle(Math.toDegrees(q) + 1.915 * sin(g) + 0.020 * sin(2 * g)))

        // Obliquity of Ecliptic
        val e = Math.toRadians(23.439 - 0.00000036 * d)

        // Right Ascension and Declination
        val raDeg = fixAngle(Math.toDegrees(atan2(cos(e) * sin(l), cos(l))))
        val sinDelta = sin(e) * sin(l)
        val delta = asin(sinDelta)

        // Equation of Time in hours with angular difference normalization
        var diffDeg = fixAngle(Math.toDegrees(q)) - raDeg
        if (diffDeg > 180.0) diffDeg -= 360.0
        if (diffDeg < -180.0) diffDeg += 360.0
        val eotHours = diffDeg / 15.0

        // Solar Noon (Dhuhr) in hours (adding 1 minute precaution after Zawal)
        val noon = fixHour(12.0 + timezoneOffset - (longitude / 15.0) - eotHours)
        val dhuhrTime = fixHour(noon + (1.0 / 60.0))

        // Hour Angle Calculation Helper
        fun getHourAngle(angleDegrees: Double): Double {
            val sinAngle = sin(Math.toRadians(angleDegrees))
            val sinLat = sin(Math.toRadians(latitude))
            val cosLat = cos(Math.toRadians(latitude))
            val cosDelta = cos(delta)

            val cosH = (sinAngle - sinLat * sinDelta) / (cosLat * cosDelta)
            if (cosH > 1.0) return 0.0
            if (cosH < -1.0) return 12.0
            return Math.toDegrees(acos(cosH)) / 15.0
        }

        // Asr Calculation based on shadow length (shadow = noon shadow + factor)
        // Altitude angle above horizon is positive for Asr (e.g. ~35°)
        fun getAsrHourAngle(factor: Double = 1.0): Double {
            val latRad = Math.toRadians(latitude)
            val dAngle = Math.toDegrees(atan(1.0 / (factor + tan(kotlin.math.abs(latRad - delta)))))
            return getHourAngle(dAngle)
        }

        val hFajr = getHourAngle(-calculationMethod.fajrAngle)
        val hSunrise = getHourAngle(-0.833)
        val hAsr = getAsrHourAngle(1.0)
        val hSunset = getHourAngle(-0.833)
        val hMaghrib = getHourAngle(-calculationMethod.maghribAngle)
        val hIsha = getHourAngle(-14.0)

        val fajrTime = fixHour(noon - hFajr)
        val sunriseTime = fixHour(noon - hSunrise)
        val asrTime = fixHour(noon + hAsr)
        val sunsetTime = fixHour(noon + hSunset)
        val maghribTime = fixHour(noon + hMaghrib)
        val ishaTime = fixHour(noon + hIsha)

        fun formatHoursToTimeString(hoursVal: Double): String {
            val totalMins = Math.round(hoursVal * 60).toInt()
            val h = (totalMins / 60) % 24
            val m = totalMins % 60
            return String.format(Locale.ENGLISH, "%02d:%02d", h, m)
        }

        val fajrStr = formatHoursToTimeString(fajrTime)
        val sunriseStr = formatHoursToTimeString(sunriseTime)
        val doherStr = formatHoursToTimeString(dhuhrTime)
        val asrStr = formatHoursToTimeString(asrTime)
        val sunsetStr = formatHoursToTimeString(sunsetTime)
        val maghribStr = formatHoursToTimeString(maghribTime)
        val ishaStr = formatHoursToTimeString(ishaTime)

        val midnightStr = if (midnightMethod == MidnightMethod.SUNSET_TO_FAJR) {
            calculateMidnight(sunsetStr, fajrStr)
        } else {
            calculateMidnight(sunsetStr, sunriseStr)
        }

        return mapOf(
            PrayerType.FAJR to fajrStr,
            PrayerType.SUNRISE to sunriseStr,
            PrayerType.DHUHR to doherStr,
            PrayerType.ASR to asrStr,
            PrayerType.SUNSET to sunsetStr,
            PrayerType.MAGHRIB to maghribStr,
            PrayerType.ISHA to ishaStr,
            PrayerType.MIDNIGHT to midnightStr
        )
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle - (360.0 * Math.floor(angle / 360.0))
        if (a < 0) a += 360.0
        return a
    }

    private fun fixHour(hour: Double): Double {
        var h = hour - (24.0 * Math.floor(hour / 24.0))
        if (h < 0) h += 24.0
        return h
    }

    /**
     * Generates a complete high-precision offline PrayerTimesData for any city/coordinates
     */
    fun calculateOfflinePrayerData(
        city: CityLocation,
        calendar: Calendar = Calendar.getInstance(),
        calculationMethod: CalculationMethod = CalculationMethod.JAFARI_KAFEEL,
        midnightMethod: MidnightMethod = MidnightMethod.SUNSET_TO_FAJR
    ): PrayerTimesData {
        val tz = parseTimezoneOffset(city.timezone)
        val times = calculateAstronomicalPrayerTimes(
            latitude = city.latitude,
            longitude = city.longitude,
            timezoneOffset = tz,
            calendar = calendar,
            calculationMethod = calculationMethod,
            midnightMethod = midnightMethod
        )

        val fajir = times[PrayerType.FAJR] ?: "04:12"
        val sunrise = times[PrayerType.SUNRISE] ?: "05:37"
        val doher = times[PrayerType.DHUHR] ?: "11:57"
        val asr = times[PrayerType.ASR] ?: "15:28"
        val sunset = times[PrayerType.SUNSET] ?: "18:17"
        val maghrib = times[PrayerType.MAGHRIB] ?: "18:32"
        val isha = times[PrayerType.ISHA] ?: "19:35"
        val midnight = times[PrayerType.MIDNIGHT] ?: calculateMidnight(sunset, fajir)

        return PrayerTimesData(
            fajir = fajir,
            sunrise = sunrise,
            doher = doher,
            asr = asr,
            sunset = sunset,
            maghrib = maghrib,
            isha = isha,
            midnight = midnight,
            hijriDate = getFormattedHijriDate(calendar.time),
            gregorianDate = getFormattedGregorianDate(calendar.time),
            city = city,
            poweredBy = "حقيبة المؤمن",
            lastUpdated = System.currentTimeMillis()
        )
    }

    /**
     * Adds minute offset to 24h time string with proper 24h wrap around
     */
    fun addMinutesOffset(time24: String, offsetMinutes: Int): String {
        if (offsetMinutes == 0) return cleanTimeString(time24)
        try {
            val parts = cleanTimeString(time24).split(":")
            val h = parts[0].toInt()
            val m = parts[1].toInt()
            var totalMinutes = h * 60 + m + offsetMinutes
            while (totalMinutes < 0) totalMinutes += 24 * 60
            totalMinutes %= (24 * 60)
            val newH = totalMinutes / 60
            val newM = totalMinutes % 60
            return String.format(Locale.ENGLISH, "%02d:%02d", newH, newM)
        } catch (_: Exception) {
            return time24
        }
    }

    /**
     * Formats Hijri Islamic date with day offset (e.g. +1, -1)
     */
    fun getFormattedHijriDateWithOffset(date: Date = Date(), dayOffset: Int = 0, lang: AppLanguage = AppLanguage.ARABIC): String {
        if (dayOffset == 0) return getFormattedHijriDate(date, lang)
        val cal = Calendar.getInstance().apply {
            time = date
            add(Calendar.DAY_OF_YEAR, dayOffset)
        }
        return getFormattedHijriDate(cal.time, lang)
    }

    /**
     * Formats Hijri Islamic date according to selected language
     * (e.g., Arabic: 23 ربيع الأول 1448 هـ, English: 23 Rabi' al-Awwal 1448 AH)
     */
    fun getFormattedHijriDate(date: Date = Date(), lang: AppLanguage = AppLanguage.ARABIC): String {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                val icuCalendar = android.icu.util.IslamicCalendar()
                icuCalendar.time = date
                val day = icuCalendar.get(android.icu.util.IslamicCalendar.DAY_OF_MONTH)
                val month = icuCalendar.get(android.icu.util.IslamicCalendar.MONTH)
                val year = icuCalendar.get(android.icu.util.IslamicCalendar.YEAR)
                return if (lang == AppLanguage.ARABIC) {
                    val monthsAr = arrayOf(
                        "محرم الحرام", "صفر الخير", "ربيع الأول", "ربيع الثاني",
                        "جمادى الأولى", "جمادى الآخرة", "رجب الأصب", "شعبان المعظم",
                        "رمضان المبارك", "شوال المكرم", "ذو القعدة", "ذو الحجة"
                    )
                    val monthName = monthsAr.getOrElse(month) { "هجري" }
                    "$day $monthName $year هـ"
                } else {
                    val monthsEn = arrayOf(
                        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
                        "Jumada al-Ula", "Jumada al-Thaniyah", "Rajab", "Sha'ban",
                        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
                    )
                    val monthName = monthsEn.getOrElse(month) { "AH" }
                    "$day $monthName $year AH"
                }
            }
        } catch (_: Exception) {}
        return if (lang == AppLanguage.ARABIC) "التقويم الهجري الشريف" else "Hijri Calendar"
    }

    /**
     * Formats and ensures full Hijri Islamic date with full month and year according to language
     * (e.g. 23 ربيع الأول 1448 هـ / 23 Rabi' al-Awwal 1448 AH)
     */
    fun formatFullHijriDate(rawHijri: String? = null, lang: AppLanguage = AppLanguage.ARABIC, date: Date = Date()): String {
        if (rawHijri.isNullOrBlank()) {
            return getFormattedHijriDate(date, lang)
        }
        val trimmed = rawHijri.trim()
        val arabicDate = if (trimmed.matches(Regex(".*\\d{4}.*"))) {
            if (!trimmed.endsWith("هـ") && !trimmed.endsWith("ه")) "$trimmed هـ" else trimmed
        } else {
            var hijriYear = 1448
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    val icuCalendar = android.icu.util.IslamicCalendar()
                    icuCalendar.time = date
                    hijriYear = icuCalendar.get(android.icu.util.IslamicCalendar.YEAR)
                }
            } catch (_: Exception) {}
            "$trimmed $hijriYear هـ"
        }

        if (lang == AppLanguage.ARABIC) {
            return arabicDate
        }

        return convertArabicHijriToEnglish(arabicDate, date)
    }

    private fun convertArabicHijriToEnglish(arabicHijri: String, fallbackDate: Date): String {
        var text = arabicHijri
        val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        for (i in arabicDigits.indices) {
            text = text.replace(arabicDigits[i], ('0' + i))
        }

        val monthMap = listOf(
            "محرم الحرام" to "Muharram",
            "محرم" to "Muharram",
            "صفر الخير" to "Safar",
            "صفر" to "Safar",
            "ربيع الأول" to "Rabi' al-Awwal",
            "ربيع الاول" to "Rabi' al-Awwal",
            "ربيع الثاني" to "Rabi' al-Thani",
            "ربيع الآخر" to "Rabi' al-Thani",
            "ربيع الاخر" to "Rabi' al-Thani",
            "جمادى الأولى" to "Jumada al-Ula",
            "جمادى الاولى" to "Jumada al-Ula",
            "جمادى الأول" to "Jumada al-Ula",
            "جمادى الاول" to "Jumada al-Ula",
            "جمادى الآخرة" to "Jumada al-Thaniyah",
            "جمادى الاخره" to "Jumada al-Thaniyah",
            "جمادى الثانية" to "Jumada al-Thaniyah",
            "جمادى الثانيه" to "Jumada al-Thaniyah",
            "رجب الأصب" to "Rajab",
            "رجب الاصب" to "Rajab",
            "رجب" to "Rajab",
            "شعبان المعظم" to "Sha'ban",
            "شعبان" to "Sha'ban",
            "رمضان المبارك" to "Ramadan",
            "رمضان" to "Ramadan",
            "شوال المكرم" to "Shawwal",
            "شوال" to "Shawwal",
            "ذو القعدة" to "Dhu al-Qi'dah",
            "ذو القعده" to "Dhu al-Qi'dah",
            "ذو الحجة" to "Dhu al-Hijjah",
            "ذو الحجه" to "Dhu al-Hijjah"
        )

        var monthFound = false
        for ((arMonth, enMonth) in monthMap) {
            if (text.contains(arMonth)) {
                text = text.replace(arMonth, enMonth)
                monthFound = true
                break
            }
        }

        text = text.replace("هـ", "AH")
            .replace("ه", "AH")
            .replace("التقويم الهجري الشريف", "Hijri Calendar")
            .trim()

        if (!text.endsWith("AH", ignoreCase = true)) {
            text = "$text AH"
        }

        return if (monthFound) text else getFormattedHijriDate(fallbackDate, AppLanguage.ENGLISH)
    }

    /**
     * Clean and trim time string from API (e.g., "5:18 " -> "05:18")
     */
    fun cleanTimeString(raw: String?): String {
        if (raw.isNullOrBlank()) return "00:00"
        val trimmed = raw.trim()
        val parts = trimmed.split(":")
        if (parts.size == 2) {
            val h = parts[0].trim().toIntOrNull() ?: 0
            val m = parts[1].trim().toIntOrNull() ?: 0
            return String.format(Locale.ENGLISH, "%02d:%02d", h, m)
        }
        return trimmed
    }

    /**
     * Format 24-hour time "13:45" to 12-hour format with language-aware AM/PM
     */
    fun formatTo12h(time24: String, lang: AppLanguage = AppLanguage.ARABIC): String {
        val cleaned = cleanTimeString(time24)
        val parts = cleaned.split(":")
        if (parts.size == 2) {
            val h = parts[0].toIntOrNull() ?: 0
            val m = parts[1].toIntOrNull() ?: 0
            val period = when (lang) {
                AppLanguage.ARABIC -> if (h >= 12) "م" else "ص"
                AppLanguage.ENGLISH -> if (h >= 12) "PM" else "AM"
            }
            val h12 = when {
                h == 0 -> 12
                h > 12 -> h - 12
                else -> h
            }
            return String.format(Locale.ENGLISH, "%02d:%02d %s", h12, m, period)
        }
        return time24
    }

    /**
     * Format 24-hour time "13:45" to Arabic 12-hour format "01:45 م"
     */
    fun formatTo12hArabic(time24: String): String {
        return formatTo12h(time24, AppLanguage.ARABIC)
    }

    /**
     * Calculates Islamic Midnight (منتصف الليل الشرعي)
     * In Shia Fiqh (Ayatollah Sistani & Al-Kafeel standard):
     * Exactly halfway between Sunset (الغروب) and Dawn/Fajr (طلوع الفجر) the next day.
     */
    fun calculateMidnight(sunsetStr: String, fajrStr: String): String {
        try {
            val sunsetClean = cleanTimeString(sunsetStr).split(":")
            val fajrClean = cleanTimeString(fajrStr).split(":")

            val sunsetHour = sunsetClean[0].toInt()
            val sunsetMin = sunsetClean[1].toInt()

            val fajrHour = fajrClean[0].toInt()
            val fajrMin = fajrClean[1].toInt()

            val sunsetTotalMinutes = sunsetHour * 60 + sunsetMin
            val fajrTotalMinutes = (fajrHour + 24) * 60 + fajrMin // next day morning

            val diff = fajrTotalMinutes - sunsetTotalMinutes
            val midnightTotalMinutes = (sunsetTotalMinutes + (diff / 2)) % (24 * 60)

            val h = midnightTotalMinutes / 60
            val m = midnightTotalMinutes % 60
            return String.format(Locale.ENGLISH, "%02d:%02d", h, m)
        } catch (e: Exception) {
            return "23:15"
        }
    }

    /**
     * Determines next prayer and computes remaining countdown time
     */
    fun getNextPrayerInfo(data: PrayerTimesData, nowCalendar: Calendar = Calendar.getInstance()): NextPrayerInfo {
        val nowMillis = nowCalendar.timeInMillis
        val today = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        fun getTimeMillis(timeStr: String, dayOffset: Int = 0): Long {
            val parts = cleanTimeString(timeStr).split(":")
            val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val c = today.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, dayOffset)
            c.set(Calendar.HOUR_OF_DAY, h)
            c.set(Calendar.MINUTE, m)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
            return c.timeInMillis
        }

        val fajrToday = getTimeMillis(data.fajir)
        val dhuhrToday = getTimeMillis(data.doher)
        val maghribToday = getTimeMillis(data.maghrib)
        val fajrTomorrow = getTimeMillis(data.fajir, dayOffset = 1)
        val maghribYesterday = getTimeMillis(data.maghrib, dayOffset = -1)

        // Only Fajr, Dhuhr, and Maghrib are the Adhan prayers featured in the Upcoming Prayer hero card
        val prayerSchedule = listOf(
            Triple(PrayerType.FAJR, fajrToday, data.fajir),
            Triple(PrayerType.DHUHR, dhuhrToday, data.doher),
            Triple(PrayerType.MAGHRIB, maghribToday, data.maghrib)
        ).sortedBy { it.second }

        var nextType = PrayerType.FAJR
        var nextTimeStr = data.fajir
        var nextMillis = fajrTomorrow
        var prevMillis = maghribToday

        var found = false
        for (i in prayerSchedule.indices) {
            val current = prayerSchedule[i]
            if (nowMillis < current.second) {
                nextType = current.first
                nextTimeStr = current.third
                nextMillis = current.second
                prevMillis = if (i > 0) prayerSchedule[i - 1].second else maghribYesterday
                found = true
                break
            }
        }
        if (!found) {
            // After today's Maghrib -> Next is Fajr Tomorrow
            nextType = PrayerType.FAJR
            nextTimeStr = data.fajir
            nextMillis = fajrTomorrow
            prevMillis = maghribToday
        }

        val remainingMillis = (nextMillis - nowMillis).coerceAtLeast(0L)
        val totalInterval = (nextMillis - prevMillis).coerceAtLeast(1L)
        val progress = (1f - (remainingMillis.toFloat() / totalInterval.toFloat())).coerceIn(0f, 1f)

        val totalSeconds = remainingMillis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        val formattedRemaining = if (hours > 0) {
            String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds)
        }

        return NextPrayerInfo(
            prayerType = nextType,
            targetTimeStr = nextTimeStr,
            remainingMillis = remainingMillis,
            remainingFormatted = formattedRemaining,
            progress = progress
        )
    }

    /**
     * Calculates Qibla direction (bearing in degrees 0..360) from any latitude & longitude to Mecca
     */
    fun calculateQiblaBearing(latitude: Double, longitude: Double): Double {
        val latRad = Math.toRadians(latitude)
        val lonRad = Math.toRadians(longitude)
        val kaabaLatRad = Math.toRadians(KAABA_LATITUDE)
        val kaabaLonRad = Math.toRadians(KAABA_LONGITUDE)

        val deltaLon = kaabaLonRad - lonRad

        val y = sin(deltaLon)
        val x = cos(latRad) * tan(kaabaLatRad) - sin(latRad) * cos(deltaLon)

        var qiblaRad = atan2(y, x)
        var qiblaDeg = Math.toDegrees(qiblaRad)
        qiblaDeg = (qiblaDeg + 360) % 360

        return qiblaDeg
    }

    /**
     * Distance to Kaaba in kilometers
     */
    fun calculateDistanceToKaaba(latitude: Double, longitude: Double): Double {
        val r = 6371.0 // Earth radius in km
        val lat1 = Math.toRadians(latitude)
        val lon1 = Math.toRadians(longitude)
        val lat2 = Math.toRadians(KAABA_LATITUDE)
        val lon2 = Math.toRadians(KAABA_LONGITUDE)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return r * c
    }

    /**
     * Formats Gregorian date in Arabic or English
     * (e.g. Arabic: الأحد، 6 سبتمبر 2026 م, English: Sunday, 6 September 2026)
     */
    fun getFormattedGregorianDate(date: Date = Date(), lang: AppLanguage = AppLanguage.ARABIC): String {
        return if (lang == AppLanguage.ARABIC) {
            val sdf = SimpleDateFormat("EEEE، d MMMM yyyy م", Locale("ar"))
            sdf.format(date)
        } else {
            val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.ENGLISH)
            sdf.format(date)
        }
    }

    /**
     * Ensures Gregorian date is in the requested language (translates/re-formats if language mismatch)
     */
    fun formatFullGregorianDate(rawGregorian: String? = null, lang: AppLanguage = AppLanguage.ARABIC, date: Date = Date()): String {
        return if (lang == AppLanguage.ENGLISH) {
            if (!rawGregorian.isNullOrBlank() && rawGregorian.any { it in 'a'..'z' || it in 'A'..'Z' } && !rawGregorian.any { it in '\u0600'..'\u06FF' }) {
                rawGregorian
            } else {
                getFormattedGregorianDate(date, AppLanguage.ENGLISH)
            }
        } else {
            if (!rawGregorian.isNullOrBlank() && rawGregorian.any { it in '\u0600'..'\u06FF' }) {
                rawGregorian
            } else {
                getFormattedGregorianDate(date, AppLanguage.ARABIC)
            }
        }
    }

    /**
     * Offline fallback prayer times for Karbala/Iraq according to Al-Kafeel standards
     */
    fun getOfflineFallbackData(city: CityLocation): PrayerTimesData {
        return calculateOfflinePrayerData(city)
    }
}
