package com.example.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class AppThemeMode(
    val id: String,
    val titleAr: String,
    val subtitleAr: String,
    val primaryColor: Color,
    val backgroundColor: Color,
    val isDark: Boolean
) {
    ROYAL_DARK(
        id = "royal_dark",
        titleAr = "الداكن الملكي",
        subtitleAr = "الأسود الملكي الفاخر مع لمسات الذهب",
        primaryColor = Color(0xFFD4AF37),
        backgroundColor = Color(0xFF0F1115),
        isDark = true
    ),
    EMERALD_DARK(
        id = "emerald_dark",
        titleAr = "الزمردي الذهبي",
        subtitleAr = "الثيم الفخم باللون الأخضر الغامق والذهبي الملكي",
        primaryColor = Color(0xFF52B788),
        backgroundColor = Color(0xFF061A13),
        isDark = true
    ),
    ICE_BLUE_LIGHT(
        id = "ice_blue_light",
        titleAr = "السماوي الفضي (فاتح هادئ)",
        subtitleAr = "ثيم السحاب الهادئ باللون السماوي الناعم والأزرق الملكي",
        primaryColor = Color(0xFF2B5282),
        backgroundColor = Color(0xFFE8EFF8),
        isDark = false
    )
}

object ThemeManager {
    var currentThemeMode by mutableStateOf(AppThemeMode.ICE_BLUE_LIGHT)
        private set

    fun init(context: Context) {
        val ahlPrefs = context.getSharedPreferences("AhlAlBaytPrefs", Context.MODE_PRIVATE)
        val selectedBg = ahlPrefs.getString("selected_bg_color", "ice_blue")
        currentThemeMode = when (selectedBg) {
            "dark_night" -> AppThemeMode.ROYAL_DARK
            "emerald_green" -> AppThemeMode.EMERALD_DARK
            else -> AppThemeMode.ICE_BLUE_LIGHT
        }
    }

    fun setTheme(context: Context, mode: AppThemeMode) {
        currentThemeMode = mode
        val ahlPrefs = context.getSharedPreferences("AhlAlBaytPrefs", Context.MODE_PRIVATE)
        val bgValue = when (mode) {
            AppThemeMode.ROYAL_DARK -> "dark_night"
            AppThemeMode.EMERALD_DARK -> "emerald_green"
            AppThemeMode.ICE_BLUE_LIGHT -> "ice_blue"
        }
        ahlPrefs.edit().putString("selected_bg_color", bgValue).apply()
        val prefs = context.getSharedPreferences("quran_app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("theme_mode", mode.id).apply()
    }
}
