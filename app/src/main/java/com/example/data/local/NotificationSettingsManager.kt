package com.example.data.local

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.worker.AyahWorker
import com.example.worker.DailyAyahWorker
import com.example.worker.EventNotificationWorker
import com.example.worker.ReminderWorker
import java.util.concurrent.TimeUnit

object NotificationSettingsManager {
    private const val PREF_NAME = "quran_notification_prefs"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val KEY_QURAN_NOTIFICATIONS = "quran_notifications_enabled"
    private const val KEY_UPCOMING_EVENTS = "upcoming_events_enabled"

    fun areNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun isQuranEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_QURAN_NOTIFICATIONS, true)
    }

    fun setQuranEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_QURAN_NOTIFICATIONS, enabled).apply()
        if (enabled) {
            scheduleAllNotifications(context)
        } else {
            cancelAyahNotifications(context)
        }
    }

    fun isUpcomingEventsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_UPCOMING_EVENTS, true)
    }

    fun setUpcomingEventsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_UPCOMING_EVENTS, enabled).apply()
        scheduleAllNotifications(context)
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        
        val ahlPrefs = context.getSharedPreferences("AhlAlBaytPrefs", Context.MODE_PRIVATE)
        ahlPrefs.edit().putBoolean("notifications_enabled", enabled).apply()

        if (enabled) {
            scheduleAllNotifications(context)
        } else {
            cancelAllNotifications(context)
        }
    }

    fun scheduleAllNotifications(context: Context) {
        try {
            val workManager = WorkManager.getInstance(context)
            val masterEnabled = areNotificationsEnabled(context)
            if (!masterEnabled) return

            // 1. Ayah Notifications (Hourly with KEEP policy to avoid resetting or batching when app opens)
            if (isQuranEnabled(context)) {
                val periodicAyahRequest = PeriodicWorkRequestBuilder<AyahWorker>(1, TimeUnit.HOURS).build()
                workManager.enqueueUniquePeriodicWork(
                    "AyahNotificationPeriodic",
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicAyahRequest
                )

                val dailyAyahWorkRequest = PeriodicWorkRequestBuilder<DailyAyahWorker>(12, TimeUnit.HOURS).build()
                workManager.enqueueUniquePeriodicWork(
                    "DailyAyahWorker",
                    ExistingPeriodicWorkPolicy.KEEP,
                    dailyAyahWorkRequest
                )
            } else {
                cancelAyahNotifications(context)
            }

            // 2. Worship and Occasion Reminders (Hourly with KEEP policy)
            val ahlPrefs = context.getSharedPreferences("AhlAlBaytPrefs", Context.MODE_PRIVATE)
            val worshipEnabled = ahlPrefs.getBoolean("notifications_enabled", true)
            if (worshipEnabled) {
                val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(60, TimeUnit.MINUTES)
                    .build()
                workManager.enqueueUniqueWork(
                    "ReminderWorkerChain",
                    ExistingWorkPolicy.KEEP,
                    reminderRequest
                )
            } else {
                workManager.cancelUniqueWork("ReminderWorkerChain")
            }

            // 3. Religious Events & Approaching Events Notifications (Hourly with KEEP policy)
            val eventRequest = PeriodicWorkRequestBuilder<EventNotificationWorker>(1, TimeUnit.HOURS).build()
            workManager.enqueueUniquePeriodicWork(
                "EventNotificationWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                eventRequest
            )

            // 4. Exact AlarmManager Scheduling (ensures notifications arrive on time even when the app is completely closed)
            com.example.receiver.NotificationAlarmScheduler.scheduleNextAlarm(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelAyahNotifications(context: Context) {
        try {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork("AyahNotificationPeriodic")
            workManager.cancelUniqueWork("AyahNotificationWork")
            workManager.cancelUniqueWork("DailyAyahWorker")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelAllNotifications(context: Context) {
        try {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork("AyahNotificationPeriodic")
            workManager.cancelUniqueWork("AyahNotificationWork")
            workManager.cancelUniqueWork("DailyAyahWorker")
            workManager.cancelUniqueWork("ReminderWorkerChain")
            workManager.cancelUniqueWork("EventNotificationWorker")
            com.example.receiver.NotificationAlarmScheduler.cancelAlarm(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
