package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.SawtQuranActivity
import com.example.data.AppDatabase
import com.example.data.NotificationItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AyahNotificationHelper {
    const val CHANNEL_ID = "quran_ayah_daily_channel"
    const val CHANNEL_NAME = "إشعارات الآيات القرآنية والتذكيرات"
    private var cachedLargeIcon: Bitmap? = null

    fun getAppLargeIcon(context: Context): Bitmap? {
        if (cachedLargeIcon != null && !cachedLargeIcon!!.isRecycled) {
            return cachedLargeIcon
        }
        return try {
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.noor)
                ?: BitmapFactory.decodeResource(context.resources, R.drawable.icon)
            cachedLargeIcon = bitmap
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    fun showAyahNotification(
        context: Context,
        ayahText: String,
        surahName: String,
        ayahNumber: Int,
        surahId: Int,
        customTitle: String = "هل استمعت اليوم لكلام الله؟ 📖"
    ) {
        // Strictly enforce 1 notification per 1 hour maximum across the entire app
        if (!NotificationRateLimiter.canPostNotification(context)) {
            return
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات دورية للآيات القرآنية الكريمة وتذكيرات كلام الله لتطبيق نور العترة"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = (System.currentTimeMillis() % 100000).toInt() + 1000

        // Intent to open reader at that surah and ayah
        val readIntent = Intent(context, SawtQuranActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_surah_id", surahId)
            putExtra("open_ayah_number", ayahNumber)
        }
        val pendingReadIntent = PendingIntent.getActivity(
            context,
            notificationId * 2,
            readIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent to listen/play audio
        val listenIntent = Intent(context, SawtQuranActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_surah_id", surahId)
            putExtra("open_ayah_number", ayahNumber)
            putExtra("auto_play_ayah", true)
        }
        val pendingListenIntent = PendingIntent.getActivity(
            context,
            notificationId * 2 + 1,
            listenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullAyahFormatted = "﴿ $ayahText ﴾ - سورة $surahName, الآية $ayahNumber"
        val largeIcon = getAppLargeIcon(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(customTitle)
            .setContentText("﴿ $ayahText ﴾")
            .setSubText("نور العترة")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(fullAyahFormatted)
                    .setBigContentTitle(customTitle)
                    .setSummaryText("تطبيق نور العترة")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingReadIntent)
            .addAction(R.drawable.ic_notification, "استمع الآن ▶", pendingListenIntent)
            .addAction(R.drawable.ic_notification, "اقرأ الآية 📖", pendingReadIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        if (largeIcon != null) {
            builder.setLargeIcon(largeIcon)
        }

        notificationManager.notify(notificationId, builder.build())

        // Also save to internal database so it appears in the app's notification history
        try {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    db.notificationDao().insertNotification(
                        NotificationItem(
                            title = customTitle,
                            message = fullAyahFormatted
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
