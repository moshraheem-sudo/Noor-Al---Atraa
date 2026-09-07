package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Close
import kotlinx.coroutines.withContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.AVAILABLE_RECITERS
import com.example.audio.QuranAudioDownloader
import com.example.audio.Reciter

@Composable
fun DownloadFullQuranDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val progress by QuranAudioDownloader.downloadProgress.collectAsStateWithLifecycle()
    
    val downloadedReciters = remember { mutableStateMapOf<String, Boolean>() }
    
    LaunchedEffect(progress) {
        if (progress == null) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                for (reciter in AVAILABLE_RECITERS) {
                    val isDownloaded = QuranAudioDownloader.isReciterDownloaded(context, reciter)
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        downloadedReciters[reciter.id] = isDownloaded
                    }
                }
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = { if (progress == null) onDismiss() },
        title = {
            Text(
                text = "تحميل المصحف كاملاً",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (progress != null) {
                    val p = progress!!
                    
                    if (p.isCompleted) {
                        Text(
                            text = "مكتمل بنجاح!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF388E3C),
                            modifier = Modifier.padding(bottom = 8.dp).align(Alignment.CenterHorizontally),
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "مكتمل",
                            tint = Color(0xFF388E3C),
                            modifier = Modifier.size(64.dp).align(Alignment.CenterHorizontally).padding(bottom = 16.dp)
                        )
                    } else {
                        Text(
                            text = if (p.isPaused) "تم إيقاف التحميل مؤقتاً..." else "جاري التحميل... سورة ${p.currentSurah} من ${p.totalSurahs}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (p.isPaused) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LinearProgressIndicator(
                            progress = p.currentSurah / p.totalSurahs.toFloat(),
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (p.isPaused) {
                                IconButton(onClick = { QuranAudioDownloader.resume() }) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "استئناف", tint = MaterialTheme.colorScheme.primary)
                                }
                            } else {
                                IconButton(onClick = { QuranAudioDownloader.pause() }) {
                                    Icon(Icons.Default.Pause, contentDescription = "إيقاف مؤقت", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            IconButton(onClick = { QuranAudioDownloader.cancel() }) {
                                Icon(Icons.Default.Close, contentDescription = "إلغاء التحميل", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "يمكنك إخفاء هذه النافذة ومتابعة التحميل في الخلفية.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Text(
                        text = "اختر القارئ لتحميل جميع سور القرآن الكريم للعمل بدون إنترنت:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(AVAILABLE_RECITERS) { reciter ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                             if (downloadedReciters[reciter.id] != true) {
                                                 QuranAudioDownloader.downloadAll(context, reciter)
                                             } else {
                                                 Toast.makeText(context, "تم تنزيل هذا القارئ مسبقاً", Toast.LENGTH_SHORT).show()
                                             }
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = reciter.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (downloadedReciters[reciter.id] == true) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "تم التنزيل",
                                            tint = Color(0xFF388E3C)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (progress != null && !progress!!.isCompleted) {
                    Toast.makeText(context, "التحميل مستمر. يمكنك المتابعة من زر التحميل أعلى الشاشة.", Toast.LENGTH_LONG).show()
                }
                if (progress?.isCompleted == true) {
                    QuranAudioDownloader.cancel() // clear the progress state
                }
                onDismiss()
            }) {
                Text(if (progress != null && !progress!!.isCompleted) "إخفاء" else if (progress?.isCompleted == true) "إغلاق" else "إلغاء")
            }
        }
    )
}
