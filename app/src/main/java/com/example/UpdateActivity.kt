package com.example

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.ui.theme.MyApplicationTheme
import java.io.File

class UpdateActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val apkUrl = intent.getStringExtra("APK_URL") ?: ""
        val newVersion = intent.getStringExtra("NEW_VERSION") ?: ""

        setContent {
            MyApplicationTheme {
                UpdateScreen(
                    newVersion = newVersion,
                    onUpdateClick = {
                        startDownload(this, apkUrl, "Update_$newVersion.apk")
                        finish()
                    },
                    onDismiss = {
                        finish()
                    }
                )
            }
        }
    }

    private fun startDownload(context: Context, url: String, fileName: String) {
        try {
            Toast.makeText(context, "جاري التحميل... ستظهر رسالة التثبيت عند الانتهاء", Toast.LENGTH_LONG).show()
            
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("تحديث التطبيق")
                .setDescription("جاري تحميل النسخة الجديدة...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setMimeType("application/vnd.android.package-archive")

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(ctxt: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        installApk(context, fileName)
                        try {
                            context.unregisterReceiver(this)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            androidx.core.content.ContextCompat.registerReceiver(
                context,
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                androidx.core.content.ContextCompat.RECEIVER_EXPORTED
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "فشل بدء التحميل.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun installApk(context: Context, fileName: String) {
        try {
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            if (file.exists()) {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun UpdateScreen(newVersion: String, onUpdateClick: () -> Unit, onDismiss: () -> Unit) {
    var showConsentDialog by remember { mutableStateOf(false) }

    if (showConsentDialog) {
        com.example.ui.OfficialPermissionsConsentDialog(
            onDismiss = { showConsentDialog = false },
            onConfirmInstall = {
                showConsentDialog = false
                onUpdateClick()
            }
        )
    }

    com.example.ui.IslamicBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            com.example.ui.UpdateProgressCard(
                onInstallClick = {
                    showConsentDialog = true
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إغلاق والعودة للتطبيق", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
