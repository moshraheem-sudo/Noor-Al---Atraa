package com.example.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.MainActivity
import com.example.data.model.AlarmRepeatMode
import com.example.data.model.PrayerTimesData
import com.example.data.model.PrayerType
import com.example.data.repository.PrayerTimesRepository
import java.util.Calendar

object PrayerNotificationScheduler {

    private const val TAG = "PrayerScheduler"

    fun scheduleAllPrayerNotifications(
        context: Context,
        prayerData: PrayerTimesData,
        cityName: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val repo = PrayerTimesRepository(context)

        val prayerTimeMap = listOf(
            PrayerType.FAJR to prayerData.fajir,
            PrayerType.SUNRISE to prayerData.sunrise,
            PrayerType.DHUHR to prayerData.doher,
            PrayerType.ASR to prayerData.asr,
            PrayerType.SUNSET to prayerData.sunset,
            PrayerType.MAGHRIB to prayerData.maghrib,
            PrayerType.ISHA to prayerData.isha,
            PrayerType.MIDNIGHT to prayerData.midnight
        )

        for ((prayerType, timeStr) in prayerTimeMap) {
            val config = repo.getPrayerAlarmConfig(prayerType)
            if (!config.isEnabled || timeStr.isBlank()) {
                cancelPrayerAlarm(context, alarmManager, prayerType)
                continue
            }

            val isAdhanPrayer = prayerType in listOf(
                PrayerType.FAJR,
                PrayerType.DHUHR,
                PrayerType.ASR,
                PrayerType.MAGHRIB,
                PrayerType.ISHA
            )

            // Primary alarm at exact prayer time (offset 0: pure Adhan audio without ringing)
            scheduleSinglePrayerAlarm(
                context = context,
                alarmManager = alarmManager,
                prayerType = prayerType,
                timeStr = timeStr,
                cityName = cityName,
                repeatIteration = 0,
                offsetMinutes = 0
            )

            // Pre-Adhan notifications: gentle reminder 10 minutes before Adhan if configured in repeatMode
            if (isAdhanPrayer && config.repeatMode == AlarmRepeatMode.REMIND_BEFORE_10) {
                // 10 minutes before
                scheduleSinglePrayerAlarm(
                    context = context,
                    alarmManager = alarmManager,
                    prayerType = prayerType,
                    timeStr = timeStr,
                    cityName = cityName,
                    repeatIteration = 1,
                    offsetMinutes = -10
                )
            } else {
                cancelPreRepeatAlarms(context, alarmManager, prayerType)
            }

            // Additional repetitions / follow-up alarms based on AlarmRepeatMode
            when (config.repeatMode) {
                AlarmRepeatMode.ONCE, AlarmRepeatMode.REMIND_BEFORE_10 -> {
                    cancelPostRepeatAlarms(context, alarmManager, prayerType)
                }
                AlarmRepeatMode.REPEAT_TWICE -> {
                    scheduleSinglePrayerAlarm(
                        context = context,
                        alarmManager = alarmManager,
                        prayerType = prayerType,
                        timeStr = timeStr,
                        cityName = cityName,
                        repeatIteration = 3,
                        offsetMinutes = 5
                    )
                }
                AlarmRepeatMode.REPEAT_THREE -> {
                    scheduleSinglePrayerAlarm(
                        context = context,
                        alarmManager = alarmManager,
                        prayerType = prayerType,
                        timeStr = timeStr,
                        cityName = cityName,
                        repeatIteration = 3,
                        offsetMinutes = 5
                    )
                    scheduleSinglePrayerAlarm(
                        context = context,
                        alarmManager = alarmManager,
                        prayerType = prayerType,
                        timeStr = timeStr,
                        cityName = cityName,
                        repeatIteration = 4,
                        offsetMinutes = 10
                    )
                }
            }
        }
    }

    private fun scheduleSinglePrayerAlarm(
        context: Context,
        alarmManager: AlarmManager,
        prayerType: PrayerType,
        timeStr: String,
        cityName: String,
        repeatIteration: Int = 0,
        offsetMinutes: Int = 0
    ) {
        if (timeStr.isBlank()) return

        val triggerTimeMillis = calculateNextTriggerMillis(timeStr, offsetMinutes)

        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = "com.example.ACTION_PRAYER_ALARM_${prayerType.name}_$repeatIteration"
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_TYPE, prayerType.name)
            putExtra(PrayerAlarmReceiver.EXTRA_TIME_STR, timeStr)
            putExtra(PrayerAlarmReceiver.EXTRA_CITY_NAME, cityName)
            putExtra(PrayerAlarmReceiver.EXTRA_REPEAT_ITERATION, repeatIteration)
            putExtra(PrayerAlarmReceiver.EXTRA_OFFSET_MINUTES, offsetMinutes)
        }

        val requestCode = prayerType.ordinal * 10 + repeatIteration + 2000
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Show Intent for AlarmClockInfo
        val showIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            requestCode + 1000,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Use AlarmClockInfo for guaranteed high-priority wakeup through Doze mode & battery savers
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTimeMillis, showPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled alarm for ${prayerType.name} (iter: $repeatIteration, offset: $offsetMinutes) at millis: $triggerTimeMillis ($timeStr)")
        } catch (e: Exception) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to schedule alarm for ${prayerType.name}: ${ex.message}")
            }
        }
    }

    private fun cancelPrayerAlarm(
        context: Context,
        alarmManager: AlarmManager,
        prayerType: PrayerType
    ) {
        for (i in 0..5) {
            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = "com.example.ACTION_PRAYER_ALARM_${prayerType.name}_$i"
            }
            val requestCode = prayerType.ordinal * 10 + i + 2000
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
            }
        }
    }

    private fun cancelPreRepeatAlarms(
        context: Context,
        alarmManager: AlarmManager,
        prayerType: PrayerType
    ) {
        for (i in 1..2) {
            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = "com.example.ACTION_PRAYER_ALARM_${prayerType.name}_$i"
            }
            val requestCode = prayerType.ordinal * 10 + i + 2000
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
            }
        }
    }

    private fun cancelPostRepeatAlarms(
        context: Context,
        alarmManager: AlarmManager,
        prayerType: PrayerType
    ) {
        for (i in 3..5) {
            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = "com.example.ACTION_PRAYER_ALARM_${prayerType.name}_$i"
            }
            val requestCode = prayerType.ordinal * 10 + i + 2000
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
            }
        }
    }

    private fun calculateNextTriggerMillis(timeStr: String, offsetMinutes: Int = 0): Long {
        val clean = PrayerCalculator.cleanTimeString(timeStr)
        val parts = clean.trim().split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val second = parts.getOrNull(2)?.toIntOrNull() ?: 0

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, second)
            set(Calendar.MILLISECOND, 0)
            if (offsetMinutes != 0) {
                add(Calendar.MINUTE, offsetMinutes)
            }
        }

        // If the prayer time for today has already passed, schedule for tomorrow
        if (target.timeInMillis <= now.timeInMillis) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        return target.timeInMillis
    }
}
