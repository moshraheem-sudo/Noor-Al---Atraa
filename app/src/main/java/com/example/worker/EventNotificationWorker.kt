package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AhlAlBaytRepository
import com.example.data.getHijriMonthName
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

class EventNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val sharedPref = applicationContext.getSharedPreferences("AhlAlBaytPrefs", Context.MODE_PRIVATE)

        val notificationsEnabled = sharedPref.getBoolean("notifications_enabled", true)
        if (!notificationsEnabled) return Result.success()

        val db = com.example.data.AppDatabase.getInstance(applicationContext)
        val record = db.hijriDateRecordDao().getRecord()
        val hijrahDate = com.example.data.HijriSyncManager.getCalculatedHijriDate(record)

        val currentDay = hijrahDate.get(ChronoField.DAY_OF_MONTH)

        val lastEventNotifyDay = sharedPref.getInt("last_event_notify_day", -1)
        if (lastEventNotifyDay != currentDay) {
            val todayDate = hijrahDate
            val tomorrowDate = hijrahDate.plus(1, ChronoUnit.DAYS)
            val inTwoDaysDate = hijrahDate.plus(2, ChronoUnit.DAYS)

            val todayDay = todayDate.get(ChronoField.DAY_OF_MONTH)
            val todayMonth = todayDate.get(ChronoField.MONTH_OF_YEAR)

            val tomorrowDay = tomorrowDate.get(ChronoField.DAY_OF_MONTH)
            val tomorrowMonth = tomorrowDate.get(ChronoField.MONTH_OF_YEAR)

            val inTwoDaysDay = inTwoDaysDate.get(ChronoField.DAY_OF_MONTH)
            val inTwoDaysMonth = inTwoDaysDate.get(ChronoField.MONTH_OF_YEAR)

            // Select the single most relevant event notification (Today > Tomorrow > In 2 days)
            // to strictly avoid spamming the user with multiple simultaneous notifications
            val todayEvents = AhlAlBaytRepository.events.filter { event ->
                event.month == todayMonth && event.day == todayDay
            }
            val tomorrowEvents = AhlAlBaytRepository.events.filter { event ->
                event.month == tomorrowMonth && event.day == tomorrowDay
            }
            val inTwoDaysEvents = AhlAlBaytRepository.events.filter { event ->
                event.month == inTwoDaysMonth && event.day == inTwoDaysDay
            }

            val targetEvent = todayEvents.firstOrNull()?.let {
                Triple(
                    "مناسبة اليوم: ${it.name}",
                    "يصادف اليوم ${it.day} ${getHijriMonthName(it.month)}.",
                    (it.name + "today").hashCode()
                )
            } ?: tomorrowEvents.firstOrNull()?.let {
                Triple(
                    "اقتربت مناسبة: ${it.name}",
                    "تنويه: غداً تصادف مناسبة (${it.name}) في ${it.day} ${getHijriMonthName(it.month)}.",
                    (it.name + "tomorrow").hashCode()
                )
            } ?: inTwoDaysEvents.firstOrNull()?.let {
                Triple(
                    "اقتربت مناسبة: ${it.name}",
                    "تنويه: اقتربت مناسبة (${it.name}) - بعد يومين (${it.day} ${getHijriMonthName(it.month)}.",
                    (it.name + "twodays").hashCode()
                )
            }

            if (targetEvent != null && com.example.util.NotificationRateLimiter.canPostNotification(applicationContext)) {
                showNotification(applicationContext, targetEvent.third, targetEvent.first, targetEvent.second)
            }

            sharedPref.edit().putInt("last_event_notify_day", currentDay).apply()
        }

        // Periodically check for app updates in background
        com.example.UpdateChecker.checkForUpdate(applicationContext, isManualCheck = false)

        return Result.success()
    }

    private fun showNotification(context: Context, id: Int, title: String, message: String) {
        val channelId = "ahlalbayt_events"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "المناسبات الدينية والأحداث",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.example.R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }
}
