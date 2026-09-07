package com.example.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import com.example.data.NotificationItem
import com.example.MainActivity

class NotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!com.example.util.NotificationRateLimiter.canPostNotification(context)) {
            return Result.success()
        }

        val messages = listOf(
            Pair("تذكير بالتوبة", "لا تنسَ التوبة والاستغفار، فإن الله غفور رحيم."),
            Pair("ذكر الله", "ألا بذكر الله تطمئن القلوب. سبحان الله والحمد لله ولا إله إلا الله والله أكبر."),
            Pair("الحذر من الغفلة", "احذر من خطوات الشيطان واغتنم وقتك في طاعة الله."),
            Pair("دعاء", "اللهم صل على محمد وآل محمد وعجل فرجهم."),
            Pair("مناجاة", "إلهي قلبي محجوب، وعقلي مغلوب، ولساني معقود..."),
            Pair("تذكير", "هل صليت على النبي وآله اليوم؟"),
            Pair("أعمال اليوم", "تصفح أعمال هذا اليوم في التطبيق لتنال الأجر."),
            Pair("الاستغفار", "استغفر الله ربي وأتوب إليه."),
            Pair("دعاء الفرج", "اللهم كن لوليك الحجة بن الحسن صلواتك عليه وعلى آبائه..."),
            Pair("تذكير", "لا تنس قراءة القرآن الكريم هذا اليوم.")
        )
        
        val randomMessage = messages.random()

        val db = AppDatabase.getInstance(context)
        db.notificationDao().insertNotification(
            NotificationItem(title = randomMessage.first, message = randomMessage.second)
        )

        showNotification(context, randomMessage.first, randomMessage.second)

        return Result.success()
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "ahlalbayt_notifications"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "إشعارات أدعية ومناسبات",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
