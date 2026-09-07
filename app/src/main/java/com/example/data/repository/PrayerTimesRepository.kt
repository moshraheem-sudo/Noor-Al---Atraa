package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.model.AdhanSoundMode
import com.example.data.model.AlarmRepeatMode
import com.example.data.model.AppLanguage
import com.example.data.model.CalculationMethod
import com.example.data.model.CityLocation
import com.example.data.model.MidnightMethod
import com.example.data.model.Muezzin
import com.example.data.model.PrayerCustomAlarmConfig
import com.example.data.model.PrayerTimesData
import com.example.data.model.PrayerType
import com.example.data.model.PredefinedCities
import com.example.data.model.ThemeMode
import com.example.data.remote.ApiClient
import com.example.utils.PrayerCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrayerTimesRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("kafeel_prayer_prefs", Context.MODE_PRIVATE)

    private val _selectedCity = MutableStateFlow(loadSavedCity())
    val selectedCity: StateFlow<CityLocation> = _selectedCity.asStateFlow()

    private val _themeMode = MutableStateFlow(loadSavedThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _appLanguage = MutableStateFlow(loadSavedAppLanguage())
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _calculationMethod = MutableStateFlow(loadSavedCalculationMethod())
    val calculationMethod: StateFlow<CalculationMethod> = _calculationMethod.asStateFlow()

    private val _midnightMethod = MutableStateFlow(loadSavedMidnightMethod())
    val midnightMethod: StateFlow<MidnightMethod> = _midnightMethod.asStateFlow()

    private val _showAsrSeparate = MutableStateFlow(loadSavedShowAsrSeparate())
    val showAsrSeparate: StateFlow<Boolean> = _showAsrSeparate.asStateFlow()

    private val _showIshaSeparate = MutableStateFlow(loadSavedShowIshaSeparate())
    val showIshaSeparate: StateFlow<Boolean> = _showIshaSeparate.asStateFlow()

    private val _selectedMuezzin = MutableStateFlow(loadSavedMuezzin())
    val selectedMuezzin: StateFlow<Muezzin> = _selectedMuezzin.asStateFlow()

    private val _isAdhanAudioEnabled = MutableStateFlow(prefs.getBoolean("adhan_audio_enabled", true))
    val isAdhanAudioEnabled: StateFlow<Boolean> = _isAdhanAudioEnabled.asStateFlow()

    fun setSelectedMuezzin(muezzin: Muezzin) {
        _selectedMuezzin.value = muezzin
        prefs.edit().putString("selected_muezzin_id", muezzin.id).apply()
    }

    private fun loadSavedMuezzin(): Muezzin {
        val savedId = prefs.getString("selected_muezzin_id", Muezzin.defaultMuezzin.id)
        return Muezzin.fromId(savedId)
    }

    fun setAdhanAudioEnabled(enabled: Boolean) {
        _isAdhanAudioEnabled.value = enabled
        prefs.edit().putBoolean("adhan_audio_enabled", enabled).apply()
    }

    private val _githubAutoSyncEnabled = MutableStateFlow(prefs.getBoolean("github_auto_sync", true))
    val githubAutoSyncEnabled: StateFlow<Boolean> = _githubAutoSyncEnabled.asStateFlow()

    private val _githubRepoOwner = MutableStateFlow(prefs.getString("github_repo_owner", "moshraheem-sudo") ?: "moshraheem-sudo")
    val githubRepoOwner: StateFlow<String> = _githubRepoOwner.asStateFlow()

    private val _githubRepoName = MutableStateFlow(prefs.getString("github_repo_name", "hijri") ?: "hijri")
    val githubRepoName: StateFlow<String> = _githubRepoName.asStateFlow()

    private val _githubToken = MutableStateFlow(prefs.getString("github_token", "") ?: "")
    val githubToken: StateFlow<String> = _githubToken.asStateFlow()

    private val _githubWebhookUrl = MutableStateFlow(prefs.getString("github_webhook_url", "") ?: "")
    val githubWebhookUrl: StateFlow<String> = _githubWebhookUrl.asStateFlow()

    private val _lastHijriSyncTime = MutableStateFlow(prefs.getLong("last_hijri_sync_time", 0L))
    val lastHijriSyncTime: StateFlow<Long> = _lastHijriSyncTime.asStateFlow()

    fun setGithubAutoSyncEnabled(enabled: Boolean) {
        _githubAutoSyncEnabled.value = enabled
        prefs.edit().putBoolean("github_auto_sync", enabled).apply()
    }

    fun saveGithubSettings(owner: String, name: String, token: String, webhookUrl: String) {
        _githubRepoOwner.value = owner.trim()
        _githubRepoName.value = name.trim()
        _githubToken.value = token.trim()
        _githubWebhookUrl.value = webhookUrl.trim()
        prefs.edit().apply {
            putString("github_repo_owner", owner.trim())
            putString("github_repo_name", name.trim())
            putString("github_token", token.trim())
            putString("github_webhook_url", webhookUrl.trim())
            apply()
        }
    }

    fun setAppLanguage(language: AppLanguage) {
        _appLanguage.value = language
        prefs.edit().putString("app_language_code", language.name).apply()
    }

    private fun loadSavedAppLanguage(): AppLanguage {
        val saved = prefs.getString("app_language_code", AppLanguage.ARABIC.name)
        return try {
            AppLanguage.valueOf(saved ?: AppLanguage.ARABIC.name)
        } catch (_: Exception) {
            AppLanguage.ARABIC
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("app_theme_mode", mode.name).apply()
    }

    private fun loadSavedThemeMode(): ThemeMode {
        val saved = prefs.getString("app_theme_mode", null)
        if (saved == null || saved == ThemeMode.SYSTEM.name) {
            return ThemeMode.LIGHT
        }
        return try {
            ThemeMode.valueOf(saved)
        } catch (_: Exception) {
            ThemeMode.LIGHT
        }
    }

    fun setCalculationMethod(method: CalculationMethod) {
        _calculationMethod.value = method
        prefs.edit().putString("calc_method_id", method.id).apply()
        recomputeAndRefreshCurrentTimes()
    }

    private fun loadSavedCalculationMethod(): CalculationMethod {
        val id = prefs.getString("calc_method_id", CalculationMethod.JAFARI_KAFEEL.id)
        return CalculationMethod.fromId(id)
    }

    fun setMidnightMethod(method: MidnightMethod) {
        _midnightMethod.value = method
        prefs.edit().putString("midnight_method_id", method.id).apply()
        recomputeAndRefreshCurrentTimes()
    }

    private fun loadSavedMidnightMethod(): MidnightMethod {
        val id = prefs.getString("midnight_method_id", MidnightMethod.SUNSET_TO_FAJR.id)
        return MidnightMethod.fromId(id)
    }

    fun setShowAsrSeparate(show: Boolean) {
        _showAsrSeparate.value = show
        prefs.edit().putBoolean("show_asr_separate", show).apply()
    }

    private fun loadSavedShowAsrSeparate(): Boolean {
        return prefs.getBoolean("show_asr_separate", false)
    }

    fun setShowIshaSeparate(show: Boolean) {
        _showIshaSeparate.value = show
        prefs.edit().putBoolean("show_isha_separate", show).apply()
    }

    private fun loadSavedShowIshaSeparate(): Boolean {
        return prefs.getBoolean("show_isha_separate", false)
    }

    private fun loadSavedPrayerOffsets(): Map<PrayerType, Int> {
        val map = mutableMapOf<PrayerType, Int>()
        PrayerType.values().forEach { type ->
            map[type] = prefs.getInt("offset_${type.name}", 0)
        }
        return map
    }

    private val _prayerOffsets = MutableStateFlow<Map<PrayerType, Int>>(loadSavedPrayerOffsets())
    val prayerOffsets: StateFlow<Map<PrayerType, Int>> = _prayerOffsets.asStateFlow()

    fun setPrayerOffset(prayerType: PrayerType, offsetMinutes: Int) {
        val clamped = offsetMinutes.coerceIn(-60, 60)
        val current = _prayerOffsets.value.toMutableMap()
        current[prayerType] = clamped
        _prayerOffsets.value = current
        prefs.edit().putInt("offset_${prayerType.name}", clamped).apply()
        recomputeAndRefreshCurrentTimes()
    }

    fun resetAllPrayerOffsets() {
        val resetMap = PrayerType.values().associateWith { 0 }
        _prayerOffsets.value = resetMap
        val editor = prefs.edit()
        PrayerType.values().forEach {
            editor.remove("offset_${it.name}")
        }
        editor.apply()
        recomputeAndRefreshCurrentTimes()
    }

    private val _manualHijriOffset = MutableStateFlow(prefs.getInt("manual_hijri_offset", 0))
    val manualHijriOffset: StateFlow<Int> = _manualHijriOffset.asStateFlow()

    private val _manualHijriDateOverride = MutableStateFlow(prefs.getString("manual_hijri_custom", null))
    val manualHijriDateOverride: StateFlow<String?> = _manualHijriDateOverride.asStateFlow()

    fun setManualHijriOffset(offsetDays: Int) {
        val clamped = offsetDays.coerceIn(-30, 30)
        _manualHijriOffset.value = clamped
        _manualHijriDateOverride.value = null
        prefs.edit()
            .putInt("manual_hijri_offset", clamped)
            .remove("manual_hijri_custom")
            .apply()
        recomputeAndRefreshCurrentTimes()
    }

    fun setManualHijriCustomDate(customDate: String?) {
        val cleaned = customDate?.trim()?.ifBlank { null }
        _manualHijriDateOverride.value = cleaned
        if (cleaned != null) {
            prefs.edit().putString("manual_hijri_custom", cleaned).apply()
        } else {
            prefs.edit().remove("manual_hijri_custom").apply()
        }
        recomputeAndRefreshCurrentTimes()
    }

    fun resetManualHijri() {
        _manualHijriOffset.value = 0
        _manualHijriDateOverride.value = null
        prefs.edit()
            .remove("manual_hijri_offset")
            .remove("manual_hijri_custom")
            .apply()
        recomputeAndRefreshCurrentTimes()
    }

    fun applyCustomAdjustments(baseData: PrayerTimesData): PrayerTimesData {
        val offsets = _prayerOffsets.value
        val fajrOffset = offsets[PrayerType.FAJR] ?: 0
        val sunriseOffset = offsets[PrayerType.SUNRISE] ?: 0
        val dhuhrOffset = offsets[PrayerType.DHUHR] ?: 0
        val asrOffset = offsets[PrayerType.ASR] ?: 0
        val sunsetOffset = offsets[PrayerType.SUNSET] ?: 0
        val maghribOffset = offsets[PrayerType.MAGHRIB] ?: 0
        val ishaOffset = offsets[PrayerType.ISHA] ?: 0
        val midnightOffset = offsets[PrayerType.MIDNIGHT] ?: 0

        val finalFajir = PrayerCalculator.addMinutesOffset(baseData.fajir, fajrOffset)
        val finalSunrise = PrayerCalculator.addMinutesOffset(baseData.sunrise, sunriseOffset)
        val finalDoher = PrayerCalculator.addMinutesOffset(baseData.doher, dhuhrOffset)
        val finalAsr = if (baseData.asr.isNotBlank()) PrayerCalculator.addMinutesOffset(baseData.asr, asrOffset) else ""
        val finalSunset = PrayerCalculator.addMinutesOffset(baseData.sunset, sunsetOffset)
        val finalMaghrib = PrayerCalculator.addMinutesOffset(baseData.maghrib, maghribOffset)
        val finalIsha = if (baseData.isha.isNotBlank()) PrayerCalculator.addMinutesOffset(baseData.isha, ishaOffset) else ""
        val finalMidnight = PrayerCalculator.addMinutesOffset(baseData.midnight, midnightOffset)

        val customHijri = _manualHijriDateOverride.value?.trim()?.ifBlank { null }
        val dayOffset = _manualHijriOffset.value
        val finalHijri = when {
            customHijri != null -> customHijri
            dayOffset != 0 -> PrayerCalculator.getFormattedHijriDateWithOffset(dayOffset = dayOffset)
            prefs.getString("github_hijri_override", null) != null -> prefs.getString("github_hijri_override", null)!!
            else -> baseData.hijriDate
        }

        return baseData.copy(
            fajir = finalFajir,
            sunrise = finalSunrise,
            doher = finalDoher,
            asr = finalAsr,
            sunset = finalSunset,
            maghrib = finalMaghrib,
            isha = finalIsha,
            midnight = finalMidnight,
            hijriDate = finalHijri
        )
    }

    private fun recomputeAndRefreshCurrentTimes() {
        val city = _selectedCity.value
        val calc = PrayerCalculator.calculateOfflinePrayerData(
            city = city,
            calculationMethod = _calculationMethod.value,
            midnightMethod = _midnightMethod.value
        )
        val finalData = applyCustomAdjustments(calc)
        _prayerTimesData.value = finalData
        saveCachedPrayerData(finalData)
    }

    // Immediately display cached prayer times (or offline calculations) on app startup without any delay
    private val _prayerTimesData = MutableStateFlow<PrayerTimesData?>(
        loadCachedPrayerData(loadSavedCity())?.let { applyCustomAdjustments(it) } ?: PrayerCalculator.calculateOfflinePrayerData(
            city = loadSavedCity(),
            calculationMethod = loadSavedCalculationMethod(),
            midnightMethod = loadSavedMidnightMethod()
        ).let {
            val adjusted = applyCustomAdjustments(it)
            saveCachedPrayerData(adjusted)
            adjusted
        }
    )
    val prayerTimesData: StateFlow<PrayerTimesData?> = _prayerTimesData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isBackgroundSyncing = MutableStateFlow(false)
    val isBackgroundSyncing: StateFlow<Boolean> = _isBackgroundSyncing.asStateFlow()

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Automatically sync latest prayer times in background and preload cities cache
        CoroutineScope(Dispatchers.IO).launch {
            fetchPrayerTimes(loadSavedCity(), forceRefresh = false)
            preloadAllCitiesInBackground()
            if (_githubAutoSyncEnabled.value) {
                try {
                    syncHijriDateWithGithub(isAuto = true, notifyUser = false)
                } catch (e: Exception) {
                    Log.d("PrayerRepo", "Auto GitHub sync: ${e.message}")
                }
            }
        }
    }

    /**
     * Preloads and caches prayer times for all predefined cities and Iraqi districts in background
     * Ensures all cities are immediately available and displayed instantly when selected
     */
    suspend fun preloadAllCitiesInBackground() = withContext(Dispatchers.IO) {
        for (city in PredefinedCities.list) {
            try {
                val fallback = PrayerCalculator.calculateOfflinePrayerData(
                    city = city,
                    calculationMethod = _calculationMethod.value,
                    midnightMethod = _midnightMethod.value
                )
                saveCachedPrayerData(fallback)
            } catch (e: Exception) {
                Log.d("PrayerRepo", "Preload exception for ${city.id}: ${e.message}")
            }
        }
    }

    /**
     * Fetches prayer times with a Cache-First, Background-Update strategy:
     * 1. Priority 1: Trusted Haqibat Al-Momen API (hq.alkafeel.net/Api/init/init.php)
     * 2. Priority 2: High-Precision Shia Astronomical Engine (Al-Kafeel / Sistani standard)
     */
    suspend fun fetchPrayerTimes(city: CityLocation = _selectedCity.value, forceRefresh: Boolean = false): Result<PrayerTimesData> {
        return withContext(Dispatchers.IO) {
            _isBackgroundSyncing.value = true
            _errorMessage.value = null

            // 1. Primary: Trusted Haqibat Al-Momen (Alkafeel) API
            try {
                val latStr = if (city.latitude % 1.0 == 0.0) {
                    city.latitude.toInt().toString()
                } else {
                    String.format(java.util.Locale.US, "%.4f", city.latitude)
                }

                val lonStr = if (city.longitude % 1.0 == 0.0) {
                    city.longitude.toInt().toString()
                } else {
                    String.format(java.util.Locale.US, "%.4f", city.longitude)
                }

                val tzStr = if (!city.timezone.startsWith("+") && !city.timezone.startsWith("-")) {
                    "+${city.timezone}"
                } else {
                    city.timezone
                }

                val response = ApiClient.apiService.getPrayerTimes(
                    version = "jsonPrayerTimes",
                    timezone = tzStr,
                    longitude = lonStr,
                    latitude = latStr
                )

                if (response.fajir != null && response.fajir.isNotBlank()) {
                    val fajir = PrayerCalculator.cleanTimeString(response.fajir)
                    val sunrise = PrayerCalculator.cleanTimeString(response.sunrise ?: "05:37")
                    val doher = PrayerCalculator.cleanTimeString(response.doher ?: "11:57")
                    val sunset = PrayerCalculator.cleanTimeString(response.sunset ?: "18:17")
                    val maghrib = PrayerCalculator.cleanTimeString(response.maghrib ?: "18:32")
                    
                    val calculatedOffline = PrayerCalculator.calculateOfflinePrayerData(
                        city = city,
                        calculationMethod = _calculationMethod.value,
                        midnightMethod = _midnightMethod.value
                    )
                    val asr = calculatedOffline.asr
                    val isha = calculatedOffline.isha
                    val midnight = if (_midnightMethod.value == MidnightMethod.SUNSET_TO_FAJR) {
                        PrayerCalculator.calculateMidnight(sunset, fajir)
                    } else {
                        PrayerCalculator.calculateMidnight(sunset, sunrise)
                    }

                    val apiHijri = response.date?.trim()?.ifBlank { null } ?: PrayerCalculator.getFormattedHijriDate()
                    val hijri = prefs.getString("github_hijri_override", null) ?: apiHijri
                    val gregorian = PrayerCalculator.getFormattedGregorianDate()

                    val data = PrayerTimesData(
                        fajir = fajir,
                        sunrise = sunrise,
                        doher = doher,
                        asr = asr,
                        sunset = sunset,
                        maghrib = maghrib,
                        isha = isha,
                        midnight = midnight,
                        hijriDate = hijri,
                        gregorianDate = gregorian,
                        city = city,
                        poweredBy = "حقيبة المؤمن • تحديث مباشر عبر الإنترنت",
                        lastUpdated = System.currentTimeMillis()
                    )

                    val adjusted = applyCustomAdjustments(data)
                    saveCachedPrayerData(adjusted)
                    _prayerTimesData.value = adjusted
                    _isOfflineMode.value = false
                    _isBackgroundSyncing.value = false
                    _isLoading.value = false
                    return@withContext Result.success(adjusted)
                }
            } catch (eKafeel: Exception) {
                Log.d("PrayerRepo", "Haqibat Al-Momen primary API attempt: ${eKafeel.message}")
            }

            // 2. High-Precision Shia Astronomical Engine Fallback (Offline / Calculated)
            val calculatedRaw = PrayerCalculator.calculateOfflinePrayerData(
                city = city,
                calculationMethod = _calculationMethod.value,
                midnightMethod = _midnightMethod.value
            )
            val finalData = applyCustomAdjustments(calculatedRaw)

            saveCachedPrayerData(finalData)
            _prayerTimesData.value = finalData
            _isOfflineMode.value = false
            _isBackgroundSyncing.value = false
            _isLoading.value = false
            Result.success(finalData)
        }
    }

    fun selectCity(city: CityLocation) {
        _selectedCity.value = city
        saveCity(city)
        // Immediately display cached data or calculate offline for zero-latency UI update
        val calculated = PrayerCalculator.calculateOfflinePrayerData(
            city = city,
            calculationMethod = _calculationMethod.value,
            midnightMethod = _midnightMethod.value
        )
        val adjusted = applyCustomAdjustments(calculated)
        _prayerTimesData.value = adjusted
        saveCachedPrayerData(adjusted)

        CoroutineScope(Dispatchers.IO).launch {
            fetchPrayerTimes(city, forceRefresh = false)
        }
    }

    private fun saveCity(city: CityLocation) {
        prefs.edit().apply {
            putString("city_id", city.id)
            putString("city_name_ar", city.nameAr)
            putString("city_name_en", city.nameEn)
            putString("city_country_ar", city.countryAr)
            putFloat("city_lat", city.latitude.toFloat())
            putFloat("city_lon", city.longitude.toFloat())
            putString("city_tz", city.timezone)
            putBoolean("city_holy", city.isHolyCity)
            putString("city_province", city.province)
            apply()
        }
    }

    private fun loadSavedCity(): CityLocation {
        val id = prefs.getString("city_id", null) ?: return PredefinedCities.defaultCity
        val nameAr = prefs.getString("city_name_ar", "كربلاء المقدسة") ?: "كربلاء المقدسة"
        val nameEn = prefs.getString("city_name_en", "Karbala") ?: "Karbala"
        val countryAr = prefs.getString("city_country_ar", "العراق") ?: "العراق"
        val lat = prefs.getFloat("city_lat", 32.6143f).toDouble()
        val lon = prefs.getFloat("city_lon", 44.0228f).toDouble()
        val tz = prefs.getString("city_tz", "+3") ?: "+3"
        val isHoly = prefs.getBoolean("city_holy", true)
        val province = prefs.getString("city_province", "كربلاء") ?: "كربلاء"

        // If saved city has emulator coordinates (e.g. Ukiah / California), reset to Karbala Holy City!
        if (id.startsWith("gps_") && (lon < -50.0 || (lat in 36.0..42.0 && lon in -125.0..-120.0) || nameAr.contains("Ukiah", ignoreCase = true) || nameEn.contains("Ukiah", ignoreCase = true))) {
            prefs.edit().remove("city_id").apply()
            return PredefinedCities.defaultCity
        }

        return CityLocation(
            id = id,
            nameAr = nameAr,
            nameEn = nameEn,
            countryAr = countryAr,
            latitude = lat,
            longitude = lon,
            timezone = tz,
            isHolyCity = isHoly,
            province = province
        )
    }

    private fun saveCachedPrayerData(data: PrayerTimesData) {
        val prefix = "cache_${data.city.id}_"
        prefs.edit().apply {
            putString("${prefix}fajir", data.fajir)
            putString("${prefix}sunrise", data.sunrise)
            putString("${prefix}doher", data.doher)
            putString("${prefix}asr", data.asr)
            putString("${prefix}sunset", data.sunset)
            putString("${prefix}maghrib", data.maghrib)
            putString("${prefix}isha", data.isha)
            putString("${prefix}midnight", data.midnight)
            putString("${prefix}hijri", data.hijriDate)
            putString("${prefix}gregorian", data.gregorianDate)
            putString("${prefix}powered", data.poweredBy)
            putLong("${prefix}time", data.lastUpdated)

            // Also update the global last cache
            putString("cached_fajir", data.fajir)
            putString("cached_sunrise", data.sunrise)
            putString("cached_doher", data.doher)
            putString("cached_asr", data.asr)
            putString("cached_sunset", data.sunset)
            putString("cached_maghrib", data.maghrib)
            putString("cached_isha", data.isha)
            putString("cached_midnight", data.midnight)
            putString("cached_hijri", data.hijriDate)
            putString("cached_gregorian", data.gregorianDate)
            putString("cached_powered", data.poweredBy)
            putLong("cached_time", data.lastUpdated)
            apply()
        }
    }

    private fun loadCachedPrayerData(city: CityLocation): PrayerTimesData? {
        val prefix = "cache_${city.id}_"
        val fajir = prefs.getString("${prefix}fajir", null) ?: prefs.getString("cached_fajir", null) ?: return null
        val sunrise = prefs.getString("${prefix}sunrise", null) ?: prefs.getString("cached_sunrise", "05:37") ?: "05:37"
        val doher = prefs.getString("${prefix}doher", null) ?: prefs.getString("cached_doher", "11:57") ?: "11:57"
        val asr = prefs.getString("${prefix}asr", null) ?: prefs.getString("cached_asr", "15:28") ?: "15:28"
        val sunset = prefs.getString("${prefix}sunset", null) ?: prefs.getString("cached_sunset", "18:17") ?: "18:17"
        val maghrib = prefs.getString("${prefix}maghrib", null) ?: prefs.getString("cached_maghrib", "18:32") ?: "18:32"
        val isha = prefs.getString("${prefix}isha", null) ?: prefs.getString("cached_isha", "19:35") ?: "19:35"
        val midnight = prefs.getString("${prefix}midnight", null) ?: prefs.getString("cached_midnight", "23:15") ?: "23:15"
        val cachedHijri = prefs.getString("${prefix}hijri", null) ?: prefs.getString("cached_hijri", "مركز الكفيل للثقافة والإعلام") ?: "مركز الكفيل للثقافة والإعلام"
        val hijri = prefs.getString("github_hijri_override", null) ?: cachedHijri
        val gregorian = prefs.getString("${prefix}gregorian", null) ?: prefs.getString("cached_gregorian", PrayerCalculator.getFormattedGregorianDate()) ?: PrayerCalculator.getFormattedGregorianDate()
        val powered = prefs.getString("${prefix}powered", null) ?: prefs.getString("cached_powered", "حقيبة المؤمن") ?: "حقيبة المؤمن"
        val lastUpdated = prefs.getLong("${prefix}time", prefs.getLong("cached_time", System.currentTimeMillis()))

        return PrayerTimesData(
            fajir = fajir,
            sunrise = sunrise,
            doher = doher,
            asr = asr,
            sunset = sunset,
            maghrib = maghrib,
            isha = isha,
            midnight = midnight,
            hijriDate = hijri,
            gregorianDate = gregorian,
            city = city,
            poweredBy = powered,
            lastUpdated = lastUpdated
        )
    }

    fun isNotificationEnabled(prayerName: String): Boolean {
        val defaultVal = when (prayerName) {
            PrayerType.FAJR.name,
            PrayerType.DHUHR.name,
            PrayerType.ASR.name,
            PrayerType.MAGHRIB.name,
            PrayerType.ISHA.name -> true
            else -> false
        }
        return prefs.getBoolean("notif_$prayerName", defaultVal)
    }

    fun setNotificationEnabled(prayerName: String, enabled: Boolean) {
        prefs.edit().putBoolean("notif_$prayerName", enabled).apply()
    }

    fun getPrayerAlarmConfig(prayerType: PrayerType): PrayerCustomAlarmConfig {
        val isEnabled = isNotificationEnabled(prayerType.name)
        val soundModeId = prefs.getString("alarm_sound_${prayerType.name}", AdhanSoundMode.FULL_ADHAN.id)
        val soundMode = AdhanSoundMode.fromId(soundModeId)
        val muezzinId = prefs.getString("alarm_muezzin_${prayerType.name}", null)
        val repeatId = prefs.getString("alarm_repeat_${prayerType.name}", AlarmRepeatMode.ONCE.id)
        val repeatMode = AlarmRepeatMode.fromId(repeatId)
        val volume = prefs.getInt("alarm_volume_${prayerType.name}", 100)

        return PrayerCustomAlarmConfig(
            prayerType = prayerType,
            isEnabled = isEnabled,
            soundMode = soundMode,
            specificMuezzinId = muezzinId,
            repeatMode = repeatMode,
            customVolumePercent = volume
        )
    }

    fun savePrayerAlarmConfig(config: PrayerCustomAlarmConfig) {
        setNotificationEnabled(config.prayerType.name, config.isEnabled)
        prefs.edit().apply {
            putString("alarm_sound_${config.prayerType.name}", config.soundMode.id)
            if (config.specificMuezzinId != null) {
                putString("alarm_muezzin_${config.prayerType.name}", config.specificMuezzinId)
            } else {
                remove("alarm_muezzin_${config.prayerType.name}")
            }
            putString("alarm_repeat_${config.prayerType.name}", config.repeatMode.id)
            putInt("alarm_volume_${config.prayerType.name}", config.customVolumePercent)
            apply()
        }
    }

    fun isPrayerVisible(prayerName: String): Boolean {
        return prefs.getBoolean("visible_$prayerName", true)
    }

    fun setPrayerVisible(prayerName: String, visible: Boolean) {
        prefs.edit().putBoolean("visible_$prayerName", visible).apply()
    }

    suspend fun syncHijriDateWithGithub(isAuto: Boolean = false, notifyUser: Boolean = false): Result<String> = withContext(Dispatchers.IO) {
        try {
            val owner = _githubRepoOwner.value.ifBlank { "moshraheem-sudo" }
            val repo = _githubRepoName.value.ifBlank { "hijri" }
            val url = URL("https://raw.githubusercontent.com/$owner/$repo/main/hijri.json")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 7000
            connection.readTimeout = 7000
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "AndroidPrayerTimesApp/1.0")

            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val json = JSONObject(response.toString())
                val hDay = json.getInt("hijri_day")
                val hMonth = json.getInt("hijri_month")
                val hYear = json.getInt("hijri_year")
                val anchorDateStr = json.getString("gregorian_anchor_date")

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                val anchorDate = sdf.parse(anchorDateStr) ?: Date()
                val today = Date()

                val diffMillis = today.time - anchorDate.time
                val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

                var currentDay = hDay + diffDays
                var currentMonth = hMonth
                var currentYear = hYear

                while (currentDay > 30) {
                    currentDay -= 30
                    currentMonth++
                    if (currentMonth > 12) {
                        currentMonth = 1
                        currentYear++
                    }
                }
                while (currentDay <= 0) {
                    currentDay += 30
                    currentMonth--
                    if (currentMonth < 1) {
                        currentMonth = 12
                        currentYear--
                    }
                }

                val monthsAr = arrayOf(
                    "محرم الحرام", "صفر الخير", "ربيع الأول", "ربيع الثاني",
                    "جمادى الأولى", "جمادى الآخرة", "رجب الأصب", "شعبان المعظم",
                    "رمضان المبارك", "شوال المكرم", "ذو القعدة", "ذو الحجة"
                )
                val monthName = monthsAr.getOrElse(currentMonth - 1) { "هجري" }
                val formattedHijri = "$currentDay $monthName $currentYear هـ"

                val prevOverride = prefs.getString("github_hijri_override", null)
                val isDateChanged = prevOverride != formattedHijri

                val currentData = _prayerTimesData.value
                if (currentData != null) {
                    val updatedData = currentData.copy(hijriDate = formattedHijri)
                    _prayerTimesData.value = updatedData
                    saveCachedPrayerData(updatedData)
                }
                
                val nowTime = System.currentTimeMillis()
                _lastHijriSyncTime.value = nowTime
                prefs.edit()
                    .putString("github_hijri_override", formattedHijri)
                    .putLong("last_hijri_sync_time", nowTime)
                    .apply()

                if (notifyUser || (isAuto && isDateChanged)) {
                    try {
                        com.example.utils.PrayerNotificationHelper.showHijriSyncNotification(
                            context = context,
                            hijriDate = formattedHijri,
                            isAuto = isAuto
                        )
                    } catch (eNotif: Exception) {
                        Log.e("PrayerRepo", "Notification error: ${eNotif.message}")
                    }
                }

                Result.success(formattedHijri)
            } else {
                Result.failure(Exception("HTTP ${connection.responseCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendGithubNotificationDispatch(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val owner = _githubRepoOwner.value.ifBlank { "moshraheem-sudo" }
            val repo = _githubRepoName.value.ifBlank { "hijri" }
            val token = _githubToken.value.trim()
            val customWebhook = _githubWebhookUrl.value.trim()

            // 1. If custom webhook is configured, post to it
            if (customWebhook.isNotBlank()) {
                val url = URL(customWebhook)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; utf-8")
                connection.setRequestProperty("User-Agent", "AndroidPrayerTimesApp/1.0")

                val payload = JSONObject().apply {
                    put("event", "update_hijri_calendar")
                    put("source", "Android Prayer Times App")
                    put("timestamp", System.currentTimeMillis())
                    put("city", _selectedCity.value.nameAr)
                }

                connection.outputStream.use { os ->
                    val input = payload.toString().toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                val code = connection.responseCode
                if (code in 200..299) {
                    com.example.utils.PrayerNotificationHelper.showGithubDispatchNotification(
                        context = context,
                        isSuccess = true,
                        message = "تم إرسال إشعار التحديث إلى Webhook المخصص بنجاح (HTTP $code)"
                    )
                    return@withContext Result.success("تم الإرسال إلى Webhook بنجاح (HTTP $code)")
                }
            }

            // 2. Standard GitHub Repository Dispatch
            val dispatchUrl = URL("https://api.github.com/repos/$owner/$repo/dispatches")
            val connection = dispatchUrl.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.doOutput = true
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setRequestProperty("User-Agent", "AndroidPrayerTimesApp/1.0")
            if (token.isNotBlank()) {
                connection.setRequestProperty("Authorization", if (token.startsWith("Bearer ") || token.startsWith("token ")) token else "Bearer $token")
            }

            val payload = JSONObject().apply {
                put("event_type", "update_hijri_request")
                put("client_payload", JSONObject().apply {
                    put("source", "android_app")
                    put("timestamp", System.currentTimeMillis())
                    put("city", _selectedCity.value.nameAr)
                    put("reason", "automatic_hijri_sync_trigger")
                })
            }

            connection.outputStream.use { os ->
                val input = payload.toString().toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            val code = connection.responseCode
            if (code == 204 || code in 200..299) {
                com.example.utils.PrayerNotificationHelper.showGithubDispatchNotification(
                    context = context,
                    isSuccess = true,
                    message = "تم إرسال إشعار التحديث بنجاح إلى مستودع GitHub ($owner/$repo)"
                )
                Result.success("تم إرسال إشعار التحديث بنجاح إلى GitHub ($owner/$repo)")
            } else {
                val errorMsg = if (code == 401 || code == 403) {
                    "يتطلب GitHub إدخال Personal Access Token في الإعدادات للسماح بالـ Dispatch (HTTP $code)"
                } else {
                    "استجابة GitHub: HTTP $code"
                }
                com.example.utils.PrayerNotificationHelper.showGithubDispatchNotification(
                    context = context,
                    isSuccess = false,
                    message = errorMsg
                )
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            com.example.utils.PrayerNotificationHelper.showGithubDispatchNotification(
                context = context,
                isSuccess = false,
                message = e.localizedMessage ?: "حدث خطأ أثناء الاتصال بـ GitHub"
            )
            Result.failure(e)
        }
    }
}
