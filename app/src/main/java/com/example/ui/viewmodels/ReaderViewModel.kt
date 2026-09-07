package com.example.ui.viewmodels
import androidx.lifecycle.ViewModelProvider

import kotlinx.coroutines.isActive

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.data.local.QuranBookmark
import com.example.data.local.BookmarkManager
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlayerManager
import com.example.audio.Reciter
import com.example.data.local.AyahEntity
import com.example.data.repository.QuranRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

data class SurahDownloadProgress(
    val surahId: Int,
    val reciterId: String,
    val downloadedCount: Int,
    val totalAyahs: Int,
    val isDownloading: Boolean = true
)

class ReaderViewModel(
    private val repository: QuranRepository,
    private val audioPlayerManager: AudioPlayerManager
) : ViewModel() {

    private val _surahId = MutableStateFlow<Int>(1)
    val surahId: StateFlow<Int> = _surahId.asStateFlow()

    private val _surahName = MutableStateFlow<String>("")
    val surahName: StateFlow<String> = _surahName.asStateFlow()

    private val _surahDetails = MutableStateFlow<com.example.data.local.SurahEntity?>(null)
    val surahDetails: StateFlow<com.example.data.local.SurahEntity?> = _surahDetails.asStateFlow()

    private val _ayahs = MutableStateFlow<List<AyahEntity>>(emptyList())
    val ayahs: StateFlow<List<AyahEntity>> = _ayahs.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _bookmarks = MutableStateFlow<List<QuranBookmark>>(emptyList())
    val bookmarks: StateFlow<List<QuranBookmark>> = _bookmarks.asStateFlow()

    private var downloadJob: kotlinx.coroutines.Job? = null
    private val _downloadProgress = MutableStateFlow<SurahDownloadProgress?>(null)
    val downloadProgress: StateFlow<SurahDownloadProgress?> = _downloadProgress.asStateFlow()

    private val _isCurrentSurahDownloaded = MutableStateFlow(false)
    val isCurrentSurahDownloaded: StateFlow<Boolean> = _isCurrentSurahDownloaded.asStateFlow()

    val currentPlayingAyah = audioPlayerManager.currentPlayingAyah
    val isPlaying = audioPlayerManager.isPlaying
    val currentReciter = audioPlayerManager.currentReciter

    private var isPrefsLoaded = false

    init {
        viewModelScope.launch {
            currentPlayingAyah.collect { playing ->
                if (playing != null) {
                    val (playingSurahId, _) = playing
                    if (playingSurahId != _surahId.value) {
                        loadSurah(playingSurahId)
                    }
                }
            }
        }
    }

    fun initBookmarks(context: Context) {
        if (isPrefsLoaded) return
        isPrefsLoaded = true
        loadBookmarks(context)
    }

    private fun loadBookmarks(context: Context) {
        val sharedPrefs = context.getSharedPreferences("quran_bookmarks_pref", Context.MODE_PRIVATE)
        val jsonStr = sharedPrefs.getString("bookmarks_json", "[]") ?: "[]"
        try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<QuranBookmark>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val sId = obj.optInt("surahId")
                val aNum = obj.optInt("ayahNumber")
                list.add(
                    QuranBookmark(
                        id = obj.optString("id", "${sId}_$aNum"),
                        surahId = sId,
                        surahName = obj.optString("surahName"),
                        ayahNumber = aNum,
                        ayahText = obj.optString("ayahText"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            _bookmarks.value = list
        } catch (e: Exception) {
            _bookmarks.value = emptyList()
        }
    }

    fun toggleBookmark(context: Context, ayahNumber: Int, ayahText: String) {
        val currentSurah = _surahId.value
        val name = _surahName.value
        val bookmarkId = "${currentSurah}_$ayahNumber"
        val currentList = _bookmarks.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.id == bookmarkId }

        if (existingIndex >= 0) {
            currentList.removeAt(existingIndex)
        } else {
            currentList.add(
                0, // Add to top
                QuranBookmark(
                    id = bookmarkId,
                    surahId = currentSurah,
                    surahName = name,
                    ayahNumber = ayahNumber,
                    ayahText = ayahText,
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        _bookmarks.value = currentList
        saveBookmarksToPrefs(context, currentList)
    }

    fun deleteBookmark(context: Context, bookmark: QuranBookmark) {
        val currentList = _bookmarks.value.toMutableList()
        currentList.removeAll { it.id == bookmark.id }
        _bookmarks.value = currentList
        saveBookmarksToPrefs(context, currentList)
    }

    private fun saveBookmarksToPrefs(context: Context, list: List<QuranBookmark>) {
        val sharedPrefs = context.getSharedPreferences("quran_bookmarks_pref", Context.MODE_PRIVATE)
        val array = JSONArray()
        list.forEach { b ->
            val obj = JSONObject()
            obj.put("id", b.id)
            obj.put("surahId", b.surahId)
            obj.put("surahName", b.surahName)
            obj.put("ayahNumber", b.ayahNumber)
            obj.put("ayahText", b.ayahText)
            obj.put("timestamp", b.timestamp)
            array.put(obj)
        }
        sharedPrefs.edit().putString("bookmarks_json", array.toString()).apply()
    }

    fun loadSurah(id: Int, context: Context? = null) {
        _surahId.value = id
        val fallbackName = if (id in 1..114) com.example.audio.SURAH_NAMES_AR.getOrElse(id - 1) { "سورة $id" } else "سورة $id"
        _surahName.value = fallbackName
        _errorMessage.value = null

        val cachedDetails = repository.getCachedSurahDetails(id)
        if (cachedDetails != null) {
            _surahDetails.value = cachedDetails
            _surahName.value = cachedDetails.nameAr
        } else if (id in 1..114) {
            val totalAyahs = com.example.audio.SURAH_AYAH_COUNTS[id - 1]
            _surahDetails.value = com.example.data.local.SurahEntity(
                id = id,
                nameAr = fallbackName,
                nameEn = "",
                revelationType = "مكية",
                ayahCount = totalAyahs
            )
        }

        val cachedAyahs = repository.getCachedAyahs(id)
        if (cachedAyahs != null && cachedAyahs.isNotEmpty()) {
            _ayahs.value = cachedAyahs
            _isLoading.value = false
            context?.let { ctx -> checkSurahDownloaded(ctx, id) }
        } else {
            _ayahs.value = emptyList() // clear previous
            _isLoading.value = true
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.getSurah(id).collect { surah ->
                if (surah != null) {
                    _surahName.value = surah.nameAr
                    _surahDetails.value = surah
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAyahs(id).collect {
                if (it.isNotEmpty()) {
                    _ayahs.value = it
                    _isLoading.value = false
                    _errorMessage.value = null
                    context?.let { ctx -> checkSurahDownloaded(ctx, id) }
                } else if (_ayahs.value.isEmpty()) {
                    // if it is empty, we keep it empty but what if it's the final emit and it failed?
                    // well, getAyahs emits emptyList() on error.
                    _isLoading.value = false
                    _errorMessage.value = "تعذر تحميل الآيات. يرجى التحقق من اتصالك بالإنترنت."
                }
            }
        }
    }

    fun checkSurahDownloaded(context: Context, surahId: Int = _surahId.value, reciter: Reciter = currentReciter.value) {
        viewModelScope.launch(Dispatchers.IO) {
            val total = _ayahs.value.size.coerceAtLeast(
                if (surahId in 1..114) com.example.audio.SURAH_AYAH_COUNTS[surahId - 1] else 1
            )
            val audioDir = java.io.File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_MUSIC), "quran_ayahs_audio/${reciter.path}")
            val surahStr = String.format(Locale.US, "%03d", surahId)
            
            var existingCount = 0
            for (i in 1..total) {
                val ayahStr = String.format(Locale.US, "%03d", i)
                val f = java.io.File(audioDir, "$surahStr$ayahStr.mp3")
                if (f.exists() && f.length() > 0) {
                    existingCount++
                }
            }
            _isCurrentSurahDownloaded.value = (existingCount == total && total > 0)
        }
    }

    fun downloadSurahAudio(context: Context) {
        val sId = _surahId.value
        val reciter = currentReciter.value
        val sName = _surahName.value.ifEmpty { "سورة $sId" }
        val total = _ayahs.value.size.coerceAtLeast(
            if (sId in 1..114) com.example.audio.SURAH_AYAH_COUNTS[sId - 1] else 1
        )
        
        if (_downloadProgress.value?.isDownloading == true) return

        downloadJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "بدأ تنزيل صُوتيات $sName للاستماع بدون إنترنت...", android.widget.Toast.LENGTH_SHORT).show()
                }

                val audioDir = java.io.File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_MUSIC), "quran_ayahs_audio/${reciter.path}")
                if (!audioDir.exists()) audioDir.mkdirs()

                val surahStr = String.format(Locale.US, "%03d", sId)
                
                _downloadProgress.value = SurahDownloadProgress(sId, reciter.id, 0, total, true)

                for (i in 1..total) {
                    if (!isActive) break
                    val ayahStr = String.format(Locale.US, "%03d", i)
                    val destFile = java.io.File(audioDir, "$surahStr$ayahStr.mp3")
                    
                    if (!destFile.exists() || destFile.length() == 0L) {
                        val urlStr = "https://everyayah.com/data/${reciter.path}/$surahStr$ayahStr.mp3"
                        val url = java.net.URL(urlStr)
                        val conn = url.openConnection() as java.net.HttpURLConnection
                        conn.connectTimeout = 10000
                        conn.readTimeout = 15000
                        conn.connect()

                        if (conn.responseCode == java.net.HttpURLConnection.HTTP_OK) {
                            conn.inputStream.use { input ->
                                java.io.FileOutputStream(destFile).use { output ->
                                    input.copyTo(output)
                                }
                            }
                        }
                    }
                    _downloadProgress.value = SurahDownloadProgress(sId, reciter.id, i, total, true)
                }

                _downloadProgress.value = null
                _isCurrentSurahDownloaded.value = true

                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "تم تنزيل صُوتيات $sName بنجاح 🟢 جاهزة للعمل بدون إنترنت", android.widget.Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                _downloadProgress.value = null
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "حدث خطأ أثناء التنزيل: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun cancelDownload(context: Context) {
        downloadJob?.cancel()
        downloadJob = null
        _downloadProgress.value = null
        checkSurahDownloaded(context)
    }

    fun deleteSurahAudio(context: Context) {
        val sId = _surahId.value
        val reciter = currentReciter.value
        val sName = _surahName.value
        viewModelScope.launch(Dispatchers.IO) {
            val audioDir = java.io.File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_MUSIC), "quran_ayahs_audio/${reciter.path}")
            val surahStr = String.format(Locale.US, "%03d", sId)
            val total = _ayahs.value.size.coerceAtLeast(
                if (sId in 1..114) com.example.audio.SURAH_AYAH_COUNTS[sId - 1] else 1
            )
            for (i in 1..total) {
                val ayahStr = String.format(Locale.US, "%03d", i)
                val f = java.io.File(audioDir, "$surahStr$ayahStr.mp3")
                if (f.exists()) f.delete()
            }
            _isCurrentSurahDownloaded.value = false
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(context, "تم حذف صُوتيات $sName المحفوظة", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun playAyah(ayahNumber: Int, continuous: Boolean = false) {
        val totalAyahs = _ayahs.value.size
        audioPlayerManager.playAyah(_surahId.value, ayahNumber, totalAyahs, continuous)
    }

    fun stopAudio() {
        audioPlayerManager.stop()
    }
    
    fun togglePlayPause() {
        audioPlayerManager.togglePlayPause()
    }

    fun playNext() {
        audioPlayerManager.playNext()
    }

    fun playPrev() {
        audioPlayerManager.playPrev()
    }

    fun setReciter(reciter: Reciter, context: Context? = null) {
        audioPlayerManager.setReciter(reciter)
        context?.let { checkSurahDownloaded(it, reciter = reciter) }
    }
    
    override fun onCleared() {
        super.onCleared()
        audioPlayerManager.stop()
    }

    class Factory(
        private val repository: QuranRepository,
        private val audioPlayerManager: AudioPlayerManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReaderViewModel::class.java)) {
                return ReaderViewModel(repository, audioPlayerManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
