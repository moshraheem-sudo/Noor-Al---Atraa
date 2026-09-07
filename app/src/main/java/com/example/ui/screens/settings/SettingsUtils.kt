package com.example.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.io.File
import java.util.Locale
fun openFolder(context: Context, folderPath: String) {
    try {
        val folder = File(folderPath)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val uri = try {
            androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                folder
            )
        } catch (e: Exception) {
            Uri.parse(folderPath)
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "فتح المجلد"))
    } catch (e: Exception) {
        try {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                setDataAndType(Uri.parse(folderPath), "*/*")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e2: Exception) {
            Toast.makeText(context, "مسار المجلد:\n$folderPath", Toast.LENGTH_LONG).show()
        }
    }
}

fun installApk(context: Context, apkFile: File) {
    android.os.Handler(android.os.Looper.getMainLooper()).post {
        try {
            if (!apkFile.exists()) {
                Toast.makeText(context, "ملف التحديث غير موجود", Toast.LENGTH_SHORT).show()
                return@post
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    Toast.makeText(
                        context,
                        "يرجى تفعيل خيار (السماح بتثبيت التطبيقات) لإكمال التثبيت",
                        Toast.LENGTH_LONG
                    ).show()
                    val permissionIntent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(permissionIntent)
                    return@post
                }
            }

            val contentUri: Uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "فشل بدء تثبيت التحديث: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 بايت"
    val k = 1024.0
    val sizes = arrayOf("بايت", "كيلوبايت", "ميجابايت", "جيجابايت")
    val i = (Math.log(bytes.toDouble()) / Math.log(k)).toInt().coerceIn(0, sizes.size - 1)
    val value = bytes / Math.pow(k, i.toDouble())
    return String.format(Locale.US, "%.1f %s", value, sizes[i])
}

enum class UpdateStatus {
    IDLE,
    CHECKING,
    AVAILABLE,
    DOWNLOADING,
    DOWNLOADED,
    UP_TO_DATE
}

