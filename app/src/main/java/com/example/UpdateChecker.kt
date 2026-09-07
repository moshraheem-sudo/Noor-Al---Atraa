package com.example

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object UpdateChecker {
    private const val API_URL = "https://api.github.com/repos/moshraheem-sudo/Noor-Al-Atra-/releases/latest"

    // Update state for Compose UI & In-App Popups
    var isUpdateAvailable by mutableStateOf(false)
    var updateVersion by mutableStateOf("v${BuildConfig.VERSION_NAME}")
    var apkUrl by mutableStateOf("")
    var releaseNotes by mutableStateOf("")

    var isDownloading by mutableStateOf(false)
    var downloadProgress by mutableStateOf(0f)
    var downloadedSizeMb by mutableStateOf(0f)
    var totalSizeMb by mutableStateOf(24.5f)
    var isDownloadComplete by mutableStateOf(false)

    var showInAppPopup by mutableStateOf(false)
    var showOfficialPermissionsDialog by mutableStateOf(false)

    fun triggerUpdateAvailable(version: String = "v${BuildConfig.VERSION_NAME}", notes: String = "", url: String = "") {
        updateVersion = version
        if (notes.isNotEmpty()) releaseNotes = notes
        apkUrl = url
        isUpdateAvailable = true
        showInAppPopup = true
        isDownloadComplete = false
        isDownloading = false
        downloadProgress = 0f
        downloadedSizeMb = 0f
    }

    private var downloadJob: kotlinx.coroutines.Job? = null
    private var currentUrlConnection: HttpURLConnection? = null

    fun cancelDownload() {
        try {
            downloadJob?.cancel()
            downloadJob = null
            currentUrlConnection?.disconnect()
            currentUrlConnection = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isDownloading = false
        downloadProgress = 0f
        downloadedSizeMb = 0f
    }

    fun openDownloadFolder(context: Context) {
        try {
            val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                val fileName = "Update_$updateVersion.apk"
                val appSpecificFile = File(downloadsDir, fileName)
                val fileToOpen = if (appSpecificFile.exists()) appSpecificFile else downloadsDir

                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", fileToOpen!!)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "*/*")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                Toast.makeText(context, "الملف متوفر في مجلد التنزيلات الداخلي للتطبيق", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun startUpdateDownload(context: Context, url: String = apkUrl, version: String = updateVersion) {
        if (isDownloading) return
        isDownloading = true
        isDownloadComplete = false
        downloadProgress = 0f
        downloadedSizeMb = 0f
        
        val targetUrl = if (url.isNotEmpty()) url else "https://github.com/moshraheem-sudo/Noor-Al-Atra-/releases/download/$version/app-release.apk"

        downloadJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val apkFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Update_$version.apk")
                if (apkFile.exists()) {
                    apkFile.delete()
                }

                var urlConnection = URL(targetUrl).openConnection() as HttpURLConnection
                currentUrlConnection = urlConnection
                urlConnection.setRequestProperty("User-Agent", "NoorAlAtraApp/${BuildConfig.VERSION_NAME}")
                urlConnection.connectTimeout = 15000
                urlConnection.readTimeout = 15000
                urlConnection.instanceFollowRedirects = true
                urlConnection.connect()

                var redirect = false
                val status = urlConnection.responseCode
                if (status != HttpURLConnection.HTTP_OK) {
                    if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER || status == 307) {
                        redirect = true
                    }
                }

                if (redirect) {
                    val newUrl = urlConnection.getHeaderField("Location")
                    urlConnection.disconnect()
                    urlConnection = URL(newUrl).openConnection() as HttpURLConnection
                    currentUrlConnection = urlConnection
                    urlConnection.setRequestProperty("User-Agent", "NoorAlAtraApp/${BuildConfig.VERSION_NAME}")
                    urlConnection.connectTimeout = 15000
                    urlConnection.readTimeout = 15000
                    urlConnection.connect()
                }

                val contentLength = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    urlConnection.contentLengthLong
                } else {
                    urlConnection.contentLength.toLong()
                }
                val totalBytes = if (contentLength > 0) contentLength else (24.5f * 1024 * 1024).toLong()
                
                withContext(Dispatchers.Main) {
                    totalSizeMb = totalBytes / (1024f * 1024f)
                }

                var downloadedBytes = 0L
                val buffer = ByteArray(8192)

                urlConnection.inputStream.use { input ->
                    apkFile.outputStream().use { output ->
                        while (coroutineContext.isActive) {
                            val bytesRead = input.read(buffer)
                            if (bytesRead == -1) break

                            downloadedBytes += bytesRead
                            output.write(buffer, 0, bytesRead)

                            val currentProgress = (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                            val currentMb = downloadedBytes / (1024f * 1024f)

                            withContext(Dispatchers.Main) {
                                downloadProgress = currentProgress
                                downloadedSizeMb = currentMb
                            }
                        }
                    }
                }

                if (!coroutineContext.isActive) {
                    apkFile.delete()
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    isDownloading = false
                    isDownloadComplete = true
                    downloadProgress = 1.0f
                    downloadedSizeMb = totalSizeMb
                    Toast.makeText(context, "اكتمل تنزيل التحديث (100%) - اضغط تثبيت التحديث للمتابعة", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (!coroutineContext.isActive) return@launch
                withContext(Dispatchers.Main) {
                    startDownloadManagerFallback(context, targetUrl, "Update_$version.apk")
                }
            } finally {
                currentUrlConnection = null
            }
        }
    }

    private fun startDownloadManagerFallback(context: Context, url: String, fileName: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("تحديث التطبيق $updateVersion")
                .setDescription("جاري تحميل الحزمة الرسمية...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
                .setMimeType("application/vnd.android.package-archive")

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            CoroutineScope(Dispatchers.IO).launch {
                var downloading = true
                while (downloading) {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    if (cursor != null && cursor.moveToFirst()) {
                        val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                        val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)

                        if (bytesDownloadedIndex != -1 && bytesTotalIndex != -1) {
                            val downloaded = cursor.getLong(bytesDownloadedIndex)
                            val total = cursor.getLong(bytesTotalIndex)
                            if (total > 0) {
                                val prog = (downloaded.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                                val mb = downloaded / (1024f * 1024f)
                                val totMb = total / (1024f * 1024f)
                                withContext(Dispatchers.Main) {
                                    downloadProgress = prog
                                    downloadedSizeMb = mb
                                    totalSizeMb = totMb
                                }
                            }
                        }

                        if (statusIndex != -1) {
                            val status = cursor.getInt(statusIndex)
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                downloading = false
                                withContext(Dispatchers.Main) {
                                    isDownloading = false
                                    isDownloadComplete = true
                                    downloadProgress = 1f
                                    downloadedSizeMb = totalSizeMb
                                    Toast.makeText(context, "اكتمل تنزيل التحديث (100%)", Toast.LENGTH_LONG).show()
                                }
                            } else if (status == DownloadManager.STATUS_FAILED) {
                                downloading = false
                                withContext(Dispatchers.Main) {
                                    isDownloading = false
                                    Toast.makeText(context, "فشل التنزيل عبر مدير التنزيلات", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        cursor.close()
                    }
                    kotlinx.coroutines.delay(300)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun installApk(context: Context, fileName: String = "Update_$updateVersion.apk") {
        try {
            val appSpecificFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            val publicFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            
            val targetFile = when {
                appSpecificFile.exists() -> appSpecificFile
                publicFile.exists() -> publicFile
                else -> null
            }

            if (targetFile != null && targetFile.exists()) {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", targetFile)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "لم يتم العثور على ملف التحديث التمييزي، يرجى إعادة التنزيل.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "تعذر فتح أداة تثبيت الحزم: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun checkForUpdate(context: Context, isManualCheck: Boolean = false) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentVersion = BuildConfig.VERSION_NAME
                val targetVersion = BuildConfig.VERSION_NAME

                var highestTag = ""
                var highestNotes = ""
                var highestApkUrl = ""

                // 1. Fetch all releases from GitHub API to find the maximum version released
                try {
                    val listUrl = URL("https://api.github.com/repos/moshraheem-sudo/Noor-Al-Atra-/releases")
                    val conn = listUrl.openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("User-Agent", "NoorAlAtraApp/$currentVersion")
                    conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
                    conn.connectTimeout = 12000
                    conn.readTimeout = 12000

                    if (conn.responseCode == 200) {
                        val jsonString = conn.inputStream.bufferedReader().use { it.readText() }
                        val releasesArray = JSONArray(jsonString)

                        for (i in 0 until releasesArray.length()) {
                            val releaseObj = releasesArray.getJSONObject(i)
                            if (releaseObj.optBoolean("draft", false)) continue

                            val tag = releaseObj.optString("tag_name", "").trim()
                            val body = releaseObj.optString("body", "").trim()

                            var apkUrl = ""
                            val assets = releaseObj.optJSONArray("assets")
                            if (assets != null) {
                                for (j in 0 until assets.length()) {
                                    val assetUrl = assets.getJSONObject(j).optString("browser_download_url", "")
                                    if (assetUrl.endsWith(".apk")) {
                                        apkUrl = assetUrl
                                        break
                                    }
                                }
                            }

                            if (tag.isNotEmpty()) {
                                if (highestTag.isEmpty() || isNewerVersion(tag, highestTag)) {
                                    highestTag = tag
                                    highestNotes = body
                                    highestApkUrl = apkUrl
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // 2. Fallback to /releases/latest if list call returned no valid releases
                if (highestTag.isEmpty()) {
                    try {
                        val latestUrl = URL(API_URL)
                        val conn = latestUrl.openConnection() as HttpURLConnection
                        conn.requestMethod = "GET"
                        conn.setRequestProperty("User-Agent", "NoorAlAtraApp/$currentVersion")
                        conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
                        conn.connectTimeout = 12000
                        conn.readTimeout = 12000

                        if (conn.responseCode == 200) {
                            val jsonString = conn.inputStream.bufferedReader().use { it.readText() }
                            val jsonObj = JSONObject(jsonString)
                            val tag = jsonObj.optString("tag_name", "").trim()
                            val body = jsonObj.optString("body", "").trim()
                            var apkUrl = ""
                            val assets = jsonObj.optJSONArray("assets")
                            if (assets != null) {
                                for (j in 0 until assets.length()) {
                                    val urlStr = assets.getJSONObject(j).optString("browser_download_url", "")
                                    if (urlStr.endsWith(".apk")) {
                                        apkUrl = urlStr
                                        break
                                    }
                                }
                            }
                            highestTag = tag
                            highestNotes = body
                            highestApkUrl = apkUrl
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // 3. Ensure target baseline version is evaluated if online releases are empty or smaller
                if (highestTag.isEmpty() || isNewerVersion(targetVersion, highestTag)) {
                    highestTag = if (targetVersion.startsWith("v", ignoreCase = true)) targetVersion else "v$targetVersion"
                    if (highestNotes.isEmpty()) {
                        highestNotes = "تحديث جديد متوفر للإصدار $highestTag"
                    }
                }

                // 4. Set fallback APK download URL if none found directly from assets
                if (highestApkUrl.isEmpty()) {
                    val formattedTag = if (highestTag.startsWith("v", ignoreCase = true)) highestTag else "v$highestTag"
                    highestApkUrl = "https://github.com/moshraheem-sudo/Noor-Al-Atra-/releases/download/$formattedTag/app-release.apk"
                }

                // 5. Check if highest release is newer than currently installed app
                if (isNewerVersion(highestTag, currentVersion)) {
                    withContext(Dispatchers.Main) {
                        triggerUpdateAvailable(
                            version = highestTag,
                            notes = if (highestNotes.isNotEmpty()) highestNotes else "تحديث جديد متوفر للإصدار $highestTag",
                            url = highestApkUrl
                        )
                        if (!isManualCheck) {
                            sendUpdateNotification(context, highestTag, highestApkUrl)
                        } else {
                            Toast.makeText(context, "تم العثور على تحديث جديد ($highestTag) 🎉", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        isUpdateAvailable = false
                        if (isManualCheck) {
                            Toast.makeText(context, "تطبيقك مثبت بأحدث إصدار حالياً (v$currentVersion) ✨", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    if (isManualCheck) {
                        Toast.makeText(context, "تطبيقك مثبت بأحدث إصدار حالياً (v${BuildConfig.VERSION_NAME}) ✨", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    fun sendUpdateNotification(context: Context, newVersion: String, apkUrl: String) {
        try {
            // Check if we should throttle notifications to at most 4 times a day
            val sharedPref = context.getSharedPreferences("AhlAlBaytPrefs", Context.MODE_PRIVATE)
            val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
            
            val lastUpdateNotifyDate = sharedPref.getString("last_update_notify_date", "")
            var currentDayCount = sharedPref.getInt("update_notify_count_today", 0)
            
            if (lastUpdateNotifyDate != todayStr) {
                currentDayCount = 0
            }
            
            if (currentDayCount >= 4) {
                return
            }
            
            // Increment and persist count
            currentDayCount++
            sharedPref.edit()
                .putString("last_update_notify_date", todayStr)
                .putInt("update_notify_count_today", currentDayCount)
                .apply()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            val channelId = "app_updates_channel"
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    channelId,
                    "تحديثات التطبيق",
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "إشعارات توفر نسخ جديدة من التطبيق"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, UpdateActivity::class.java).apply {
                putExtra("APK_URL", apkUrl)
                putExtra("NEW_VERSION", newVersion)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            
            val pendingIntent = android.app.PendingIntent.getActivity(
                context,
                1001,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or (if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) android.app.PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("تحديث جديد متوفر 🎉 ($newVersion)")
                .setContentText("توجد نسخة أحدث. يجب حذف النسخة الحالية أولاً ثم تثبيت الجديدة لتجنب تعارض الحزمة.")
                .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText("توجد نسخة جديدة من تطبيق نور العترة ($newVersion).\n\n⚠️ تنبيه هام: لتجنب ظهور خطأ 'تعارض الحزمة'، يرجى إلغاء تثبيت (حذف) النسخة الحالية أولاً من جهازك، ثم تثبيت النسخة الجديدة بعد تحميلها."))
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            notificationManager.notify(999, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseVersionParts(versionStr: String): List<Int> {
        val clean = versionStr.replace(Regex("[^0-9.]"), "").trim()
        if (clean.isEmpty()) return emptyList()
        return clean.split(".").mapNotNull { it.toIntOrNull() }
    }

    fun isNewerVersion(remote: String, local: String): Boolean {
        val remoteParts = parseVersionParts(remote)
        val localParts = parseVersionParts(local)

        if (remoteParts.isEmpty()) return false
        if (localParts.isEmpty()) return true

        val length = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until length) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }
        return false
    }

    private fun showUpdateDialog(context: Context, newVersion: String, apkUrl: String) {
        try {
            val intent = Intent(context, UpdateActivity::class.java).apply {
                putExtra("APK_URL", apkUrl)
                putExtra("NEW_VERSION", newVersion)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startDownload(context: Context, url: String, fileName: String) {
        try {
            Toast.makeText(context, "جاري التحميل...", Toast.LENGTH_SHORT).show()
            
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("تحديث التطبيق")
                .setDescription("جاري تحميل النسخة الجديدة...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setMimeType("application/vnd.android.package-archive")

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(ctxt: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        installApk(context, fileName)
                        try {
                            context.unregisterReceiver(this)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
