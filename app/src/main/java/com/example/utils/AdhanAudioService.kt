package com.example.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.AdhanSoundMode
import com.example.data.model.Muezzin
import com.example.data.model.PrayerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AdhanPlaybackStatus {
    IDLE,
    BUFFERING,
    PLAYING,
    ERROR
}

data class AdhanPlaybackState(
    val status: AdhanPlaybackStatus = AdhanPlaybackStatus.IDLE,
    val currentMuezzinId: String? = null,
    val prayerName: String? = null,
    val isPreview: Boolean = false,
    val errorMessage: String? = null
)

class AdhanAudioService : Service(), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    companion object {
        private const val TAG = "AdhanAudioService"
        const val CHANNEL_ID = "adhan_audio_streaming_channel"
        const val NOTIFICATION_ID = 9001

        const val ACTION_PLAY_ADHAN = "com.example.action.PLAY_ADHAN"
        const val ACTION_PREVIEW_MUEZZIN = "com.example.action.PREVIEW_MUEZZIN"
        const val ACTION_STOP_ADHAN = "com.example.action.STOP_ADHAN"

        const val EXTRA_MUEZZIN_ID = "extra_muezzin_id"
        const val EXTRA_PRAYER_TYPE = "extra_prayer_type"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_CITY_NAME = "extra_city_name"
        const val EXTRA_TIME_STR = "extra_time_str"
        const val EXTRA_SOUND_MODE = "extra_sound_mode"
        const val EXTRA_VOLUME_PERCENT = "extra_volume_percent"

        private val _playbackState = MutableStateFlow(AdhanPlaybackState())
        val playbackState: StateFlow<AdhanPlaybackState> = _playbackState.asStateFlow()

        fun isPlaying(): Boolean = _playbackState.value.status == AdhanPlaybackStatus.PLAYING

        fun startAdhan(
            context: Context,
            muezzin: Muezzin,
            prayerType: PrayerType,
            cityName: String,
            timeFormatted: String,
            soundMode: AdhanSoundMode = AdhanSoundMode.FULL_ADHAN,
            volumePercent: Int = 100
        ) {
            val intent = Intent(context, AdhanAudioService::class.java).apply {
                action = ACTION_PLAY_ADHAN
                putExtra(EXTRA_MUEZZIN_ID, muezzin.id)
                putExtra(EXTRA_PRAYER_TYPE, prayerType.name)
                putExtra(EXTRA_PRAYER_NAME, prayerType.arName)
                putExtra(EXTRA_CITY_NAME, cityName)
                putExtra(EXTRA_TIME_STR, timeFormatted)
                putExtra(EXTRA_SOUND_MODE, soundMode.id)
                putExtra(EXTRA_VOLUME_PERCENT, volumePercent)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start AdhanAudioService: ${e.message}")
                PrayerNotificationHelper.showPrayerNotification(
                    context = context,
                    prayerType = prayerType,
                    timeFormatted = timeFormatted,
                    cityName = cityName,
                    playAlertSound = true
                )
                AudioPlayerHelper.vibratePattern(context)
            }
        }

        fun previewMuezzin(context: Context, muezzin: Muezzin, soundMode: AdhanSoundMode = AdhanSoundMode.FULL_ADHAN, volumePercent: Int = 100) {
            val intent = Intent(context, AdhanAudioService::class.java).apply {
                action = ACTION_PREVIEW_MUEZZIN
                putExtra(EXTRA_MUEZZIN_ID, muezzin.id)
                putExtra(EXTRA_SOUND_MODE, soundMode.id)
                putExtra(EXTRA_VOLUME_PERCENT, volumePercent)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to preview muezzin: ${e.message}")
            }
        }

        fun stopAdhan(context: Context) {
            val intent = Intent(context, AdhanAudioService::class.java).apply {
                action = ACTION_STOP_ADHAN
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop Adhan: ${e.message}")
            }
        }

        fun isOnline(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null

    private var currentMuezzin: Muezzin = Muezzin.defaultMuezzin
    private var currentPrayerName: String = ""
    private var currentPrayerType: PrayerType? = null
    private var currentCityName: String = ""
    private var currentTimeFormatted: String = ""
    private var isCurrentPreview: Boolean = false
    private var currentSoundMode: AdhanSoundMode = AdhanSoundMode.FULL_ADHAN
    private var currentVolumeRatio: Float = 1.0f

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        acquireWakeLock()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val soundModeId = intent?.getStringExtra(EXTRA_SOUND_MODE) ?: AdhanSoundMode.FULL_ADHAN.id
        currentSoundMode = AdhanSoundMode.fromId(soundModeId)
        val volPct = intent?.getIntExtra(EXTRA_VOLUME_PERCENT, 100) ?: 100
        currentVolumeRatio = (volPct.coerceIn(5, 100) / 100f)

        when (intent?.action) {
            ACTION_PLAY_ADHAN -> {
                val muezzinId = intent.getStringExtra(EXTRA_MUEZZIN_ID)
                currentMuezzin = Muezzin.fromId(muezzinId)
                currentPrayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "المغرب"
                val prayerTypeName = intent.getStringExtra(EXTRA_PRAYER_TYPE)
                currentPrayerType = prayerTypeName?.let { runCatching { PrayerType.valueOf(it) }.getOrNull() }
                currentCityName = intent.getStringExtra(EXTRA_CITY_NAME) ?: ""
                currentTimeFormatted = intent.getStringExtra(EXTRA_TIME_STR) ?: ""
                isCurrentPreview = false

                if (currentSoundMode == AdhanSoundMode.VIBRATE_ONLY) {
                    AudioPlayerHelper.vibratePattern(this)
                    stopSelf()
                    return START_NOT_STICKY
                } else if (currentSoundMode == AdhanSoundMode.BEEP_ALERT) {
                    AudioPlayerHelper.playBeep()
                    AudioPlayerHelper.vibratePattern(this)
                    stopSelf()
                    return START_NOT_STICKY
                }

                val title = if (currentSoundMode == AdhanSoundMode.SHORT_TAKBIR) "تكبيرات أذان ${currentPrayerName}" else "أذان ${currentPrayerName}"
                val text = "بصوت ${currentMuezzin.nameAr}"

                val notification = buildPlayingNotification(
                    muezzin = currentMuezzin,
                    title = title,
                    text = text
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }

                playAudio(muezzin = currentMuezzin, isPreview = false)
            }
            ACTION_PREVIEW_MUEZZIN -> {
                val muezzinId = intent.getStringExtra(EXTRA_MUEZZIN_ID)
                currentMuezzin = Muezzin.fromId(muezzinId)
                currentPrayerName = "المغرب"
                isCurrentPreview = true

                if (currentSoundMode == AdhanSoundMode.VIBRATE_ONLY) {
                    AudioPlayerHelper.vibratePattern(this)
                    stopSelf()
                    return START_NOT_STICKY
                } else if (currentSoundMode == AdhanSoundMode.BEEP_ALERT) {
                    AudioPlayerHelper.playBeep()
                    AudioPlayerHelper.vibratePattern(this)
                    stopSelf()
                    return START_NOT_STICKY
                }

                val title = if (currentSoundMode == AdhanSoundMode.SHORT_TAKBIR) "معاينة تكبيرات ${currentPrayerName}" else "معاينة أذان ${currentPrayerName}"
                val text = "بصوت ${currentMuezzin.nameAr}"

                val notification = buildPlayingNotification(
                    muezzin = currentMuezzin,
                    title = title,
                    text = text
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }

                playAudio(muezzin = currentMuezzin, isPreview = true)
            }
            ACTION_STOP_ADHAN -> {
                stopPlayback()
                stopSelf()
            }
            else -> {
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun playAudio(muezzin: Muezzin, isPreview: Boolean) {
        val isDownloaded = MuezzinDownloadManager.isAudioDownloaded(this, muezzin)
        val localFile = MuezzinDownloadManager.getAudioFile(this, muezzin)
        val online = isOnline(this)

        // If offline and selected muezzin is not yet downloaded, check if ANY other muezzin is available locally
        val fallbackMuezzin = if (!isDownloaded && !online) {
            Muezzin.values().firstOrNull { MuezzinDownloadManager.isAudioDownloaded(this, it) }
        } else {
            null
        }

        if (!isDownloaded && !online && fallbackMuezzin == null) {
            _playbackState.value = AdhanPlaybackState(
                status = AdhanPlaybackStatus.ERROR,
                currentMuezzinId = muezzin.id,
                prayerName = currentPrayerName,
                isPreview = isPreview,
                errorMessage = "الملف الصوتي غير محمل ولا يوجد اتصال بالإنترنت"
            )
            if (!isPreview) {
                playFallbackSystemAlarm()
                return
            }
            stopSelf()
            return
        }

        val effectiveMuezzin = fallbackMuezzin ?: muezzin
        val effectiveLocalFile = if (fallbackMuezzin != null) {
            MuezzinDownloadManager.getAudioFile(this, fallbackMuezzin)
        } else {
            localFile
        }
        val effectiveIsDownloaded = fallbackMuezzin != null || isDownloaded

        _playbackState.value = AdhanPlaybackState(
            status = AdhanPlaybackStatus.BUFFERING,
            currentMuezzinId = effectiveMuezzin.id,
            prayerName = currentPrayerName,
            isPreview = isPreview
        )

        try {
            acquireWakeLock()
            requestAudioFocus()

            // Ensure alarm stream volume is audible on device for actual prayer adhan
            if (!isPreview) {
                try {
                    val maxAlarmVol = audioManager?.getStreamMaxVolume(AudioManager.STREAM_ALARM) ?: 7
                    val currentAlarmVol = audioManager?.getStreamVolume(AudioManager.STREAM_ALARM) ?: 0
                    val minRequiredVol = (maxAlarmVol * 0.60f).toInt().coerceAtLeast(1)
                    if (currentAlarmVol < minRequiredVol && maxAlarmVol > 0) {
                        audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, minRequiredVol, 0)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Could not adjust alarm stream volume: ${e.message}")
                }
            }

            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(if (isPreview) AudioAttributes.USAGE_MEDIA else AudioAttributes.USAGE_ALARM)
                        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                        .build()
                )
                setVolume(currentVolumeRatio, currentVolumeRatio)
                setOnCompletionListener(this@AdhanAudioService)
                setOnErrorListener(this@AdhanAudioService)

                if (effectiveIsDownloaded && effectiveLocalFile.exists()) {
                    setDataSource(effectiveLocalFile.absolutePath)
                    // Synchronous prepare for local files - zero latency, instant start at exact second!
                    prepare()
                    this@AdhanAudioService.onPrepared(this)
                } else {
                    setDataSource(effectiveMuezzin.audioUrl)
                    // Trigger background caching for next time
                    MuezzinDownloadManager.downloadMuezzin(this@AdhanAudioService, effectiveMuezzin)
                    setOnPreparedListener(this@AdhanAudioService)
                    prepareAsync()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initiating MediaPlayer: ${e.message}")
            handlePlaybackError("تعذر تشغيل ملف الأذان")
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        try {
            mp?.setVolume(currentVolumeRatio, currentVolumeRatio)
            mp?.start()
            _playbackState.value = AdhanPlaybackState(
                status = AdhanPlaybackStatus.PLAYING,
                currentMuezzinId = currentMuezzin.id,
                prayerName = currentPrayerName,
                isPreview = isCurrentPreview
            )

            // If SHORT_TAKBIR mode is selected, schedule auto-stop after 25 seconds (Takbeerat length)
            if (currentSoundMode == AdhanSoundMode.SHORT_TAKBIR) {
                CoroutineScope(Dispatchers.Default).launch {
                    delay(25000)
                    if (_playbackState.value.status == AdhanPlaybackStatus.PLAYING) {
                        stopPlayback()
                        stopSelf()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting MediaPlayer: ${e.message}")
            handlePlaybackError("تعذر بدء تشغيل الصوت")
        }
    }

    private fun playFallbackSystemAlarm() {
        if (isCurrentPreview) {
            handlePlaybackError("تعذر تشغيل الصوت")
            return
        }
        try {
            acquireWakeLock()
            requestAudioFocus()
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                        .build()
                )
                setVolume(currentVolumeRatio, currentVolumeRatio)
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                setDataSource(applicationContext, alarmUri)
                isLooping = true
                setOnCompletionListener(this@AdhanAudioService)
                prepare()
                start()
            }

            _playbackState.value = AdhanPlaybackState(
                status = AdhanPlaybackStatus.PLAYING,
                currentMuezzinId = currentMuezzin.id,
                prayerName = currentPrayerName,
                isPreview = false
            )

            // Stop fallback after 30 seconds
            CoroutineScope(Dispatchers.Default).launch {
                delay(30000)
                if (_playbackState.value.status == AdhanPlaybackStatus.PLAYING) {
                    stopPlayback()
                    stopSelf()
                }
            }
            AudioPlayerHelper.vibratePattern(this)
        } catch (e: Exception) {
            Log.e(TAG, "Fallback alarm failed: ${e.message}")
            handlePlaybackError("تعذر تشغيل الصوت")
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (!isCurrentPreview && currentPrayerType != null) {
            try {
                PrayerNotificationHelper.showPrayerNotification(
                    context = applicationContext,
                    prayerType = currentPrayerType!!,
                    timeFormatted = currentTimeFormatted,
                    cityName = currentCityName,
                    playAlertSound = false,
                    repeatIteration = 0,
                    offsetMinutes = 0
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error posting completion notification: ${e.message}")
            }
        }
        stopPlayback()
        stopSelf()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
        if (!isCurrentPreview) {
            playFallbackSystemAlarm()
        } else {
            handlePlaybackError("حدث خطأ أثناء تشغيل الأذان")
        }
        return true
    }

    private fun handlePlaybackError(message: String) {
        _playbackState.value = AdhanPlaybackState(
            status = AdhanPlaybackStatus.ERROR,
            currentMuezzinId = currentMuezzin.id,
            prayerName = currentPrayerName,
            isPreview = isCurrentPreview,
            errorMessage = message
        )
        if (!isCurrentPreview) {
            currentPrayerType?.let { pt ->
                try {
                    PrayerNotificationHelper.showPrayerNotification(
                        context = applicationContext,
                        prayerType = pt,
                        timeFormatted = currentTimeFormatted,
                        cityName = currentCityName,
                        playAlertSound = true
                    )
                } catch (e: Exception) {
                    // ignore
                }
            }
            AudioPlayerHelper.playBeep()
            AudioPlayerHelper.vibratePattern(this)
        }
        stopPlayback()
        stopSelf()
    }

    private fun stopPlayback() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            // ignore
        }
        abandonAudioFocus()
        releaseWakeLock()
        _playbackState.value = AdhanPlaybackState(status = AdhanPlaybackStatus.IDLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun requestAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val playbackAttributes = AudioAttributes.Builder()
                    .setUsage(if (isCurrentPreview) AudioAttributes.USAGE_MEDIA else AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setOnAudioFocusChangeListener { focusChange ->
                        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                            stopPlayback()
                            stopSelf()
                        }
                    }
                    .build()
                focusRequest?.let { audioManager?.requestAudioFocus(it) }
            } else {
                @Suppress("DEPRECATION")
                audioManager?.requestAudioFocus(
                    { focusChange ->
                        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                            stopPlayback()
                            stopSelf()
                        }
                    },
                    if (isCurrentPreview) AudioManager.STREAM_MUSIC else AudioManager.STREAM_ALARM,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                )
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun abandonAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                focusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                audioManager?.abandonAudioFocus(null)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock == null) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
                wakeLock = powerManager?.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "PrayerTimes:AdhanAudioWakeLock"
                )
            }
            wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes max to guarantee full Adhan completion
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "تشغيل صوت الأذان والمؤذن",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعار التحكم في تشغيل صوت الأذان للمؤذن المختار"
                setSound(null, null)
                enableVibration(false)
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildPlayingNotification(
        muezzin: Muezzin,
        title: String,
        text: String
    ): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AdhanAudioService::class.java).apply {
            action = ACTION_STOP_ADHAN
        }

        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val appIconBitmap = try {
            BitmapFactory.decodeResource(resources, R.drawable.ic_app_icon)
        } catch (e: Exception) {
            null
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_prayer)
            .apply {
                if (appIconBitmap != null) {
                    setLargeIcon(appIconBitmap)
                }
            }
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(text)
            )
            .setContentIntent(openPendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                "⏹ إيقاف الأذان",
                stopPendingIntent
            )
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(0xFF14B8A6.toInt())
            .setSilent(true)
            .setSound(null)
            .build()
    }

    override fun onDestroy() {
        stopPlayback()
        super.onDestroy()
    }
}
