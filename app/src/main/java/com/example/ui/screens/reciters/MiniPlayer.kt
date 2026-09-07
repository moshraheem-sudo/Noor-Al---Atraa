package com.example.ui.screens.reciters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.AppRepeatMode
import com.example.audio.PlayerManager

@Composable
fun MiniPlayer() {
    val title by PlayerManager.currentTitle.collectAsState()
    val isPlaying by PlayerManager.isPlaying.collectAsState()
    val position by PlayerManager.currentPosition.collectAsState()
    val duration by PlayerManager.duration.collectAsState()
    val currentTrack by PlayerManager.currentTrack.collectAsState()

    var showFullScreenPlayer by remember { mutableStateOf(false) }

    if (title.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clickable { showFullScreenPlayer = true },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎧", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            if (currentTrack?.reciterName?.isNotEmpty() == true) {
                                Text(
                                    text = currentTrack?.reciterName ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            val progress = if (duration > 0) (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
                            val posFormatted = String.format("%02d:%02d", java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(position), java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(position) % 60)
                            Text(
                                text = posFormatted,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        val progress = if (duration > 0) (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .padding(top = 4.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)
                        )
                    }
                    IconButton(
                        onClick = { PlayerManager.togglePlayPause() }
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(
                        onClick = { PlayerManager.closePlayer() },
                        modifier = Modifier.padding(start = 4.dp).size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (duration > 0) {
                    LinearProgressIndicator(
                        progress = { if (duration > 0L) (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }

    if (showFullScreenPlayer) {
        FullScreenPlayerModal(onDismiss = { showFullScreenPlayer = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenPlayerModal(onDismiss: () -> Unit) {
    val title by PlayerManager.currentTitle.collectAsState()
    val isPlaying by PlayerManager.isPlaying.collectAsState()
    val position by PlayerManager.currentPosition.collectAsState()
    val duration by PlayerManager.duration.collectAsState()
    val currentTrack by PlayerManager.currentTrack.collectAsState()
    val repeatMode by PlayerManager.repeatMode.collectAsState()
    val sleepTimerMinutes by PlayerManager.sleepTimerMinutes.collectAsState()
    val sleepTimerRemainingSeconds by PlayerManager.sleepTimerRemainingSeconds.collectAsState()

    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(-1f) }

    val parsedTitle = remember(title, currentTrack) {
        if (title.contains("(") && title.endsWith(")")) {
            val parts = title.split("(")
            val surah = parts[0].trim()
            val reciter = parts[1].replace(")", "").trim()
            Pair(surah, reciter)
        } else {
            val surah = currentTrack?.surahName?.ifEmpty { title } ?: title
            val reciter = currentTrack?.reciterName ?: ""
            Pair(surah, reciter)
        }
    }
    val displaySurahName = parsedTitle.first
    val displayReciterName = parsedTitle.second

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "تصغير المشغل",
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = "مشغل القرآن",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.size(48.dp))
                    }

                    // Central Group (Card, Info, Slider, Controls) - centered perfectly
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Hero Art / Card (Smaller: 120.dp)
                        Card(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                MaterialTheme.colorScheme.tertiaryContainer
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📖", fontSize = 36.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "القرآن الكريم",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Column containing Surah Name, Reciter Name, and Slider (Closely attached)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        ) {
                            // 1. Surah Name (In primary color, bold, first)
                            Text(
                                text = displaySurahName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            // 2. Reciter Name (Closely attached)
                            if (displayReciterName.isNotEmpty()) {
                                Text(
                                    text = displayReciterName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // 3. Timeline Slider (Closely attached)
                            val activePos = if (sliderPosition >= 0f) sliderPosition.toLong() else position
                            val maxDur = duration.coerceAtLeast(1L)

                            Slider(
                                value = activePos.toFloat().coerceIn(0f, maxDur.toFloat()),
                                valueRange = 0f..maxDur.toFloat(),
                                onValueChange = { sliderPosition = it },
                                onValueChangeFinished = {
                                    PlayerManager.seekTo(sliderPosition.toLong())
                                    sliderPosition = -1f
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(20.dp) // very tight height to keep it closely attached
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = PlayerManager.formatTime(activePos),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = PlayerManager.formatTime(maxDur),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Symmetrical Playback Controls Row (Beautiful, sleek layout in the center)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Repeat Button
                            IconButton(onClick = { PlayerManager.toggleRepeatMode() }) {
                                Icon(
                                    imageVector = if (repeatMode == AppRepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                    contentDescription = "تكرار",
                                    tint = if (repeatMode != AppRepeatMode.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Skip Previous
                            IconButton(onClick = { PlayerManager.playPrev() }) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "السورة السابقة",
                                    modifier = Modifier.size(26.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Rewind 10s
                            IconButton(onClick = { PlayerManager.rewind(10000L) }) {
                                Icon(
                                    imageVector = Icons.Default.Replay10,
                                    contentDescription = "إرجاع 10 ثواني",
                                    modifier = Modifier.size(26.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Play/Pause Big FAB
                            FloatingActionButton(
                                onClick = { PlayerManager.togglePlayPause() },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                shape = CircleShape,
                                modifier = Modifier.size(60.dp),
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            // Fast Forward 10s
                            IconButton(onClick = { PlayerManager.fastForward(10000L) }) {
                                Icon(
                                    imageVector = Icons.Default.Forward10,
                                    contentDescription = "تقديم 10 ثواني",
                                    modifier = Modifier.size(26.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Skip Next
                            IconButton(onClick = { PlayerManager.playNext() }) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "السورة التالية",
                                    modifier = Modifier.size(26.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Sleep Timer Button
                            IconButton(onClick = { showSleepTimerDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "مؤقت النوم",
                                    tint = if (sleepTimerMinutes > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSleepTimerDialog) {
        SleepTimerDialog(
            currentMinutes = sleepTimerMinutes,
            onSelectMinutes = { mins ->
                PlayerManager.setSleepTimer(mins)
                showSleepTimerDialog = false
            },
            onDismiss = { showSleepTimerDialog = false }
        )
    }
}

@Composable
fun SleepTimerDialog(
    currentMinutes: Int,
    onSelectMinutes: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        0 to "🛑 إيقاف المؤقت",
        5 to "⏱️ 5 دقائق",
        10 to "⏱️ 10 دقائق",
        15 to "⏱️ 15 دقيقة",
        30 to "⏱️ 30 دقيقة",
        45 to "⏱️ 45 دقيقة",
        60 to "⏱️ 60 دقيقة (ساعة واحدة)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "مؤقت النوم ⏱️",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "سيتم إيقاف تشغيل الصوت تلقائياً بعد انقضاء الوقت المحدد:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                options.forEach { (mins, label) ->
                    val isSelected = currentMinutes == mins
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectMinutes(mins) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Text("✓", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

