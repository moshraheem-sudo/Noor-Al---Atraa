package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

data class LastReadItem(
    val surahId: Int,
    val surahName: String,
    val ayahNumber: Int,
    val timestamp: Long
)

object LastReadManager {
    private const val PREF_NAME = "quran_last_read_pref"

    fun saveLastRead(context: Context, surahId: Int, surahName: String, ayahNumber: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("surahId", surahId)
            .putString("surahName", surahName)
            .putInt("ayahNumber", ayahNumber)
            .putLong("timestamp", System.currentTimeMillis())
            .apply()
    }

    fun getLastRead(context: Context): LastReadItem? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val surahId = prefs.getInt("surahId", -1)
        if (surahId == -1) return null
        return LastReadItem(
            surahId = surahId,
            surahName = prefs.getString("surahName", "") ?: "",
            ayahNumber = prefs.getInt("ayahNumber", 1),
            timestamp = prefs.getLong("timestamp", 0L)
        )
    }
}
