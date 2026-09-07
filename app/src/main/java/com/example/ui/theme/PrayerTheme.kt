package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Sophisticated Dark Palette (Design System for Prayer Times)
val SophisticatedBg = Color(0xFF121417)
val SophisticatedSurface = Color(0xFF1C1F26)
val SophisticatedSurfaceVariant = Color(0xFF242833)
val SophisticatedBorder = Color(0xFF334155)
val SophisticatedBorderSubtle = Color(0xFF262C36)

// Glowing Teal Accents (Sophisticated Highlights)
val TealAccentLight = Color(0xFF2DD4BF) // teal-400
val TealAccent = Color(0xFF14B8A6)      // teal-500
val TealAccentDark = Color(0xFF0D9488)  // teal-600
val TealGlow10 = Color(0x1A14B8A6)     // 10% alpha
val TealGlow20 = Color(0x3314B8A6)     // 20% alpha
val TealGlow40 = Color(0x6614B8A6)     // 40% alpha

// Sophisticated Light Palette (Day Mode)
val LightBg = Color(0xFFF4F6F9)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE9ECEF)
val LightBorder = Color(0xFFD1D5DB)
val LightBorderSubtle = Color(0xFFE5E7EB)

val LightTextMain = Color(0xFF1F2937)
val LightTextTitle = Color(0xFF111827)
val LightTextMuted = Color(0xFF4B5563)
val LightTextSubtle = Color(0xFF6B7280)

val LightTealAccent = Color(0xFF0D9488)
val LightTealAccentLight = Color(0xFF0F766E)
val LightTealGlow10 = Color(0x1A0D9488)
val LightTealGlow20 = Color(0x330D9488)
val LightTealGlow40 = Color(0x660D9488)

val LightHeroGradientStart = Color(0xFFE6FFFA)
val LightHeroGradientEnd = Color(0xFFCCFBF1)

// Text & Content Colors
val TextMain = Color(0xFFE6E1E5)
val TextMuted = Color(0xFF94A3B8)       // slate-400
val TextSubtle = Color(0xFF64748B)      // slate-500

// Gradients
val HeroGradientStart = Color(0xFF1E293B)
val HeroGradientEnd = Color(0xFF0F172A)
val TealGradientStart = Color(0xFF2DD4BF)
val TealGradientEnd = Color(0xFF0D9488)

// Auxiliary Gold Accents for Holy items
val IslamicGold = Color(0xFFF59E0B)
val IslamicGoldLight = Color(0xFFFCD34D)
val IslamicGoldDark = Color(0xFFB45309)

val IslamicGreenPrimary = TealAccent
val IslamicGreenDark = SophisticatedBg
val IslamicGreenLight = TealAccentLight

val GoldGradientStart = Color(0xFFFCD34D)
val GoldGradientEnd = Color(0xFFD97706)

val GreenGradientStart = Color(0xFF1E293B)
val GreenGradientEnd = Color(0xFF0F172A)

data class AppThemeColors(
    val bg: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val border: Color,
    val borderSubtle: Color,
    val textMain: Color,
    val textTitle: Color,
    val textMuted: Color,
    val textSubtle: Color,
    val tealAccent: Color,
    val tealAccentLight: Color,
    val tealGlow10: Color,
    val tealGlow20: Color,
    val tealGlow40: Color,
    val heroGradientStart: Color,
    val heroGradientEnd: Color,
    val bottomNavBg: Color,
    val isDark: Boolean
)

// Theme Palette matching Noor Al-Itrah Royal Dark mode (الداكن الملكي)
val RoyalDarkAppThemeColors = AppThemeColors(
    bg = Color(0xFF0F1115),
    surface = Color(0xFF16191E),
    surfaceVariant = Color(0xFF20242B),
    border = Color(0x40D4AF37),
    borderSubtle = Color(0x25D4AF37),
    textMain = Color(0xFFF2F4F7),
    textTitle = Color(0xFFF2F4F7),
    textMuted = Color(0xFFDCDFE5),
    textSubtle = Color(0xFF9EABB8),
    tealAccent = Color(0xFFD4AF37),
    tealAccentLight = Color(0xFFF0D28B),
    tealGlow10 = Color(0x1AD4AF37),
    tealGlow20 = Color(0x33D4AF37),
    tealGlow40 = Color(0x66D4AF37),
    heroGradientStart = Color(0xFF262215),
    heroGradientEnd = Color(0xFF0F1115),
    bottomNavBg = Color(0xFF16191E),
    isDark = true
)

// Theme Palette matching Noor Al-Itrah Emerald Dark mode (الزمردي الذهبي)
val EmeraldDarkAppThemeColors = AppThemeColors(
    bg = Color(0xFF061A13),
    surface = Color(0xFF0E2A20),
    surfaceVariant = Color(0xFF15382B),
    border = Color(0x4052B788),
    borderSubtle = Color(0x2552B788),
    textMain = Color(0xFFF2F4F7),
    textTitle = Color(0xFFF2F4F7),
    textMuted = Color(0xFFB5CFBE),
    textSubtle = Color(0xFF7D9E8A),
    tealAccent = Color(0xFF52B788),
    tealAccentLight = Color(0xFF74C69D),
    tealGlow10 = Color(0x1A52B788),
    tealGlow20 = Color(0x3352B788),
    tealGlow40 = Color(0x6652B788),
    heroGradientStart = Color(0xFF163223),
    heroGradientEnd = Color(0xFF061A13),
    bottomNavBg = Color(0xFF0E2A20),
    isDark = true
)

// Theme Palette matching Noor Al-Itrah Ice Blue Light mode (السماوي الفضي الفاتح)
val IceBlueLightAppThemeColors = AppThemeColors(
    bg = Color(0xFFE8EFF8),
    surface = Color(0xFFF4F7FC),
    surfaceVariant = Color(0xFFD3E1F2),
    border = Color(0xFF8CAACF),
    borderSubtle = Color(0xFFBFD2E8),
    textMain = Color(0xFF0F1D30),
    textTitle = Color(0xFF0F1D30),
    textMuted = Color(0xFF2C4366),
    textSubtle = Color(0xFF4B6B94),
    tealAccent = Color(0xFF2B5282),
    tealAccentLight = Color(0xFF3B629B),
    tealGlow10 = Color(0x1A2B5282),
    tealGlow20 = Color(0x332B5282),
    tealGlow40 = Color(0x662B5282),
    heroGradientStart = Color(0xFFCBE0F5),
    heroGradientEnd = Color(0xFFE8EFF8),
    bottomNavBg = Color(0xFFF4F7FC),
    isDark = false
)

// Legacy fallbacks for backward compatibility
val DarkAppThemeColors = RoyalDarkAppThemeColors
val LightAppThemeColors = IceBlueLightAppThemeColors

val LocalAppThemeColors = staticCompositionLocalOf { RoyalDarkAppThemeColors }

object AppColors {
    val current: AppThemeColors
        @Composable
        get() = LocalAppThemeColors.current
}

@Composable
fun PrayerAppTheme(
    appThemeMode: AppThemeMode = ThemeManager.currentThemeMode,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (appThemeMode) {
        AppThemeMode.ROYAL_DARK -> RoyalDarkColorScheme
        AppThemeMode.EMERALD_DARK -> DarkEmeraldColorScheme
        AppThemeMode.ICE_BLUE_LIGHT -> LightIceBlueColorScheme
    }

    val appThemeColors = when (appThemeMode) {
        AppThemeMode.ROYAL_DARK -> RoyalDarkAppThemeColors
        AppThemeMode.EMERALD_DARK -> EmeraldDarkAppThemeColors
        AppThemeMode.ICE_BLUE_LIGHT -> IceBlueLightAppThemeColors
    }

    CompositionLocalProvider(LocalAppThemeColors provides appThemeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
