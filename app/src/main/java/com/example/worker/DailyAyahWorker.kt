package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.QuranNotificationData
import com.example.data.local.NotificationSettingsManager
import com.example.util.AyahNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class DailyAyahWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val sharedPref = context.getSharedPreferences("AhlAlBaytPrefs", Context.MODE_PRIVATE)
        val notificationsEnabled = sharedPref.getBoolean("notifications_enabled", true) &&
                NotificationSettingsManager.areNotificationsEnabled(context)
        val dailyAyahEnabled = sharedPref.getBoolean("daily_ayah_enabled", true)

        if (!notificationsEnabled || !dailyAyahEnabled) return@withContext Result.success()

        val todayDayOfYear = LocalDate.now().dayOfYear
        val lastNotifiedDay = sharedPref.getInt("last_daily_ayah_day", -1)

        if (lastNotifiedDay == todayDayOfYear) {
            return@withContext Result.success()
        }

        val verses = QuranNotificationData.curatedVerses
        val index = (todayDayOfYear + LocalDate.now().year) % verses.size
        val ayahOfDay = verses[index]

        AyahNotificationHelper.showAyahNotification(
            context = context,
            ayahText = ayahOfDay.textUthmani,
            surahName = ayahOfDay.surahName,
            ayahNumber = ayahOfDay.ayahNumber,
            surahId = ayahOfDay.surahId,
            customTitle = "📖 آية اليوم المباركة"
        )

        sharedPref.edit().putInt("last_daily_ayah_day", todayDayOfYear).apply()

        Result.success()
    }
}
