package com.example.ui.screens.reciters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import com.example.ui.screens.settings.formatBytes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import com.example.audio.AudioTrack
import com.example.audio.PlayerManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReciterDetailScreen(
    reciterId: Int,
    onBack: () -> Unit,
    onPlaySurah: (String, String, Int) -> Unit // url, title, surahId
) {
    val context = LocalContext.current
    val viewModel: ReciterDetailViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(reciterId) {
        viewModel.initContext(context)
        viewModel.loadReciter(reciterId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.reciterName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (state.styles.size > 1) {
                    // Style selector
                    ScrollableTabRow(
                        selectedTabIndex = state.selectedStyleIndex,
                        edgePadding = 16.dp,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        state.styles.forEachIndexed { index, style ->
                            Tab(
                                selected = state.selectedStyleIndex == index,
                                onClick = { viewModel.selectStyle(index) },
                                text = { Text(style.name) }
                            )
                        }
                    }
                }

                val currentStyle = state.styles.getOrNull(state.selectedStyleIndex)
                if (currentStyle != null) {
                    val surahNames = listOf(
                        "الفاتحة", "البقرة", "آل عمران", "النساء", "المائدة", "الأنعام", "الأعراف", "الأنفال", "التوبة", "يونس",
                        "هود", "يوسف", "الرعد", "إبراهيم", "الحجر", "النحل", "الإسراء", "الكهف", "مريم", "طه",
                        "الأنبياء", "الحج", "المؤمنون", "النور", "الفرقان", "الشعراء", "النمل", "القصص", "العنكبوت", "الروم",
                        "لقمان", "السجدة", "الأحزاب", "سبأ", "فاطر", "يس", "الصافات", "ص", "الزمر", "غافر",
                        "فصلت", "الشورى", "الزخرف", "الدخان", "الجاثية", "الأحقاف", "محمد", "الفتح", "الحجرات", "ق",
                        "الذاريات", "الطور", "النجم", "القمر", "الرحمن", "الواقعة", "الحديد", "المجادلة", "الحشر", "الممتحنة",
                        "الصف", "الجمعة", "المنافقون", "التغابن", "الطلاق", "التحريم", "الملك", "القلم", "الحاقة", "المعارج",
                        "نوح", "الجن", "المزمل", "المدثر", "القيامة", "الإنسان", "المرسلات", "النبأ", "النازعات", "عبس",
                        "التكوير", "الانفطار", "المطففين", "الانشقاق", "البروج", "الطارق", "الأعلى", "الغاشية", "الفجر", "البلد",
                        "الشمس", "الليل", "الضحى", "الشرح", "التين", "العلق", "القدر", "البينة", "الزلزلة", "العاديات",
                        "القارعة", "التكاثر", "العصر", "الهمزة", "الفيل", "قريش", "الماعون", "الكوثر", "الكافرون", "النصر",
                        "المسد", "الإخلاص", "الفلق", "الناس"
                    )

                    var searchQuery by remember { mutableStateOf("") }
                    val allSurahs = currentStyle.surahList.split(",").mapNotNull { it.toIntOrNull() }
                    val categoryFilteredSurahs = when {
                        state.showOnlyFavorites -> allSurahs.filter { surahId ->
                            val favKey = "${reciterId}_${currentStyle.id}_$surahId"
                            state.favorites.contains(favKey)
                        }
                        state.showOnlyDownloaded -> allSurahs.filter { surahId ->
                            val itemKey = "${reciterId}_${currentStyle.id}_$surahId"
                            state.downloadedSet.contains(itemKey)
                        }
                        else -> allSurahs
                    }

                    val filteredSurahs = remember(categoryFilteredSurahs, searchQuery, surahNames) {
                        if (searchQuery.isBlank()) {
                            categoryFilteredSurahs
                        } else {
                            val q = searchQuery.trim()
                            categoryFilteredSurahs.filter { surahId ->
                                val name = surahNames.getOrElse(surahId - 1) { "" }
                                name.contains(q, ignoreCase = true) || surahId.toString() == q
                            }
                        }
                    }

                    if (categoryFilteredSurahs.isEmpty() && (state.showOnlyFavorites || state.showOnlyDownloaded)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (state.showOnlyDownloaded) Icons.Default.DownloadForOffline else Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (state.showOnlyDownloaded) "لا توجد صوتيات مُحمّلة لهذا القارئ بعد" else "لا توجد سور مضافة للمفضلة لهذا القارئ",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (state.showOnlyDownloaded) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "اضغط على زر التنزيل ⬇️ بجانب أي سورة لحفظها للاستماع أوفلاين",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Search Surahs Box
                            item {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 4.dp),
                                    placeholder = { Text("ابحث عن سورة بالاسم أو الرقم...", fontSize = 14.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "بحث السور",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "حذف كلمة البحث",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    )
                                )
                            }

                            if (!state.showOnlyFavorites && !state.showOnlyDownloaded && searchQuery.isBlank()) {
                                item {
                                    val downloadedForStyleCount = allSurahs.count { sId ->
                                        state.downloadedSet.contains("${reciterId}_${currentStyle.id}_$sId")
                                    }
                                    FullQuranBatchDownloadCard(
                                        reciterName = state.reciterName,
                                        styleName = currentStyle.name,
                                        totalSurahsCount = allSurahs.size,
                                        downloadedCount = downloadedForStyleCount,
                                        batchState = state.batchDownload,
                                        onStartBatchDownload = {
                                            viewModel.startBatchDownload(context, reciterId, currentStyle, allSurahs, surahNames)
                                        },
                                        onPauseResume = {
                                            viewModel.togglePauseBatchDownload()
                                        },
                                        onCancel = {
                                            viewModel.cancelBatchDownload()
                                        }
                                    )
                                }
                            }

                            if (state.showOnlyDownloaded) {
                                item {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "📜 ${currentStyle.name}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = "${filteredSurahs.size} سورة محملة 💾",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            items(filteredSurahs) { surahId ->
                                val surahIdStr = surahId.toString()
                                val paddedId = surahIdStr.padStart(3, '0')
                                val url = "${currentStyle.server}$paddedId.mp3"
                                val name = surahNames.getOrElse(surahId - 1) { "" }
                                val favKey = "${reciterId}_${currentStyle.id}_$surahId"
                                val isFav = state.favorites.contains(favKey)
                                val itemKey = "${reciterId}_${currentStyle.id}_$surahId"
                                val isDownloaded = state.downloadedSet.contains(itemKey)
                                val progress = state.downloadingProgress[itemKey]

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val playUrl = viewModel.getAudioSourceUrl(context, reciterId, currentStyle.id, surahId, url)
                                            val playlistTracks = filteredSurahs.map { sId ->
                                                val sPadded = sId.toString().padStart(3, '0')
                                                val sRawUrl = "${currentStyle.server}$sPadded.mp3"
                                                val sName = surahNames.getOrElse(sId - 1) { "" }
                                                val sPlayUrl = viewModel.getAudioSourceUrl(context, reciterId, currentStyle.id, sId, sRawUrl)
                                                AudioTrack(
                                                    url = sPlayUrl,
                                                    title = "سورة $sName",
                                                    surahName = "سورة $sName",
                                                    reciterName = state.reciterName,
                                                    styleName = currentStyle.name,
                                                    surahId = sId
                                                )
                                            }
                                            val clickedIndex = playlistTracks.indexOfFirst { it.surahId == surahId }.coerceAtLeast(0)
                                            PlayerManager.playList(playlistTracks, clickedIndex)
                                            onPlaySurah(playUrl, "سورة $name (${state.reciterName})", surahId)
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "سورة $name",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isDownloaded) {
                                                Text(
                                                    text = "مُحمّلة جاهزة للاستماع أوفلاين 💾",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            // Favorite Button
                                            IconButton(
                                                onClick = {
                                                    viewModel.toggleFavorite(
                                                        favKey = favKey,
                                                        reciterName = state.reciterName,
                                                        styleName = currentStyle.name,
                                                        server = currentStyle.server,
                                                        surahName = name
                                                    )
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isFav) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                    contentDescription = "المفضلة",
                                                    tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }

                                            // Download / Delete Button
                                            IconButton(
                                                onClick = {
                                                    if (isDownloaded) {
                                                        viewModel.deleteDownloadedSurah(
                                                            context = context,
                                                            reciterId = reciterId,
                                                            styleId = currentStyle.id,
                                                            surahId = surahId,
                                                            surahName = name
                                                        )
                                                    } else if (progress == null) {
                                                        viewModel.downloadSurah(
                                                            context = context,
                                                            reciterId = reciterId,
                                                            styleId = currentStyle.id,
                                                            surahId = surahId,
                                                            url = url,
                                                            surahName = name
                                                        )
                                                    }
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                if (progress != null) {
                                                    CircularProgressIndicator(
                                                        progress = { progress },
                                                        modifier = Modifier.size(20.dp),
                                                        strokeWidth = 2.5.dp
                                                    )
                                                } else if (isDownloaded) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "حذف السورة المحملة",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.FileDownload,
                                                        contentDescription = "تنزيل السورة",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            }

                                            // Share Button
                                            IconButton(
                                                onClick = {
                                                    viewModel.shareAudio(
                                                        context = context,
                                                        reciterId = reciterId,
                                                        styleId = currentStyle.id,
                                                        surahId = surahId,
                                                        url = url,
                                                        surahName = name,
                                                        reciterName = state.reciterName
                                                    )
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Share,
                                                    contentDescription = "مشاركة",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            // Play Button
                                            IconButton(
                                                onClick = {
                                                    val playUrl = viewModel.getAudioSourceUrl(context, reciterId, currentStyle.id, surahId, url)
                                                    val playlistTracks = filteredSurahs.map { sId ->
                                                        val sPadded = sId.toString().padStart(3, '0')
                                                        val sRawUrl = "${currentStyle.server}$sPadded.mp3"
                                                        val sName = surahNames.getOrElse(sId - 1) { "" }
                                                        val sPlayUrl = viewModel.getAudioSourceUrl(context, reciterId, currentStyle.id, sId, sRawUrl)
                                                        AudioTrack(
                                                            url = sPlayUrl,
                                                            title = "سورة $sName",
                                                            surahName = "سورة $sName",
                                                            reciterName = state.reciterName,
                                                            styleName = currentStyle.name,
                                                            surahId = sId
                                                        )
                                                    }
                                                    val clickedIndex = playlistTracks.indexOfFirst { it.surahId == surahId }.coerceAtLeast(0)
                                                    PlayerManager.playList(playlistTracks, clickedIndex)
                                                    onPlaySurah(playUrl, "سورة $name (${state.reciterName})", surahId)
                                                },
                                                modifier = Modifier.size(36.dp),
                                                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "تشغيل",
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullQuranBatchDownloadCard(
    reciterName: String,
    styleName: String,
    totalSurahsCount: Int,
    downloadedCount: Int,
    batchState: BatchDownloadState,
    onStartBatchDownload: () -> Unit,
    onPauseResume: () -> Unit,
    onCancel: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "تنزيل وحجم ملفات القرآن الكريم ($downloadedCount / $totalSurahsCount سورة)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "طي" else "توسيع",
                    tint = primaryColor
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    if (!batchState.isDownloading && !batchState.isPaused) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "حجم الملفات المحملة: ${formatBytes(batchState.totalBytesDownloaded)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Button(
                                onClick = onStartBatchDownload,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (downloadedCount > 0) "تنزيل الكل ($downloadedCount)" else "تنزيل المصحف كاملاً ⬇️",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    } else {
                        // Active Download Progress Bar
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val overallProgress = if (batchState.totalSurahs > 0) {
                                batchState.downloadedCount.toFloat() / batchState.totalSurahs.toFloat()
                            } else 0f
                            val percentage = (overallProgress * 100).toInt()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (batchState.isPaused) "التنزيل متوقف مؤقتاً ⏸️" else "جاري تنزيل سورة ${batchState.currentSurahName}...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "$percentage%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                            }

                            LinearProgressIndicator(
                                progress = { overallProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = primaryColor,
                                trackColor = primaryColor.copy(alpha = 0.2f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "تم تحميل ${batchState.downloadedCount} من ${batchState.totalSurahs} سورة",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "الحجم: ${formatBytes(batchState.totalBytesDownloaded)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onPauseResume,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (batchState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (batchState.isPaused) "استئناف" else "إيقاف مؤقت", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = onCancel,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("إلغاء التنزيل", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

object GlobalDownloadManager {
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO)
    private var batchJob: kotlinx.coroutines.Job? = null
    
    private val _batchState = MutableStateFlow<Map<String, BatchDownloadState>>(emptyMap())
    val batchState: StateFlow<Map<String, BatchDownloadState>> = _batchState

    var currentReciterId: Int? = null
        private set
    var currentStyleId: Int? = null
        private set

    fun startBatchDownload(
        context: Context,
        reciterId: Int,
        style: ReciterStyle,
        surahList: List<Int>,
        surahNames: List<String>
    ) {
        val key = "${reciterId}_${style.id}"
        if (_batchState.value[key]?.isDownloading == true) return

        currentReciterId = reciterId
        currentStyleId = style.id

        batchJob = scope.launch {
            try {
                val audioDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "quran_audio")
                if (!audioDir.exists()) audioDir.mkdirs()

                var totalBytes = 0L
                val initialDownloaded = mutableSetOf<String>()

                for (sId in surahList) {
                    val itemKey = "${reciterId}_${style.id}_$sId"
                    val file = File(audioDir, "$itemKey.mp3")
                    if (file.exists() && file.length() > 0) {
                        totalBytes += file.length()
                        initialDownloaded.add(itemKey)
                    }
                }

                val downloadedCount = surahList.count { sId ->
                    initialDownloaded.contains("${reciterId}_${style.id}_$sId")
                }

                _batchState.value = _batchState.value + (key to BatchDownloadState(
                    isDownloading = true,
                    totalSurahs = surahList.size,
                    downloadedCount = downloadedCount,
                    totalBytesDownloaded = totalBytes,
                    isPaused = false,
                    downloadedKeys = initialDownloaded.toSet()
                ))

                withContext(Dispatchers.Main) {
                    Toast.makeText(context.applicationContext, "بدأ تنزيل المصحف كاملاً 💾", Toast.LENGTH_SHORT).show()
                }

                for ((index, surahId) in surahList.withIndex()) {
                    val itemKey = "${reciterId}_${style.id}_$surahId"
                    val destFile = File(audioDir, "$itemKey.mp3")

                    if (destFile.exists() && destFile.length() > 0) {
                        continue
                    }

                    while (_batchState.value[key]?.isPaused == true) {
                        kotlinx.coroutines.delay(500)
                    }

                    if (batchJob?.isCancelled == true) break

                    val surahName = surahNames.getOrElse(surahId - 1) { surahId.toString() }
                    val paddedId = surahId.toString().padStart(3, '0')
                    val remoteUrl = "${style.server}$paddedId.mp3"

                    val currentState = _batchState.value[key] ?: BatchDownloadState()
                    _batchState.value = _batchState.value + (key to currentState.copy(
                        currentSurahName = surahName,
                        currentSurahIndex = index + 1,
                        currentSurahProgress = 0f
                    ))

                    try {
                        val connection = URL(remoteUrl).openConnection() as HttpURLConnection
                        connection.connectTimeout = 15000
                        connection.readTimeout = 15000
                        connection.connect()

                        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                            val input = connection.inputStream
                            val output = FileOutputStream(destFile)
                            val buffer = ByteArray(8192)
                            var count: Int

                            while (input.read(buffer).also { count = it } != -1) {
                                if (batchJob?.isCancelled == true) {
                                    output.flush()
                                    output.close()
                                    input.close()
                                    destFile.delete()
                                    break
                                }
                                while (_batchState.value[key]?.isPaused == true) {
                                    kotlinx.coroutines.delay(500)
                                }
                                output.write(buffer, 0, count)

                                val sState = _batchState.value[key] ?: BatchDownloadState()
                                val newTotalBytes = sState.totalBytesDownloaded + count
                                _batchState.value = _batchState.value + (key to sState.copy(
                                    totalBytesDownloaded = newTotalBytes
                                ))
                            }

                            output.flush()
                            output.close()
                            input.close()

                            if (batchJob?.isCancelled == true) {
                                destFile.delete()
                                break
                            }

                            val sState = _batchState.value[key] ?: BatchDownloadState()
                            val newDownloadedCount = sState.downloadedCount + 1
                            initialDownloaded.add(itemKey)

                            _batchState.value = _batchState.value + (key to sState.copy(
                                downloadedCount = newDownloadedCount,
                                downloadedKeys = initialDownloaded.toSet()
                            ))
                        }
                    } catch (e: Exception) {
                        if (destFile.exists() && destFile.length() == 0L) {
                            destFile.delete()
                        }
                    }
                }

                val finalState = _batchState.value[key] ?: BatchDownloadState()
                _batchState.value = _batchState.value + (key to finalState.copy(
                    isDownloading = false,
                    isPaused = false
                ))

                withContext(Dispatchers.Main) {
                    Toast.makeText(context.applicationContext, "تم اكتمال تنزيل المصحف الشريف بنجاح 🎉", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                val sState = _batchState.value[key] ?: BatchDownloadState()
                _batchState.value = _batchState.value + (key to sState.copy(isDownloading = false))
            }
        }
    }

    fun togglePauseBatchDownload(reciterId: Int, styleId: Int) {
        val key = "${reciterId}_$styleId"
        val current = _batchState.value[key] ?: return
        _batchState.value = _batchState.value + (key to current.copy(isPaused = !current.isPaused))
    }

    fun cancelBatchDownload(reciterId: Int, styleId: Int) {
        val key = "${reciterId}_$styleId"
        batchJob?.cancel()
        // do not set to null immediately so isActive becomes false and it breaks
        _batchState.value = _batchState.value - key
    }
}

class ReciterDetailViewModel : ViewModel() {
    private val _state = MutableStateFlow(ReciterDetailState())
    val state: StateFlow<ReciterDetailState> = _state

    private var sharedPrefs: android.content.SharedPreferences? = null
    private var loadedReciterId: Int? = null

    init {
        viewModelScope.launch {
            GlobalDownloadManager.batchState.collect { states ->
                updateBatchDownloadFromGlobal(states)
            }
        }
    }

    private fun updateBatchDownloadFromGlobal(states: Map<String, BatchDownloadState>) {
        val reciterId = loadedReciterId ?: return
        val styles = _state.value.styles
        val styleIndex = _state.value.selectedStyleIndex
        val style = styles.getOrNull(styleIndex) ?: return
        val key = "${reciterId}_${style.id}"
        val bState = states[key] ?: BatchDownloadState()
        
        val mergedDownloadedSet = _state.value.downloadedSet.toMutableSet()
        mergedDownloadedSet.addAll(bState.downloadedKeys)

        _state.value = _state.value.copy(
            batchDownload = bState,
            downloadedSet = mergedDownloadedSet
        )
    }

    fun startBatchDownload(
        context: Context,
        reciterId: Int,
        style: ReciterStyle,
        surahList: List<Int>,
        surahNames: List<String>
    ) {
        GlobalDownloadManager.startBatchDownload(
            context = context,
            reciterId = reciterId,
            style = style,
            surahList = surahList,
            surahNames = surahNames
        )
    }

    fun togglePauseBatchDownload() {
        val reciterId = loadedReciterId ?: return
        val style = _state.value.styles.getOrNull(_state.value.selectedStyleIndex) ?: return
        GlobalDownloadManager.togglePauseBatchDownload(reciterId, style.id)
    }

    fun cancelBatchDownload() {
        val reciterId = loadedReciterId ?: return
        val style = _state.value.styles.getOrNull(_state.value.selectedStyleIndex) ?: return
        GlobalDownloadManager.cancelBatchDownload(reciterId, style.id)
    }

    fun initContext(context: Context) {
        if (sharedPrefs == null) {
            sharedPrefs = context.getSharedPreferences("reciter_prefs", Context.MODE_PRIVATE)
            val favs = sharedPrefs?.getStringSet("favorites", emptySet()) ?: emptySet()
            _state.value = _state.value.copy(favorites = favs)
            checkDownloadedFiles(context)
        }
    }

    private fun checkDownloadedFiles(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val audioDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "quran_audio")
            if (audioDir.exists()) {
                val files = audioDir.listFiles() ?: emptyArray()
                val downloaded = files.map { file ->
                    // filename format: reciterId_styleId_surahId.mp3
                    file.nameWithoutExtension
                }.toSet()
                _state.value = _state.value.copy(downloadedSet = downloaded)
            }
        }
    }

    fun loadReciter(reciterId: Int) {
        loadedReciterId = reciterId
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = "https://www.mp3quran.net/api/v3/reciters?language=ar&reciter=$reciterId"
                val response = URL(url).readText()
                val json = JSONObject(response)
                val reciter = json.getJSONArray("reciters").getJSONObject(0)
                val name = reciter.getString("name")
                val moshafArray = reciter.getJSONArray("moshaf")
                val styles = mutableListOf<ReciterStyle>()
                
                for (i in 0 until moshafArray.length()) {
                    val m = moshafArray.getJSONObject(i)
                    val moshafName = m.getString("name")
                    val isOther = moshafName.contains("ورش") || moshafName.contains("قالون") || moshafName.contains("الدوري") || moshafName.contains("شعبة") || moshafName.contains("السوسي") || moshafName.contains("البزي") || moshafName.contains("قنبل") || moshafName.contains("خلف") || moshafName.contains("روح") || moshafName.contains("رويس") || moshafName.contains("أبي الحارث") || moshafName.contains("ابن ذكوان") || moshafName.contains("هشام") || moshafName.contains("يعقوب") || moshafName.contains("ابن وردان") || moshafName.contains("ابن جماز")
                    if (!isOther) {
                        var cleanedName = moshafName.split("-")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .distinct()
                            .joinToString(" - ")

                        val words = cleanedName.split(" ").map { it.trim() }.filter { it.isNotEmpty() }
                        if (words.size >= 4 && words.size % 2 == 0) {
                            val half = words.size / 2
                            val firstHalf = words.subList(0, half).joinToString(" ")
                            val secondHalf = words.subList(half, words.size).joinToString(" ")
                            if (firstHalf == secondHalf) {
                                cleanedName = firstHalf
                            }
                        }

                        styles.add(
                            ReciterStyle(
                                id = m.getInt("id"),
                                name = cleanedName,
                                server = m.getString("server"),
                                surahList = m.getString("surah_list")
                            )
                        )
                    }
                }
                val distinctStyles = styles.distinctBy { it.name }
                _state.value = _state.value.copy(
                    isLoading = false,
                    reciterName = name,
                    styles = distinctStyles,
                    selectedStyleIndex = 0
                )
                updateBatchDownloadFromGlobal(GlobalDownloadManager.batchState.value)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, reciterName = "خطأ في التحميل")
            }
        }
    }

    fun selectStyle(index: Int) {
        _state.value = _state.value.copy(selectedStyleIndex = index)
        updateBatchDownloadFromGlobal(GlobalDownloadManager.batchState.value)
    }

    fun toggleShowOnlyFavorites() {
        val newFav = !_state.value.showOnlyFavorites
        _state.value = _state.value.copy(
            showOnlyFavorites = newFav,
            showOnlyDownloaded = if (newFav) false else _state.value.showOnlyDownloaded
        )
    }

    fun toggleShowOnlyDownloaded() {
        val newDown = !_state.value.showOnlyDownloaded
        _state.value = _state.value.copy(
            showOnlyDownloaded = newDown,
            showOnlyFavorites = if (newDown) false else _state.value.showOnlyFavorites
        )
    }

    fun toggleFavorite(
        favKey: String,
        reciterName: String = "",
        styleName: String = "",
        server: String = "",
        surahName: String = ""
    ) {
        val currentFavs = _state.value.favorites.toMutableSet()
        val editor = sharedPrefs?.edit()
        if (currentFavs.contains(favKey)) {
            currentFavs.remove(favKey)
            editor?.remove("${favKey}_metadata")
        } else {
            currentFavs.add(favKey)
            if (reciterName.isNotEmpty() && styleName.isNotEmpty() && server.isNotEmpty() && surahName.isNotEmpty()) {
                editor?.putString("${favKey}_metadata", "$reciterName|$styleName|$server|$surahName")
            }
        }
        _state.value = _state.value.copy(favorites = currentFavs)
        editor?.putStringSet("favorites", currentFavs)?.apply()
    }

    fun downloadSurah(
        context: Context,
        reciterId: Int,
        styleId: Int,
        surahId: Int,
        url: String,
        surahName: String
    ) {
        val itemKey = "${reciterId}_${styleId}_$surahId"
        if (_state.value.downloadingProgress.containsKey(itemKey)) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "بدأ تنزيل سورة $surahName...", Toast.LENGTH_SHORT).show()
                }
                val currentProgress = _state.value.downloadingProgress.toMutableMap()
                currentProgress[itemKey] = 0f
                _state.value = _state.value.copy(downloadingProgress = currentProgress)

                val audioDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "quran_audio")
                if (!audioDir.exists()) audioDir.mkdirs()

                val destFile = File(audioDir, "$itemKey.mp3")
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val fileLength = connection.contentLength
                    val input = connection.inputStream
                    val output = FileOutputStream(destFile)

                    val buffer = ByteArray(4096)
                    var total: Long = 0
                    var count: Int
                    while (input.read(buffer).also { count = it } != -1) {
                        total += count
                        if (fileLength > 0) {
                            val progressVal = total.toFloat() / fileLength.toFloat()
                            val updatedMap = _state.value.downloadingProgress.toMutableMap()
                            updatedMap[itemKey] = progressVal
                            _state.value = _state.value.copy(downloadingProgress = updatedMap)
                        }
                        output.write(buffer, 0, count)
                    }

                    output.flush()
                    output.close()
                    input.close()

                    val finalProgress = _state.value.downloadingProgress.toMutableMap()
                    finalProgress.remove(itemKey)

                    val updatedDownloaded = _state.value.downloadedSet.toMutableSet()
                    updatedDownloaded.add(itemKey)

                    _state.value = _state.value.copy(
                        downloadingProgress = finalProgress,
                        downloadedSet = updatedDownloaded
                    )

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "تم تنزيل سورة $surahName بنجاح 🟢", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                val finalProgress = _state.value.downloadingProgress.toMutableMap()
                finalProgress.remove(itemKey)
                _state.value = _state.value.copy(downloadingProgress = finalProgress)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "حدث خطأ أثناء تنزيل سورة $surahName", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun deleteDownloadedSurah(
        context: Context,
        reciterId: Int,
        styleId: Int,
        surahId: Int,
        surahName: String
    ) {
        val itemKey = "${reciterId}_${styleId}_$surahId"
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val audioDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "quran_audio")
                val file = File(audioDir, "$itemKey.mp3")
                if (file.exists()) {
                    file.delete()
                }
                val updatedSet = _state.value.downloadedSet.toMutableSet()
                updatedSet.remove(itemKey)
                _state.value = _state.value.copy(downloadedSet = updatedSet)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "تم حذف سورة $surahName من المحمّلة 🗑️", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun getAudioSourceUrl(context: Context, reciterId: Int, styleId: Int, surahId: Int, remoteUrl: String): String {
        val itemKey = "${reciterId}_${styleId}_$surahId"
        val audioDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "quran_audio")
        val localFile = File(audioDir, "$itemKey.mp3")
        return if (localFile.exists() && localFile.length() > 0) {
            localFile.absolutePath
        } else {
            remoteUrl
        }
    }

    fun shareAudio(
        context: Context,
        reciterId: Int,
        styleId: Int,
        surahId: Int,
        url: String,
        surahName: String,
        reciterName: String
    ) {
        val itemKey = "${reciterId}_${styleId}_$surahId"
        val audioDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "quran_audio")
        val localFile = File(audioDir, "$itemKey.mp3")

        if (localFile.exists() && localFile.length() > 0) {
            shareDownloadedFile(context, localFile, surahName, reciterName)
        } else {
            Toast.makeText(context, "يجب أن تنزل الملف الصوتي أولاً لمشاركته", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareDownloadedFile(context: Context, file: File, surahName: String, reciterName: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "سورة $surahName - بصوت $reciterName")
                putExtra(Intent.EXTRA_TEXT, "تلاوة سورة $surahName بصوت القارئ $reciterName")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "مشاركة تلاوة سورة $surahName"))
        } catch (e: Exception) {
            Toast.makeText(context, "حدث خطأ أثناء المشاركة", Toast.LENGTH_SHORT).show()
        }
    }
}

data class ReciterStyle(
    val id: Int,
    val name: String,
    val server: String,
    val surahList: String
)

data class BatchDownloadState(
    val isDownloading: Boolean = false,
    val totalSurahs: Int = 0,
    val downloadedCount: Int = 0,
    val currentSurahName: String = "",
    val currentSurahIndex: Int = 0,
    val totalBytesDownloaded: Long = 0L,
    val currentSurahProgress: Float = 0f,
    val isPaused: Boolean = false,
    val downloadedKeys: Set<String> = emptySet()
)

data class ReciterDetailState(
    val isLoading: Boolean = true,
    val reciterName: String = "",
    val styles: List<ReciterStyle> = emptyList(),
    val selectedStyleIndex: Int = 0,
    val favorites: Set<String> = emptySet(),
    val downloadedSet: Set<String> = emptySet(),
    val downloadingProgress: Map<String, Float> = emptyMap(),
    val showOnlyFavorites: Boolean = false,
    val showOnlyDownloaded: Boolean = false,
    val batchDownload: BatchDownloadState = BatchDownloadState()
)

