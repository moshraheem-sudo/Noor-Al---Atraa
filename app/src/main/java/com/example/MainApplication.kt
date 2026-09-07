package com.example

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.room.Room
import com.example.data.local.QuranDatabase
import com.example.data.remote.RetrofitInstance
import com.example.data.repository.QuranRepository


class MainApplication : Application(), Configuration.Provider {
    lateinit var repository: QuranRepository
        private set
    lateinit var khatmaRepository: com.example.khatma.KhatmaRepository
        private set

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `khatma_progress` (`juzNumber` INTEGER NOT NULL, `lastReadPage` INTEGER NOT NULL, `progressPercent` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL, `lastUpdatedAt` INTEGER NOT NULL, PRIMARY KEY(`juzNumber`))")
                // Insert default 30 Juz
                for (i in 1..30) {
                    db.execSQL("INSERT OR IGNORE INTO `khatma_progress` (juzNumber, lastReadPage, progressPercent, isCompleted, lastUpdatedAt) VALUES ($i, 0, 0, 0, 0)")
                }
            }
        }

        val db = Room.databaseBuilder(
            applicationContext,
            QuranDatabase::class.java,
            "quran.db"
        )
        .addMigrations(MIGRATION_1_2)
        .fallbackToDestructiveMigration()
        .build()

        repository = QuranRepository(db.quranDao(), RetrofitInstance.api)
        khatmaRepository = com.example.khatma.KhatmaRepository(db.khatmaDao())

        try {
            WorkManager.initialize(this, workManagerConfiguration)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            com.example.utils.PrayerNotificationHelper.createNotificationChannel(this)
            com.example.utils.MuezzinDownloadManager.initAndAutoDownloadAll(this)

            val prayerRepo = com.example.data.repository.PrayerTimesRepository(this)
            val cachedPrayerData = prayerRepo.prayerTimesData.value
            val city = prayerRepo.selectedCity.value
            if (cachedPrayerData != null) {
                com.example.utils.PrayerNotificationScheduler.scheduleAllPrayerNotifications(
                    context = this,
                    prayerData = cachedPrayerData,
                    cityName = city.nameAr
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
