package com.example

import android.content.Context
import android.os.Bundle
import android.os.Build
import android.content.Intent
import android.provider.Settings
import android.net.Uri
import android.os.PowerManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.ui.MainApp
import com.example.ui.theme.ThemeManager
import com.example.ui.theme.MyApplicationTheme
import com.example.worker.EventNotificationWorker
import com.example.worker.ReminderWorker
import java.util.concurrent.TimeUnit
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.init(this)
        
        // Setup periodic background workers for notifications
        try {
            com.example.data.local.NotificationSettingsManager.scheduleAllNotifications(this)
            
            // Background Hijri Sync
            com.example.data.HijriSyncManager.schedulePeriodicSync(this)

            // Prayer Times Notification Channels & Scheduling
            com.example.utils.PrayerNotificationHelper.createNotificationChannel(this)
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
        
        try {
            UpdateChecker.checkForUpdate(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val forwardSurahId = intent.getIntExtra("open_surah_id", -1).takeIf { it != -1 } ?: intent.getIntExtra("surah_id", -1)
        val forwardAyahNumber = intent.getIntExtra("open_ayah_number", -1).takeIf { it != -1 } ?: intent.getIntExtra("ayah_number", -1)
        if (forwardSurahId != -1) {
            val quranIntent = Intent(this, SawtQuranActivity::class.java).apply {
                putExtra("open_surah_id", forwardSurahId)
                putExtra("open_ayah_number", forwardAyahNumber)
            }
            startActivity(quranIntent)
        }

        enableEdgeToEdge()
        setContent {
            val sharedPref = getSharedPreferences("AhlAlBaytPrefs", Context.MODE_PRIVATE)
            var isDarkMode by remember { mutableStateOf(sharedPref.getBoolean("dark_mode_enabled", false)) }
            
            val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
            } else null

            LaunchedEffect(Unit) {
                try {
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Quran integrity check failed: ${e.message}", e)
                }

                try {
                    com.example.data.HijriSyncManager.syncIfNeeded(this@MainActivity)
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Hijri sync failed: ${e.message}", e)
                }

                try {
                    if (notificationPermissionState != null && !notificationPermissionState.status.isGranted) {
                        notificationPermissionState.launchPermissionRequest()
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Notification permission request failed: ${e.message}", e)
                }
            }
            
            MyApplicationTheme(themeMode = ThemeManager.currentThemeMode) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MainApp()
                }
            }
        }
    }
}
