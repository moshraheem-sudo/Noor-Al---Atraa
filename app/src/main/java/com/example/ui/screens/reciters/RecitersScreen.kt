package com.example.ui.screens.reciters

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.isActive
import com.example.audio.PlayerManager
import com.example.audio.AudioTrack
import java.io.File

val SURAH_NAMES = arrayOf(
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

data class DownloadedItem(
    val fileKey: String,
    val reciterId: Int,
    val reciterName: String,
    val styleId: Int,
    val styleName: String,
    val surahId: Int,
    val surahName: String,
    val file: File
)

enum class ReciterTab { ALL, FAVORITES, DOWNLOADED }

@Composable
fun RecitersScreen(
    onReciterSelected: (Int) -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(ReciterTab.ALL) }

    val reciters = remember {
        listOf(
            ReciterItem(51, "عبد الباسط عبد الصمد", listOf("مصر", "مجود")),
            ReciterItem(112, "محمد صديق المنشاوي", listOf("مصر", "مجود")),
            ReciterItem(118, "محمود خليل الحصري", listOf("مصر", "معلم")),
            ReciterItem(125, "مصطفى إسماعيل", listOf("مصر")),
            ReciterItem(81, "فارس عباد", listOf("اليمن")),
            ReciterItem(30, "سعد الغامدي", listOf("السعودية")),
            ReciterItem(241, "محمد رفعت", listOf("مصر", "مجود"))
        )
    }

    // Load Favorite Reciter IDs from SharedPreferences
    val favoriteIds = remember(selectedTab) {
        val prefs = context.getSharedPreferences("quran_app_prefs", Context.MODE_PRIVATE)
        prefs.getStringSet("favorite_reciters", emptySet()) ?: emptySet()
    }

    // Load Downloaded Items across all reciters
    var downloadedItems by remember(selectedTab) {
        mutableStateOf(loadDownloadedItems(context))
    }

    var favoriteSurahs by remember(selectedTab) {
        mutableStateOf(loadFavoriteSurahs(context))
    }

    var favoritesSet by remember(selectedTab) {
        val prefs = context.getSharedPreferences("reciter_prefs", Context.MODE_PRIVATE)
        mutableStateOf(prefs.getStringSet("favorites", emptySet()) ?: emptySet())
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { (context as? android.app.Activity)?.finish() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = "القُرّاء",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                // General Segmented Tab Control for Reciters window - Centered & Premium
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                            .padding(4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomTabButton(
                                selected = selectedTab == ReciterTab.ALL,
                                onClick = { selectedTab = ReciterTab.ALL },
                                text = "الكل",
                                icon = Icons.Default.List
                            )
                            CustomTabButton(
                                selected = selectedTab == ReciterTab.FAVORITES,
                                onClick = { selectedTab = ReciterTab.FAVORITES },
                                text = "المفضلة",
                                icon = Icons.Default.Bookmark
                            )
                            CustomTabButton(
                                selected = selectedTab == ReciterTab.DOWNLOADED,
                                onClick = {
                                    downloadedItems = loadDownloadedItems(context)
                                    selectedTab = ReciterTab.DOWNLOADED
                                },
                                text = "المحملة",
                                icon = Icons.Default.Download
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when (selectedTab) {
            ReciterTab.ALL -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(reciters) { reciter ->
                        ReciterCardItem(reciter = reciter, onClick = { onReciterSelected(reciter.id) })
                    }
                }
            }

            ReciterTab.FAVORITES -> {
                val favReciters = reciters.filter { favoriteIds.contains(it.id.toString()) }
                if (favReciters.isEmpty() && favoriteSurahs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "قائمة المفضلة فارغة حالياً 🔖\nيمكنك إضافة القراء أو السور للمفضلة.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        // Section 1: Favorite Surahs (السور المفضلة)
                        if (favoriteSurahs.isNotEmpty()) {
                            item {
                                Text(
                                    text = "📖 السور المفضلة (${favoriteSurahs.size})",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            
                            items(favoriteSurahs) { favSurah ->
                                FavoriteSurahRowItem(
                                    item = favSurah,
                                    onRemove = {
                                        val prefs = context.getSharedPreferences("reciter_prefs", Context.MODE_PRIVATE)
                                        val currentFavs = prefs.getStringSet("favorites", emptySet())?.toMutableSet() ?: mutableSetOf()
                                        currentFavs.remove(favSurah.favKey)
                                        prefs.edit()
                                            .putStringSet("favorites", currentFavs)
                                            .remove("${favSurah.favKey}_metadata")
                                            .apply()
                                            
                                        favoriteSurahs = loadFavoriteSurahs(context)
                                        Toast.makeText(context, "تمت إزالة السورة من المفضلة", Toast.LENGTH_SHORT).show()
                                    },
                                    onPlay = {
                                        val playUrl = getAudioPlayUrl(context, favSurah)
                                        PlayerManager.play(
                                            url = playUrl,
                                            title = "سورة ${favSurah.surahName}",
                                            reciterName = favSurah.reciterName,
                                            styleName = favSurah.styleName,
                                            surahId = favSurah.surahId
                                        )
                                        Toast.makeText(context, "جاري تشغيل سورة ${favSurah.surahName}", Toast.LENGTH_SHORT).show()
                                    },
                                    onDownloadComplete = {
                                        downloadedItems = loadDownloadedItems(context)
                                    }
                                )
                            }
                        }
                        
                        // Section 2: Favorite Reciters (القراء المفضلون)
                        if (favReciters.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "🎙️ القراء المفضلون (${favReciters.size})",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE6C280),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            
                            items(favReciters) { reciter ->
                                ReciterCardItem(reciter = reciter, onClick = { onReciterSelected(reciter.id) })
                            }
                        }
                    }
                }
            }

            ReciterTab.DOWNLOADED -> {
                if (downloadedItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد سور محمّلة حالياً 💾\nيمكنك تحميل السور للاستماع إليها أوفلاين.",
                            color = Color(0xFFC5B08A),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                } else {
                    val groupedByReciter = downloadedItems.groupBy { it.reciterName }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(groupedByReciter.keys.toList()) { reciterName ->
                            val itemsForReciter = groupedByReciter[reciterName] ?: emptyList()
                            DownloadedReciterCard(
                                reciterName = reciterName,
                                itemsForReciter = itemsForReciter,
                                favoritesSet = favoritesSet,
                                onToggleFavorite = { item ->
                                    val prefs = context.getSharedPreferences("reciter_prefs", Context.MODE_PRIVATE)
                                    val currentFavs = prefs.getStringSet("favorites", emptySet())?.toMutableSet() ?: mutableSetOf()
                                    val favKey = "${item.reciterId}_${item.styleId}_${item.surahId}"
                                    if (currentFavs.contains(favKey)) {
                                        currentFavs.remove(favKey)
                                        prefs.edit()
                                            .putStringSet("favorites", currentFavs)
                                            .remove("${favKey}_metadata")
                                            .apply()
                                        Toast.makeText(context, "تمت إزالة السورة من المفضلة", Toast.LENGTH_SHORT).show()
                                    } else {
                                        currentFavs.add(favKey)
                                        prefs.edit()
                                            .putStringSet("favorites", currentFavs)
                                            .putString("${favKey}_metadata", "${item.reciterName}|${item.styleName}||${item.surahName}")
                                            .apply()
                                        Toast.makeText(context, "تمت إضافة السورة للمفضلة", Toast.LENGTH_SHORT).show()
                                    }
                                    favoritesSet = prefs.getStringSet("favorites", emptySet()) ?: emptySet()
                                    favoriteSurahs = loadFavoriteSurahs(context)
                                },
                                onItemDelete = { itemToDelete ->
                                    if (itemToDelete.file.exists()) {
                                        itemToDelete.file.delete()
                                    }
                                    downloadedItems = loadDownloadedItems(context)
                                    Toast.makeText(
                                        context,
                                        "تم حذف سورة ${itemToDelete.surahName}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadedReciterCard(
    reciterName: String,
    itemsForReciter: List<DownloadedItem>,
    favoritesSet: Set<String>,
    onToggleFavorite: (DownloadedItem) -> Unit,
    onItemDelete: (DownloadedItem) -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row - Clicking toggles expand/collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = reciterName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${itemsForReciter.size} سورة محمّلة",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    var showDeleteDialog by remember { mutableStateOf(false) }
                    
                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("تأكيد الحذف") },
                            text = { Text("هل أنت متأكد من حذف جميع الملفات المحملة لهذا القارئ؟") },
                            confirmButton = {
                                TextButton(onClick = {
                                    itemsForReciter.forEach { item ->
                                        if (item.file.exists()) {
                                            item.file.delete()
                                        }
                                        onItemDelete(item)
                                    }
                                    showDeleteDialog = false
                                }) {
                                    Text("حذف", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text("إلغاء")
                                }
                            }
                        )
                    }

                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف الكل",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "إغلاق" else "فتح",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Expandable Content: Styles (المصحف المرتل / المصحف المجود) and Surahs inside
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 1.dp)

                    val groupedByStyle = itemsForReciter.groupBy { it.styleName }
                    groupedByStyle.forEach { (styleName, surahsInStyle) ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Subheader for Recitation Style (e.g., المصحف المرتل / المصحف المجود)
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "📜 $styleName (${surahsInStyle.size} سورة)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }

                            // Surahs inside this style
                            surahsInStyle.forEach { downloadedItem ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val playlistTracks = surahsInStyle.map { item ->
                                                AudioTrack(
                                                    url = item.file.absolutePath,
                                                    title = "سورة ${item.surahName}",
                                                    surahName = "سورة ${item.surahName}",
                                                    reciterName = item.reciterName,
                                                    styleName = item.styleName,
                                                    surahId = item.surahId
                                                )
                                            }
                                            val clickedIndex = playlistTracks.indexOfFirst { it.surahId == downloadedItem.surahId }.coerceAtLeast(0)
                                            PlayerManager.playList(playlistTracks, clickedIndex)
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = "تشغيل",
                                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "سورة ${downloadedItem.surahName}",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                         val isFav = favoritesSet.contains("${downloadedItem.reciterId}_${downloadedItem.styleId}_${downloadedItem.surahId}")
                                         IconButton(onClick = { onToggleFavorite(downloadedItem) }) {
                                             Icon(
                                                 imageVector = if (isFav) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                 contentDescription = "المفضلة",
                                                 tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                 modifier = Modifier.size(18.dp)
                                             )
                                         }
                                        IconButton(onClick = {
                                             shareDownloadedFile(context, downloadedItem.file, downloadedItem.surahName, downloadedItem.reciterName)
                                         }) {
                                             Icon(
                                                 imageVector = Icons.Default.Share,
                                                 contentDescription = "مشاركة",
                                                 tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                 modifier = Modifier.size(18.dp)
                                             )
                                         }
                                         IconButton(onClick = { onItemDelete(downloadedItem) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "حذف",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
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

@Composable
fun ReciterCardItem(
    reciter: ReciterItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = reciter.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reciter.styles.joinToString(" • "),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

val globalStyleMap = mapOf(
    1 to "حفص عن عاصم - مرتل",
    10 to "حفص عن عاصم - مرتل",
    100 to "حفص عن عاصم - مرتل",
    133 to "المصحف المجود",
    103 to "المصحف المعلم",
    102 to "حفص عن عاصم - مرتل",
    105 to "حفص عن عاصم - مرتل",
    10912 to "المصحف المجود",
    106 to "حفص عن عاصم - مرتل",
    107 to "حفص عن عاصم - مرتل",
    108 to "حفص عن عاصم - مرتل",
    320 to "حفص عن عاصم - تلاوة مميزة",
    109 to "حفص عن عاصم - مرتل",
    11 to "المصحف المعلم",
    110 to "حفص عن عاصم - مرتل",
    111 to "حفص عن عاصم - مرتل",
    10924 to "حفص عن عاصم - تسجيل عام 1387 هـ - 1967م",
    114 to "المصحف المعلم",
    113 to "المصحف المجود",
    112 to "حفص عن عاصم - مرتل",
    115 to "حفص عن عاصم - مرتل",
    119 to "المصحف المجود",
    118 to "حفص عن عاصم - مرتل",
    12 to "حفص عن عاصم - مرتل",
    122 to "المصحف المجود",
    121 to "حفص عن عاصم - مرتل",
    123 to "حفص عن عاصم - مرتل",
    288 to "المصحف المجود",
    125 to "حفص عن عاصم - مرتل",
    126 to "حفص عن عاصم - مرتل",
    127 to "حفص عن عاصم - مرتل",
    128 to "حفص عن عاصم - مرتل",
    182 to "حفص عن عاصم - مرتل",
    13 to "حفص عن عاصم - مرتل",
    135 to "حفص عن عاصم - مرتل",
    136 to "حفص عن عاصم - مرتل",
    137 to "حفص عن عاصم - مرتل",
    139 to "حفص عن عاصم - مرتل",
    149 to "حفص عن عاصم - مرتل",
    15 to "حفص عن عاصم - مرتل",
    150 to "حفص عن عاصم - مرتل",
    152 to "حفص عن عاصم - مرتل",
    153 to "حفص عن عاصم - مرتل",
    154 to "حفص عن عاصم - مرتل",
    159 to "حفص عن عاصم - مرتل",
    160 to "حفص عن عاصم - مرتل",
    161 to "حفص عن عاصم - مرتل",
    162 to "حفص عن عاصم - مرتل",
    163 to "حفص عن عاصم - مرتل",
    164 to "حفص عن عاصم - مرتل",
    165 to "حفص عن عاصم - مرتل",
    166 to "حفص عن عاصم - مرتل",
    167 to "حفص عن عاصم - مرتل",
    17 to "حفص عن عاصم - مرتل",
    232 to "حفص عن عاصم - مرتل",
    18 to "حفص عن عاصم - مرتل",
    181 to "حفص عن عاصم - مرتل",
    183 to "حفص عن عاصم - مرتل",
    184 to "حفص عن عاصم - مرتل",
    185 to "حفص عن عاصم - مرتل",
    187 to "حفص عن عاصم - مرتل",
    188 to "حفص عن عاصم - مرتل",
    189 to "حفص عن عاصم - مرتل",
    19 to "حفص عن عاصم - مرتل",
    190 to "حفص عن عاصم - مرتل",
    191 to "حفص عن عاصم - مرتل",
    192 to "حفص عن عاصم - مرتل",
    193 to "حفص عن عاصم - مرتل",
    194 to "حفص عن عاصم - مرتل",
    197 to "حفص عن عاصم - مرتل",
    198 to "حفص عن عاصم - مرتل",
    2 to "حفص عن عاصم - مرتل",
    20 to "حفص عن عاصم - مرتل",
    201 to "حفص عن عاصم - مرتل",
    202 to "حفص عن عاصم - مرتل",
    203 to "حفص عن عاصم - مرتل",
    204 to "حفص عن عاصم - مرتل",
    205 to "حفص عن عاصم - مرتل",
    206 to "حفص عن عاصم - مرتل",
    207 to "حفص عن عاصم - مرتل",
    209 to "حفص عن عاصم - مرتل",
    21 to "حفص عن عاصم - مرتل",
    10914 to "حفص عن عاصم - مرتل",
    10915 to "حفص عن عاصم - مرتل",
    340 to "حفص عن عاصم - مرتل",
    10904 to "حفص عن عاصم - تلاوة مميزة",
    10905 to "حفص عن عاصم - مرتل",
    10906 to "حفص عن عاصم - مرتل",
    10908 to "حفص عن عاصم - مرتل",
    10909 to "حفص عن عاصم - مرتل",
    10910 to "حفص عن عاصم - مرتل",
    10911 to "حفص عن عاصم - مرتل",
    10913 to "حفص عن عاصم - مرتل",
    10917 to "حفص عن عاصم - مرتل",
    10918 to "حفص عن عاصم - مرتل",
    10922 to "حفص عن عاصم - مرتل",
    10923 to "حفص عن عاصم - مرتل",
    216 to "حفص عن عاصم - مرتل",
    217 to "حفص عن عاصم - مرتل",
    218 to "حفص عن عاصم - مرتل",
    219 to "حفص عن عاصم - مرتل",
    22 to "حفص عن عاصم - مرتل",
    221 to "حفص عن عاصم - مرتل",
    225 to "حفص عن عاصم - مرتل",
    226 to "حفص عن عاصم - مرتل",
    227 to "حفص عن عاصم - مرتل",
    228 to "حفص عن عاصم - مرتل",
    229 to "حفص عن عاصم - مرتل",
    23 to "حفص عن عاصم - مرتل",
    230 to "حفص عن عاصم - مرتل",
    231 to "حفص عن عاصم - مرتل",
    236 to "حفص عن عاصم - مرتل",
    24 to "حفص عن عاصم - مرتل",
    240 to "حفص عن عاصم - مرتل",
    241 to "حفص عن عاصم - مرتل",
    286 to "المصحف المعلم",
    243 to "حفص عن عاصم - مرتل",
    244 to "حفص عن عاصم - مرتل",
    245 to "حفص عن عاصم - مرتل",
    246 to "حفص عن عاصم - مرتل",
    247 to "حفص عن عاصم - مرتل",
    248 to "حفص عن عاصم - مرتل",
    25 to "حفص عن عاصم - مرتل",
    250 to "حفص عن عاصم - مرتل",
    251 to "حفص عن عاصم - مرتل",
    252 to "حفص عن عاصم - مرتل",
    253 to "حفص عن عاصم - مرتل",
    254 to "حفص عن عاصم - مرتل",
    255 to "حفص عن عاصم - مرتل",
    256 to "حفص عن عاصم - مرتل",
    257 to "حفص عن عاصم - مرتل",
    259 to "حفص عن عاصم - مرتل",
    260 to "حفص عن عاصم - مرتل",
    263 to "حفص عن عاصم - مرتل",
    265 to "حفص عن عاصم - مرتل",
    267 to "حفص عن عاصم - مرتل",
    268 to "حفص عن عاصم - مرتل",
    261 to "حفص عن عاصم - مرتل",
    271 to "حفص عن عاصم - مرتل",
    273 to "حفص عن عاصم - تلاوة مميزة",
    277 to "حفص عن عاصم - مرتل",
    289 to "حفص عن عاصم - مرتل",
    28 to "حفص عن عاصم - مرتل",
    292 to "حفص عن عاصم - مرتل",
    293 to "حفص عن عاصم - مرتل",
    294 to "حفص عن عاصم - مرتل",
    295 to "حفص عن عاصم - مرتل",
    297 to "حفص عن عاصم - مرتل",
    298 to "حفص عن عاصم - مرتل",
    299 to "حفص عن عاصم - مرتل",
    302 to "حفص عن عاصم - مرتل",
    303 to "حفص عن عاصم - مرتل",
    304 to "حفص عن عاصم - مرتل",
    281 to "حفص عن عاصم - مرتل",
    306 to "حفص عن عاصم - مرتل",
    3 to "حفص عن عاصم - مرتل",
    30 to "حفص عن عاصم - مرتل",
    300 to "حفص عن عاصم - مرتل",
    307 to "حفص عن عاصم - مرتل",
    314 to "حفص عن عاصم - مرتل",
    315 to "حفص عن عاصم - مرتل",
    316 to "حفص عن عاصم - مرتل",
    318 to "حفص عن عاصم - مرتل",
    31 to "حفص عن عاصم - مرتل",
    32 to "حفص عن عاصم - مرتل",
    33 to "حفص عن عاصم - مرتل",
    34 to "حفص عن عاصم - مرتل",
    35 to "حفص عن عاصم - مرتل",
    36 to "حفص عن عاصم - مرتل",
    37 to "حفص عن عاصم - مرتل",
    38 to "حفص عن عاصم - مرتل",
    39 to "حفص عن عاصم - مرتل",
    4 to "حفص عن عاصم - مرتل",
    40 to "حفص عن عاصم - مرتل",
    41 to "حفص عن عاصم - مرتل",
    42 to "حفص عن عاصم - مرتل",
    43 to "حفص عن عاصم - مرتل",
    44 to "حفص عن عاصم - مرتل",
    46 to "حفص عن عاصم - مرتل",
    283 to "حفص عن عاصم - مرتل",
    48 to "حفص عن عاصم - مرتل",
    49 to "حفص عن عاصم - مرتل",
    5 to "حفص عن عاصم - مرتل",
    169 to "المصحف المعلم",
    50 to "حفص عن عاصم - مرتل",
    53 to "حفص عن عاصم - مرتل",
    51 to "المصحف المجود",
    54 to "حفص عن عاصم - مرتل",
    55 to "حفص عن عاصم - مرتل",
    56 to "حفص عن عاصم - مرتل",
    57 to "حفص عن عاصم - مرتل",
    58 to "حفص عن عاصم - مرتل",
    59 to "حفص عن عاصم - مرتل",
    6 to "حفص عن عاصم - مرتل",
    60 to "حفص عن عاصم - مرتل",
    61 to "حفص عن عاصم - مرتل",
    62 to "حفص عن عاصم - مرتل",
    63 to "حفص عن عاصم - مرتل",
    258 to "حفص عن عاصم - مرتل",
    66 to "حفص عن عاصم - مرتل",
    67 to "حفص عن عاصم - مرتل",
    68 to "حفص عن عاصم - مرتل",
    69 to "حفص عن عاصم - مرتل",
    7 to "حفص عن عاصم - مرتل",
    70 to "حفص عن عاصم - مرتل",
    71 to "حفص عن عاصم - مرتل",
    72 to "حفص عن عاصم - مرتل",
    73 to "حفص عن عاصم - مرتل",
    74 to "حفص عن عاصم - مرتل",
    76 to "حفص عن عاصم - مرتل",
    77 to "حفص عن عاصم - مرتل",
    78 to "حفص عن عاصم - مرتل",
    282 to "حفص عن عاصم - مرتل",
    8 to "حفص عن عاصم - مرتل",
    81 to "حفص عن عاصم - مرتل",
    82 to "حفص عن عاصم - مرتل",
    83 to "حفص عن عاصم - مرتل",
    84 to "حفص عن عاصم - مرتل",
    85 to "حفص عن عاصم - مرتل",
    86 to "حفص عن عاصم - مرتل",
    87 to "حفص عن عاصم - مرتل",
    88 to "حفص عن عاصم - مرتل",
    89 to "حفص عن عاصم - مرتل",
    9 to "حفص عن عاصم - مرتل",
    90 to "حفص عن عاصم - مرتل",
    92 to "حفص عن عاصم - مرتل",
    93 to "حفص عن عاصم - مرتل",
    94 to "حفص عن عاصم - مرتل",
    96 to "حفص عن عاصم - مرتل",
    97 to "حفص عن عاصم - مرتل",
    284 to "حفص عن عاصم - مرتل",
)

fun loadDownloadedItems(context: Context): List<DownloadedItem> {
    val audioDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "quran_audio")
    if (!audioDir.exists()) return emptyList()
    val files = audioDir.listFiles() ?: return emptyList()
    val items = mutableListOf<DownloadedItem>()
    val prefs = context.getSharedPreferences("reciter_prefs", Context.MODE_PRIVATE)
    
    val recitersMap = mapOf(
        51 to "عبد الباسط عبد الصمد",
        112 to "محمد صديق المنشاوي",
        118 to "محمود خليل الحصري",
        125 to "مصطفى إسماعيل",
        81 to "فارس عباد",
        30 to "سعد الغامدي",
        241 to "محمد رفعت"
    )
    for (file in files) {
        if (file.isFile && file.name.endsWith(".mp3")) {
            val nameWithoutExt = file.nameWithoutExtension
            val parts = nameWithoutExt.split("_")
            if (parts.size == 3) {
                val rId = parts[0].toIntOrNull() ?: continue
                val sStyleId = parts[1].toIntOrNull() ?: 1
                val sId = parts[2].toIntOrNull() ?: continue
                
                // Fallback map for style names if not found in SharedPreferences
                val defaultStyleName = globalStyleMap[sStyleId] ?: "قراءة $sStyleId"
                
                val rName = prefs.getString("reciter_name_$rId", recitersMap[rId] ?: "قارئ #$rId") ?: "قارئ #$rId"
                val styleName = prefs.getString("style_name_$sStyleId", defaultStyleName) ?: defaultStyleName
                
                val surahName = SURAH_NAMES.getOrNull(sId - 1) ?: "سورة #$sId"
                items.add(
                    DownloadedItem(
                        fileKey = nameWithoutExt,
                        reciterId = rId,
                        reciterName = rName,
                        styleId = sStyleId,
                        styleName = styleName,
                        surahId = sId,
                        surahName = surahName,
                        file = file
                    )
                )
            }
        }
    }
    return items.sortedWith(compareBy({ it.reciterName }, { it.styleName }, { it.surahId }))
}

data class ReciterItem(val id: Int, val name: String, val styles: List<String>)

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

@Composable
private fun CustomTabButton(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                color = contentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

data class FavoriteSurahItem(
    val favKey: String,
    val reciterId: Int,
    val reciterName: String,
    val styleId: Int,
    val styleName: String,
    val surahId: Int,
    val surahName: String,
    val serverUrl: String
)

fun loadFavoriteSurahs(context: Context): List<FavoriteSurahItem> {
    val prefs = context.getSharedPreferences("reciter_prefs", Context.MODE_PRIVATE)
    val favoritesSet = prefs.getStringSet("favorites", emptySet()) ?: emptySet()
    val list = mutableListOf<FavoriteSurahItem>()
    
    val recitersMap = mapOf(
        51 to "عبد الباسط عبد الصمد",
        112 to "محمد صديق المنشاوي",
        118 to "محمود خليل الحصري",
        125 to "مصطفى إسماعيل",
        81 to "فارس عباد",
        30 to "سعد الغامدي",
        241 to "محمد رفعت"
    )
    
    for (key in favoritesSet) {
        val parts = key.split("_")
        if (parts.size == 3) {
            val rId = parts[0].toIntOrNull() ?: continue
            val styleId = parts[1].toIntOrNull() ?: 1
            val surahId = parts[2].toIntOrNull() ?: continue
            
            // Try loading from saved metadata
            val metadata = prefs.getString("${key}_metadata", null)
            val reciterName: String
            val styleName: String
            val server: String
            val surahName: String
            
            if (metadata != null) {
                val mParts = metadata.split("|")
                if (mParts.size >= 4) {
                    reciterName = mParts[0]
                    styleName = mParts[1]
                    server = mParts[2]
                    surahName = mParts[3]
                } else {
                    reciterName = prefs.getString("reciter_name_$rId", recitersMap[rId] ?: "قارئ #$rId") ?: "قارئ #$rId"
                    val defaultStyle = globalStyleMap[styleId] ?: "قراءة $styleId"
                    styleName = prefs.getString("style_name_$styleId", defaultStyle) ?: defaultStyle
                    server = ""
                    surahName = SURAH_NAMES.getOrNull(surahId - 1) ?: "سورة #$surahId"
                }
            } else {
                reciterName = prefs.getString("reciter_name_$rId", recitersMap[rId] ?: "قارئ #$rId") ?: "قارئ #$rId"
                val defaultStyle = globalStyleMap[styleId] ?: "قراءة $styleId"
                styleName = prefs.getString("style_name_$styleId", defaultStyle) ?: defaultStyle
                server = ""
                surahName = SURAH_NAMES.getOrNull(surahId - 1) ?: "سورة #$surahId"
            }
            
            list.add(
                FavoriteSurahItem(
                    favKey = key,
                    reciterId = rId,
                    reciterName = reciterName,
                    styleId = styleId,
                    styleName = styleName,
                    surahId = surahId,
                    surahName = surahName,
                    serverUrl = server
                )
            )
        }
    }
    return list.sortedWith(compareBy({ it.reciterName }, { it.surahId }))
}

fun getAudioPlayUrl(context: Context, item: FavoriteSurahItem): String {
    val audioDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "quran_audio")
    val localFile = File(audioDir, "${item.reciterId}_${item.styleId}_${item.surahId}.mp3")
    if (localFile.exists()) {
        return localFile.absolutePath
    }
    val paddedId = item.surahId.toString().padStart(3, '0')
    val base = item.serverUrl.ifEmpty { "https://server8.mp3quran.net/frs_a/" }
    return if (base.endsWith("/")) "$base$paddedId.mp3" else "$base/$paddedId.mp3"
}

@Composable
fun FavoriteSurahRowItem(
    item: FavoriteSurahItem,
    onRemove: () -> Unit,
    onPlay: () -> Unit,
    onDownloadComplete: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Check if downloaded
    val audioDir = remember { File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "quran_audio") }
    val localFile = remember(item.favKey) { File(audioDir, "${item.reciterId}_${item.styleId}_${item.surahId}.mp3") }
    var isDownloaded by remember(item.favKey) { mutableStateOf(localFile.exists()) }
    var downloadingProgress by remember(item.favKey) { mutableStateOf<Float?>(null) }
    var downloadJob by remember(item.favKey) { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Surah Name (Bold, Primary) and Reciter Name (onSurface) separately
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "سورة ${item.surahName}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "بصوت القارئ: ${item.reciterName}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.styleName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Play Button
                IconButton(
                    onClick = onPlay,
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "استماع",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Remove from favorites button
                TextButton(
                    onClick = onRemove,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "إلغاء المفضلة",
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "إلغاء المفضلة",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Share Button
                    IconButton(
                        onClick = {
                            val paddedId = item.surahId.toString().padStart(3, '0')
                            val base = item.serverUrl.ifEmpty { "https://server8.mp3quran.net/frs_a/" }
                            val remoteUrl = if (base.endsWith("/")) "$base$paddedId.mp3" else "$base/$paddedId.mp3"
                            val fileToShare = if (isDownloaded) localFile else null
                            
                            if (fileToShare != null && fileToShare.exists()) {
                                shareDownloadedFile(context, fileToShare, item.surahName, item.reciterName)
                            } else {
                                android.widget.Toast.makeText(context, "يجب أن تنزل الملف الصوتي أولاً لمشاركته", android.widget.Toast.LENGTH_SHORT).show()
                            }
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
                    
                    // Download / Delete / Cancel Button
                    IconButton(
                        onClick = {
                            if (isDownloaded) {
                                if (localFile.exists()) {
                                    localFile.delete()
                                }
                                isDownloaded = false
                                onDownloadComplete()
                                Toast.makeText(context, "تم حذف الملف المحمل", Toast.LENGTH_SHORT).show()
                            } else if (downloadingProgress != null && downloadJob != null) {
                                // Cancel the ongoing download
                                downloadJob?.cancel()
                                downloadJob = null
                                downloadingProgress = null
                                if (localFile.exists()) {
                                    localFile.delete()
                                }
                                Toast.makeText(context, "تم إلغاء التنزيل", Toast.LENGTH_SHORT).show()
                            } else if (downloadingProgress == null) {
                                val paddedId = item.surahId.toString().padStart(3, '0')
                                val base = item.serverUrl.ifEmpty { "https://server8.mp3quran.net/frs_a/" }
                                val remoteUrl = if (base.endsWith("/")) "$base$paddedId.mp3" else "$base/$paddedId.mp3"
                                
                                downloadJob = coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        withContext(Dispatchers.Main) {
                                            downloadingProgress = 0f
                                            Toast.makeText(context, "بدأ التنزيل...", Toast.LENGTH_SHORT).show()
                                        }
                                        
                                        if (!audioDir.exists()) audioDir.mkdirs()
                                        val conn = java.net.URL(remoteUrl).openConnection() as java.net.HttpURLConnection
                                        conn.connect()
                                        
                                        if (conn.responseCode == java.net.HttpURLConnection.HTTP_OK) {
                                            val fileLength = conn.contentLength
                                            val input = conn.inputStream
                                            val output = java.io.FileOutputStream(localFile)
                                            val buffer = ByteArray(4096)
                                            var total: Long = 0
                                            var count: Int = 0
                                            
                                            while (isActive && input.read(buffer).also { count = it } != -1) {
                                                total += count
                                                if (fileLength > 0) {
                                                    val progress = total.toFloat() / fileLength.toFloat()
                                                    withContext(Dispatchers.Main) {
                                                        downloadingProgress = progress
                                                    }
                                                }
                                                output.write(buffer, 0, count)
                                            }
                                            output.flush()
                                            output.close()
                                            input.close()
                                            
                                            if (!isActive) {
                                                if (localFile.exists()) localFile.delete()
                                                throw kotlinx.coroutines.CancellationException("Download cancelled")
                                            }
                                            
                                            withContext(Dispatchers.Main) {
                                                isDownloaded = true
                                                downloadingProgress = null
                                                downloadJob = null
                                                onDownloadComplete()
                                                Toast.makeText(context, "اكتمل التنزيل بنجاح", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            withContext(Dispatchers.Main) {
                                                downloadingProgress = null
                                                downloadJob = null
                                                Toast.makeText(context, "حدث خطأ أثناء الاتصال بالخادم", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } catch (e: kotlinx.coroutines.CancellationException) {
                                        // Handle cancellation cleanly without showing a failure toast
                                        withContext(Dispatchers.Main) {
                                            downloadingProgress = null
                                            downloadJob = null
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            downloadingProgress = null
                                            downloadJob = null
                                            Toast.makeText(context, "فشل التنزيل: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        if (downloadingProgress != null) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { downloadingProgress ?: 0f },
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFFD4AF37)
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إلغاء التنزيل",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        } else if (isDownloaded) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف الملف",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "تنزيل",
                                tint = Color(0xFFC5B08A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


