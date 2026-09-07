package com.example.audio

import android.content.Context

object AyahAudioState {
    private var _manager: AudioPlayerManager? = null
    var manager: AudioPlayerManager?
        get() = _manager
        set(value) { _manager = value }
    
    fun getOrCreate(context: Context): AudioPlayerManager {
        val existing = _manager
        if (existing != null) return existing
        val newManager = AudioPlayerManager().apply {
            initContext(context.applicationContext)
        }
        _manager = newManager
        return newManager
    }
    
    fun togglePlayPause() { manager?.togglePlayPause() }
    fun playNext() { manager?.playNext() }
    fun playPrev() { manager?.playPrev() }
    fun stop() { manager?.stop() }
}
