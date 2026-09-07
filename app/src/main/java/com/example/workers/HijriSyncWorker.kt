package com.example.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.HijriSyncManager
import com.example.data.AppDatabase
import java.time.LocalTime

class HijriSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (runAttemptCount >= 5) {
            return Result.failure()
        }

        return try {
            val db = AppDatabase.getInstance(applicationContext)
            val record = db.hijriDateRecordDao().getRecord()
            
            var shouldForceSync = false
            
            if (record == null || record.status == "estimated") {
                // First install or no real data
                shouldForceSync = true
            } else {
                val hijriDate = HijriSyncManager.getCalculatedHijriDate(record)
                val day = hijriDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
                val currentHour = LocalTime.now().hour
                if ((day == 29 || day == 30 || day == 1) && currentHour >= 4) {
                    shouldForceSync = true
                }
            }

            val success = HijriSyncManager.syncIfNeeded(applicationContext, forceSync = shouldForceSync)
            
            if (success) {
                Result.success()
            } else {
                if (shouldForceSync) Result.retry() else Result.success()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
