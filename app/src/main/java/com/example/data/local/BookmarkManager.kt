package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class QuranBookmark(
    val id: String,
    val surahId: Int,
    val surahName: String,
    val ayahNumber: Int,
    val ayahText: String,
    val timestamp: Long
)

object BookmarkManager {
    private const val PREF_NAME = "quran_bookmarks_pref"
    private const val KEY_BOOKMARKS = "bookmarks_json"

    fun loadBookmarks(context: Context): List<QuranBookmark> {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonStr = sharedPrefs.getString(KEY_BOOKMARKS, "[]") ?: "[]"
        val list = mutableListOf<QuranBookmark>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    QuranBookmark(
                        id = obj.getString("id"),
                        surahId = obj.getInt("surahId"),
                        surahName = obj.getString("surahName"),
                        ayahNumber = obj.getInt("ayahNumber"),
                        ayahText = obj.getString("ayahText"),
                        timestamp = obj.getLong("timestamp")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list.sortedByDescending { it.timestamp }
    }

    fun saveBookmarks(context: Context, bookmarks: List<QuranBookmark>) {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val array = JSONArray()
        bookmarks.forEach { b ->
            val obj = JSONObject().apply {
                put("id", b.id)
                put("surahId", b.surahId)
                put("surahName", b.surahName)
                put("ayahNumber", b.ayahNumber)
                put("ayahText", b.ayahText)
                put("timestamp", b.timestamp)
            }
            array.put(obj)
        }
        sharedPrefs.edit().putString(KEY_BOOKMARKS, array.toString()).apply()
    }
}
