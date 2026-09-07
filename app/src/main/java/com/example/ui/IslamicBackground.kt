package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

// Colors matching the uploaded reference image background precisely
val AppGradientTop = Color(0xFFF8FAFE)
val AppGradientUpperMid = Color(0xFFE2EEF7)
val AppGradientMid = Color(0xFFC8DDF0)
val AppGradientLowerMid = Color(0xFFA9C4DE)
val AppGradientBottom = Color(0xFF9CBBD8)

val AppBackgroundBrush = Brush.verticalGradient(
    0.00f to AppGradientTop,
    0.20f to AppGradientUpperMid,
    0.55f to AppGradientMid,
    0.80f to AppGradientLowerMid,
    1.00f to AppGradientBottom
)

@Composable
fun IslamicBackgroundBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPref = androidx.compose.runtime.remember(context) {
        context.getSharedPreferences("AhlAlBaytPrefs", android.content.Context.MODE_PRIVATE)
    }

    var selectedTheme by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(sharedPref.getString("selected_bg_color", "ice_blue") ?: "ice_blue")
    }

    val listener = androidx.compose.runtime.remember {
        android.content.SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == "selected_bg_color") {
                selectedTheme = prefs.getString("selected_bg_color", "ice_blue") ?: "ice_blue"
            }
        }
    }

    androidx.compose.runtime.DisposableEffect(sharedPref) {
        sharedPref.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            sharedPref.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val topColor: Color
    val upperMidColor: Color
    val midColor: Color
    val lowerMidColor: Color
    val bottomColor: Color
    val patternColor: Color

    val activeMode = com.example.ui.theme.ThemeManager.currentThemeMode
    if (activeMode == com.example.ui.theme.AppThemeMode.ROYAL_DARK || selectedTheme == "dark_night") {
        topColor = Color(0xFF0F1115) // Deep professional charcoal black
        upperMidColor = Color(0xFF14171C)
        midColor = Color(0xFF1A1D23) // Soft dark night
        lowerMidColor = Color(0xFF16191E)
        bottomColor = Color(0xFF0F1115)
        patternColor = Color(0xFFC9A254) // Elegant soft Islamic gold lines
    } else if (activeMode == com.example.ui.theme.AppThemeMode.EMERALD_DARK || selectedTheme == "emerald_green") {
        topColor = Color(0xFF061A13) // Deep dark emerald green top
        upperMidColor = Color(0xFF09251B) // Rich emerald gradient
        midColor = Color(0xFF0C2E22) // Deep emerald mid
        lowerMidColor = Color(0xFF0A281E) // Deep emerald lower mid
        bottomColor = Color(0xFF061A13) // Dark emerald bottom
        patternColor = Color(0xFF52B788) // Soft luminous emerald green lines
    } else {
        // Default ice_blue
        topColor = AppGradientTop
        upperMidColor = AppGradientUpperMid
        midColor = AppGradientMid
        lowerMidColor = AppGradientLowerMid
        bottomColor = AppGradientBottom
        patternColor = Color(0xFF3B5E7C)
    }

    val brush = Brush.verticalGradient(
        listOf(
            topColor,
            upperMidColor,
            midColor,
            lowerMidColor,
            bottomColor
        )
    )

    Box(
        modifier = modifier
            .background(brush)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawIslamicHeaderPattern(patternColor)
        }
        content()
    }
}

private fun DrawScope.drawIslamicHeaderPattern(strokeColor: Color) {
    val centerX = size.width / 2f
    val centerY = size.height * 0.10f
    val strokeWidth = 1.3.dp.toPx()

    val patternFade = Brush.verticalGradient(
        0.0f to strokeColor.copy(alpha = 0.22f),
        0.20f to strokeColor.copy(alpha = 0.14f),
        0.38f to strokeColor.copy(alpha = 0.02f),
        0.48f to Color.Transparent,
        startY = 0f,
        endY = size.height * 0.48f
    )

    // Concentric Islamic mandala rosettes
    val radii = listOf(
        25.dp.toPx(), 50.dp.toPx(), 80.dp.toPx(), 
        120.dp.toPx(), 170.dp.toPx(), 230.dp.toPx(), 300.dp.toPx()
    )
    for (r in radii) {
        drawCircle(
            brush = patternFade,
            radius = r,
            center = Offset(centerX, centerY),
            style = Stroke(width = strokeWidth)
        )
    }

    // 16-fold symmetrical star lattice rays and intersecting arcs
    val numRays = 16
    for (i in 0 until numRays) {
        val angleRad = (i * 360f / numRays) * (Math.PI / 180.0)
        val cosA = Math.cos(angleRad).toFloat()
        val sinA = Math.sin(angleRad).toFloat()

        drawLine(
            brush = patternFade,
            start = Offset(centerX + cosA * 15.dp.toPx(), centerY + sinA * 15.dp.toPx()),
            end = Offset(centerX + cosA * 320.dp.toPx(), centerY + sinA * 320.dp.toPx()),
            strokeWidth = strokeWidth
        )

        // Petal rosettes
        if (i % 2 == 0) {
            val petCenter = Offset(centerX + cosA * 80.dp.toPx(), centerY + sinA * 80.dp.toPx())
            drawCircle(
                brush = patternFade,
                radius = 55.dp.toPx(),
                center = petCenter,
                style = Stroke(width = strokeWidth)
            )

            val outerPetCenter = Offset(centerX + cosA * 170.dp.toPx(), centerY + sinA * 170.dp.toPx())
            drawCircle(
                brush = patternFade,
                radius = 75.dp.toPx(),
                center = outerPetCenter,
                style = Stroke(width = strokeWidth)
            )
        }
    }

    // Left and Right top corner rosette accents
    listOf(-0.10f, 1.10f).forEach { sideXFactor ->
        val sideCenterX = size.width * sideXFactor
        val sideCenterY = size.height * 0.04f
        for (r in listOf(35.dp.toPx(), 75.dp.toPx(), 130.dp.toPx(), 190.dp.toPx(), 260.dp.toPx())) {
            drawCircle(
                brush = patternFade,
                radius = r,
                center = Offset(sideCenterX, sideCenterY),
                style = Stroke(width = strokeWidth)
            )
        }
        for (i in 0 until 12) {
            val angleRad = (i * 360f / 12) * (Math.PI / 180.0)
            val cosA = Math.cos(angleRad).toFloat()
            val sinA = Math.sin(angleRad).toFloat()
            drawLine(
                brush = patternFade,
                start = Offset(sideCenterX + cosA * 10.dp.toPx(), sideCenterY + sinA * 10.dp.toPx()),
                end = Offset(sideCenterX + cosA * 240.dp.toPx(), sideCenterY + sinA * 240.dp.toPx()),
                strokeWidth = strokeWidth
            )
        }
    }
}
