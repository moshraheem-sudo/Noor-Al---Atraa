package com.example.audio

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class AudioTrack(
    val url: String,
    val title: String,
    val surahName: String = "",
    val reciterName: String = "",
    val styleName: String = "",
    val surahId: Int = 0
)

enum class AppRepeatMode {
    OFF,   // لا تكرار
    ONE,   // تكرار السورة الحالية
    ALL    // تكرار كل السور
}

object PlayerManager {
    private var controller: MediaController? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _currentTitle = MutableStateFlow("")
    val currentTitle: StateFlow<String> = _currentTitle.asStateFlow()

    private val _currentTrack = MutableStateFlow<AudioTrack?>(null)
    val currentTrack: StateFlow<AudioTrack?> = _currentTrack.asStateFlow()

    private val _playlist = MutableStateFlow<List<AudioTrack>>(emptyList())
    val playlist: StateFlow<List<AudioTrack>> = _playlist.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _repeatMode = MutableStateFlow(AppRepeatMode.OFF)
    val repeatMode: StateFlow<AppRepeatMode> = _repeatMode.asStateFlow()

    private val _sleepTimerMinutes = MutableStateFlow(0)
    val sleepTimerMinutes: StateFlow<Int> = _sleepTimerMinutes.asStateFlow()

    private val _sleepTimerRemainingSeconds = MutableStateFlow(0L)
    val sleepTimerRemainingSeconds: StateFlow<Long> = _sleepTimerRemainingSeconds.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)
    
    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var appContext: Context? = null
    private var lastSaveTime = 0L

    suspend fun initialize(context: Context) {
        if (controller != null) return
        appContext = context.applicationContext
        
        val sessionToken = SessionToken(context, ComponentName(context, AudioService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controller = controllerFuture.await()
        
        appContext?.let { restoreState(it) }
        
        controller?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startProgressUpdate()
                } else {
                    progressJob?.cancel()
                    appContext?.let { saveState(it) }
                }
            }
            
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _currentTitle.value = mediaItem?.mediaMetadata?.title?.toString() ?: ""
                _duration.value = controller?.duration?.coerceAtLeast(0L) ?: 0L
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    handlePlaybackEnded()
                }
            }
        })
    }

    private fun handlePlaybackEnded() {
        when (_repeatMode.value) {
            AppRepeatMode.ONE -> {
                controller?.seekTo(0)
                controller?.play()
            }
            AppRepeatMode.ALL -> {
                playNext(forceLoop = true)
            }
            AppRepeatMode.OFF -> {
                if (_currentIndex.value + 1 < _playlist.value.size) {
                    playNext(forceLoop = false)
                } else {
                    controller?.seekTo(0)
                    controller?.pause()
                }
            }
        }
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                _currentPosition.value = controller?.currentPosition ?: 0L
                _duration.value = controller?.duration?.coerceAtLeast(0L) ?: 0L
                
                val now = System.currentTimeMillis()
                if (now - lastSaveTime > 5000) {
                    appContext?.let { saveState(it) }
                    lastSaveTime = now
                }
                
                delay(1000)
            }
        }
    }

    fun playTrack(track: AudioTrack, queue: List<AudioTrack> = emptyList()) {
        if (queue.isEmpty() && _playlist.value.isNotEmpty()) {
            val existingIndex = _playlist.value.indexOfFirst { 
                it.url == track.url || (it.surahId == track.surahId && it.reciterName == track.reciterName && track.surahId != 0) 
            }
            if (existingIndex >= 0) {
                playTrackAtIndex(existingIndex)
                return
            }
        }
        val list = if (queue.isNotEmpty()) queue else listOf(track)
        val index = list.indexOf(track).let { if (it >= 0) it else 0 }
        playList(list, index)
    }

    fun play(url: String, title: String, reciterName: String = "", styleName: String = "", surahId: Int = 0) {
        val track = AudioTrack(
            url = url,
            title = title,
            surahName = title,
            reciterName = reciterName,
            styleName = styleName,
            surahId = surahId
        )
        playTrack(track)
    }

    fun playList(tracks: List<AudioTrack>, startIndex: Int) {
        if (tracks.isEmpty()) return
        _playlist.value = tracks
        val index = startIndex.coerceIn(0, tracks.size - 1)
        playTrackAtIndex(index)
    }

    private fun playTrackAtIndex(index: Int) {
        // Stop Ayah highlighting audio if it is playing
        AyahAudioState.manager?.stop()

        val list = _playlist.value
        if (index !in list.indices) return
        _currentIndex.value = index
        val track = list[index]
        _currentTrack.value = track
        _currentTitle.value = track.title
        
        val artistName = if (track.reciterName.isNotBlank()) "${track.reciterName} • مشغل القرآن" else "مشغل القرآن"
        val mediaItem = MediaItem.Builder()
            .setUri(track.url)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(artistName)
                    .setAlbumTitle("مشغل القرآن")
                    .setDisplayTitle(track.title)
                    .build()
            )
            .build()
            
        controller?.setMediaItem(mediaItem)
        controller?.prepare()
        controller?.play()
        
        appContext?.let { saveState(it) }
    }

    fun playNext(forceLoop: Boolean = false) {
        val list = _playlist.value
        if (list.isEmpty()) return
        var nextIndex = _currentIndex.value + 1
        if (nextIndex >= list.size) {
            if (_repeatMode.value == AppRepeatMode.ALL || forceLoop) {
                nextIndex = 0
            } else {
                return
            }
        }
        playTrackAtIndex(nextIndex)
    }

    fun playPrev() {
        val list = _playlist.value
        if (list.isEmpty()) return
        if (_currentPosition.value > 3000L) {
            seekTo(0)
            return
        }
        var prevIndex = _currentIndex.value - 1
        if (prevIndex < 0) {
            prevIndex = if (_repeatMode.value == AppRepeatMode.ALL) list.size - 1 else 0
        }
        playTrackAtIndex(prevIndex)
    }

    fun pause() {
        controller?.pause()
    }

    fun togglePlayPause() {
        controller?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                AyahAudioState.manager?.stop()
                it.play()
            }
        }
    }
    
    fun seekTo(position: Long) {
        controller?.seekTo(position)
    }

    fun fastForward(millis: Long = 10000L) {
        controller?.let {
            val maxDur = it.duration.coerceAtLeast(0L)
            val newPos = (it.currentPosition + millis).coerceAtMost(maxDur)
            it.seekTo(newPos)
        }
    }

    fun rewind(millis: Long = 10000L) {
        controller?.let {
            val newPos = (it.currentPosition - millis).coerceAtLeast(0L)
            it.seekTo(newPos)
        }
    }

    fun toggleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            AppRepeatMode.OFF -> AppRepeatMode.ONE
            AppRepeatMode.ONE -> AppRepeatMode.ALL
            AppRepeatMode.ALL -> AppRepeatMode.OFF
        }
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _sleepTimerMinutes.value = 0
            _sleepTimerRemainingSeconds.value = 0L
            return
        }
        
        _sleepTimerMinutes.value = minutes
        val totalSecs = minutes * 60L
        _sleepTimerRemainingSeconds.value = totalSecs

        sleepTimerJob = scope.launch {
            var remaining = totalSecs
            while (remaining > 0) {
                delay(1000)
                remaining--
                _sleepTimerRemainingSeconds.value = remaining
            }
            controller?.pause()
            _sleepTimerMinutes.value = 0
            _sleepTimerRemainingSeconds.value = 0L
        }
    }

    fun formatTime(ms: Long): String {
        if (ms <= 0) return "00:00"
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun formatCountdown(seconds: Long): String {
        if (seconds <= 0) return "00:00"
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }

    fun closePlayer() {
        stopAndDismiss()
    }

    fun stopAndDismiss() {
        try {
            controller?.stop()
            controller?.clearMediaItems()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _isPlaying.value = false
        _currentTrack.value = null
        _currentTitle.value = ""
        _playlist.value = emptyList()
        _currentPosition.value = 0L
        _duration.value = 0L
        appContext?.let { ctx ->
            saveState(ctx)
            try {
                val stopIntent = Intent(ctx, AudioService::class.java).apply {
                    action = AudioService.ACTION_STOP_SERVICE
                }
                ctx.startService(stopIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun release() {
        progressJob?.cancel()
        sleepTimerJob?.cancel()
        if (!_isPlaying.value) {
            // Dismiss audio service and notification when releasing while stopped/paused
            appContext?.let { ctx ->
                try {
                    val stopIntent = Intent(ctx, AudioService::class.java).apply {
                        action = AudioService.ACTION_STOP_SERVICE
                    }
                    ctx.startService(stopIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        controller?.release()
        controller = null
    }

    private fun saveState(context: Context) {
        val prefs = context.getSharedPreferences("PlayerManager_State", Context.MODE_PRIVATE)
        val track = _currentTrack.value
        if (track == null) {
            prefs.edit().clear().apply()
            return
        }
        
        val jsonArray = JSONArray()
        for (t in _playlist.value) {
            val obj = JSONObject()
            obj.put("url", t.url)
            obj.put("title", t.title)
            obj.put("surahName", t.surahName)
            obj.put("reciterName", t.reciterName)
            obj.put("styleName", t.styleName)
            obj.put("surahId", t.surahId)
            jsonArray.put(obj)
        }
        
        prefs.edit()
            .putString("playlist", jsonArray.toString())
            .putInt("currentIndex", _currentIndex.value)
            .putLong("currentPosition", _currentPosition.value)
            .apply()
    }

    private fun restoreState(context: Context) {
        val prefs = context.getSharedPreferences("PlayerManager_State", Context.MODE_PRIVATE)
        val playlistStr = prefs.getString("playlist", null) ?: return
        val savedIndex = prefs.getInt("currentIndex", 0)
        val savedPosition = prefs.getLong("currentPosition", 0L)
        
        try {
            val jsonArray = JSONArray(playlistStr)
            val list = mutableListOf<AudioTrack>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    AudioTrack(
                        url = obj.getString("url"),
                        title = obj.getString("title"),
                        surahName = obj.optString("surahName", ""),
                        reciterName = obj.optString("reciterName", ""),
                        styleName = obj.optString("styleName", ""),
                        surahId = obj.optInt("surahId", 0)
                    )
                )
            }
            
            if (list.isNotEmpty()) {
                _playlist.value = list
                _currentIndex.value = savedIndex.coerceIn(0, list.size - 1)
                val track = list[_currentIndex.value]
                _currentTrack.value = track
                _currentTitle.value = track.title
                
                val artistName = if (track.reciterName.isNotBlank()) "${track.reciterName} • مشغل القرآن" else "مشغل القرآن"
                val mediaItem = MediaItem.Builder()
                    .setUri(track.url)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(track.title)
                            .setArtist(artistName)
                            .setAlbumTitle("مشغل القرآن")
                            .setDisplayTitle(track.title)
                            .build()
                    )
                    .build()
                
                controller?.setMediaItem(mediaItem)
                controller?.prepare()
                controller?.seekTo(savedPosition)
                _currentPosition.value = savedPosition
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

