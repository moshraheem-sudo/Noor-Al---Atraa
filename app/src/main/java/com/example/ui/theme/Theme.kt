package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Royal Dark Luxury Theme Scheme (Deep black and royal gold)
internal val RoyalDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD4AF37), // Royal Gold
    onPrimary = Color(0xFF0F1115),
    secondary = Color(0xFFE6C280),
    onSecondary = Color(0xFF0F1115),
    tertiary = Color(0xFFC9A254),
    background = Color(0xFF0F1115), // Deep charcoal black
    onBackground = Color(0xFFF2F4F7),
    surface = Color(0xFF16191E), // Charcoal card surface
    onSurface = Color(0xFFF2F4F7),
    surfaceVariant = Color(0xFF20242B),
    onSurfaceVariant = Color(0xFFDCDFE5),
    outline = Color(0x80D4AF37),
    primaryContainer = Color(0xFF262215),
    onPrimaryContainer = Color(0xFFF0D28B),
    secondaryContainer = Color(0xFF21252D),
    onSecondaryContainer = Color(0xFFE0E3EA)
)

// Dark Emerald Theme Scheme
internal val DarkEmeraldColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    tertiary = TertiaryDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    primaryContainer = Color(0xFF163223),
    onPrimaryContainer = Color(0xFFE6C280)
)

// Soft Icy Sky Blue Light Theme Scheme (Matching user uploaded image)
internal val LightIceBlueColorScheme = lightColorScheme(
    primary = Color(0xFF2B5282),
    onPrimary = Color.White,
    secondary = Color(0xFF3B629B),
    onSecondary = Color.White,
    tertiary = Color(0xFF1E3A8A),
    background = Color(0xFFE8EFF8),        // Soft sky blue pastel background matching uploaded image
    onBackground = Color(0xFF0F1D30),      // Deep navy charcoal text
    surface = Color(0xFFF4F7FC),           // Very light ice white surface card
    onSurface = Color(0xFF0F1D30),
    surfaceVariant = Color(0xFFD3E1F2),    // Light silver blue container
    onSurfaceVariant = Color(0xFF2C4366),
    outline = Color(0xFF8CAACF),           // Soft sky blue border
    primaryContainer = Color(0xFFCBE0F5),
    onPrimaryContainer = Color(0xFF1A3B66)
)

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = ThemeManager.currentThemeMode,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (themeMode) {
        AppThemeMode.ROYAL_DARK -> RoyalDarkColorScheme
        AppThemeMode.EMERALD_DARK -> DarkEmeraldColorScheme
        AppThemeMode.ICE_BLUE_LIGHT -> LightIceBlueColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

