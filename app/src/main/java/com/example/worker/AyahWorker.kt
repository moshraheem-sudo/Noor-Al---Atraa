package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.MainApplication
import com.example.data.QuranNotificationData
import com.example.data.local.NotificationSettingsManager
import com.example.util.AyahNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class AyahWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val isEnabled = NotificationSettingsManager.areNotificationsEnabled(context)
            if (!isEnabled) {
                return@withContext Result.success()
            }

            var ayahText: String? = null
            var surahName: String? = null
            var ayahNumber: Int = 1
            var surahId: Int = 1

            try {
                val app = context.applicationContext as? MainApplication
                val repository = app?.repository
                val randomAyah = repository?.getRandomShortAyah(250)
                if (randomAyah != null) {
                    val sName = repository.getSurahNameById(randomAyah.surahId)
                    if (!sName.isNullOrBlank() && randomAyah.textUthmani.isNotBlank()) {
                        ayahText = randomAyah.textUthmani
                        surahName = sName
                        ayahNumber = randomAyah.ayahNumber
                        surahId = randomAyah.surahId
                    }
                }
            } catch (e: Exception) {
                // Fallback to curated catalog
            }

            // Fallback to high quality curated Quranic dataset with full tashkeel
            if (ayahText.isNullOrBlank() || surahName.isNullOrBlank()) {
                val fallback = QuranNotificationData.getRandomVerse()
                ayahText = fallback.textUthmani
                surahName = fallback.surahName
                ayahNumber = fallback.ayahNumber
                surahId = fallback.surahId
            }

            val titles = listOf(
                "هل استمعت اليوم لكلام الله؟ 📖",
                "نفحات من القرآن الكريم 📖",
                "آية وتذكير من كلام الله 📖",
                "تأمل في كلام الله 📖"
            )
            val randomTitle = titles.random()

            AyahNotificationHelper.showAyahNotification(
                context = context,
                ayahText = ayahText,
                surahName = surahName,
                ayahNumber = ayahNumber,
                surahId = surahId,
                customTitle = randomTitle
            )

            // Re-chain next work after 60 minutes (1 hour) using unique work to avoid duplicates
            if (NotificationSettingsManager.areNotificationsEnabled(context)) {
                val nextWork = OneTimeWorkRequestBuilder<AyahWorker>()
                    .setInitialDelay(60, TimeUnit.MINUTES)
                    .build()
                WorkManager.getInstance(context).enqueueUniqueWork(
                    "AyahNotificationWork",
                    androidx.work.ExistingWorkPolicy.REPLACE,
                    nextWork
                )
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
