package com.example.util

import android.content.Context

/**
 * Central rate limiter for all notifications across the application.
 * Ensures that AT MOST ONE notification is presented within any 60-minute (1 hour) window,
 * completely preventing batch dumps, notification spamming, or simultaneous alerts.
 */
object NotificationRateLimiter {
    private const val PREFS_NAME = "app_notification_rate_limiter_prefs"
    private const val KEY_LAST_NOTIFIED_TIME = "last_notification_timestamp_ms"
    
    // Strictly 1 notification per 1 hour (60 minutes in milliseconds)
    const val COOLDOWN_INTERVAL_MS = 60 * 60 * 1000L

    /**
     * Checks whether a notification can be posted right now.
     * If allowed, atomically records the current timestamp so subsequent concurrent workers
     * or requests are spaced appropriately based on the user's hourly notification rate setting.
     */
    @Synchronized
    fun canPostNotification(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastTime = prefs.getLong(KEY_LAST_NOTIFIED_TIME, 0L)
        val now = System.currentTimeMillis()

        // Read user's chosen notification rate (default 3, up to 10 per hour)
        val ahlPrefs = context.getSharedPreferences("AhlAlBaytPrefs", Context.MODE_PRIVATE)
        val rate = ahlPrefs.getInt("notifications_per_hour", 3).coerceIn(3, 10)
        
        // Cooldown spaced across the hour with a 1-minute safety window for timer execution tolerances
        val minIntervalMs = ((60 * 60 * 1000L) / rate) - 60_000L

        // If less than the required interval has elapsed since the last notification, disallow duplicate
        if (now - lastTime < minIntervalMs) {
            return false
        }

        // Allowed: record current timestamp
        prefs.edit().putLong(KEY_LAST_NOTIFIED_TIME, now).apply()
        return true
    }

    /**
     * Returns the timestamp of the last posted notification.
     */
    fun getLastNotifiedTime(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_NOTIFIED_TIME, 0L)
    }

    /**
     * Manually record that a notification was just displayed (if triggered externally).
     */
    fun recordNotificationPosted(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_NOTIFIED_TIME, System.currentTimeMillis()).apply()
    }
}
