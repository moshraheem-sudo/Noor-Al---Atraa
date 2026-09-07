package com.example.util
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast


import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.FileProvider
import com.example.data.local.AyahEntity
import java.io.File
import java.io.FileOutputStream

fun Int.toArabicNumerals(): String {
    val arabicNumerals = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return this.toString().map { if (it.isDigit()) arabicNumerals[it - '0'] else it }.joinToString("")
}

object SurahExportUtil {
    fun exportSurahAsDoc(context: Context, surahName: String, surahId: Int, ayahs: List<AyahEntity>) {
        val html = StringBuilder()
        html.append("<html><head><meta charset=\"UTF-8\">")
        html.append("<style>")
        html.append("body { font-family: 'Scheherazade New', 'Amiri', 'Traditional Arabic', serif; direction: rtl; text-align: justify; padding: 40px; font-size: 26px; line-height: 2.2; }")
        html.append(".title { text-align: center; font-size: 34px; font-weight: bold; margin-bottom: 30px; color: #8B6508; border-bottom: 2px solid #8B6508; padding-bottom: 10px; }")
        html.append(".basmala { text-align: center; font-size: 30px; margin-bottom: 25px; color: #333333; }")
        html.append(".ayah { color: #000000; }")
        html.append(".number { color: #8B6508; font-weight: bold; margin: 0 5px; }")
        html.append("</style></head><body>")
        
        html.append("<div class='title'>سورة $surahName</div>")
        if (surahId != 1 && surahId != 9) {
            html.append("<div class='basmala'>بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ</div>")
        }
        
        html.append("<div>")
        for (ayah in ayahs) {
            val num = ayah.ayahNumber.toArabicNumerals()
            html.append("<span class='ayah'>${ayah.textUthmani}</span><span class='number'> ۝$num </span>")
        }
        html.append("</div></body></html>")
        
        val fileName = "سورة_${surahName.replace(" ", "_")}.doc"
        val file = File(context.cacheDir, fileName)
        file.writeText(html.toString())
        
        saveFileToDownloads(context, file, "application/msword", fileName)
    }

    fun exportSurahAsPdf(context: Context, surahName: String, surahId: Int, ayahs: List<AyahEntity>) {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 width at 72 PPI
        val pageHeight = 842 // A4 height at 72 PPI
        val margin = 50f
        
        val textPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 22f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            color = Color.BLACK
        }
        
        val titlePaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 28f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            color = Color.parseColor("#8B6508")
            textAlign = Paint.Align.CENTER
        }
        
        val basmalaPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 24f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            color = Color.DKGRAY
            textAlign = Paint.Align.CENTER
        }
        
        val content = StringBuilder()
        for (ayah in ayahs) {
            content.append("${ayah.textUthmani} ۝${ayah.ayahNumber.toArabicNumerals()} ")
        }
        
        val availableWidth = (pageWidth - 2 * margin).toInt()
        
        val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            StaticLayout.Builder.obtain(content.toString(), 0, content.length, textPaint, availableWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setTextDirection(android.text.TextDirectionHeuristics.RTL)
                .setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD)
                .setLineSpacing(0f, 1.8f)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(content.toString(), textPaint, availableWidth, Layout.Alignment.ALIGN_NORMAL, 1.8f, 0f, false)
        }
        
        var yOffset = margin
        var currentLine = 0
        var pageNumber = 1
        
        while (currentLine < staticLayout.lineCount) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            yOffset = margin
            
            if (pageNumber == 1) {
                // Draw Title
                canvas.drawText("سورة $surahName", pageWidth / 2f, yOffset + titlePaint.textSize, titlePaint)
                yOffset += titlePaint.textSize + 20f
                
                // Draw Basmala
                if (surahId != 1 && surahId != 9) {
                    canvas.drawText("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", pageWidth / 2f, yOffset + basmalaPaint.textSize, basmalaPaint)
                    yOffset += basmalaPaint.textSize + 30f
                } else {
                    yOffset += 10f
                }
            }
            
            // Draw lines
            while (currentLine < staticLayout.lineCount) {
                val lineBottom = staticLayout.getLineBottom(currentLine)
                val lineTop = staticLayout.getLineTop(currentLine)
                val lineHeight = lineBottom - lineTop
                
                if (yOffset + lineHeight > pageHeight - margin) {
                    break // Page is full
                }
                
                canvas.save()
                canvas.translate(margin, yOffset - lineTop)
                
                // draw only the current line by clipping? StaticLayout draws everything.
                // We must clip to the line's bounds.
                canvas.clipRect(0, lineTop, availableWidth, lineBottom)
                staticLayout.draw(canvas)
                canvas.restore()
                
                yOffset += lineHeight
                currentLine++
            }
            
            // Draw page number
            titlePaint.textSize = 14f
            canvas.drawText("- $pageNumber -", pageWidth / 2f, pageHeight - margin / 2, titlePaint)
            titlePaint.textSize = 28f // reset
            
            pdfDocument.finishPage(page)
            pageNumber++
        }
        
        val fileName = "سورة_${surahName.replace(" ", "_")}.pdf"
        val file = File(context.cacheDir, fileName)
        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        pdfDocument.close()
        fos.close()
        
        saveFileToDownloads(context, file, "application/pdf", fileName)
    }
    
    private fun saveFileToDownloads(context: Context, file: File, mimeType: String, fileName: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        file.inputStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val destFile = File(downloadsDir, fileName)
                file.copyTo(destFile, overwrite = true)
            }
            
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(context, "تم حفظ الملف بنجاح في التنزيلات (Downloads)", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(context, "حدث خطأ أثناء حفظ الملف", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
