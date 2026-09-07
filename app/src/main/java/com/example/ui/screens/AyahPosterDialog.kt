package com.example.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.data.local.AyahEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

enum class PosterTheme(
    val titleAr: String,
    val bgColors: List<Color>,
    val textColor: Color,
    val accentColor: Color,
    val borderColor: Color,
    val isDark: Boolean
) {
    EMERALD_GOLD(
        titleAr = "أخضر ملكي وذهبي",
        bgColors = listOf(Color(0xFF09170E), Color(0xFF153320)),
        textColor = Color(0xFFF0E6D2),
        accentColor = Color(0xFFD4AF37),
        borderColor = Color(0xFFD4AF37),
        isDark = true
    ),
    CLASSIC_CREAM(
        titleAr = "عاجي ورقي كلاسيكي",
        bgColors = listOf(Color(0xFFFAF4E8), Color(0xFFF3E8D3)),
        textColor = Color(0xFF2C2218),
        accentColor = Color(0xFF8C6D38),
        borderColor = Color(0xFFC5A059),
        isDark = false
    ),
    ROYAL_NIGHT(
        titleAr = "ليل كحلي ملكي",
        bgColors = listOf(Color(0xFF0D1B2A), Color(0xFF1B263B)),
        textColor = Color(0xFFE0E1DD),
        accentColor = Color(0xFFE0A96D),
        borderColor = Color(0xFFE0A96D),
        isDark = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyahPosterDialog(
    ayah: AyahEntity,
    surahName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTheme by remember { mutableStateOf(PosterTheme.EMERALD_GOLD) }
    var includeBasmala by remember { mutableStateOf(ayah.surahId != 9 && ayah.ayahNumber != 1) }
    var includeSadaqallah by remember { mutableStateOf(false) }
    var fontSizeScale by remember { mutableStateOf(50f) }
    var isJustified by remember { mutableStateOf(true) }

    // Generate preview bitmap
    val previewBitmap = remember(ayah, surahName, selectedTheme, includeBasmala, includeSadaqallah, fontSizeScale, isJustified) {
        generateAyahPosterBitmap(
            context = context,
            surahName = surahName,
            ayahNumber = ayah.ayahNumber,
            textUthmani = ayah.textUthmani,
            includeBasmala = includeBasmala,
            includeSadaqallah = includeSadaqallah,
            theme = selectedTheme,
            textSizePx = fontSizeScale,
            isJustified = isJustified
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(24.dp)),
            color = Color(0xFF111E16)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🖼️ شارك الآية كصورة",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE6C280)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Theme selector chips (Horizontally scrollable)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PosterTheme.values().forEach { theme ->
                            val isSelected = theme == selectedTheme
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) Color(0xFFD4AF37) else Color(0xFF1D3325))
                                    .clickable { selectedTheme = theme }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = theme.titleAr,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF09120D) else Color(0xFFB0C4B6)
                                )
                            }
                        }
                    }

                    // Basmala Toggle Row (if applicable)
                    if (ayah.surahId != 9) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF192A1F))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "إظهار البسملة أعلى الصورة",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFD0E0D4)
                            )
                            Switch(
                                checked = includeBasmala,
                                onCheckedChange = { includeBasmala = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF09120D),
                                    checkedTrackColor = Color(0xFFD4AF37)
                                )
                            )
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "إضافة صدق الله العلي العظيم",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFD0E0D4)
                            )
                            Switch(
                                checked = includeSadaqallah,
                                onCheckedChange = { includeSadaqallah = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF09120D),
                                    checkedTrackColor = Color(0xFFD4AF37)
                                )
                            )
                        }
                    }

                    // Font Size Control Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF192A1F))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "حجم نص الآية:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFD0E0D4)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF284432))
                                    .clickable { if (fontSizeScale > 32f) fontSizeScale -= 4f },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                            Text(
                                text = "${fontSizeScale.toInt()}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD4AF37)
                            )
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF284432))
                                    .clickable { if (fontSizeScale < 76f) fontSizeScale += 4f },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    }

                    // Kashida / Justification Toggle Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF192A1F))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "كشيدة ومحاذاة النص:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFD0E0D4)
                        )
                        Switch(
                            checked = isJustified,
                            onCheckedChange = { isJustified = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF09120D),
                                checkedTrackColor = Color(0xFFD4AF37)
                            )
                        )
                    }

                    // Poster Preview Image Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = previewBitmap.asImageBitmap(),
                            contentDescription = "معاينة البوستر",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons (Download & Direct Share)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Download Button
                    Button(
                        onClick = {
                            val success = savePosterToGallery(context, previewBitmap, "Ayah_${ayah.surahId}_${ayah.ayahNumber}")
                            if (success) {
                                Toast.makeText(context, "✅ تم حفظ صورة البوستر في المعرض بنجاح", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "فشل حفظ الصورة في المعرض", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تحميل الصورة", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                    }

                    // Direct Share Button
                    Button(
                        onClick = {
                            sharePosterImage(context, previewBitmap, "سورة $surahName - الآية ${ayah.ayahNumber}")
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD4AF37),
                            contentColor = Color(0xFF09120D)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة الصورة", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

/**
 * Generates high-resolution, perfectly-formatted Android Canvas Bitmap for the Quranic Ayah Poster.
 */
fun generateAyahPosterBitmap(
    context: Context,
    surahName: String,
    ayahNumber: Int,
    textUthmani: String,
    includeBasmala: Boolean,
    includeSadaqallah: Boolean,
    theme: PosterTheme,
    textSizePx: Float = 50f,
    isJustified: Boolean = true
): Bitmap {
    val width = 1080
    // Dynamic height based on text length
    val basePadding = 110
    val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = textSizePx
        color = theme.textColor.toArgb()
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }

    val fullAyahText = "$textUthmani ۝${ayahNumber.toArabicNumerals()}"
    val contentWidth = width - (basePadding * 2)
    
    val textWidth = textPaint.measureText(fullAyahText)
    val isShortText = textWidth < contentWidth

    val alignment = if (isJustified && !isShortText) Layout.Alignment.ALIGN_NORMAL else Layout.Alignment.ALIGN_CENTER

    val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val builder = StaticLayout.Builder.obtain(fullAyahText, 0, fullAyahText.length, textPaint, contentWidth)
            .setAlignment(alignment)
            .setLineSpacing(20f, 1.25f)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isJustified && !isShortText) {
            builder.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD)
        }
        builder.build()
    } else {
        @Suppress("DEPRECATION")
        StaticLayout(fullAyahText, textPaint, contentWidth, alignment, 1.25f, 20f, false)
    }

    // Dynamic height based on text length and basmala
    val basmalaExtraHeight = if (includeBasmala) ((textSizePx * 1.5f).coerceAtLeast(70f)).toInt() else 0
    val sadaqallahExtraHeight = if (includeSadaqallah) ((textSizePx * 1.2f).coerceAtLeast(60f)).toInt() else 0
    val calculatedHeight = (staticLayout.height + 650 + basmalaExtraHeight + sadaqallahExtraHeight).coerceAtLeast(1200)
    val bitmap = Bitmap.createBitmap(width, calculatedHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // 1. Draw Background
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            0f, 0f, 0f, calculatedHeight.toFloat(),
            theme.bgColors[0].toArgb(),
            theme.bgColors[1].toArgb(),
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, width.toFloat(), calculatedHeight.toFloat(), bgPaint)

    // 2. Draw Decorative Double Border
    val margin = 50f
    val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = theme.borderColor.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    val innerGoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = theme.borderColor.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    val outerRect = RectF(margin, margin, width - margin, calculatedHeight - margin)
    val innerRect = RectF(margin + 16f, margin + 16f, width - (margin + 16f), calculatedHeight - (margin + 16f))

    canvas.drawRoundRect(outerRect, 24f, 24f, goldPaint)
    canvas.drawRoundRect(innerRect, 18f, 18f, innerGoldPaint)

    // Corner Ornament Symbols
    val cornerSymbolPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = theme.accentColor.toArgb()
        textSize = 32f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("۞", margin + 35f, margin + 50f, cornerSymbolPaint)
    canvas.drawText("۞", width - (margin + 35f), margin + 50f, cornerSymbolPaint)
    canvas.drawText("۞", margin + 35f, calculatedHeight - (margin + 30f), cornerSymbolPaint)
    canvas.drawText("۞", width - (margin + 35f), calculatedHeight - (margin + 30f), cornerSymbolPaint)

    // 3. Header: App Name "نور العترة"
    val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = theme.accentColor.toArgb()
        textSize = 38f
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("۞  نور العترة  ۞", width / 2f, margin + 110f, headerPaint)

    // 4. Surah & Ayah Badge
    val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = theme.accentColor.toArgb()
        textSize = 32f
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    val surahBadgeText = "سورة $surahName  •  الآية ${ayahNumber.toArabicNumerals()}"
    canvas.drawText(surahBadgeText, width / 2f, margin + 180f, badgePaint)

    // Separator line
    val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = theme.accentColor.toArgb()
        strokeWidth = 2f
        alpha = 180
    }
    canvas.drawLine(width / 2f - 180f, margin + 215f, width / 2f + 180f, margin + 215f, linePaint)

    var currentY = margin + 295f

    // 5. Bismillah (if enabled)
    if (includeBasmala) {
        val basmalaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.accentColor.toArgb()
            textSize = (textSizePx * 0.85f).coerceIn(26f, 85f)
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", width / 2f, currentY, basmalaPaint)
        currentY += (textSizePx * 1.2f).coerceAtLeast(50f) + 40f
    }

    // Side Kashida lines if justification enabled
    if (isJustified) {
        val sideKashidaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.accentColor.toArgb()
            strokeWidth = 3f
            alpha = 150
        }
        val startY = currentY - 10f
        val endY = currentY + staticLayout.height + 10f
        canvas.drawLine(basePadding - 25f, startY, basePadding - 25f, endY, sideKashidaPaint)
        canvas.drawLine(width - basePadding + 25f, startY, width - basePadding + 25f, endY, sideKashidaPaint)
    }

    // 6. Draw Ayah Uthmani Text using StaticLayout
    canvas.save()
    canvas.translate(basePadding.toFloat(), currentY)
    staticLayout.draw(canvas)
    canvas.restore()
    
    currentY += staticLayout.height + 70f
    
    // 6.5 Draw Sadaqallah (if enabled)
    if (includeSadaqallah) {
        val sadaqallahPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.accentColor.toArgb()
            textSize = (textSizePx * 0.85f).coerceIn(26f, 85f)
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("صَدَقَ اللَّهُ العَلِيُّ العَظِيمُ", width / 2f, currentY, sadaqallahPaint)
        currentY += (textSizePx * 1.2f).coerceAtLeast(40f)
    }

    // 7. Footer: App branding at bottom
    val footerY = calculatedHeight - (margin + 80f)
    canvas.drawLine(width / 2f - 140f, footerY - 45f, width / 2f + 140f, footerY - 45f, linePaint)

    val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (theme.isDark) Color(0xFFA0B5A6).toArgb() else Color(0xFF665544).toArgb()
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("تطبيق نور العترة", width / 2f, footerY, footerPaint)

    return bitmap
}

fun savePosterToGallery(context: Context, bitmap: Bitmap, fileName: String): Boolean {
    return try {
        val imageFileName = "${fileName}_${System.currentTimeMillis()}.png"
        val outputStream: OutputStream?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QuranAudio")
            }
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            outputStream = imageUri?.let { resolver.openOutputStream(it) }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val quranDir = File(imagesDir, "QuranAudio").apply { if (!exists()) mkdirs() }
            val imageFile = File(quranDir, imageFileName)
            outputStream = FileOutputStream(imageFile)
        }

        outputStream?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun sharePosterImage(context: Context, bitmap: Bitmap, title: String) {
    try {
        val cachePath = File(context.cacheDir, "posters").apply { if (!exists()) mkdirs() }
        val file = File(cachePath, "ayah_poster_share.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_TEXT, "✨ آية قرآنية من تطبيق نور العترة\n$title")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "مشاركة بوستر الآية عبر:")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "فشل إعداد الصورة للمشاركة: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

private fun Color.toArgb(): Int {
    return android.graphics.Color.argb(
        (this.alpha * 255).toInt(),
        (this.red * 255).toInt(),
        (this.green * 255).toInt(),
        (this.blue * 255).toInt()
    )
}
