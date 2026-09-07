package com.example.data.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.graphics.BitmapFactory
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.example.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

data class AppReleaseInfo(
    val appName: String,
    val tagName: String,
    val releaseName: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val htmlUrl: String,
    val isNewerAvailable: Boolean
)

sealed class DownloadState {
    object Idle : DownloadState()
    object Checking : DownloadState()
    data class Progress(
        val downloadedBytes: Long,
        val totalBytes: Long,
        val progressPercent: Int,
        val speedMBs: Float
    ) : DownloadState()
    data class Completed(val apkFile: File) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

object AppUpdateManager {
    const val QURAN_APP_NAME = "صوت القرآن"
    val QURAN_CURRENT_VERSION = "v" + com.example.BuildConfig.VERSION_NAME
    const val QURAN_CHECK_API = "https://api.github.com/repos/moshraheem-sudo/Sawt-Al-Quran-/releases/latest"
    const val QURAN_RELEASE_PAGE = "https://github.com/moshraheem-sudo/Sawt-Al-Quran-/releases/latest"
    const val QURAN_DIRECT_APK = "https://github.com/moshraheem-sudo/Sawt-Al-Quran-/releases/latest/download/app-release.apk"

    const val NOUR_APP_NAME = "نور العترة"
    val NOUR_CURRENT_VERSION = "v" + com.example.BuildConfig.VERSION_NAME
    const val NOUR_CHECK_API = "https://api.github.com/repos/moshraheem-sudo/Noor-Al-Atra-/releases/latest"
    const val NOUR_RELEASE_PAGE = "https://github.com/moshraheem-sudo/Noor-Al-Atra-/releases/latest"

    const val PRAYER_APP_NAME = "مواقيت الصلاة"
    const val PRAYER_CURRENT_VERSION = "1.0.0"
    const val PRAYER_CHECK_API = "https://api.github.com/repos/moshraheem-sudo/Prayer-Times/releases/latest"
    const val PRAYER_RELEASES_LIST_API = "https://api.github.com/repos/moshraheem-sudo/Prayer-Times/releases"
    const val PRAYER_RELEASE_PAGE = "https://github.com/moshraheem-sudo/Prayer-Times/releases/latest"
    const val PRAYER_REPO_PAGE = "https://github.com/moshraheem-sudo/Prayer-Times"
    const val PRAYER_DIRECT_APK = "https://github.com/moshraheem-sudo/Prayer-Times/releases/download/1.20.0/Mawaqit_Al-Salah_v1.20.0.apk"

    private const val PREFS_NAME = "app_update_prefs"
    private const val KEY_POSTPONED_QURAN = "postponed_quran_version"
    private const val KEY_POSTPONED_NOUR = "postponed_nour_version"
    private const val KEY_POSTPONED_PRAYER = "postponed_prayer_version"

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .protocols(listOf(Protocol.HTTP_1_1))
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    fun isQuranVersionPostponed(context: Context, versionTag: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val postponed = prefs.getString(KEY_POSTPONED_QURAN, "") ?: ""
        return postponed == versionTag
    }

    fun postponeQuranVersion(context: Context, versionTag: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_POSTPONED_QURAN, versionTag).apply()
    }

    fun isNourVersionPostponed(context: Context, versionTag: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val postponed = prefs.getString(KEY_POSTPONED_NOUR, "") ?: ""
        return postponed == versionTag
    }

    fun postponeNourVersion(context: Context, versionTag: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_POSTPONED_NOUR, versionTag).apply()
    }

    fun isPrayerVersionPostponed(context: Context, versionTag: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val postponed = prefs.getString(KEY_POSTPONED_PRAYER, "") ?: ""
        return postponed == versionTag
    }

    fun postponePrayerVersion(context: Context, versionTag: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_POSTPONED_PRAYER, versionTag).apply()
    }

    suspend fun downloadQuranApk(
        context: Context,
        onProgress: (DownloadState) -> Unit
    ) = withContext(Dispatchers.IO) {
        val candidates = mutableListOf<String>()

        // 1. Try resolving through GitHub Releases API (latest)
        try {
            val res = checkQuranUpdate().getOrNull()
            if (res != null && res.downloadUrl.isNotBlank() && res.downloadUrl.endsWith(".apk", ignoreCase = true)) {
                candidates.add(res.downloadUrl)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Try resolving through all releases list of Sawt-Al-Quran-
        try {
            val listRequest = Request.Builder()
                .url("https://api.github.com/repos/moshraheem-sudo/Sawt-Al-Quran-/releases")
                .header("User-Agent", "SawtALQuranApp/${com.example.BuildConfig.VERSION_NAME}")
                .header("Accept", "application/vnd.github.v3+json")
                .build()
            client.newCall(listRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: ""
                    val array = org.json.JSONArray(bodyStr)
                    for (i in 0 until array.length()) {
                        val rel = array.getJSONObject(i)
                        val assets = rel.optJSONArray("assets") ?: continue
                        for (j in 0 until assets.length()) {
                            val assetUrl = assets.getJSONObject(j).optString("browser_download_url", "")
                            if (assetUrl.contains(".apk", ignoreCase = true) && !candidates.contains(assetUrl)) {
                                candidates.add(assetUrl)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Try resolving from Noor-Al-Atra- repo
        try {
            val listRequest2 = Request.Builder()
                .url("https://api.github.com/repos/moshraheem-sudo/Noor-Al-Atra-/releases")
                .header("User-Agent", "NoorAlAtraApp/${com.example.BuildConfig.VERSION_NAME}")
                .header("Accept", "application/vnd.github.v3+json")
                .build()
            client.newCall(listRequest2).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: ""
                    val array = org.json.JSONArray(bodyStr)
                    for (i in 0 until array.length()) {
                        val rel = array.getJSONObject(i)
                        val assets = rel.optJSONArray("assets") ?: continue
                        for (j in 0 until assets.length()) {
                            val assetUrl = assets.getJSONObject(j).optString("browser_download_url", "")
                            if (assetUrl.contains(".apk", ignoreCase = true) && !candidates.contains(assetUrl)) {
                                candidates.add(assetUrl)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 4. Fallback direct URLs
        if (!candidates.contains(QURAN_DIRECT_APK)) candidates.add(QURAN_DIRECT_APK)
        candidates.add("https://github.com/moshraheem-sudo/Sawt-Al-Quran-/releases/latest/download/app-release.apk")
        candidates.add("https://github.com/moshraheem-sudo/Noor-Al-Atra-/releases/latest/download/app-release.apk")

        var downloadSuccess = false
        var lastError = ""

        for (url in candidates) {
            var succeeded = false
            downloadApkFile(
                context = context,
                downloadUrl = url,
                fileName = "SawtQuran_Latest.apk",
                onProgress = { state ->
                    when (state) {
                        is DownloadState.Completed -> {
                            succeeded = true
                            downloadSuccess = true
                            onProgress(state)
                        }
                        is DownloadState.Error -> {
                            lastError = state.message
                        }
                        is DownloadState.Progress -> {
                            onProgress(state)
                        }
                        else -> {
                            onProgress(state)
                        }
                    }
                }
            )

            if (succeeded) break
        }

        if (!downloadSuccess) {
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(
                    context,
                    "لم يتوفر ملف APK مباشر (404). جارٍ فتح صفحة التنزيل في المتصفح...",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(QURAN_RELEASE_PAGE)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(browserIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            onProgress(DownloadState.Error(if (lastError.isNotBlank()) lastError else "فشل التنزيل: كود 404"))
        }
    }

    suspend fun downloadPrayerApk(
        context: Context,
        onProgress: (DownloadState) -> Unit
    ) = withContext(Dispatchers.IO) {
        val candidates = mutableListOf<String>()

        // 1. Try resolving through GitHub Releases API (latest)
        try {
            val res = checkPrayerUpdate().getOrNull()
            if (res != null && res.downloadUrl.isNotBlank() && res.downloadUrl.endsWith(".apk", ignoreCase = true)) {
                candidates.add(res.downloadUrl)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Try resolving through all releases list of Prayer-Times
        try {
            val listRequest = Request.Builder()
                .url(PRAYER_RELEASES_LIST_API)
                .header("User-Agent", "PrayerTimesApp/${com.example.BuildConfig.VERSION_NAME}")
                .header("Accept", "application/vnd.github.v3+json")
                .build()
            client.newCall(listRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: ""
                    val array = org.json.JSONArray(bodyStr)
                    for (i in 0 until array.length()) {
                        val rel = array.getJSONObject(i)
                        val assets = rel.optJSONArray("assets") ?: continue
                        for (j in 0 until assets.length()) {
                            val assetUrl = assets.getJSONObject(j).optString("browser_download_url", "")
                            if (assetUrl.contains(".apk", ignoreCase = true) && !candidates.contains(assetUrl)) {
                                candidates.add(assetUrl)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Fallback direct URLs
        if (!candidates.contains(PRAYER_DIRECT_APK)) candidates.add(PRAYER_DIRECT_APK)
        candidates.add("https://github.com/moshraheem-sudo/Prayer-Times/releases/latest/download/app-release.apk")
        candidates.add("https://github.com/moshraheem-sudo/Prayer-Times/releases/download/1.20.0/Mawaqit_Al-Salah_v1.20.0.apk")

        var downloadSuccess = false
        var lastError = ""

        for (url in candidates) {
            var succeeded = false
            downloadApkFile(
                context = context,
                downloadUrl = url,
                fileName = "Mawaqit_Al-Salah.apk",
                onProgress = { state ->
                    when (state) {
                        is DownloadState.Completed -> {
                            succeeded = true
                            downloadSuccess = true
                            onProgress(state)
                        }
                        is DownloadState.Error -> {
                            lastError = state.message
                        }
                        is DownloadState.Progress -> {
                            onProgress(state)
                        }
                        else -> {
                            onProgress(state)
                        }
                    }
                }
            )

            if (succeeded) break
        }

        if (!downloadSuccess) {
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(
                    context,
                    "لم يتوفر ملف APK مباشر (404). جارٍ فتح صفحة التنزيل في المتصفح...",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(PRAYER_RELEASE_PAGE)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(browserIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            onProgress(DownloadState.Error(if (lastError.isNotBlank()) lastError else "فشل التنزيل: كود 404"))
        }
    }

    suspend fun checkPrayerUpdate(): Result<AppReleaseInfo> = withContext(Dispatchers.IO) {
        try {
            // 1. Try checking latest release endpoint
            val request = Request.Builder()
                .url(PRAYER_CHECK_API)
                .header("User-Agent", "PrayerTimesApp/${com.example.BuildConfig.VERSION_NAME}")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            var latestJson: JSONObject? = null
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyStr = response.body?.string() ?: ""
                        if (bodyStr.isNotBlank() && bodyStr.startsWith("{")) {
                            latestJson = JSONObject(bodyStr)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 2. If latest release endpoint wasn't successful (e.g. 404), check releases list
            if (latestJson == null) {
                try {
                    val listReq = Request.Builder()
                        .url(PRAYER_RELEASES_LIST_API)
                        .header("User-Agent", "PrayerTimesApp/${com.example.BuildConfig.VERSION_NAME}")
                        .header("Accept", "application/vnd.github.v3+json")
                        .build()
                    client.newCall(listReq).execute().use { response ->
                        if (response.isSuccessful) {
                            val bodyStr = response.body?.string() ?: ""
                            val array = org.json.JSONArray(bodyStr)
                            if (array.length() > 0) {
                                latestJson = array.getJSONObject(0)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (latestJson == null) {
                return@withContext Result.success(
                    AppReleaseInfo(
                        appName = PRAYER_APP_NAME,
                        tagName = "1.20.0",
                        releaseName = "تطبيق مواقيت الصلاة والأذان",
                        releaseNotes = "تطبيق مواقيت الصلاة والأذان بدقة عالية مع اتجاه القبلة والتقويم الهجري المبارك.",
                        downloadUrl = PRAYER_DIRECT_APK,
                        htmlUrl = PRAYER_REPO_PAGE,
                        isNewerAvailable = true
                    )
                )
            }

            val json = latestJson!!
            val tagName = json.optString("tag_name", "1.20.0")
            val releaseName = json.optString("name", "تطبيق مواقيت الصلاة والأذان").ifBlank { "الإصدار $tagName" }
            val releaseNotes = json.optString("body", "").ifBlank {
                "تطبيق مواقيت الصلاة والأذان الدقيقة مع التنبيهات والأذكار والتقويم الهجري الشريف."
            }
            val htmlUrl = json.optString("html_url", PRAYER_RELEASE_PAGE)

            var downloadUrl = PRAYER_DIRECT_APK
            val assets = json.optJSONArray("assets")
            if (assets != null && assets.length() > 0) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val assetUrl = asset.optString("browser_download_url", "")
                    if (assetUrl.contains(".apk", ignoreCase = true)) {
                        downloadUrl = assetUrl
                        break
                    }
                }
            }

            val isNewer = isVersionNewer(tagName, PRAYER_CURRENT_VERSION)

            Result.success(
                AppReleaseInfo(
                    appName = PRAYER_APP_NAME,
                    tagName = tagName,
                    releaseName = releaseName,
                    releaseNotes = releaseNotes,
                    downloadUrl = downloadUrl,
                    htmlUrl = htmlUrl,
                    isNewerAvailable = isNewer
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkQuranUpdate(): Result<AppReleaseInfo> = withContext(Dispatchers.IO) {
        try {
            // 1. Try checking latest release endpoint
            val request = Request.Builder()
                .url(QURAN_CHECK_API)
                .header("User-Agent", "SawtALQuranApp/${com.example.BuildConfig.VERSION_NAME}")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            var latestJson: JSONObject? = null
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyStr = response.body?.string() ?: ""
                        if (bodyStr.isNotBlank() && bodyStr.startsWith("{")) {
                            latestJson = JSONObject(bodyStr)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 2. If latest release endpoint wasn't successful (e.g. 404), check releases list
            if (latestJson == null) {
                try {
                    val listReq = Request.Builder()
                        .url("https://api.github.com/repos/moshraheem-sudo/Sawt-Al-Quran-/releases")
                        .header("User-Agent", "SawtALQuranApp/${com.example.BuildConfig.VERSION_NAME}")
                        .header("Accept", "application/vnd.github.v3+json")
                        .build()
                    client.newCall(listReq).execute().use { response ->
                        if (response.isSuccessful) {
                            val bodyStr = response.body?.string() ?: ""
                            val array = org.json.JSONArray(bodyStr)
                            if (array.length() > 0) {
                                latestJson = array.getJSONObject(0)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (latestJson == null) {
                return@withContext Result.success(
                    AppReleaseInfo(
                        appName = QURAN_APP_NAME,
                        tagName = QURAN_CURRENT_VERSION,
                        releaseName = "تحديث تطبيق صوت القرآن",
                        releaseNotes = "تطبيق صوت القرآن الكريم بالتلاوات والأدعية المباركة.",
                        downloadUrl = QURAN_DIRECT_APK,
                        htmlUrl = "https://github.com/moshraheem-sudo/Sawt-Al-Quran-",
                        isNewerAvailable = false
                    )
                )
            }

            val json = latestJson!!
            val tagName = json.optString("tag_name", QURAN_CURRENT_VERSION)
            val releaseName = json.optString("name", "تحديث صوت القرآن الكريم")
            val releaseNotes = json.optString("body", "يتضمن هذا التحديث تحسينات وإصلاحات عامة لتطبيق صوت القرآن الكريم.").ifBlank {
                "يتضمن هذا التحديث تحسينات وإصلاحات عامة لتطبيق صوت القرآن الكريم."
            }
            val htmlUrl = json.optString("html_url", QURAN_RELEASE_PAGE)

            var downloadUrl = QURAN_DIRECT_APK
            val assets = json.optJSONArray("assets")
            if (assets != null && assets.length() > 0) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val assetUrl = asset.optString("browser_download_url", "")
                    if (assetUrl.contains(".apk", ignoreCase = true)) {
                        downloadUrl = assetUrl
                        break
                    }
                }
            }

            val isNewer = isVersionNewer(tagName, QURAN_CURRENT_VERSION)

            Result.success(
                AppReleaseInfo(
                    appName = QURAN_APP_NAME,
                    tagName = tagName,
                    releaseName = releaseName,
                    releaseNotes = releaseNotes,
                    downloadUrl = downloadUrl,
                    htmlUrl = htmlUrl,
                    isNewerAvailable = isNewer
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkNourUpdate(): Result<AppReleaseInfo> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(NOUR_CHECK_API)
                .header("User-Agent", "SawtALQuranApp/1.40.0")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.success(
                        AppReleaseInfo(
                            appName = NOUR_APP_NAME,
                            tagName = NOUR_CURRENT_VERSION,
                            releaseName = "تحديث تطبيق نور العترة",
                            releaseNotes = "تطبيق نور العترة بالأدعية والتلاوات المباركة.",
                            downloadUrl = "https://github.com/moshraheem-sudo/Noor-Al-Atra-/releases/latest/download/app-release.apk",
                            htmlUrl = NOUR_RELEASE_PAGE,
                            isNewerAvailable = false
                        )
                    )
                }

                val bodyStr = response.body?.string() ?: ""
                val json = JSONObject(bodyStr)
                val tagName = json.optString("tag_name", NOUR_CURRENT_VERSION)
                val releaseName = json.optString("name", "تحديث نور العترة")
                val releaseNotes = json.optString("body", "يتضمن هذا التحديث ميزات ومحتويات جديدة لتطبيق نور العترة.").ifBlank {
                    "يتضمن هذا التحديث ميزات ومحتويات جديدة لتطبيق نور العترة."
                }
                val htmlUrl = json.optString("html_url", NOUR_RELEASE_PAGE)

                var downloadUrl = ""
                val assets = json.optJSONArray("assets")
                if (assets != null && assets.length() > 0) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val assetUrl = asset.optString("browser_download_url", "")
                        if (assetUrl.isNotEmpty()) {
                            downloadUrl = assetUrl
                            break
                        }
                    }
                }
                if (downloadUrl.isEmpty()) {
                    downloadUrl = "https://github.com/moshraheem-sudo/Noor-Al-Atra-/releases/latest/download/app-release.apk"
                }

                val isNewer = isVersionNewer(tagName, NOUR_CURRENT_VERSION)

                Result.success(
                    AppReleaseInfo(
                        appName = NOUR_APP_NAME,
                        tagName = tagName,
                        releaseName = releaseName,
                        releaseNotes = releaseNotes,
                        downloadUrl = downloadUrl,
                        htmlUrl = htmlUrl,
                        isNewerAvailable = isNewer
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun sendNourUpdateNotification(context: Context, releaseInfo: AppReleaseInfo) {
        if (isNourVersionPostponed(context, releaseInfo.tagName)) return

        val channelId = "nour_update_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "تحديثات تطبيق نور العترة",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "إشعارات تذكير بوجود تحديثات جديدة لتطبيق نور العترة"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_settings", true)
        }

        val pendingIntent = if (launchIntent != null) {
            PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else null

        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.icon)
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(largeIcon)
            .setContentTitle("تحديث جديد لتطبيق نور العترة 🌟")
            .setContentText("يتوفر إصدار جديد (${releaseInfo.tagName}) لتطبيق نور العترة.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("يتوفر إصدار جديد (${releaseInfo.tagName}) لتطبيق نور العترة.\n${releaseInfo.releaseNotes}"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent)
        }

        notificationManager.notify(1002, builder.build())
    }

    private fun isVersionNewer(newVer: String, currentVer: String): Boolean {
        val cleanNew = newVer.trim().removePrefix("v").removePrefix("V")
        val cleanCurrent = currentVer.trim().removePrefix("v").removePrefix("V")
        if (cleanNew == cleanCurrent) return false

        val newParts = cleanNew.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }

        if (newParts.isEmpty() || currentParts.isEmpty()) return cleanNew != cleanCurrent

        val maxLen = maxOf(newParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val pNew = newParts.getOrElse(i) { 0 }
            val pCurr = currentParts.getOrElse(i) { 0 }
            if (pNew > pCurr) return true
            if (pNew < pCurr) return false
        }
        return false
    }

    suspend fun downloadApkFile(
        context: Context,
        downloadUrl: String,
        fileName: String,
        onProgress: (DownloadState) -> Unit
    ) = withContext(Dispatchers.IO) {
        val notifyProgress: suspend (DownloadState) -> Unit = { state ->
            withContext(Dispatchers.Main) {
                try {
                    onProgress(state)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        try {
            notifyProgress(DownloadState.Progress(0, 0, 0, 0f))

            val request = Request.Builder()
                .url(downloadUrl)
                .header("User-Agent", "SawtALQuranApp/1.30.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    notifyProgress(DownloadState.Error("فشل التنزيل: رمز ${response.code}"))
                    return@withContext
                }

                val body = response.body
                if (body == null) {
                    notifyProgress(DownloadState.Error("ملف التنزيل فارغ"))
                    return@withContext
                }

                val totalBytes = body.contentLength()
                val updatesDir = File(context.cacheDir, "updates")
                if (!updatesDir.exists()) updatesDir.mkdirs()

                val apkFile = File(updatesDir, fileName)
                if (apkFile.exists()) apkFile.delete()

                val inputStream: InputStream = body.byteStream()
                val outputStream = FileOutputStream(apkFile)

                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytesRead = 0L
                val startTime = System.currentTimeMillis()
                var lastProgressTime = System.currentTimeMillis()

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead

                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastProgressTime > 120 || (totalBytes > 0 && totalBytesRead == totalBytes)) {
                        lastProgressTime = currentTime
                        val timeDiffSec = (currentTime - startTime) / 1000f
                        val speedMBs = if (timeDiffSec > 0) (totalBytesRead / (1024f * 1024f)) / timeDiffSec else 0f
                        val progressPercent = if (totalBytes > 0) ((totalBytesRead * 100) / totalBytes).toInt() else 0

                        notifyProgress(
                            DownloadState.Progress(
                                downloadedBytes = totalBytesRead,
                                totalBytes = totalBytes,
                                progressPercent = progressPercent,
                                speedMBs = speedMBs
                            )
                        )
                    }
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                notifyProgress(DownloadState.Completed(apkFile))
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            // Cancelled by user
        } catch (e: Exception) {
            notifyProgress(DownloadState.Error("خطأ أثناء التنزيل: ${e.localizedMessage ?: e.message}"))
        }
    }

    fun installApk(context: Context, apkFile: File) {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            try {
                if (!apkFile.exists()) return@post

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (!context.packageManager.canRequestPackageInstalls()) {
                        android.widget.Toast.makeText(
                            context,
                            "يرجى تفعيل خيار (السماح بتثبيت التطبيقات) لإكمال التثبيت",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        val permissionIntent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                            data = Uri.parse("package:${context.packageName}")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(permissionIntent)
                        return@post
                    }
                }

                val contentUri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(contentUri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(context, "فشل بدء تثبيت التحديث: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }
}
