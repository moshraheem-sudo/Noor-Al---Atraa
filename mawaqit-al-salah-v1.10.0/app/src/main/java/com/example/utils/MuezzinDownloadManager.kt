package com.example.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.data.model.Muezzin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

sealed class MuezzinDownloadStatus {
    object NotDownloaded : MuezzinDownloadStatus()
    data class Downloading(val progressPercent: Int) : MuezzinDownloadStatus()
    data class Downloaded(val fileSizeBytes: Long) : MuezzinDownloadStatus()
    data class Failed(val error: String) : MuezzinDownloadStatus()
}

object MuezzinDownloadManager {
    private const val TAG = "MuezzinDownloadManager"
    private const val MIN_VALID_MP3_SIZE = 50 * 1024L // 50 KB minimum for valid MP3

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val _downloadStatuses = MutableStateFlow<Map<String, MuezzinDownloadStatus>>(emptyMap())
    val downloadStatuses: StateFlow<Map<String, MuezzinDownloadStatus>> = _downloadStatuses.asStateFlow()

    private val activeDownloadJobs = mutableMapOf<String, Job>()

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Initializes statuses on app startup and triggers background download for all muezzins
     * if files are not yet downloaded.
     */
    fun initAndAutoDownloadAll(context: Context) {
        checkLocalFiles(context)
        // Delay slightly on startup so initial UI loads smoothly before starting downloads
        scope.launch {
            delay(1500)
            downloadAllInBackground(context)
        }
    }

    /**
     * Inspects local storage for existing muezzin files and updates the status map.
     */
    fun checkLocalFiles(context: Context) {
        val appContext = context.applicationContext
        val currentMap = mutableMapOf<String, MuezzinDownloadStatus>()
        Muezzin.values().forEach { muezzin ->
            val file = getAudioFile(appContext, muezzin)
            if (file.exists() && file.length() >= MIN_VALID_MP3_SIZE) {
                currentMap[muezzin.id] = MuezzinDownloadStatus.Downloaded(file.length())
            } else {
                currentMap[muezzin.id] = MuezzinDownloadStatus.NotDownloaded
            }
        }
        _downloadStatuses.value = currentMap
    }

    fun isAudioDownloaded(context: Context, muezzin: Muezzin): Boolean {
        val file = getAudioFile(context, muezzin)
        return file.exists() && file.length() >= MIN_VALID_MP3_SIZE
    }

    fun getAudioFile(context: Context, muezzin: Muezzin): File {
        val dir = File(context.applicationContext.filesDir, "muezzin_audios")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File(dir, "adhan_${muezzin.id}.mp3")
    }

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Forces background download for all muezzin audios sequentially if not already present.
     */
    fun downloadAllInBackground(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            val prefs = appContext.getSharedPreferences("prayer_times_prefs", Context.MODE_PRIVATE)
            val selectedId = prefs.getString("selected_muezzin_id", Muezzin.defaultMuezzin.id)
            val sortedMuezzins = Muezzin.values().sortedByDescending { it.id == selectedId }

            sortedMuezzins.forEach { muezzin ->
                if (!isAudioDownloaded(appContext, muezzin)) {
                    downloadMuezzinInternal(appContext, muezzin, maxRetries = 2)
                    delay(500) // Brief pause between sequential downloads to keep connection clean
                }
            }
        }
    }

    /**
     * Downloads a single muezzin audio file in the background (called from UI or user interaction).
     */
    fun downloadMuezzin(context: Context, muezzin: Muezzin) {
        val appContext = context.applicationContext
        if (activeDownloadJobs[muezzin.id]?.isActive == true) return
        if (isAudioDownloaded(appContext, muezzin)) {
            val file = getAudioFile(appContext, muezzin)
            updateStatus(muezzin.id, MuezzinDownloadStatus.Downloaded(file.length()))
            return
        }

        val job = scope.launch {
            downloadMuezzinInternal(appContext, muezzin, maxRetries = 3)
        }
        activeDownloadJobs[muezzin.id] = job
    }

    private suspend fun downloadMuezzinInternal(context: Context, muezzin: Muezzin, maxRetries: Int) {
        if (isAudioDownloaded(context, muezzin)) {
            val file = getAudioFile(context, muezzin)
            updateStatus(muezzin.id, MuezzinDownloadStatus.Downloaded(file.length()))
            return
        }

        if (!isOnline(context)) {
            Log.w(TAG, "Cannot download ${muezzin.nameAr}: No internet connection")
            updateStatus(muezzin.id, MuezzinDownloadStatus.Failed("لا يوجد اتصال بالإنترنت"))
            return
        }

        val targetFile = getAudioFile(context, muezzin)
        val tempFile = File(targetFile.parentFile, "${targetFile.name}.tmp")

        var attempt = 0
        var success = false
        var lastErrorMessage = "خطأ غير محدد"

        while (attempt < maxRetries && !success) {
            attempt++
            try {
                updateStatus(muezzin.id, MuezzinDownloadStatus.Downloading(0))

                val request = Request.Builder()
                    .url(muezzin.audioUrl)
                    .header("User-Agent", "Mozilla/5.0 (Android; Mobile; PrayerTimesApp/1.0)")
                    .header("Accept", "*/*")
                    .header("Connection", "keep-alive")
                    .build()

                val response = okHttpClient.newCall(request).execute()

                if (!response.isSuccessful) {
                    val code = response.code
                    response.close()
                    throw Exception("فشل الاستجابة من الخادم ($code)")
                }

                val body = response.body ?: throw Exception("محتوى الملف فارغ")
                val totalLength = body.contentLength()
                val inputStream: InputStream = body.byteStream()
                val outputStream = FileOutputStream(tempFile)

                try {
                    val buffer = ByteArray(32 * 1024)
                    var bytesRead: Int
                    var totalBytesRead = 0L
                    var lastProgressUpdate = 0L

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        if (totalLength > 0) {
                            val progress = ((totalBytesRead * 100) / totalLength).toInt()
                            val now = System.currentTimeMillis()
                            if (now - lastProgressUpdate > 250) {
                                lastProgressUpdate = now
                                updateStatus(muezzin.id, MuezzinDownloadStatus.Downloading(progress.coerceIn(0, 100)))
                            }
                        }
                    }

                    outputStream.flush()
                } finally {
                    try { outputStream.close() } catch (e: Exception) {}
                    try { inputStream.close() } catch (e: Exception) {}
                    try { response.close() } catch (e: Exception) {}
                }

                if (tempFile.exists() && tempFile.length() >= MIN_VALID_MP3_SIZE) {
                    if (targetFile.exists()) {
                        targetFile.delete()
                    }
                    if (tempFile.renameTo(targetFile)) {
                        Log.i(TAG, "Successfully downloaded ${muezzin.nameAr}: ${targetFile.length()} bytes")
                        updateStatus(muezzin.id, MuezzinDownloadStatus.Downloaded(targetFile.length()))
                        success = true
                    } else {
                        throw Exception("تعذر حفظ الملف على الجهاز")
                    }
                } else {
                    throw Exception("الملف غير مكتمل")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Download attempt $attempt failed for ${muezzin.nameAr}: ${e.message}")
                if (tempFile.exists()) {
                    tempFile.delete()
                }
                lastErrorMessage = when {
                    e.message?.contains("connection abort", ignoreCase = true) == true ||
                    e.message?.contains("Software caused connection", ignoreCase = true) == true ->
                        "انقطع الاتصال، أعد المحاولة"
                    e.message?.contains("timeout", ignoreCase = true) == true ->
                        "انتهت مهلة الاتصال بالخادم"
                    e.message?.contains("No route to host", ignoreCase = true) == true ||
                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
                        "لا يمكن الوصول إلى خادم التحميل"
                    else -> "فشل التحميل: ${e.localizedMessage ?: "خطأ في الشبكة"}"
                }

                if (attempt < maxRetries) {
                    delay(1200L * attempt) // Exponential backoff before retry
                }
            }
        }

        if (!success) {
            updateStatus(muezzin.id, MuezzinDownloadStatus.Failed(lastErrorMessage))
        }
        activeDownloadJobs.remove(muezzin.id)
    }

    private fun updateStatus(muezzinId: String, status: MuezzinDownloadStatus) {
        val updated = _downloadStatuses.value.toMutableMap()
        updated[muezzinId] = status
        _downloadStatuses.value = updated
    }

    fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> String.format("%.1f ميغابايت", bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> String.format("%d كيلوبايت", bytes / 1024)
            else -> "$bytes بايت"
        }
    }
}
