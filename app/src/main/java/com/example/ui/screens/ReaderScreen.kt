package com.example.ui.screens

import android.content.Intent
import androidx.core.content.FileProvider
import android.widget.Toast
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.AVAILABLE_RECITERS
import com.example.data.local.AyahEntity
import com.example.data.local.QuranBookmark
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import com.example.data.local.LastReadManager
import com.example.ui.viewmodels.ReaderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

private val SURAH_NAMES_AR = listOf(
    "الفاتحة", "البقرة", "آل عمران", "النساء", "المائدة", "الأنعام", "الأعراف", "الأنفال", "التوبة", "يونس",
    "هود", "يوسف", "الرعد", "إبراهيم", "الحجر", "النحل", "الإسراء", "الكهف", "مريم", "طه",
    "الأنبياء", "الحج", "المؤمنون", "النور", "الفرقان", "الشعراء", "النمل", "القصص", "العنكبوت", "الروم",
    "لقمان", "السجدة", "سبأ", "فاطر", "يس", "الصافات", "ص", "الزمر", "غافر", "فصلت",
    "الشورى", "الزخرف", "الدخان", "الجاثية", "الأحقاف", "محمد", "الفتح", "الحجرات", "ق", "الذاريات",
    "الطور", "النجم", "القمر", "الرحمن", "الواقعة", "الحديد", "المجادلة", "الحشر", "الممتحنة", "الصف",
    "الجمعة", "المنافقون", "التغابن", "الطلاق", "التحريم", "الملك", "القلم", "الحاقة", "المعارج", "نوح",
    "الجن", "المزمل", "المدثر", "القيامة", "الإنسان", "المرسلات", "النبأ", "النازعات", "عبس", "التكوير",
    "الانفطار", "المطففين", "الانشقاق", "البروج", "الطارق", "الأعلى", "الغاشية", "الفجر", "البلد", "الشمس",
    "الليل", "الضحى", "الشرح", "التين", "العلق", "القدر", "البينة", "الزلزلة", "العاديات", "القارعة",
    "التكاثر", "العصر", "الهمزة", "الفيل", "قريش", "الماعون", "الكوثر", "الكافرون", "النصر", "المسد",
    "الإخلاص", "الفلق", "الناس"
)

val QuranFontFamily = androidx.compose.ui.text.font.FontFamily.Serif

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    surahId: Int,
    initialAyah: Int = -1,
    initialPage: Int = -1,
    isKhatmah: Boolean = false,
    onBack: () -> Unit,
    onNavigateToSurah: ((Int, Int, Int) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                viewModel.stopAudio()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopAudio()
        }
    }

    LaunchedEffect(surahId) {
        viewModel.loadSurah(surahId, context)
    }

    LaunchedEffect(Unit) {
        viewModel.initBookmarks(context)
    }

    val activeSurahId by viewModel.surahId.collectAsStateWithLifecycle()
    val ayahs by viewModel.ayahs.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val surahName by viewModel.surahName.collectAsStateWithLifecycle()
    val surahDetails by viewModel.surahDetails.collectAsStateWithLifecycle()
    val playingAyah by viewModel.currentPlayingAyah.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentReciter by viewModel.currentReciter.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()
    val isCurrentSurahDownloaded by viewModel.isCurrentSurahDownloaded.collectAsStateWithLifecycle()

    LaunchedEffect(activeSurahId, currentReciter) {
        viewModel.checkSurahDownloaded(context, activeSurahId, currentReciter)
    }
    val listState = rememberLazyListState()

    var selectedAyah by remember { mutableStateOf<AyahEntity?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showReciterDialog by remember { mutableStateOf(false) }
    var showGoToPageDialog by remember { mutableStateOf(false) }
    var showPosterDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val bookmarkedAyahNumbers = remember(bookmarks, activeSurahId) {
        bookmarks.filter { it.surahId == activeSurahId }.map { it.ayahNumber }.toSet()
    }
    
    // Check if auto-scroll should be enabled
    var autoScrollEnabled by remember { mutableStateOf(true) }
    
    // Text scale
    var textScale by remember { mutableFloatStateOf(1f) }
    
    // Mushaf mode
    var isMushafMode by remember { mutableStateOf(true) }

    // Highlighted Ayah from Search
    var highlightedAyahNumber by remember(initialAyah) { mutableStateOf(if (initialAyah > 0) initialAyah else null) }

    // Audio Player Bar visibility
    var isPlayerBarVisible by remember { mutableStateOf(false) }

    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    val pageMap = remember(activeSurahId, ayahs) { buildPageMap(activeSurahId, ayahs) }


    // Scroll to initialAyah if provided from search
    var hasScrolledToInitial by remember(initialAyah) { mutableStateOf(false) }
    var hasScrolledToInitialPageNonMushaf by remember(initialPage) { mutableStateOf(false) }
    
    LaunchedEffect(ayahs, initialAyah, initialPage, isMushafMode, pageMap) {
        if (initialAyah > 0 && ayahs.isNotEmpty() && !hasScrolledToInitial) {
            val index = ayahs.indexOfFirst { it.ayahNumber == initialAyah }
            if (index != -1) {
                if (!isMushafMode) {
                    val scrollIndex = (index).coerceAtMost(ayahs.size)
                    listState.scrollToItem(scrollIndex)
                }
                // isMushafMode scrolling is handled inside AuthenticMushafView directly to precisely scroll to the ayah's offset
            }
            hasScrolledToInitial = true
        } else if (initialPage > 0 && ayahs.isNotEmpty() && !hasScrolledToInitialPageNonMushaf && !isMushafMode) {
            // Find the first ayah that belongs to this page
            val firstAyahOnPage = pageMap.entries.firstOrNull { it.value == initialPage }?.key
            if (firstAyahOnPage != null) {
                val index = ayahs.indexOfFirst { it.ayahNumber == firstAyahOnPage }
                if (index != -1) {
                    val scrollIndex = (index).coerceAtMost(ayahs.size)
                    listState.scrollToItem(scrollIndex)
                }
            }
            hasScrolledToInitialPageNonMushaf = true
        }
    }
    var lastSurahId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(playingAyah, autoScrollEnabled, isMushafMode, activeSurahId, ayahs) {
        if (autoScrollEnabled && ayahs.isNotEmpty() && !isMushafMode) {
            val surahChanged = lastSurahId != activeSurahId
            lastSurahId = activeSurahId

            playingAyah?.let { (playingSurahId, playingAyahNumber) ->
                if (playingSurahId == activeSurahId) {
                    val index = ayahs.indexOfFirst { it.ayahNumber == playingAyahNumber }
                    if (index != -1) {
                        val targetIndex = (index + 1).coerceAtMost(ayahs.size)
                        if (surahChanged) {
                            listState.scrollToItem(targetIndex)
                        } else {
                            listState.animateScrollToItem(targetIndex)
                        }
                    }
                }
            } ?: run {
                if (surahChanged && initialAyah <= 0) {
                    listState.scrollToItem(0)
                }
            }
        } else if (ayahs.isNotEmpty() && !isMushafMode) {
            val surahChanged = lastSurahId != activeSurahId
            lastSurahId = activeSurahId
            if (surahChanged && initialAyah <= 0) {
                listState.scrollToItem(0)
            }
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                // Back & Title
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (surahName.isNotEmpty()) "سورة $surahName" else "سورة $activeSurahId",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isKhatmah) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "قراءة الختمة",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
                
                // Settings & Tools
                var isToolsExpanded by remember { mutableStateOf(false) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isToolsExpanded,
                        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandHorizontally(),
                        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkHorizontally()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Offline Download Button
                    if (downloadProgress != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.clickable { viewModel.cancelDownload(context) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "الغاء التحميل",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                CircularProgressIndicator(
                                    progress = { downloadProgress!!.downloadedCount.toFloat() / downloadProgress!!.totalAyahs.coerceAtLeast(1) },
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                val pct = ((downloadProgress!!.downloadedCount.toFloat() / downloadProgress!!.totalAyahs) * 100).toInt()
                                Text(
                                    text = "$pct%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    } else if (isCurrentSurahDownloaded) {
                        var showDeleteMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(
                                onClick = { showDeleteMenu = true },
                                modifier = Modifier.size(34.dp),
                                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = "صوتيات السورة محمّلة بدون إنترنت",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            DropdownMenu(
                                expanded = showDeleteMenu,
                                onDismissRequest = { showDeleteMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("صُوتيات السورة محمّلة بدون إنترنت 🟢", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                    onClick = { }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("إعادة تنزيل صُوتيات السورة", fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    onClick = {
                                        showDeleteMenu = false
                                        viewModel.downloadSurahAudio(context)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("حذف صُوتيات السورة من الجهاز", fontSize = 13.sp, color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showDeleteMenu = false
                                        viewModel.deleteSurahAudio(context)
                                    }
                                )
                            }
                        }
                    } else {
                        IconButton(
                            onClick = { viewModel.downloadSurahAudio(context) },
                            modifier = Modifier.size(34.dp),
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "تنزيل السورة للعمل بدون إنترنت",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(3.dp))
                    IconButton(
                        onClick = { isMushafMode = !isMushafMode },
                        modifier = Modifier.size(34.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isMushafMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = if (isMushafMode) "الآيات المنفصلة" else "وضع المصحف",
                            modifier = Modifier.size(18.dp),
                            tint = if (isMushafMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(3.dp))
                    IconButton(
                        onClick = { textScale = (textScale + 0.1f).coerceAtMost(2f) },
                        modifier = Modifier.size(34.dp),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "تكبير",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(3.dp))
                    IconButton(
                        onClick = { textScale = (textScale - 0.1f).coerceAtLeast(0.5f) },
                        modifier = Modifier.size(34.dp),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomOut,
                            contentDescription = "تصغير",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(3.dp))
                    IconButton(
                        onClick = { showReciterDialog = true },
                        modifier = Modifier.size(34.dp),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "الإعدادات",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    }
                    }
                    Spacer(modifier = Modifier.width(3.dp))
                    IconButton(
                        onClick = { isToolsExpanded = !isToolsExpanded },
                        modifier = Modifier.size(34.dp),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Icon(
                            imageVector = if (isToolsExpanded) Icons.Default.KeyboardArrowRight else Icons.Default.KeyboardArrowLeft,
                            contentDescription = if (isToolsExpanded) "طي الأدوات" else "إظهار الأدوات",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            
            val surahProgress by remember {
                derivedStateOf {
                    val total = listState.layoutInfo.totalItemsCount
                    val firstVisible = listState.firstVisibleItemIndex
                    val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    
                    if (total <= 1) {
                        0f
                    } else if (lastVisible >= total - 1) {
                        1f
                    } else {
                        firstVisible.toFloat() / (total - 1).toFloat()
                    }
                }
            }
            
            LinearProgressIndicator(
                progress = { surahProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = playingAyah != null && !isPlayerBarVisible,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        isPlayerBarVisible = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "عرض شريط الصوت"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentReciter.name.split(" ").firstOrNull() ?: "المقرئ",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        bottomBar = {
            AnimatedVisibility(
                visible = playingAyah != null && isPlayerBarVisible,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                AudioPlayerBar(
                    isPlaying = isPlaying,
                    reciterName = currentReciter.name,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onStop = { viewModel.stopAudio() },
                    onNext = { viewModel.playNext() },
                    onPrev = { viewModel.playPrev() },
                    onHide = { isPlayerBarVisible = false }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (isMushafMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AuthenticMushafView(
                    surahId = activeSurahId,
                    surahName = surahName,
                    surahDetails = surahDetails,
                    ayahs = ayahs,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    playingAyah = playingAyah,
                    bookmarkedAyahNumbers = bookmarkedAyahNumbers,
                    highlightedAyahNumber = highlightedAyahNumber,
                    initialPage = initialPage,
                    isKhatmah = isKhatmah,
                    textScale = textScale,
                    listState = listState,
                    autoScrollEnabled = autoScrollEnabled,
                    onAyahClick = { clickedAyah ->
                        selectedAyah = clickedAyah
                        showBottomSheet = true
                    },
                    onAyahDoubleClick = { clickedAyah ->
                        viewModel.playAyah(clickedAyah.ayahNumber, continuous = true)
                    },
                    onNavigateToSurah = onNavigateToSurah,
                    onLoadSurah = { sId -> viewModel.loadSurah(sId) }
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SurahHeaderBanner(
                        surahDetails = surahDetails,
                        surahId = activeSurahId,
                        surahName = surahName,
                        downloadProgress = downloadProgress,
                        isDownloaded = isCurrentSurahDownloaded,
                        onDownload = { viewModel.downloadSurahAudio(context) },
                        onDelete = { viewModel.deleteSurahAudio(context) }
                    )
                    if (activeSurahId != 9) {
                        BasmalaBanner()
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                itemsIndexed(ayahs) { _, ayah ->
                    val isAyahPlaying = playingAyah?.first == activeSurahId && playingAyah?.second == ayah.ayahNumber
                    val isBookmarked = bookmarkedAyahNumbers.contains(ayah.ayahNumber)
                    val isHighlighted = highlightedAyahNumber == ayah.ayahNumber
                    AyahItem(
                        ayah = ayah,
                        isPlaying = isAyahPlaying,
                        isBookmarked = isBookmarked,
                        isHighlighted = isHighlighted,
                        textScale = textScale,
                        onDoubleClick = {
                            viewModel.playAyah(ayah.ayahNumber, continuous = true)
                        },
                        onLongPress = {
                            selectedAyah = ayah
                            showBottomSheet = true
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (activeSurahId > 1) {
                            val prevName = SURAH_NAMES_AR.getOrElse(activeSurahId - 2) { "" }
                            OutlinedButton(
                                onClick = {
                                    if (onNavigateToSurah != null) {
                                        onNavigateToSurah(activeSurahId - 1, -1, -1)
                                    } else {
                                        viewModel.loadSurah(activeSurahId - 1)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                modifier = Modifier.weight(1f, fill = false).padding(end = 4.dp)
                            ) {
                                Text(
                                    "السورة السابقة: $prevName", 
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f, fill = false).padding(end = 4.dp))
                        }

                        if (activeSurahId < 114) {
                            val nextName = SURAH_NAMES_AR.getOrElse(activeSurahId) { "" }
                            Button(
                                onClick = {
                                    if (onNavigateToSurah != null) {
                                        onNavigateToSurah(activeSurahId + 1, -1, -1)
                                    } else {
                                        viewModel.loadSurah(activeSurahId + 1)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                modifier = Modifier.weight(1f, fill = false).padding(start = 4.dp)
                            ) {
                                Text(
                                    "السورة التالية: $nextName", 
                                    fontSize = 11.sp, 
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        } else {
                             Spacer(modifier = Modifier.weight(1f, fill = false).padding(start = 4.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }

        if (showBottomSheet && selectedAyah != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "الآية ${selectedAyah!!.ayahNumber}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = {
                                viewModel.playAyah(selectedAyah!!.ayahNumber, continuous = false)
                                showBottomSheet = false
                            })
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "استماع", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("استماع للآية", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = {
                                viewModel.playAyah(selectedAyah!!.ayahNumber, continuous = true)
                                showBottomSheet = false
                            })
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "استماع متواصل", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("استماع متواصل (من هنا)", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = {
                                clipboardManager.setText(AnnotatedString(selectedAyah!!.textUthmani))
                                showBottomSheet = false
                            })
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📋", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("نسخ الآية", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = {
                                showBottomSheet = false
                                showPosterDialog = true
                            })
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🖼️", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("شارك الآية كصورة", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    val isSelectedBookmarked = bookmarks.any { it.surahId == activeSurahId && it.ayahNumber == selectedAyah!!.ayahNumber }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = {
                                viewModel.toggleBookmark(context, selectedAyah!!.ayahNumber, selectedAyah!!.textUthmani)
                                val msg = if (isSelectedBookmarked) "تمت إزالة الآية من العلامات المرجعية" else "تمت إضافة الآية للعلامات المرجعية"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                showBottomSheet = false
                            })
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSelectedBookmarked) Icons.Default.BookmarkRemove else Icons.Default.BookmarkAdd,
                            contentDescription = "علامة مرجعية",
                            tint = if (isSelectedBookmarked) Color(0xFFE65100) else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = if (isSelectedBookmarked) "إزالة من العلامات المرجعية" else "حفظ كعلامة مرجعية",
                            fontSize = 18.sp,
                            fontWeight = if (isSelectedBookmarked) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = {
                                showBottomSheet = false
                                shareAyahAudio(context, activeSurahId, surahName, selectedAyah!!, currentReciter)
                            })
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة الصوت",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "مشاركة الآية كصوت",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.padding(24.dp))
                }
            }
        }

        if (showPosterDialog && selectedAyah != null) {
            AyahPosterDialog(
                ayah = selectedAyah!!,
                surahName = surahName,
                onDismiss = { showPosterDialog = false }
            )
        }
        
        if (showReciterDialog) {
            AlertDialog(
                onDismissRequest = { showReciterDialog = false },
                modifier = Modifier
                    .widthIn(max = 290.dp)
                    .fillMaxWidth(0.8f),
                properties = DialogProperties(usePlatformDefaultWidth = false),
                title = {
                    Text(
                        text = "اختر القارئ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        itemsIndexed(AVAILABLE_RECITERS) { _, reciter ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.setReciter(reciter, context)
                                        showReciterDialog = false
                                    }
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = currentReciter.id == reciter.id,
                                    onClick = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = reciter.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (currentReciter.id == reciter.id) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showReciterDialog = false }) {
                        Text("إغلاق")
                    }
                }
            )
        }

        if (showGoToPageDialog) {
            val startPage = SURAH_START_PAGES.getOrElse(activeSurahId - 1) { 1 }
            val endPage = if (activeSurahId < 114) (SURAH_START_PAGES.getOrElse(activeSurahId) { startPage + 1 } - 1).coerceAtLeast(startPage) else 604
            GoToPageDialog(
                currentSurahId = activeSurahId,
                currentStartPage = startPage,
                currentEndPage = endPage,
                onDismiss = { showGoToPageDialog = false },
                onSelectPage = { targetSurah, targetPage ->
                    if (targetSurah == activeSurahId) {
                        val pageOffset = (targetPage - startPage).coerceAtLeast(0)
                        coroutineScope.launch {
                            listState.animateScrollToItem(pageOffset)
                        }
                    } else {
                        viewModel.loadSurah(targetSurah)
                    }
                }
            )
        }
    }
}

@Composable
fun AudioPlayerBar(
    isPlaying: Boolean,
    reciterName: String,
    onTogglePlayPause: () -> Unit,
    onStop: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onHide: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    if (dragAmount.y > 15 || dragAmount.x > 25 || dragAmount.x < -25) {
                        onHide()
                    }
                }
            },
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            )
            Spacer(modifier = Modifier.height(6.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onHide, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "إخفاء",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = reciterName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(modifier = Modifier.size(32.dp)) // Empty box for symmetry
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrev) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "السابق",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                IconButton(onClick = onStop) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = "إيقاف الصوت",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onTogglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }
                
                IconButton(onClick = onNext) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "التالي",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AyahItem(
    ayah: AyahEntity,
    isPlaying: Boolean,
    isBookmarked: Boolean = false,
    isHighlighted: Boolean = false,
    textScale: Float = 1f,
    onDoubleClick: () -> Unit = {},
    onLongPress: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isPlaying -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            isHighlighted -> Color(0xFFE6B800).copy(alpha = 0.35f)
            isBookmarked -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else -> MaterialTheme.colorScheme.surface
        },
        label = "Highlight Animation"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            isPlaying -> MaterialTheme.colorScheme.primary
            isHighlighted -> Color(0xFFD4AF37)
            isBookmarked -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.outline
        },
        label = "Border Highlight Animation"
    )

    fun Int.toArabicNumerals(): String {
        val arabicNumerals = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        return this.toString().map { if (it.isDigit()) arabicNumerals[it - '0'] else it }.joinToString("")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onDoubleClick = onDoubleClick,
                onLongClick = onLongPress
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(if (isBookmarked || isHighlighted) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            if (isHighlighted) {
                Row(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFD4AF37).copy(alpha = 0.25f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔍 الآية المحددة من البحث",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B6508)
                    )
                }
            }
            Text(
                text = "${ayah.textUthmani} ﴿${ayah.ayahNumber.toArabicNumerals()}﴾",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Justify,
                fontSize = (24 * textScale).sp,
                lineHeight = (40 * textScale).sp,
                fontFamily = QuranFontFamily,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun Int.toArabicNumerals(): String {
    val arabicNumerals = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return this.toString().map { if (it.isDigit()) arabicNumerals[it - '0'] else it }.joinToString("")
}

val SURAH_START_PAGES = intArrayOf(
    1, 2, 50, 77, 106, 128, 151, 177, 187, 208, 221, 235, 249, 255, 262, 267,
    282, 293, 305, 312, 322, 332, 342, 350, 359, 367, 377, 385, 396, 404, 411, 415,
    418, 428, 434, 440, 446, 453, 458, 467, 477, 483, 489, 496, 499, 502, 507, 511,
    515, 518, 520, 523, 526, 528, 531, 534, 537, 542, 545, 549, 551, 553, 554, 556,
    558, 560, 562, 564, 566, 568, 570, 572, 574, 575, 577, 578, 580, 582, 583, 585,
    586, 587, 587, 589, 590, 591, 591, 592, 593, 594, 595, 595, 596, 596, 597, 597,
    598, 598, 599, 599, 600, 600, 601, 601, 601, 602, 602, 602, 603, 603, 603, 604,
    604, 604
)

fun buildPageMap(
    surahId: Int,
    ayahs: List<AyahEntity>
): Map<Int, Int> {
    val startPage = SURAH_START_PAGES.getOrElse(surahId - 1) { 1 }
    val endPage = if (surahId < 114) (SURAH_START_PAGES.getOrElse(surahId) { startPage + 1 } - 1).coerceAtLeast(startPage) else 604
    val totalPages = (endPage - startPage + 1).coerceAtLeast(1)

    if (totalPages <= 1 || ayahs.isEmpty()) {
        return ayahs.associate { it.ayahNumber to startPage }
    }

    val totalChars = ayahs.sumOf { it.textUthmani.length }
    if (totalChars == 0) {
        return ayahs.associate { it.ayahNumber to startPage }
    }

    val targetCharsPerPage = totalChars.toDouble() / totalPages
    var accumulatedChars = 0L
    var currentPageIndex = 0
    val result = mutableMapOf<Int, Int>()

    for (ayah in ayahs) {
        val len = ayah.textUthmani.length
        if (currentPageIndex < totalPages - 1) {
            val targetForNext = (currentPageIndex + 1) * targetCharsPerPage
            if (accumulatedChars + len / 2.0 >= targetForNext) {
                currentPageIndex++
            }
        }
        result[ayah.ayahNumber] = (startPage + currentPageIndex).coerceIn(startPage, endPage)
        accumulatedChars += len
    }

    return result
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoToPageDialog(
    currentSurahId: Int,
    currentStartPage: Int,
    currentEndPage: Int,
    onDismiss: () -> Unit,
    onSelectPage: (targetSurahId: Int, pageNumber: Int) -> Unit
) {
    var pageInput by remember { mutableStateOf(currentStartPage.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("الانتقال إلى صفحة المصحف", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "الصفحات في هذه السورة: من صفحة $currentStartPage إلى $currentEndPage (نطاق المصحف: 1 - 604)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = pageInput,
                    onValueChange = { input ->
                        pageInput = input.filter { it.isDigit() }
                    },
                    label = { Text("أدخل رقم الصفحة (1 - 604)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pageNum = pageInput.toIntOrNull()
                    if (pageNum != null && pageNum in 1..604) {
                        var targetSurah = 1
                        for (i in 0..113) {
                            val sp = SURAH_START_PAGES[i]
                            val ep = if (i < 113) (SURAH_START_PAGES[i + 1] - 1).coerceAtLeast(sp) else 604
                            if (pageNum in sp..ep) {
                                targetSurah = i + 1
                                break
                            }
                        }
                        onSelectPage(targetSurah, pageNum)
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("انتقال", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun SurahHeaderBanner(
    surahDetails: com.example.data.local.SurahEntity?,
    surahId: Int,
    surahName: String,
    downloadProgress: com.example.ui.viewmodels.SurahDownloadProgress? = null,
    isDownloaded: Boolean = false,
    onDownload: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val surahNumberAr = surahId.toArabicNumerals()
    val ayahCountAr = (surahDetails?.ayahCount ?: 0).toArabicNumerals()
    val isMakkah = (surahDetails?.revelationType == "makkah" || surahDetails?.revelationType == "مكية")
    val revelationTypeAr = if (isMakkah) "مكّيّة" else "مدنيّة"
    val startPage = SURAH_START_PAGES.getOrElse(surahId - 1) { 1 }
    val startPageAr = startPage.toArabicNumerals()
    val isIceBlue = com.example.ui.theme.ThemeManager.currentThemeMode == com.example.ui.theme.AppThemeMode.ICE_BLUE_LIGHT
    val goldBright = if (isIceBlue) MaterialTheme.colorScheme.primary else Color(0xFFD4AF37)
    val goldDark = if (isIceBlue) MaterialTheme.colorScheme.secondary else Color(0xFFB88E4C)
    val paperBg = MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = paperBg
        ),
        border = BorderStroke(2.dp, goldBright),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MushafSurahHeaderFrame(
                surahName = surahName.ifEmpty { surahId.toString() },
                borderColor = goldBright
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SurahInfoBadge(label = "ترتيبها", value = surahNumberAr, color = goldDark)
                SurahInfoBadge(label = "آياتها", value = ayahCountAr, color = goldDark)
                SurahInfoBadge(label = "نزولها", value = revelationTypeAr, color = goldDark)
                SurahInfoBadge(label = "صفحة", value = startPageAr, color = goldDark)
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (downloadProgress != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { downloadProgress.downloadedCount.toFloat() / downloadProgress.totalAyahs.coerceAtLeast(1) },
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val pct = ((downloadProgress.downloadedCount.toFloat() / downloadProgress.totalAyahs) * 100).toInt()
                        Text(
                            text = "جاري تنزيل الصوتيات بدون إنترنت: $pct% (${downloadProgress.downloadedCount}/${downloadProgress.totalAyahs})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            } else if (isDownloaded) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "صُوتيات السورة مُحمّلة للعمل بدون إنترنت 🟢",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف الصُوتيات",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onDownload,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, goldDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = goldDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تحميل صُوتيات السورة للاستماع والتظليل بدون إنترنت 📥",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldDark
                    )
                }
            }
        }
    }
}

@Composable
fun SurahInfoBadge(label: String, value: String, color: Color = MaterialTheme.colorScheme.primary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = color.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun BasmalaBanner() {
    val isIceBlue = com.example.ui.theme.ThemeManager.currentThemeMode == com.example.ui.theme.AppThemeMode.ICE_BLUE_LIGHT
    val goldBright = if (isIceBlue) MaterialTheme.colorScheme.primary else Color(0xFFD4AF37)
    val paperBg = MaterialTheme.colorScheme.surface
    val textInkColor = MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = paperBg,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, goldBright.copy(alpha = 0.85f)),
        shadowElevation = 1.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = textInkColor
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

fun buildPageAnnotatedString(
    pageAyahs: List<AyahEntity>,
    playingAyah: Pair<Int, Int>?,
    bookmarkedAyahNumbers: Set<Int>,
    highlightedAyahNumber: Int? = null,
    goldAccentColor: Color,
    textScale: Float,
    surahId: Int
): AnnotatedString {
    return buildAnnotatedString {
        pageAyahs.forEach { ayah ->
            val isAyahPlaying = playingAyah?.first == surahId && playingAyah?.second == ayah.ayahNumber
            val isBookmarked = bookmarkedAyahNumbers.contains(ayah.ayahNumber)
            val isHighlighted = highlightedAyahNumber == ayah.ayahNumber

            val annotationIndex = pushStringAnnotation(
                tag = "AYAH",
                annotation = ayah.ayahNumber.toString()
            )

            val bgHighlight = when {
                isAyahPlaying -> goldAccentColor.copy(alpha = 0.40f)
                isHighlighted -> Color(0xFFE6B800).copy(alpha = 0.45f)
                isBookmarked -> goldAccentColor.copy(alpha = 0.18f)
                else -> Color.Transparent
            }

            withStyle(
                SpanStyle(
                    background = bgHighlight,
                    fontWeight = if (isAyahPlaying || isHighlighted) FontWeight.Bold else FontWeight.Normal
                )
            ) {
                append("${ayah.textUthmani} ")
            }

            withStyle(
                SpanStyle(
                    color = if (isHighlighted) Color(0xFF8B6508) else goldAccentColor,
                    background = if (isHighlighted) Color(0xFFE6B800).copy(alpha = 0.45f) else Color.Transparent,
                    fontWeight = FontWeight.Bold,
                    fontSize = (18 * textScale).sp
                )
            ) {
                append(" ۝${ayah.ayahNumber.toArabicNumerals()} ")
            }

            pop(annotationIndex)
        }
    }
}

@Composable
fun AuthenticMushafView(
    surahId: Int,
    surahName: String,
    surahDetails: com.example.data.local.SurahEntity?,
    ayahs: List<AyahEntity>,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    playingAyah: Pair<Int, Int>?,
    bookmarkedAyahNumbers: Set<Int>,
    highlightedAyahNumber: Int? = null,
    initialPage: Int = -1,
    isKhatmah: Boolean = false,
    textScale: Float,
    listState: androidx.compose.foundation.lazy.LazyListState,
    autoScrollEnabled: Boolean = true,
    onAyahClick: (AyahEntity) -> Unit,
    onAyahDoubleClick: (AyahEntity) -> Unit = {},
    onNavigateToSurah: ((Int, Int, Int) -> Unit)?,
    onLoadSurah: (Int) -> Unit
) {
    val isIceBlue = com.example.ui.theme.ThemeManager.currentThemeMode == com.example.ui.theme.AppThemeMode.ICE_BLUE_LIGHT
    val paperBg = MaterialTheme.colorScheme.surface
    val textInkColor = MaterialTheme.colorScheme.onSurface
    val frameBorderColor = if (isIceBlue) MaterialTheme.colorScheme.primary else Color(0xFFB88E4C)
    val goldAccentColor = if (isIceBlue) MaterialTheme.colorScheme.primary else Color(0xFFC5A059)

    var lastClickAyahNum by remember { mutableStateOf<Int?>(null) }
    var lastClickTime by remember { mutableLongStateOf(0L) }

    val startPage = SURAH_START_PAGES.getOrElse(surahId - 1) { 1 }

    val pageMap = remember(surahId, ayahs) { buildPageMap(surahId, ayahs) }


    val pageGroups = remember(ayahs, pageMap, startPage) {
        val groups = mutableMapOf<Int, MutableList<AyahEntity>>()
        ayahs.forEach { ayah ->
            val p = pageMap[ayah.ayahNumber] ?: startPage
            groups.getOrPut(p) { mutableListOf() }.add(ayah)
        }
        groups.toSortedMap()
    }
    
    val context = LocalContext.current
    LaunchedEffect(listState, pageGroups, surahId, isKhatmah) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                if (pageGroups.isNotEmpty() && index < pageGroups.size) {
                    val pageEntries = pageGroups.entries.toList()
                    val visiblePage = pageEntries[index]
                    val firstAyah = visiblePage.value.firstOrNull()
                    if (firstAyah != null) {
                        LastReadManager.saveLastRead(context, surahId, surahName, firstAyah.ayahNumber)
                        // Update Khatma progress ONLY if opened in Khatma mode
                        if (isKhatmah) {
                            val pageNumber = visiblePage.key
                            val khatmaRepo = (context.applicationContext as com.example.MainApplication).khatmaRepository
                            khatmaRepo.updateProgress(pageNumber)
                        }
                    }
                }
            }
    }

    val textLayoutResults = remember { mutableStateMapOf<Int, androidx.compose.ui.text.TextLayoutResult>() }

    val targetPage = playingAyah?.let { (playingSurahId, playingAyahNumber) ->
        if (playingSurahId == surahId) {
            pageMap[playingAyahNumber] ?: startPage
        } else null
    }

    LaunchedEffect(playingAyah, autoScrollEnabled, targetPage, textLayoutResults[targetPage ?: -1]) {
        if (autoScrollEnabled && playingAyah != null && targetPage != null) {
            val (playingSurahId, playingAyahNumber) = playingAyah
            if (playingSurahId == surahId) {
                val targetPageIndex = pageGroups.keys.toList().indexOf(targetPage)
                if (targetPageIndex != -1) {
                    val targetPageAyahs = pageGroups[targetPage] ?: emptyList()
                    val targetAnnotatedText = buildPageAnnotatedString(
                        pageAyahs = targetPageAyahs,
                        playingAyah = playingAyah,
                        bookmarkedAyahNumbers = bookmarkedAyahNumbers,
                        highlightedAyahNumber = highlightedAyahNumber,
                        goldAccentColor = goldAccentColor,
                        textScale = textScale,
                        surahId = surahId
                    )
                    val annotations = targetAnnotatedText.getStringAnnotations(tag = "AYAH", start = 0, end = targetAnnotatedText.length)
                    val matchingAnnotation = annotations.find { it.item == playingAyahNumber.toString() }
                    
                    val scrollOffset = if (matchingAnnotation != null) {
                        val layoutResult = textLayoutResults[targetPage]
                        if (layoutResult != null) {
                            val startCharIndex = matchingAnnotation.start
                            val line = layoutResult.getLineForOffset(startCharIndex)
                            val lineTop = layoutResult.getLineTop(line)
                            (lineTop.toInt() - 100).coerceAtLeast(0)
                        } else {
                            0
                        }
                    } else {
                        0
                    }

                    listState.animateScrollToItem(targetPageIndex, scrollOffset)
                }
            }
        }
    }

    // Scroll to initialPage exactly
    var hasScrolledToInitialPage by remember(initialPage) { mutableStateOf(false) }
    LaunchedEffect(initialPage, pageGroups, textLayoutResults[initialPage]) {
        if (initialPage > 0 && pageGroups.isNotEmpty() && !hasScrolledToInitialPage) {
            val targetPageIndex = pageGroups.keys.toList().indexOf(initialPage)
            if (targetPageIndex != -1) {
                // Determine vertical offset logic similar to highlighted scroll if needed.
                // For simplicity, just scrolling to the top of the page.
                listState.scrollToItem(targetPageIndex)
                hasScrolledToInitialPage = true
            }
        }
    }

    // Scroll to highlighted ayah exactly
    var hasScrolledToHighlight by remember(highlightedAyahNumber) { mutableStateOf(false) }
    val highlightedTargetPage = highlightedAyahNumber?.let { pageMap[it] ?: startPage }
    LaunchedEffect(highlightedAyahNumber, highlightedTargetPage, textLayoutResults[highlightedTargetPage ?: -1]) {
        if (highlightedAyahNumber != null && highlightedTargetPage != null && !hasScrolledToHighlight) {
            val targetPageIndex = pageGroups.keys.toList().indexOf(highlightedTargetPage)
            if (targetPageIndex != -1) {
                val targetPageAyahs = pageGroups[highlightedTargetPage] ?: emptyList()
                val targetAnnotatedText = buildPageAnnotatedString(
                    pageAyahs = targetPageAyahs,
                    playingAyah = playingAyah,
                    bookmarkedAyahNumbers = bookmarkedAyahNumbers,
                    highlightedAyahNumber = highlightedAyahNumber,
                    goldAccentColor = goldAccentColor,
                    textScale = textScale,
                    surahId = surahId
                )
                val annotations = targetAnnotatedText.getStringAnnotations(tag = "AYAH", start = 0, end = targetAnnotatedText.length)
                val matchingAnnotation = annotations.find { it.item == highlightedAyahNumber.toString() }
                
                val layoutResult = textLayoutResults[highlightedTargetPage]
                if (layoutResult != null) {
                    val scrollOffset = if (matchingAnnotation != null) {
                        val startCharIndex = matchingAnnotation.start
                        val line = layoutResult.getLineForOffset(startCharIndex)
                        val lineTop = layoutResult.getLineTop(line)
                        (lineTop.toInt() - 200).coerceAtLeast(0) // scroll to make it comfortably visible
                    } else {
                        0
                    }
                    listState.scrollToItem(targetPageIndex, scrollOffset)
                    hasScrolledToHighlight = true
                } else {
                    // Force the page to render first so we can get its layout result
                    listState.scrollToItem(targetPageIndex)
                }
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(paperBg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (pageGroups.isEmpty()) {
            item(key = "mushaf_page_placeholder") {
                val safeSurahName = surahName.ifBlank {
                    if (surahId in 1..114) com.example.audio.SURAH_NAMES_AR.getOrElse(surahId - 1) { "سورة $surahId" } else "سورة $surahId"
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = paperBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.5.dp, frameBorderColor.copy(alpha = 0.65f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "الجزء الأول",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = QuranFontFamily,
                                color = frameBorderColor
                            )
                            Text(
                                text = safeSurahName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = QuranFontFamily,
                                color = frameBorderColor
                            )
                        }
                        HorizontalDivider(
                            color = frameBorderColor.copy(alpha = 0.5f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                        )
                        MushafSurahHeaderFrame(
                            surahName = safeSurahName,
                            borderColor = frameBorderColor
                        )
                        if (surahId != 9) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                color = Color.Transparent
                            ) {
                                Text(
                                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontFamily = QuranFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = textInkColor
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        if (isLoading) {
                            Spacer(modifier = Modifier.height(24.dp))
                            CircularProgressIndicator(color = textInkColor)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "جاري تحميل الآيات...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textInkColor.copy(alpha = 0.7f)
                            )
                        } else if (!errorMessage.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Icon(
                                imageVector = Icons.Filled.WifiOff,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { onLoadSurah(surahId) },
                                border = BorderStroke(1.dp, textInkColor)
                            ) {
                                Text("إعادة المحاولة", color = textInkColor)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        pageGroups.forEach { (pageNumber, pageAyahs) ->
            item(key = "mushaf_page_$pageNumber") {
                val firstAyah = pageAyahs.firstOrNull()
                val ayahNum = firstAyah?.ayahNumber ?: 1
                val juzNumber = getJuzNumber(surahId, ayahNum)
                val hizbText = getHizbInfo(juzNumber, surahId, ayahNum)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = paperBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.5.dp, frameBorderColor.copy(alpha = 0.65f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // --- Top Header ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "الجزء ${juzNumber.toArabicNumerals()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = QuranFontFamily,
                                color = frameBorderColor
                            )
                            Text(
                                text = surahName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = QuranFontFamily,
                                color = frameBorderColor
                            )
                        }

                        // Top Header Ornamental Border Line
                        HorizontalDivider(
                            color = frameBorderColor.copy(alpha = 0.5f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                        )

                        // If Page contains Ayah 1 of the Surah
                        if (pageAyahs.any { it.ayahNumber == 1 }) {
                            MushafSurahHeaderFrame(
                                surahName = surahName,
                                borderColor = frameBorderColor
                            )
                            if (surahId != 9) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    color = Color.Transparent
                                ) {
                                    Text(
                                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontFamily = QuranFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = textInkColor
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // --- Main Quran Text ---
                        val annotatedText = remember(pageAyahs, playingAyah, bookmarkedAyahNumbers, highlightedAyahNumber, textScale) {
                            buildPageAnnotatedString(
                                pageAyahs = pageAyahs,
                                playingAyah = playingAyah,
                                bookmarkedAyahNumbers = bookmarkedAyahNumbers,
                                highlightedAyahNumber = highlightedAyahNumber,
                                goldAccentColor = goldAccentColor,
                                textScale = textScale,
                                surahId = surahId
                            )
                        }

                        ClickableText(
                            text = annotatedText,
                            style = androidx.compose.ui.text.TextStyle(
                                textAlign = TextAlign.Justify,
                                fontSize = (22 * textScale).sp,
                                lineHeight = (42 * textScale).sp,
                                fontFamily = QuranFontFamily,
                                color = textInkColor
                            ),
                            onTextLayout = { layoutResult ->
                                textLayoutResults[pageNumber] = layoutResult
                            },
                            onClick = { offset ->
                                annotatedText.getStringAnnotations(tag = "AYAH", start = offset, end = offset)
                                    .firstOrNull()?.let { annotation ->
                                        val ayahNum = annotation.item.toIntOrNull()
                                        if (ayahNum != null) {
                                            val clickedAyah = pageAyahs.find { it.ayahNumber == ayahNum }
                                            if (clickedAyah != null) {
                                                val now = System.currentTimeMillis()
                                                if (lastClickAyahNum == ayahNum && (now - lastClickTime < 450)) {
                                                    onAyahDoubleClick(clickedAyah)
                                                    lastClickAyahNum = null
                                                } else {
                                                    lastClickAyahNum = ayahNum
                                                    lastClickTime = now
                                                    onAyahClick(clickedAyah)
                                                }
                                            }
                                        }
                                    }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        HorizontalDivider(
                            color = frameBorderColor.copy(alpha = 0.5f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        // --- Footer ---
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Hizb badge
                            Surface(
                                color = frameBorderColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, frameBorderColor.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = hizbText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                    color = frameBorderColor,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Surah Navigation Buttons at end of Mushaf
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (surahId > 1) {
                    val prevName = SURAH_NAMES_AR.getOrElse(surahId - 2) { "" }
                    OutlinedButton(
                        onClick = {
                            if (onNavigateToSurah != null) {
                                onNavigateToSurah(surahId - 1, -1, -1)
                            } else {
                                onLoadSurah(surahId - 1)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, frameBorderColor),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f, fill = false).padding(end = 4.dp)
                    ) {
                        Text(
                            "السورة السابقة: $prevName", 
                            fontSize = 11.sp, 
                            color = frameBorderColor,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f, fill = false).padding(end = 4.dp))
                }

                if (surahId < 114) {
                    val nextName = SURAH_NAMES_AR.getOrElse(surahId) { "" }
                    Button(
                        onClick = {
                            if (onNavigateToSurah != null) {
                                onNavigateToSurah(surahId + 1, -1, -1)
                            } else {
                                onLoadSurah(surahId + 1)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = frameBorderColor),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f, fill = false).padding(start = 4.dp)
                    ) {
                        Text(
                            "السورة التالية: $nextName", 
                            fontSize = 11.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = Color.White,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f, fill = false).padding(start = 4.dp))
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun MushafSurahHeaderFrame(surahName: String, borderColor: Color = MaterialTheme.colorScheme.primary) {
    val isIceBlue = com.example.ui.theme.ThemeManager.currentThemeMode == com.example.ui.theme.AppThemeMode.ICE_BLUE_LIGHT
    val goldBright = if (isIceBlue) MaterialTheme.colorScheme.primary else Color(0xFFD4AF37)
    val goldDark = if (isIceBlue) MaterialTheme.colorScheme.secondary else Color(0xFFB88E4C)
    val textColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .background(
                color = goldBright.copy(alpha = 0.10f),
                shape = RoundedCornerShape(14.dp)
            )
            .border(
                width = 2.dp,
                color = goldBright,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(3.dp)
            .border(
                width = 1.dp,
                color = goldDark.copy(alpha = 0.75f),
                shape = RoundedCornerShape(11.dp)
            )
            .padding(vertical = 10.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "۞",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = goldBright
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "سُورَةُ $surahName",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                color = textColor
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "۞",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = goldBright
            )
        }
    }
}

fun getJuzNumber(surahId: Int, ayahNumber: Int): Int {
    return when {
        surahId == 1 -> 1
        surahId == 2 -> if (ayahNumber < 142) 1 else if (ayahNumber < 253) 2 else 3
        surahId == 3 -> if (ayahNumber < 93) 3 else 4
        surahId == 4 -> if (ayahNumber < 24) 4 else if (ayahNumber < 148) 5 else 6
        surahId == 5 -> if (ayahNumber < 82) 6 else 7
        surahId == 6 -> if (ayahNumber < 111) 7 else 8
        surahId == 7 -> if (ayahNumber < 88) 8 else 9
        surahId == 8 -> if (ayahNumber < 41) 9 else 10
        surahId == 9 -> if (ayahNumber < 93) 10 else 11
        surahId == 10 -> 11
        surahId == 11 -> if (ayahNumber < 6) 11 else 12
        surahId == 12 -> if (ayahNumber < 53) 12 else 13
        surahId in 13..14 -> 13
        surahId in 15..16 -> 14
        surahId == 17 -> 15
        surahId == 18 -> if (ayahNumber < 75) 15 else 16
        surahId in 19..20 -> 16
        surahId in 21..22 -> 17
        surahId in 23..24 -> 18
        surahId == 25 -> if (ayahNumber < 21) 18 else 19
        surahId == 26 -> 19
        surahId == 27 -> if (ayahNumber < 56) 19 else 20
        surahId == 28 -> 20
        surahId == 29 -> if (ayahNumber < 46) 20 else 21
        surahId in 30..32 -> 21
        surahId == 33 -> if (ayahNumber < 31) 21 else 22
        surahId in 34..35 -> 22
        surahId == 36 -> if (ayahNumber < 28) 22 else 23
        surahId in 37..38 -> 23
        surahId == 39 -> if (ayahNumber < 32) 23 else 24
        surahId == 40 -> 24
        surahId == 41 -> if (ayahNumber < 47) 24 else 25
        surahId in 42..45 -> 25
        surahId in 46..51 -> if (surahId == 51 && ayahNumber >= 31) 27 else 26
        surahId in 52..57 -> 27
        surahId in 58..66 -> 28
        surahId in 67..77 -> 29
        else -> 30
    }
}

fun getHizbInfo(juz: Int, surahId: Int, ayahNumber: Int): String {
    val hizbBase = (juz - 1) * 2 + 1
    val hizbBaseAr = hizbBase.toArabicNumerals()
    val hizbNextAr = (hizbBase + 1).toArabicNumerals()

    return when {
        surahId == 1 -> "الحزب $hizbBaseAr"
        surahId == 2 -> when {
            ayahNumber < 75 -> "الحزب $hizbBaseAr"
            ayahNumber < 106 -> "١/٤ الحزب $hizbBaseAr"
            ayahNumber < 142 -> "نصف الحزب $hizbBaseAr"
            ayahNumber < 177 -> "٣/٤ الحزب $hizbBaseAr"
            ayahNumber < 203 -> "الحزب $hizbNextAr"
            ayahNumber < 232 -> "١/٤ الحزب $hizbNextAr"
            ayahNumber < 253 -> "نصف الحزب $hizbNextAr"
            else -> "٣/٤ الحزب $hizbNextAr"
        }
        else -> "الحزب $hizbBaseAr"
    }
}

fun shareAyahAudio(context: android.content.Context, surahId: Int, surahName: String, ayah: com.example.data.local.AyahEntity, reciter: com.example.audio.Reciter) {
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        try {
            val surahStr = String.format(java.util.Locale.US, "%03d", surahId)
            val ayahStr = String.format(java.util.Locale.US, "%03d", ayah.ayahNumber)
            val fileName = "${surahStr}${ayahStr}.mp3"
            
            val audioDir = java.io.File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_MUSIC), "quran_ayahs_audio/${reciter.path}")
            var audioFile = java.io.File(audioDir, fileName)
            
            if (!audioFile.exists() || audioFile.length() == 0L) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "جاري تجهيز المقطع الصوتي للمشاركة...", android.widget.Toast.LENGTH_SHORT).show()
                }
                val cacheDir = java.io.File(context.cacheDir, "shared_audio")
                if (!cacheDir.exists()) cacheDir.mkdirs()
                audioFile = java.io.File(cacheDir, fileName)
                
                if (!audioFile.exists() || audioFile.length() == 0L) {
                    val urlString = "https://everyayah.com/data/${reciter.path}/$fileName"
                    val conn = java.net.URL(urlString).openConnection()
                    conn.connect()
                    conn.getInputStream().use { input ->
                        java.io.FileOutputStream(audioFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
            
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                audioFile
            )
            
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "audio/mp3"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                putExtra(android.content.Intent.EXTRA_TEXT, "استمع للآية ${ayah.ayahNumber} من سورة $surahName بصوت القارئ ${reciter.name}")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                context.startActivity(android.content.Intent.createChooser(shareIntent, "مشاركة الآية كصوت"))
            }
            
        } catch (e: Exception) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                android.widget.Toast.makeText(context, "حدث خطأ أثناء تجهيز الصوت: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}
