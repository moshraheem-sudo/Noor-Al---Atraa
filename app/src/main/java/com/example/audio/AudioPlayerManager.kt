package com.example.audio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.graphics.BitmapFactory
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.example.data.local.LastReadManager
import com.example.R

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class Reciter(val id: String, val name: String, val path: String)


val SURAH_NAMES_AR = listOf(
    "الفاتحة", "البقرة", "آل عمران", "النساء", "المائدة", "الأنعام", "الأعراف", "الأنفال", "التوبة", "يونس",
    "هود", "يوسف", "الرعد", "إبراهيم", "الحجر", "النحل", "الإسراء", "الكهف", "مريم", "طه",
    "الأنبياء", "الحج", "المؤمنون", "النور", "الفرقان", "الشعراء", "النمل", "القصص", "العنكبوت", "الروم",
    "لقمان", "السجدة", "الأحزاب", "سبأ", "فاطر", "يس", "الصافات", "ص", "الزمر", "غافر",
    "فصلت", "الشورى", "الزخرف", "الدخان", "الجاثية", "الأحقاف", "محمد", "الفتح", "الحجرات", "ق",
    "الذاريات", "الطور", "النجم", "القمر", "الرحمن", "الواقعة", "الحديد", "المجادلة", "الحشر", "الممتحنة",
    "الصف", "الجمعة", "المنافقون", "التغابن", "الطلاق", "التحريم", "الملك", "القلم", "الحاقة", "المعارج",
    "نوح", "الجن", "المزمل", "المدثر", "القيامة", "الإنسان", "المرسلات", "النبأ", "النازعات", "عبس",
    "التكوير", "الانفطار", "المطففين", "الانشقاق", "البروج", "الطارق", "الأعلى", "الغاشية", "الفجر", "البلد",
    "الشمس", "الليل", "الضحى", "الشرح", "التين", "العلق", "القدر", "البينة", "الزلزلة", "العاديات",
    "القارعة", "التكاثر", "العصر", "الهمزة", "الفيل", "قريش", "الماعون", "الكوثر", "الكافرون", "النصر",
    "المسد", "الإخلاص", "الفلق", "الناس"
)

val AVAILABLE_RECITERS = listOf(
    Reciter("alafasy", "مشاري العفاسي", "Alafasy_128kbps"),
    Reciter("abdulbasit", "عبد الباسط عبد الصمد", "Abdul_Basit_Murattal_192kbps"),
    Reciter("husary", "محمود خليل الحصري", "Husary_128kbps"),
    Reciter("minshawy", "محمد صديق المنشاوي", "Minshawy_Murattal_128kbps"),
    Reciter("maher", "ماهر المعيقلي", "MaherAlMuaiqly128kbps"),
    Reciter("shatri", "أبو بكر الشاطري", "Abu_Bakr_Ash-Shaatree_128kbps")
)

val SURAH_AYAH_COUNTS = intArrayOf(
    7, 286, 200, 176, 120, 165, 206, 75, 129, 109,
    123, 111, 43, 52, 99, 128, 111, 110, 98, 135,
    112, 78, 118, 64, 77, 227, 93, 88, 69, 60,
    34, 30, 73, 54, 45, 83, 182, 88, 75, 85,
    54, 53, 89, 59, 37, 35, 38, 29, 18, 45,
    60, 49, 62, 55, 78, 96, 29, 22, 24, 13,
    14, 11, 11, 18, 12, 12, 30, 52, 52, 44,
    28, 28, 20, 56, 40, 31, 50, 40, 46, 42,
    29, 19, 36, 25, 22, 17, 19, 26, 30, 20,
    15, 21, 11, 8, 8, 19, 5, 8, 8, 11,
    11, 8, 3, 9, 5, 4, 7, 3, 6, 3,
    6, 4, 5, 6
)

class AudioPlayerManager {
    var onSingleAyahComplete: (() -> Unit)? = null
    
    private var mediaPlayer: MediaPlayer? = null
    private var applicationContext: android.content.Context? = null

    fun initContext(context: android.content.Context) {
        applicationContext = context.applicationContext
        AyahAudioState.manager = this
    }

    private val _currentPlayingAyah = MutableStateFlow<Pair<Int, Int>?>(null) // SurahId, AyahNumber
    val currentPlayingAyah: StateFlow<Pair<Int, Int>?> = _currentPlayingAyah.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentReciter = MutableStateFlow(AVAILABLE_RECITERS.first())
    val currentReciter: StateFlow<Reciter> = _currentReciter.asStateFlow()

    private var currentSurahId: Int = 1
    private var currentAyahNumber: Int = 1
    private var maxAyahs: Int = 1
    private var isContinuousMode = false

    fun setReciter(reciter: Reciter) {
        _currentReciter.value = reciter
    }

    fun playAyah(surahId: Int, ayahNumber: Int, totalAyahs: Int = 0, continuous: Boolean = false) {
        stop()
        currentSurahId = surahId
        currentAyahNumber = ayahNumber
        maxAyahs = if (surahId in 1..114) SURAH_AYAH_COUNTS[surahId - 1] else totalAyahs
        isContinuousMode = continuous
        
        playCurrent()
    }

    fun playNext() {
        if (currentAyahNumber < maxAyahs) {
            currentAyahNumber++
            playCurrent()
        } else if (currentSurahId < 114) {
            currentSurahId++
            currentAyahNumber = 1
            maxAyahs = SURAH_AYAH_COUNTS[currentSurahId - 1]
            playCurrent()
        }
    }

    fun playPrev() {
        if (currentAyahNumber > 1) {
            currentAyahNumber--
            playCurrent()
        } else if (currentSurahId > 1) {
            currentSurahId--
            maxAyahs = SURAH_AYAH_COUNTS[currentSurahId - 1]
            currentAyahNumber = maxAyahs
            playCurrent()
        }
    }
    
    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            resume()
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
            }
        }
    }

    fun resume() {
        // Stop/pause background reciters player if active
        PlayerManager.pause()

        mediaPlayer?.let {
            it.start()
            _isPlaying.value = true
        } ?: run {
            if (_currentPlayingAyah.value != null) {
                playCurrent()
            }
        }
    }

    private fun playCurrent() {
        // Stop/pause background reciters player so both don't play together
        PlayerManager.pause()

        _currentPlayingAyah.value = Pair(currentSurahId, currentAyahNumber)
        applicationContext?.let { ctx ->
            val surahName = SURAH_NAMES_AR.getOrElse(currentSurahId - 1) { "" }
            LastReadManager.saveLastRead(ctx, currentSurahId, surahName, currentAyahNumber)
        }
        _isPlaying.value = true
        
        val surahStr = String.format(Locale.US, "%03d", currentSurahId)
        val ayahStr = String.format(Locale.US, "%03d", currentAyahNumber)
        val reciterPath = _currentReciter.value.path
        val url = "https://everyayah.com/data/$reciterPath/$surahStr$ayahStr.mp3"

        val localFile = applicationContext?.let { ctx ->
            val audioDir = java.io.File(ctx.getExternalFilesDir(android.os.Environment.DIRECTORY_MUSIC), "quran_ayahs_audio/$reciterPath")
            java.io.File(audioDir, "$surahStr$ayahStr.mp3")
        }

        val dataSource = if (localFile != null && localFile.exists() && localFile.length() > 0) {
            localFile.absolutePath
        } else {
            url
        }

        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(dataSource)
                setOnPreparedListener { 
                    it.start() 
                }
                setOnCompletionListener {
                    if (isContinuousMode) {
                        if (currentAyahNumber < maxAyahs) {
                            currentAyahNumber++
                            playCurrent()
                        } else if (currentSurahId < 114) {
                            currentSurahId++
                            currentAyahNumber = 1
                            maxAyahs = SURAH_AYAH_COUNTS[currentSurahId - 1]
                            playCurrent()
                        } else {
                            stop()
                        }
                    } else {
                        stop()
                        onSingleAyahComplete?.invoke()
                    }
                }
                setOnErrorListener { _, _, _ ->
                    stop()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stop()
        }
    }

    fun stop() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            // Ignore
        }
        mediaPlayer = null
        _isPlaying.value = false
        _currentPlayingAyah.value = null
    }
}
