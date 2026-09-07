package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

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

val DarkAppThemeColors = AppThemeColors(
    bg = SophisticatedBg,
    surface = SophisticatedSurface,
    surfaceVariant = SophisticatedSurfaceVariant,
    border = SophisticatedBorder,
    borderSubtle = SophisticatedBorderSubtle,
    textMain = TextMain,
    textTitle = TextWhite,
    textMuted = TextMuted,
    textSubtle = TextSubtle,
    tealAccent = TealAccent,
    tealAccentLight = TealAccentLight,
    tealGlow10 = TealGlow10,
    tealGlow20 = TealGlow20,
    tealGlow40 = TealGlow40,
    heroGradientStart = HeroGradientStart,
    heroGradientEnd = HeroGradientEnd,
    bottomNavBg = Color(0xFF14171F),
    isDark = true
)

val LightAppThemeColors = AppThemeColors(
    bg = LightBg,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    border = LightBorder,
    borderSubtle = LightBorderSubtle,
    textMain = LightTextMain,
    textTitle = LightTextTitle,
    textMuted = LightTextMuted,
    textSubtle = LightTextSubtle,
    tealAccent = LightTealAccent,
    tealAccentLight = LightTealAccentLight,
    tealGlow10 = LightTealGlow10,
    tealGlow20 = LightTealGlow20,
    tealGlow40 = LightTealGlow40,
    heroGradientStart = LightHeroGradientStart,
    heroGradientEnd = LightHeroGradientEnd,
    bottomNavBg = Color(0xFFFFFFFF),
    isDark = false
)

val LocalAppThemeColors = staticCompositionLocalOf { DarkAppThemeColors }

object AppColors {
    val current: AppThemeColors
        @Composable
        get() = LocalAppThemeColors.current
}

private val SophisticatedDarkColorScheme = darkColorScheme(
    primary = TealAccentLight,
    onPrimary = Color(0xFF042F2C),
    primaryContainer = TealGlow20,
    onPrimaryContainer = TealAccentLight,
    secondary = TealAccent,
    onSecondary = Color.Black,
    secondaryContainer = SophisticatedSurfaceVariant,
    onSecondaryContainer = TextMain,
    tertiary = IslamicGoldLight,
    onTertiary = Color.Black,
    background = SophisticatedBg,
    onBackground = TextMain,
    surface = SophisticatedSurface,
    onSurface = TextMain,
    surfaceVariant = SophisticatedSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = SophisticatedBorder,
    outlineVariant = SophisticatedBorderSubtle
)

private val SophisticatedLightColorScheme = lightColorScheme(
    primary = LightTealAccentLight,
    onPrimary = Color.White,
    primaryContainer = LightTealGlow20,
    onPrimaryContainer = LightTealAccentLight,
    secondary = LightTealAccent,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightTextMain,
    tertiary = IslamicGoldDark,
    onTertiary = Color.White,
    background = LightBg,
    onBackground = LightTextMain,
    surface = LightSurface,
    onSurface = LightTextMain,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextMuted,
    outline = LightBorder,
    outlineVariant = LightBorderSubtle
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) SophisticatedDarkColorScheme else SophisticatedLightColorScheme
    val appThemeColors = if (darkTheme) DarkAppThemeColors else LightAppThemeColors

    CompositionLocalProvider(LocalAppThemeColors provides appThemeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
