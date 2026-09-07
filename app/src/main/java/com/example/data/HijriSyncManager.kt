package com.example.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.workers.HijriSyncWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.chrono.HijrahDate
import java.util.concurrent.TimeUnit

object HijriSyncManager {

    fun schedulePeriodicSync(context: Context) {
        val hijriSyncWorkRequest = PeriodicWorkRequestBuilder<HijriSyncWorker>(24, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(
                androidx.work.BackoffPolicy.EXPONENTIAL,
                androidx.work.WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "HijriSyncWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            hijriSyncWorkRequest
        )
    }

    fun cancelPeriodicSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("HijriSyncWorker")
    }

    suspend fun syncIfNeeded(context: Context, forceSync: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        val database = AppDatabase.getInstance(context)
        val recordDao = database.hijriDateRecordDao()
        val sharedPref = context.getSharedPreferences("AhlAlBaytPrefs", Context.MODE_PRIVATE)

        val syncMode = sharedPref.getString("hijri_sync_mode", "auto") ?: "auto"
        if (syncMode == "manual" && !forceSync) {
            // In manual mode, do not perform silent auto-sync
            return@withContext false
        }

        var record = recordDao.getRecord()
        val now = LocalDate.now()

        // Fallback for first install
        if (record == null) {
            record = HijriDateRecord(
                day = 9,
                month = 2,
                year = 1448,
                gregorianDateStr = "2026-07-24",
                status = "estimated"
            )
            recordDao.insertRecord(record)
        }

        // If forceSync is true, or record is older than 1 day and we have internet, fetch a new one
        val recordDate = LocalDate.parse(record.gregorianDateStr)
        val daysSinceUpdate = ChronoUnit.DAYS.between(recordDate, now)

        var fetchSuccess = false
        if (forceSync || (daysSinceUpdate >= 1 && isNetworkAvailable(context))) {
            try {
                val remoteData = HijriRemoteSource.fetchHijriData()
                if (remoteData != null) {
                    record = HijriDateRecord(
                        id = 1,
                        day = remoteData.hijriDay,
                        month = remoteData.hijriMonth,
                        year = remoteData.hijriYear,
                        gregorianDateStr = remoteData.gregorianAnchorDate,
                        status = remoteData.status
                    )
                    recordDao.insertRecord(record)
                    fetchSuccess = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return@withContext fetchSuccess
    }

    fun getCalculatedHijriDate(record: HijriDateRecord?, targetGregorianDate: LocalDate = LocalDate.now()): HijrahDate {
        val safeRecord = record ?: HijriDateRecord(
            day = 9,
            month = 2,
            year = 1448,
            gregorianDateStr = "2026-07-24",
            status = "estimated"
        )
        val anchorGregorian = try {
            LocalDate.parse(safeRecord.gregorianDateStr)
        } catch (e: Exception) {
            LocalDate.now()
        }
        val daysDiff = ChronoUnit.DAYS.between(anchorGregorian, targetGregorianDate)
        val anchorHijri = try {
            HijrahDate.of(safeRecord.year, safeRecord.month, safeRecord.day)
        } catch (e: Exception) {
            HijrahDate.now()
        }
        return anchorHijri.plus(daysDiff, ChronoUnit.DAYS)
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}
