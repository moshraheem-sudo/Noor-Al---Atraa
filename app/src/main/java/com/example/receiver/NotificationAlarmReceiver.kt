package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.SawtQuranActivity
import com.example.data.QuranNotificationData
import com.example.data.local.NotificationSettingsManager
import com.example.util.AyahNotificationHelper
import com.example.util.NotificationRateLimiter

class NotificationAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        try {
            if (!NotificationSettingsManager.areNotificationsEnabled(context)) {
                return
            }

            // Decide whether to send a Quran Ayah or an Islamic reminder
            val prefs = context.getSharedPreferences("AhlAlBaytPrefs", Context.MODE_PRIVATE)
            val isAyahTurn = prefs.getBoolean("next_notif_is_ayah", true)
            prefs.edit().putBoolean("next_notif_is_ayah", !isAyahTurn).apply()

            if (isAyahTurn && NotificationSettingsManager.isQuranEnabled(context)) {
                val verse = QuranNotificationData.getRandomVerse()
                AyahNotificationHelper.showAyahNotification(
                    context = context,
                    ayahText = verse.textUthmani,
                    surahName = verse.surahName,
                    ayahNumber = verse.ayahNumber,
                    surahId = verse.surahId,
                    customTitle = "نفحات من القرآن الكريم 📖"
                )
            } else {
                showIslamicReminder(context)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Always schedule the next alarm so notifications continue reliably in background
            if (NotificationSettingsManager.areNotificationsEnabled(context)) {
                NotificationAlarmScheduler.scheduleNextAlarm(context)
            }
        }
    }

    private fun showIslamicReminder(context: Context) {
        val channelId = "ahlalbayt_reminders"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "تذكيرات وعبادات أهل البيت (ع)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات التذكيرات والأدعية والزيارات اليومية"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val reminders = listOf(
            "عطّر فمك بالصلاة: اللهم صل على محمد وآل محمد وعجل فرجهم",
            "هل قرأت زيارة عاشوراء اليوم؟",
            "جدد وضوءك لتكن على طهارة وبركة",
            "لا تنسَ قراءة سورة القدر",
            "هل سبحت تسبيحة الزهراء (عليها السلام)؟",
            "قراءة آية الكرسي تحفظك وتدفع عنك البلاء",
            "لا تنسَ قراءة دعاء الفرج (اللهم كن لوليك الحجة بن الحسن...)",
            "أستغفر الله ربي وأتوب إليه",
            "لا حول ولا قوة إلا بالله العلي العظيم",
            "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            "جدد بيعتك لصاحب الزمان (عج) بقراءة دعاء العهد",
            "هل وجهت سلاماً لأبي عبد الله الحسين (عليه السلام) اليوم؟",
            "اسجد سجدة الشكر وقل: شكراً لله، شكراً لله، شكراً لله",
            "اذكر الإمام الرضا (عليه السلام) بزيارته من البعد: السلام عليك يا غريب طوس",
            "سبحان الله وبحمده سبحان الله العظيم",
            "يا كاشف الكرب عن وجه أخيه الحسين اكشف كربي بحق أخيك الحسين (ع)"
        )

        val reminderText = reminders.random()
        val notifId = (System.currentTimeMillis() % 100000).toInt() + 2000

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = AyahNotificationHelper.getAppLargeIcon(context)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("فَذَكِّرْ إِن نَّفَعَتِ الذِّكْرَىٰ ✨")
            .setContentText(reminderText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(reminderText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .apply {
                if (largeIcon != null) {
                    setLargeIcon(largeIcon)
                }
            }
            .build()

        notificationManager.notify(notifId, notification)
        NotificationRateLimiter.recordNotificationPosted(context)
    }
}
