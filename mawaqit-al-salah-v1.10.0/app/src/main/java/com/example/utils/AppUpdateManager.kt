package com.example.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

sealed class UpdateCheckStatus {
    object Idle : UpdateCheckStatus()
    object Checking : UpdateCheckStatus()
    data class UpdateAvailable(
        val latestVersionName: String,
        val latestVersionCode: Int,
        val releaseNotes: String,
        val downloadUrl: String,
        val apkFileName: String
    ) : UpdateCheckStatus()
    object UpToDate : UpdateCheckStatus()
    data class Downloading(
        val progressPercent: Int,
        val bytesRead: Long = 0L,
        val totalBytes: Long = 0L
    ) : UpdateCheckStatus()
    data class DownloadReady(val apkFile: File) : UpdateCheckStatus()
    data class PermissionRequired(val apkFile: File) : UpdateCheckStatus()
    data class Error(val message: String) : UpdateCheckStatus()
}

object AppUpdateManager {
    private const val TAG = "AppUpdateManager"
    
    // Direct link to the repository manifest for update queries
    private const val REPO_API_URL = "https://api.github.com/repos/moshraheem-sudo/Prayer-Times/releases/latest"
    private const val FALLBACK_RAW_VERSION_URL = "https://raw.githubusercontent.com/moshraheem-sudo/Prayer-Times/main/app/build.gradle.kts"

    private val _updateStatus = MutableStateFlow<UpdateCheckStatus>(UpdateCheckStatus.Idle)
    val updateStatus: StateFlow<UpdateCheckStatus> = _updateStatus.asStateFlow()

    @Volatile
    private var activeDownloadCall: okhttp3.Call? = null

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    /**
     * Compares semantic versions (e.g., "1.0.1" vs "1.0.0", "1.1" vs "1.0.0").
     * Returns true ONLY if remote is strictly newer than current.
     */
    fun isNewerVersion(remoteVersionStr: String, currentVersionStr: String): Boolean {
        val cleanRemote = remoteVersionStr.trim().removePrefix("v").removePrefix("V")
        val cleanCurrent = currentVersionStr.trim().removePrefix("v").removePrefix("V")

        if (cleanRemote.isEmpty()) return false

        val remoteParts = cleanRemote.split(".", "-", "_").mapNotNull { it.toIntOrNull() }
        val currentParts = cleanCurrent.split(".", "-", "_").mapNotNull { it.toIntOrNull() }

        val maxLength = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLength) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }

    suspend fun checkForUpdates(context: Context) {
        _updateStatus.value = UpdateCheckStatus.Checking
        withContext(Dispatchers.IO) {
            try {
                val currentVersionCode = BuildConfig.VERSION_CODE
                val currentVersionName = BuildConfig.VERSION_NAME

                val request = Request.Builder()
                    .url(REPO_API_URL)
                    .header("User-Agent", "PrayerTimesApp/${currentVersionName}")
                    .header("Accept", "application/vnd.github.v3+json")
                    .build()

                val response = try {
                    httpClient.newCall(request).execute()
                } catch (e: Exception) {
                    null
                }

                if (response != null && response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    response.close()
                    val json = JSONObject(responseBody)
                    val tagName = json.optString("tag_name", "").trim()
                    val releaseNotes = json.optString("body", "").trim()
                    
                    val assets = json.optJSONArray("assets")
                    var downloadUrl: String? = null
                    var apkName = "update.apk"

                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.optString("name", "")
                            if (name.endsWith(".apk", ignoreCase = true)) {
                                downloadUrl = asset.optString("browser_download_url", "")
                                apkName = name
                                break
                            }
                        }
                    }

                    val cleanTag = tagName.removePrefix("v").removePrefix("V").trim()
                    val isNewer = isNewerVersion(cleanTag, currentVersionName)

                    if (downloadUrl != null && isNewer) {
                        val remoteVersionCode = extractVersionCode(cleanTag)
                        _updateStatus.value = UpdateCheckStatus.UpdateAvailable(
                            latestVersionName = cleanTag.ifEmpty { "1.0.1" },
                            latestVersionCode = remoteVersionCode,
                            releaseNotes = releaseNotes.ifEmpty { "تحسينات عامة واستقرار أعلى للتطبيق وتحديث مواقيت الصلاة." },
                            downloadUrl = downloadUrl,
                            apkFileName = apkName
                        )
                        return@withContext
                    }
                }

                // If releases API has no newer release, check fallback source
                val fallbackResult = checkFallbackVersion(currentVersionCode, currentVersionName)
                if (fallbackResult != null) {
                    _updateStatus.value = fallbackResult
                } else {
                    _updateStatus.value = UpdateCheckStatus.UpToDate
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking for updates: ${e.message}", e)
                _updateStatus.value = UpdateCheckStatus.Error("تعذر التحقق من التحديثات: تأكد من اتصال الإنترنت")
            }
        }
    }

    private fun extractVersionCode(versionStr: String): Int {
        val parts = versionStr.split(".", "-", "_").mapNotNull { it.toIntOrNull() }
        return try {
            when (parts.size) {
                0 -> 1
                1 -> parts[0]
                2 -> parts[0] * 1000 + parts[1]
                else -> parts[0] * 1000000 + parts[1] * 1000 + parts[2]
            }
        } catch (e: Exception) {
            1
        }
    }

    private fun checkFallbackVersion(currentVersionCode: Int, currentVersionName: String): UpdateCheckStatus? {
        try {
            val req = Request.Builder()
                .url(FALLBACK_RAW_VERSION_URL)
                .build()
            val resp = httpClient.newCall(req).execute()
            if (!resp.isSuccessful) return null
            val content = resp.body?.string() ?: return null
            resp.close()

            // Look for versionCode = X and versionName = "Y"
            val codeRegex = Regex("""versionCode\s*=\s*(\d+)""")
            val nameRegex = Regex("""versionName\s*=\s*"([^"]+)"""")

            val remoteCode = codeRegex.find(content)?.groupValues?.get(1)?.toIntOrNull() ?: currentVersionCode
            val remoteName = nameRegex.find(content)?.groupValues?.get(1) ?: currentVersionName

            if (isNewerVersion(remoteName, currentVersionName)) {
                return UpdateCheckStatus.UpdateAvailable(
                    latestVersionName = remoteName,
                    latestVersionCode = remoteCode,
                    releaseNotes = "إصدار جديد متاح يتضمن تحسينات ومزايا إضافية.",
                    downloadUrl = "https://github.com/moshraheem-sudo/Prayer-Times/raw/main/app-release.apk",
                    apkFileName = "PrayerTimes-v$remoteName.apk"
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Fallback version check failed: ${e.message}")
        }
        return null
    }

    fun cancelDownload() {
        try {
            activeDownloadCall?.cancel()
            activeDownloadCall = null
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling download: ${e.message}")
        }
        _updateStatus.value = UpdateCheckStatus.Idle
    }

    suspend fun downloadAndInstallUpdate(context: Context, downloadUrl: String, apkFileName: String) {
        cancelDownload()
        _updateStatus.value = UpdateCheckStatus.Downloading(0)
        withContext(Dispatchers.IO) {
            val updatesDir = File(context.cacheDir, "updates")
            if (!updatesDir.exists()) updatesDir.mkdirs()

            val apkFile = File(updatesDir, apkFileName)
            if (apkFile.exists()) apkFile.delete()

            try {
                val request = Request.Builder().url(downloadUrl).build()
                val call = httpClient.newCall(request)
                activeDownloadCall = call
                val response = call.execute()

                if (!response.isSuccessful) {
                    if (call.isCanceled()) {
                        apkFile.delete()
                        _updateStatus.value = UpdateCheckStatus.Idle
                        return@withContext
                    }
                    _updateStatus.value = UpdateCheckStatus.Error("فشل تحميل ملف التحديث (رمز الخطأ: ${response.code})")
                    return@withContext
                }

                val body = response.body ?: throw Exception("ملف التحديث فارغ")
                val totalBytes = body.contentLength()
                val inputStream = body.byteStream()
                val outputStream = FileOutputStream(apkFile)

                val buffer = ByteArray(16 * 1024)
                var bytesRead: Int
                var totalRead = 0L
                var lastUpdate = 0L

                try {
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        if (call.isCanceled() || activeDownloadCall == null) {
                            outputStream.close()
                            inputStream.close()
                            apkFile.delete()
                            _updateStatus.value = UpdateCheckStatus.Idle
                            return@withContext
                        }

                        outputStream.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (totalBytes > 0) {
                            val progress = ((totalRead * 100) / totalBytes).toInt()
                            val now = System.currentTimeMillis()
                            if (now - lastUpdate > 150) {
                                lastUpdate = now
                                _updateStatus.value = UpdateCheckStatus.Downloading(
                                    progressPercent = progress.coerceIn(0, 100),
                                    bytesRead = totalRead,
                                    totalBytes = totalBytes
                                )
                            }
                        }
                    }
                    outputStream.flush()
                } finally {
                    try { outputStream.close() } catch (_: Exception) {}
                    try { inputStream.close() } catch (_: Exception) {}
                    try { response.close() } catch (_: Exception) {}
                    activeDownloadCall = null
                }

                if (call.isCanceled()) {
                    apkFile.delete()
                    _updateStatus.value = UpdateCheckStatus.Idle
                    return@withContext
                }

                if (apkFile.length() > 500 * 1024) { // Valid APK minimum size
                    if (canInstallPackages(context)) {
                        _updateStatus.value = UpdateCheckStatus.DownloadReady(apkFile)
                        withContext(Dispatchers.Main) {
                            installApk(context, apkFile)
                        }
                    } else {
                        _updateStatus.value = UpdateCheckStatus.PermissionRequired(apkFile)
                    }
                } else {
                    apkFile.delete()
                    _updateStatus.value = UpdateCheckStatus.Error("ملف التحديث المحمل غير مكتمل")
                }
            } catch (e: Exception) {
                activeDownloadCall = null
                apkFile.delete()
                val isCancelError = e is java.io.IOException && (
                    e.message?.contains("Canceled", ignoreCase = true) == true ||
                    e.message?.contains("Socket closed", ignoreCase = true) == true
                )
                if (isCancelError) {
                    _updateStatus.value = UpdateCheckStatus.Idle
                } else {
                    Log.e(TAG, "Download update failed: ${e.message}", e)
                    _updateStatus.value = UpdateCheckStatus.Error("تعذر إكمال تحميل التحديث: ${e.localizedMessage}")
                }
            }
        }
    }

    fun canInstallPackages(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun openInstallPermissionSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                try {
                    val fallbackIntent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(fallbackIntent)
                } catch (_: Exception) {}
            }
        }
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            if (!apkFile.exists()) {
                _updateStatus.value = UpdateCheckStatus.Error("ملف التحديث غير موجود، يرجى إعادة التنزيل")
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
                _updateStatus.value = UpdateCheckStatus.PermissionRequired(apkFile)
                openInstallPermissionSettings(context)
                return
            }

            val authority = "${context.packageName}.fileprovider"
            val apkUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(context, authority, apkFile)
            } else {
                Uri.fromFile(apkFile)
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
            _updateStatus.value = UpdateCheckStatus.DownloadReady(apkFile)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch package installer: ${e.message}", e)
            _updateStatus.value = UpdateCheckStatus.Error("تعذر فتح معالج التثبيت: ${e.localizedMessage}")
        }
    }

    fun resetStatus() {
        _updateStatus.value = UpdateCheckStatus.Idle
    }
}
