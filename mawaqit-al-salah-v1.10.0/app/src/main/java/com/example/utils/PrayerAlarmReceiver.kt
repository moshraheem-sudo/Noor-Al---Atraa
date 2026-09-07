package com.example.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.example.data.model.AdhanSoundMode
import com.example.data.model.Muezzin
import com.example.data.model.PrayerType
import com.example.data.repository.PrayerTimesRepository

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        @Suppress("DEPRECATION")
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
            "PrayerTimes:AlarmWakeLock"
        )
        try {
            wakeLock?.acquire(45000) // 45 seconds to cover service start & buffering
        } catch (e: Exception) {
            // ignore
        }

        try {
            val prayerTypeName = intent.getStringExtra(EXTRA_PRAYER_TYPE) ?: return
            val prayerType = try {
                PrayerType.valueOf(prayerTypeName)
            } catch (e: Exception) {
                return
            }

            val timeStr = intent.getStringExtra(EXTRA_TIME_STR) ?: ""
            val cityName = intent.getStringExtra(EXTRA_CITY_NAME) ?: "مدينتك"
            val repeatIteration = intent.getIntExtra(EXTRA_REPEAT_ITERATION, 0)
            val offsetMinutes = intent.getIntExtra(EXTRA_OFFSET_MINUTES, 0)

            // Check if user enabled notifications and get per-prayer custom config
            val repository = PrayerTimesRepository(context)
            val config = repository.getPrayerAlarmConfig(prayerType)

            if (config.isEnabled) {
                val formattedTime = if (timeStr.isNotEmpty()) {
                    PrayerCalculator.formatTo12hArabic(timeStr)
                } else {
                    ""
                }

                val isAdhanPrayer = prayerType in listOf(
                    PrayerType.FAJR,
                    PrayerType.DHUHR,
                    PrayerType.ASR,
                    PrayerType.MAGHRIB,
                    PrayerType.ISHA
                )

                // Select muezzin: specific for this prayer if configured, otherwise global selected
                val muezzin = if (!config.specificMuezzinId.isNullOrBlank()) {
                    Muezzin.fromId(config.specificMuezzinId)
                } else {
                    repository.selectedMuezzin.value
                }

                val isAudioGloballyEnabled = repository.isAdhanAudioEnabled.value
                val soundMode = config.soundMode

                val shouldPlayAdhanAudio = isAdhanPrayer &&
                        isAudioGloballyEnabled &&
                        (soundMode == AdhanSoundMode.FULL_ADHAN || soundMode == AdhanSoundMode.SHORT_TAKBIR) &&
                        offsetMinutes >= 0

                if (offsetMinutes < 0) {
                    // Normal notification chime 10m & 5m before Adhan with preparation reminder
                    PrayerNotificationHelper.showPreAdhanReminderNotification(
                        context = context,
                        prayerType = prayerType,
                        cityName = cityName,
                        offsetMinutes = offsetMinutes
                    )
                } else if (shouldPlayAdhanAudio) {
                    // Exact prayer time: Start ONLY the Adhan audio foreground service directly!
                    // No separate ringing notification, no conflicting alert tone, no duplicate banners.
                    // AdhanAudioService provides the single clean playback notification with the stop button.
                    AdhanAudioService.startAdhan(
                        context = context,
                        muezzin = muezzin,
                        prayerType = prayerType,
                        cityName = cityName,
                        timeFormatted = formattedTime,
                        soundMode = soundMode,
                        volumePercent = config.customVolumePercent
                    )
                } else {
                    // Non-adhan prayer (e.g. Sunrise / Midnight) or non-audio modes (Vibrate only / Beep alert)
                    if (soundMode == AdhanSoundMode.VIBRATE_ONLY) {
                        AudioPlayerHelper.vibratePattern(context)
                    } else if (soundMode == AdhanSoundMode.BEEP_ALERT) {
                        AudioPlayerHelper.playBeep()
                    }

                    PrayerNotificationHelper.showPrayerNotification(
                        context = context,
                        prayerType = prayerType,
                        timeFormatted = formattedTime,
                        cityName = cityName,
                        playAlertSound = false,
                        repeatIteration = repeatIteration,
                        offsetMinutes = offsetMinutes
                    )
                }
            }

            // Reschedule alarms for upcoming days if prayer data exists (reschedule on exact prayer time)
            val cachedData = repository.prayerTimesData.value
            val city = repository.selectedCity.value
            if (cachedData != null && offsetMinutes == 0) {
                PrayerNotificationScheduler.scheduleAllPrayerNotifications(
                    context = context,
                    prayerData = cachedData,
                    cityName = city.nameAr
                )
            }
        } finally {
            try {
                if (wakeLock?.isHeld == true) {
                    wakeLock.release()
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    companion object {
        const val EXTRA_PRAYER_TYPE = "extra_prayer_type"
        const val EXTRA_TIME_STR = "extra_time_str"
        const val EXTRA_CITY_NAME = "extra_city_name"
        const val EXTRA_REPEAT_ITERATION = "extra_repeat_iteration"
        const val EXTRA_OFFSET_MINUTES = "extra_offset_minutes"
    }
}
