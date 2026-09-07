package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.NotificationSettingsManager
import com.example.data.repository.PrayerTimesRepository
import com.example.utils.PrayerNotificationScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            NotificationSettingsManager.scheduleAllNotifications(context)

            try {
                com.example.utils.PrayerNotificationHelper.createNotificationChannel(context)
                com.example.utils.MuezzinDownloadManager.initAndAutoDownloadAll(context)

                val repository = PrayerTimesRepository(context)
                val cachedData = repository.prayerTimesData.value
                val city = repository.selectedCity.value
                if (cachedData != null) {
                    PrayerNotificationScheduler.scheduleAllPrayerNotifications(
                        context = context,
                        prayerData = cachedData,
                        cityName = city.nameAr
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
