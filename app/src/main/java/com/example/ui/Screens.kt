package com.example.ui
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle

import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Alignment

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ArrowForward


import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AhlAlBaytRepository
import com.example.data.HijriDate
import com.example.data.Person
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import com.example.data.local.NotificationSettingsManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.example.worker.AyahWorker
import com.example.data.remote.AppReleaseInfo
import com.example.data.remote.AppUpdateManager
import com.example.data.remote.DownloadState
import com.example.ui.screens.settings.UpdateStatus
import com.example.ui.screens.settings.installApk
import kotlinx.coroutines.Job
import java.io.File
import java.util.Locale

@Composable
fun DetailScreen(person: Person, onBack: () -> Unit) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("AhlAlBaytPrefs", Context.MODE_PRIVATE)
    
    var textSizeMultiplier by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(sharedPref.getFloat("text_size_multiplier", 1.0f))
    }
    
    val listener = remember {
        android.content.SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == "text_size_multiplier") {
                textSizeMultiplier = prefs.getFloat(key, 1.0f)
            }
        }
    }

    androidx.compose.runtime.DisposableEffect(sharedPref) {
        sharedPref.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            sharedPref.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = person.name, style = MaterialTheme.typography.headlineMedium, fontSize = (28 * textSizeMultiplier).sp, fontWeight = FontWeight.Bold)
                if (person.title.isNotEmpty()) {
                    Text(text = "الألقاب: ${person.title}", style = MaterialTheme.typography.titleMedium, fontSize = (16 * textSizeMultiplier).sp, color = MaterialTheme.colorScheme.primary)
                }
            }
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize().weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { DetailRow("الأب", person.father, textSizeMultiplier) }
            item { DetailRow("الأم", person.mother, textSizeMultiplier) }
            item { DetailRow("تاريخ الولادة", person.birthDate?.toString() ?: "غير معروف", textSizeMultiplier) }
            item { DetailRow("تاريخ الاستشهاد", person.deathDate?.toString() ?: "غير معروف", textSizeMultiplier) }
        
        if (person.imamateDuration != null) {
            item { DetailRow("مدة الإمامة", person.imamateDuration, textSizeMultiplier) }
        }
        if (person.causeOfDeath != null) {
            item { DetailRow("سبب الاستشهاد", person.causeOfDeath, textSizeMultiplier) }
        }
        if (person.killer != null) {
            item { DetailRow("القاتل", person.killer, textSizeMultiplier) }
        }

        if (person.childrenCount != null || person.famousChildren.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, androidx.compose.ui.graphics.Color(0xFFD4AF37)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "الأبناء (${person.childrenCount ?: "?"})",
                            style = MaterialTheme.typography.labelMedium,
                            color = androidx.compose.ui.graphics.Color(0xFF8B6B15),
                            fontWeight = FontWeight.Bold
                        )
                        if (person.famousChildren.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = person.famousChildren.joinToString("، "),
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = (14 * textSizeMultiplier).sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        if (person.wives.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, androidx.compose.ui.graphics.Color(0xFFD4AF37)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "الزوجات",
                            style = MaterialTheme.typography.labelMedium,
                            color = androidx.compose.ui.graphics.Color(0xFF8B6B15),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = person.wives.joinToString("، "),
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = (14 * textSizeMultiplier).sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, androidx.compose.ui.graphics.Color(0xFFD4AF37)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "نبذة",
                        style = MaterialTheme.typography.labelMedium,
                        color = androidx.compose.ui.graphics.Color(0xFF8B6B15),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = person.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = (14 * textSizeMultiplier).sp,
                        lineHeight = (24 * textSizeMultiplier).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
}

@Composable
fun SettingsScreen(onBack: (() -> Unit)? = null) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("AhlAlBaytPrefs", Context.MODE_PRIVATE)
    val db = remember { com.example.data.AppDatabase.getInstance(context) }
    val hijriRecordState by db.hijriDateRecordDao().getRecordFlow().collectAsState(initial = null)
    
    var notificationsEnabled by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(sharedPref.getBoolean("notifications_enabled", true))
    }
    
    var notificationsPerHour by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(sharedPref.getInt("notifications_per_hour", 3).toFloat())
    }
    
    var hijriSyncMode by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(sharedPref.getString("hijri_sync_mode", "auto") ?: "auto")
    }

    var showDailyDeeds by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(sharedPref.getBoolean("show_daily_deeds", true))
    }

    var showSuggestions by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(sharedPref.getBoolean("show_suggestions", true))
    }

    var showEvents by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(sharedPref.getBoolean("show_events", true))
    }

    var showQabas by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(sharedPref.getBoolean("show_qabas", true))
    }

    var smartKashidaEnabled by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(sharedPref.getBoolean("smart_kashida_enabled", true))
    }

    var textSizeMultiplierSettings by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(sharedPref.getFloat("text_size_multiplier", 1.0f))
    }

    // Notification Settings State
    var isNotificationsCardExpanded by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(true)
    }
    var quranNotificationsEnabled by remember {
        mutableStateOf(com.example.data.local.NotificationSettingsManager.isQuranEnabled(context))
    }
    var upcomingEventsNotificationsEnabled by remember {
        mutableStateOf(com.example.data.local.NotificationSettingsManager.isUpcomingEventsEnabled(context))
    }
    val quranCurrentVersion = com.example.data.remote.AppUpdateManager.QURAN_CURRENT_VERSION
    var quranUpcomingVersion by remember { mutableStateOf(quranCurrentVersion) }
    var quranUpdateInfo by remember { mutableStateOf<com.example.data.remote.AppReleaseInfo?>(null) }
    var quranUpdateStatus by remember { mutableStateOf(com.example.ui.screens.settings.UpdateStatus.IDLE) }
    var quranDownloadProgress by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var quranDownloadedMB by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var quranTotalMB by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var quranSpeedMBs by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var quranDownloadedApkFile by remember { mutableStateOf<java.io.File?>(null) }
    var quranDownloadJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    val prayerCurrentVersion = com.example.data.remote.AppUpdateManager.PRAYER_CURRENT_VERSION
    var prayerUpcomingVersion by remember { mutableStateOf(prayerCurrentVersion) }
    var prayerUpdateInfo by remember { mutableStateOf<com.example.data.remote.AppReleaseInfo?>(null) }
    var prayerUpdateStatus by remember { mutableStateOf(com.example.ui.screens.settings.UpdateStatus.IDLE) }
    var prayerDownloadProgress by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var prayerDownloadedMB by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var prayerTotalMB by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var prayerSpeedMBs by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var prayerDownloadedApkFile by remember { mutableStateOf<java.io.File?>(null) }
    var prayerDownloadJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    val currentHijriCalculated = remember(hijriRecordState) {
        com.example.data.HijriSyncManager.getCalculatedHijriDate(hijriRecordState)
    }

    var manualDayStr by remember(currentHijriCalculated) {
        mutableStateOf(currentHijriCalculated.get(java.time.temporal.ChronoField.DAY_OF_MONTH).toString())
    }
    var manualMonthStr by remember(currentHijriCalculated) {
        mutableStateOf(currentHijriCalculated.get(java.time.temporal.ChronoField.MONTH_OF_YEAR).toString())
    }
    var manualYearStr by remember(currentHijriCalculated) {
        mutableStateOf(currentHijriCalculated.get(java.time.temporal.ChronoField.YEAR_OF_ERA).toString())
    }
    
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var isSyncing by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    var syncResultText by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf("") }

    androidx.compose.runtime.LaunchedEffect(Unit) {
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = "إعدادات تطبيق نور العترة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Unified Expandable Notifications Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                // Header (Clickable for Expand / Collapse)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { isNotificationsCardExpanded = !isNotificationsCardExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "باقة الإشعارات والتذكيرات اليومية",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "الآيات القرآنية، والعبادات، وتنبيهات المناسبات عند اقترابها",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                            )
                        }
                    }

                    IconButton(
                        onClick = { isNotificationsCardExpanded = !isNotificationsCardExpanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isNotificationsCardExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isNotificationsCardExpanded) "طي" else "توسيع",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Expandable Content
                androidx.compose.animation.AnimatedVisibility(visible = isNotificationsCardExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))

                        // 1. Quran Ayah Notifications
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "إشعارات الآيات القرآنية",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "إرسال إشعارين في الساعة بآيات كريمة بالرسم العثماني (كل 30 دقيقة)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Switch(
                                checked = quranNotificationsEnabled,
                                onCheckedChange = { enabled ->
                                    quranNotificationsEnabled = enabled
                                    com.example.data.local.NotificationSettingsManager.setQuranEnabled(context, enabled)
                                    if (enabled) {
                                        android.widget.Toast.makeText(context, "تم تفعيل إشعارات الآيات الكريمة", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "تم إيقاف إشعارات الآيات", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))

                        // 2. Approaching Religious Events Notifications
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "تنبيهات المناسبات عند اقترابها",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "إشعار مبكر قبل يومين ويوم وعند حلول المناسبات الدينية وأيام الفضيلة",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Switch(
                                checked = upcomingEventsNotificationsEnabled,
                                onCheckedChange = { enabled ->
                                    upcomingEventsNotificationsEnabled = enabled
                                    com.example.data.local.NotificationSettingsManager.setUpcomingEventsEnabled(context, enabled)
                                    if (enabled) {
                                        android.widget.Toast.makeText(context, "تم تفعيل تنبيهات اقتراب المناسبات", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "تم إيقاف تنبيهات اقتراب المناسبات", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))

                        // 3. Daily Worship and Reminders
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "إشعارات العبادات والأدعية اليومية",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "تذكيرات دورية بالأدعية والزيارات والأعمال المستحبة والتسبيحات",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { enabled ->
                                    notificationsEnabled = enabled
                                    sharedPref.edit().putBoolean("notifications_enabled", enabled).apply()
                                    com.example.data.local.NotificationSettingsManager.scheduleAllNotifications(context)
                                    if (enabled) {
                                        android.widget.Toast.makeText(context, "تم تفعيل إشعارات العبادات والأعمال", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "تم إيقاف إشعارات العبادات", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }

                        // Frequency slider for reminders
                        if (notificationsEnabled) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "معدل الإشعارات بالساعة",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.weight(1f, fill = false),
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = "${notificationsPerHour.toInt()} إشعارات",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Slider(
                                    value = notificationsPerHour,
                                    onValueChange = { notificationsPerHour = it },
                                    onValueChangeFinished = {
                                        sharedPref.edit().putInt("notifications_per_hour", notificationsPerHour.toInt()).apply()
                                        com.example.data.local.NotificationSettingsManager.scheduleAllNotifications(context)
                                    },
                                    valueRange = 3f..10f,
                                    steps = 6,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )
                                )

                                Text(
                                    text = "الحد الأدنى ٣ إشعارات، والحد الأقصى ١٠ إشعارات متنوعة خلال الساعة",
                                    style = MaterialTheme.typography.bodySmall,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Customize Main Screen Section
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(
                    text = "تخصيص الواجهة الرئيسية",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Toggle Daily Deeds Ticker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "عرض شريط أعمال اليوم",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "إظهار شريط الأعمال والعبادات اليومية في الواجهة الرئيسية",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Switch(
                        checked = showDailyDeeds,
                        onCheckedChange = {
                            showDailyDeeds = it
                            sharedPref.edit().putBoolean("show_daily_deeds", it).apply()
                        }
                    )
                }

                androidx.compose.material3.HorizontalDivider(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Toggle Next Prayer & Timings Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "عرض بطاقة الصلاة القادمة والمواقيت",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "إظهار بطاقة الصلاة القادمة والمدينة والعد التنازلي والمواقيت المنسدلة في الواجهة الرئيسية",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Switch(
                        checked = showSuggestions,
                        onCheckedChange = {
                            showSuggestions = it
                            sharedPref.edit().putBoolean("show_suggestions", it).apply()
                        }
                    )
                }

                androidx.compose.material3.HorizontalDivider(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Toggle Events Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "عرض أحداث هذا الشهر والمناسبات",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "إظهار أو إخفاء قائمة أحداث هذا الشهر وشريط المناسبات في التقويم والواجهة الرئيسية",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Switch(
                        checked = showEvents,
                        onCheckedChange = {
                            showEvents = it
                            sharedPref.edit().putBoolean("show_events", it).apply()
                        }
                    )
                }

                androidx.compose.material3.HorizontalDivider(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Toggle Qabas Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "عرض شريط قبس من كلام الله",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "إظهار أو إخفاء بطاقة الآية القرآنية المقترحة في الواجهة الرئيسية",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Switch(
                        checked = showQabas,
                        onCheckedChange = {
                            showQabas = it
                            sharedPref.edit().putBoolean("show_qabas", it).apply()
                        }
                    )
                }
            }
        }

        // Background Theme Customization Card
        var selectedBgColor by androidx.compose.runtime.saveable.rememberSaveable {
            androidx.compose.runtime.mutableStateOf(sharedPref.getString("selected_bg_color", "ice_blue") ?: "ice_blue")
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "مظهر الخلفية ولون الواجهة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "اختر التوازن اللوني المناسب لخلفية التطبيق والزخارف الإسلامية:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val currentAppTheme = com.example.ui.theme.ThemeManager.currentThemeMode

                    // Ice Blue option
                    val isIceBlueSelected = currentAppTheme == com.example.ui.theme.AppThemeMode.ICE_BLUE_LIGHT
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedBgColor = "ice_blue"
                                com.example.ui.theme.ThemeManager.setTheme(context, com.example.ui.theme.AppThemeMode.ICE_BLUE_LIGHT)
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isIceBlueSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(
                            width = if (isIceBlueSelected) 2.dp else 1.dp,
                            color = if (isIceBlueSelected) Color(0xFF5078A1) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Circular Color Preview for Ice Blue
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            listOf(Color(0xFFF8FAFE), Color(0xFF9CBBD8))
                                        )
                                    )
                                    .border(1.dp, Color(0xFF3B5E7C).copy(alpha = 0.3f), CircleShape)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "الأزرق الجليدي",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, lineHeight = 14.sp),
                                fontWeight = if (isIceBlueSelected) FontWeight.Bold else FontWeight.Medium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                minLines = 2,
                                maxLines = 2,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "الافتراضي",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, lineHeight = 12.sp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Emerald Green option
                    val isEmeraldSelected = currentAppTheme == com.example.ui.theme.AppThemeMode.EMERALD_DARK
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedBgColor = "emerald_green"
                                com.example.ui.theme.ThemeManager.setTheme(context, com.example.ui.theme.AppThemeMode.EMERALD_DARK)
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isEmeraldSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(
                            width = if (isEmeraldSelected) 2.dp else 1.dp,
                            color = if (isEmeraldSelected) Color(0xFF52B788) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Circular Color Preview for Emerald Green
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            listOf(Color(0xFF061A13), Color(0xFF13402E))
                                        )
                                    )
                                    .border(1.dp, Color(0xFF52B788).copy(alpha = 0.5f), CircleShape)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "الأخضر الزمردي",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, lineHeight = 14.sp),
                                fontWeight = if (isEmeraldSelected) FontWeight.Bold else FontWeight.Medium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                minLines = 2,
                                maxLines = 2,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "زمرد داكن فاخر",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, lineHeight = 12.sp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Royal Dark option
                    val isDarkNightSelected = currentAppTheme == com.example.ui.theme.AppThemeMode.ROYAL_DARK
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedBgColor = "dark_night"
                                com.example.ui.theme.ThemeManager.setTheme(context, com.example.ui.theme.AppThemeMode.ROYAL_DARK)
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkNightSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(
                            width = if (isDarkNightSelected) 2.dp else 1.dp,
                            color = if (isDarkNightSelected) Color(0xFFC9A254) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Circular Color Preview for Royal Dark
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            listOf(Color(0xFF0F1115), Color(0xFF1E2229))
                                        )
                                    )
                                    .border(1.dp, Color(0xFFC9A254).copy(alpha = 0.5f), CircleShape)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "الداكن الملكي",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, lineHeight = 14.sp),
                                fontWeight = if (isDarkNightSelected) FontWeight.Bold else FontWeight.Medium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                minLines = 2,
                                maxLines = 2,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "مظلم مريح للعين",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, lineHeight = 12.sp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Smart Kashida & Reading Customization Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.TextFields,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تنسيق وحجم خط القراءة الاحترافية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                androidx.compose.material3.HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                // Text Size Multiplier Control (Centered)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "حجم خط القراءة (${(textSizeMultiplierSettings * 100).toInt()}%)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (textSizeMultiplierSettings > 0.8f) {
                                textSizeMultiplierSettings = (textSizeMultiplierSettings - 0.1f).coerceAtLeast(0.8f)
                                    sharedPref.edit().putFloat("text_size_multiplier", textSizeMultiplierSettings).apply()
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        androidx.compose.material3.Slider(
                            value = textSizeMultiplierSettings,
                            onValueChange = { textSizeMultiplierSettings = it },
                            onValueChangeFinished = {
                                sharedPref.edit().putFloat("text_size_multiplier", textSizeMultiplierSettings).apply()
                            },
                            valueRange = 0.8f..1.6f,
                            steps = 7,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                if (textSizeMultiplierSettings < 1.6f) {
                                textSizeMultiplierSettings = (textSizeMultiplierSettings + 0.1f).coerceAtMost(1.6f)
                                    sharedPref.edit().putFloat("text_size_multiplier", textSizeMultiplierSettings).apply()
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Live Preview Card
                Text(
                    text = "معاينة حية ومباشرة للخط العربي الأصيل والتنسيق:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    val sampleText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ (1) الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ (2) الرَّحْمَٰنِ الرَّحِيمِ (3) مَالِكِ يَوْمِ الدِّينِ (4)"
                    val formattedSample = applySmartKashida(sampleText)

                    Text(
                        text = formattedSample,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            fontSize = (18 * textSizeMultiplierSettings).sp,
                            lineHeight = (36 * textSizeMultiplierSettings).sp,
                            textDirection = androidx.compose.ui.text.style.TextDirection.Rtl,
                            lineBreak = androidx.compose.ui.text.style.LineBreak.Paragraph
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Justify,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Book,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("التوقيت الهجري", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("تحديث التقويم ليوافق الرؤية الشرعية للهلال.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isAuto = hijriSyncMode == "auto"
                    Button(
                        onClick = { 
                            hijriSyncMode = "auto" 
                            sharedPref.edit().putString("hijri_sync_mode", "auto").apply()
                            com.example.data.HijriSyncManager.schedulePeriodicSync(context)
                            syncResultText = "تم تفعيل التحديث التلقائي عبر الإنترنت."
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAuto) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (isAuto) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("عبر الإنترنت", fontWeight = if (isAuto) FontWeight.Bold else FontWeight.Medium)
                    }
                    
                    val isManual = hijriSyncMode == "manual"
                    Button(
                        onClick = { 
                            hijriSyncMode = "manual" 
                            sharedPref.edit().putString("hijri_sync_mode", "manual").apply()
                            syncResultText = "تم تفعيل وضع التعديل اليدوي وإيقاف التحديث التلقائي (باستثناء التحديثات الضرورية)."
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isManual) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (isManual) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تعديل يدوي", fontWeight = if (isManual) FontWeight.Bold else FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (hijriSyncMode == "auto") {
                    Button(
                        onClick = {
                            if (isSyncing) return@Button
                            isSyncing = true
                            syncResultText = "جاري الاتصال بـ GitHub وقراءة ملف التاريخ (hijri.json)..."
                            coroutineScope.launch {
                                try {
                                    kotlinx.coroutines.delay(800)
                                    val success = com.example.data.HijriSyncManager.syncIfNeeded(context, forceSync = true)
                                    if (success) {
                                        val currentRec = db.hijriDateRecordDao().getRecord()
                                        if (currentRec != null) {
                                            syncResultText = "تمت قراءة البيانات من GitHub بنجاح! (${currentRec.day} / ${currentRec.month} / ${currentRec.year} هـ)"
                                        } else {
                                            syncResultText = "تم قراءة وتحديث التوقيت الهجري بنجاح وفقاً لـ GitHub!"
                                        }
                                    } else {
                                        syncResultText = "تعذر جلب البيانات من رابط GitHub المباشر. يرجى التحقق من الاتصال بالإنترنت."
                                    }
                                } catch(e: Exception) {
                                    syncResultText = "حدث خطأ أثناء القراءة: ${e.message ?: "فشل الاتصال بالإنترنت"}"
                                }
                                isSyncing = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        if (isSyncing) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("جاري الكشف عن التوقيت الدقيق...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تحديث التوقيت الهجري الآن", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Text("أدخل التاريخ الهجري المعتمد ليومنا هذا:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = manualDayStr,
                            onValueChange = { manualDayStr = it },
                            label = { Text("اليوم (1-30)") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = manualMonthStr,
                            onValueChange = { manualMonthStr = it },
                            label = { Text("الشهر (1-12)") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = manualYearStr,
                            onValueChange = { manualYearStr = it },
                            label = { Text("السنة (1448)") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            modifier = Modifier.weight(1.2f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val dayInt = manualDayStr.toIntOrNull()
                            val monthInt = manualMonthStr.toIntOrNull()
                            val yearInt = manualYearStr.toIntOrNull()

                            if (dayInt == null || dayInt !in 1..30) {
                                syncResultText = "خطأ: يرجى إدخال يوم صحيح بين 1 و 30."
                                return@Button
                            }
                            if (monthInt == null || monthInt !in 1..12) {
                                syncResultText = "خطأ: يرجى إدخال شهر صحيح بين 1 و 12."
                                return@Button
                            }
                            if (yearInt == null || yearInt !in 1300..1600) {
                                syncResultText = "خطأ: يرجى إدخال سنة هجرية صحيحة."
                                return@Button
                            }

                            coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                val newRecord = com.example.data.HijriDateRecord(
                                    id = 1,
                                    day = dayInt,
                                    month = monthInt,
                                    year = yearInt,
                                    gregorianDateStr = java.time.LocalDate.now().toString(),
                                    status = "manual"
                                )
                                db.hijriDateRecordDao().insertRecord(newRecord)
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    syncResultText = "تم حفظ التاريخ اليدوي بنجاح! ($dayInt / $monthInt / $yearInt هـ)"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حفظ التاريخ اليدوي", fontWeight = FontWeight.Bold)
                    }
                }

                if (syncResultText.isNotEmpty()) {
                    Text(
                        text = syncResultText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (syncResultText.contains("بنجاح") || syncResultText.contains("تفعيل")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        
        Button(
            onClick = {
                // Trigger manual worker sync
                val sharedPref = context.getSharedPreferences("AhlAlBaytPrefs", android.content.Context.MODE_PRIVATE)
                sharedPref.edit().remove("last_event_notify_day").apply()
                val workRequest = androidx.work.OneTimeWorkRequestBuilder<com.example.worker.EventNotificationWorker>().build()
                androidx.work.WorkManager.getInstance(context).enqueue(workRequest)
                syncResultText = "جاري مزامنة المناسبات والتقويم الهجري في الخلفية..."
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(modifier = Modifier.width(8.dp))
            Text("مزامنة يدوية / التحقق من المناسبات الآن", fontWeight = FontWeight.Bold)
        }

        // Update App Card with Progress Bar, Percentage & Official Permissions Consent
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            if (com.example.UpdateChecker.isUpdateAvailable) {
                com.example.ui.UpdateProgressCard(
                    onInstallClick = {
                        com.example.UpdateChecker.showOfficialPermissionsDialog = true
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تحديثات تطبيق نور العترة",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "النسخة الحالية: v${com.example.BuildConfig.VERSION_NAME} (أحدث إصدار مثبت)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Button(
                onClick = {
                    com.example.UpdateChecker.checkForUpdate(context, isManualCheck = true)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("فحص تحديثات نور العترة", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Sawt Al-Quran App Direct Download & Update (تنزيل وتثبيت تطبيق صوت القرآن الكريم)
        var isQuranUpdateCardExpanded by remember { mutableStateOf(false) }
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Bar (قابل للطي بالضغط عليه)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isQuranUpdateCardExpanded = !isQuranUpdateCardExpanded }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "تنزيل تطبيق صوت القرآن الكريم",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "تحميل مباشر وتثبيت الحزمة بأحدث إصدار",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = if (isQuranUpdateCardExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isQuranUpdateCardExpanded) "طي" else "توسيع",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                androidx.compose.animation.AnimatedVisibility(visible = isQuranUpdateCardExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        when (quranUpdateStatus) {
                            com.example.ui.screens.settings.UpdateStatus.IDLE,
                            com.example.ui.screens.settings.UpdateStatus.CHECKING,
                            com.example.ui.screens.settings.UpdateStatus.UP_TO_DATE,
                            com.example.ui.screens.settings.UpdateStatus.AVAILABLE -> {
                                Text(
                                    text = "تطبيق صوت القرآن الكريم يتيح لك الاستماع وتلاوة القرآن الكريم بأصوات نخبة من أشهر القراء. اضغط للتحميل والتثبيت المباشر بصيغة APK.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val intent = android.content.Intent(
                                                android.content.Intent.ACTION_VIEW,
                                                android.net.Uri.parse(com.example.data.remote.AppUpdateManager.QURAN_RELEASE_PAGE)
                                            )
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("صفحة GitHub 🌐", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            quranUpdateStatus = com.example.ui.screens.settings.UpdateStatus.DOWNLOADING
                                            quranDownloadJob = coroutineScope.launch {
                                                com.example.data.remote.AppUpdateManager.downloadQuranApk(
                                                    context = context,
                                                    onProgress = { state: com.example.data.remote.DownloadState ->
                                                        when (state) {
                                                            is com.example.data.remote.DownloadState.Progress -> {
                                                                quranDownloadProgress = state.progressPercent / 100f
                                                                quranDownloadedMB = state.downloadedBytes / (1024f * 1024f)
                                                                quranTotalMB = state.totalBytes / (1024f * 1024f)
                                                                quranSpeedMBs = state.speedMBs
                                                            }
                                                            is com.example.data.remote.DownloadState.Completed -> {
                                                                quranDownloadedApkFile = state.apkFile
                                                                quranUpdateStatus = com.example.ui.screens.settings.UpdateStatus.DOWNLOADED
                                                                com.example.data.remote.AppUpdateManager.installApk(context, state.apkFile)
                                                            }
                                                            is com.example.data.remote.DownloadState.Error -> {
                                                                android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_LONG).show()
                                                                quranUpdateStatus = com.example.ui.screens.settings.UpdateStatus.IDLE
                                                            }
                                                            else -> {}
                                                        }
                                                    }
                                                )
                                            }
                                        },
                                        modifier = Modifier.weight(1.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("تحميل وتثبيت التطبيق", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            com.example.ui.screens.settings.UpdateStatus.DOWNLOADING -> {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "جاري تحميل تطبيق صوت القرآن...",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "${(quranDownloadProgress * 100).toInt()}%",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        LinearProgressIndicator(
                                            progress = { quranDownloadProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = String.format(Locale.US, "⚡ السرعة: %.1f ميغابايت/ث", quranSpeedMBs),
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = String.format(Locale.US, "%.1f / %.1f ميغابايت", quranDownloadedMB, quranTotalMB),
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                quranDownloadJob?.cancel()
                                                quranDownloadJob = null
                                                quranUpdateStatus = com.example.ui.screens.settings.UpdateStatus.IDLE
                                                android.widget.Toast.makeText(context, "تم إيقاف التحميل", android.widget.Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        ) {
                                            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("إلغاء التحميل", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            com.example.ui.screens.settings.UpdateStatus.DOWNLOADED -> {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.padding(14.dp)
                                    ) {
                                        Text(
                                            text = "✅ اكتمل تحميل ملف التطبيق بنجاح (جاهز للتثبيت)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            textAlign = TextAlign.Center
                                        )
                                        Button(
                                            onClick = {
                                                quranDownloadedApkFile?.let {
                                                    com.example.data.remote.AppUpdateManager.installApk(context, it)
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("تثبيت التطبيق الآن 🚀", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Prayer Times App Direct Download & Update (تنزيل وتثبيت تطبيق مواقيت الصلاة)
        var isPrayerUpdateCardExpanded by remember { mutableStateOf(false) }
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Bar (قابل للطي بالضغط عليه)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPrayerUpdateCardExpanded = !isPrayerUpdateCardExpanded }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "تنزيل تطبيق مواقيت الصلاة والأذان",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "تحميل مباشر وتثبيت الحزمة بأحدث إصدار v1.20.0",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = if (isPrayerUpdateCardExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isPrayerUpdateCardExpanded) "طي" else "توسيع",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                androidx.compose.animation.AnimatedVisibility(visible = isPrayerUpdateCardExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        when (prayerUpdateStatus) {
                            com.example.ui.screens.settings.UpdateStatus.IDLE,
                            com.example.ui.screens.settings.UpdateStatus.CHECKING,
                            com.example.ui.screens.settings.UpdateStatus.UP_TO_DATE,
                            com.example.ui.screens.settings.UpdateStatus.AVAILABLE -> {
                                Text(
                                    text = "تطبيق مواقيت الصلاة والأذان بدقة عالية مع اتجاه القبلة والتقويم الهجري وتنبيهات الأذان بصوت أشهر المؤذنين. اضغط للتحميل والتثبيت المباشر بصيغة APK عبر GitHub API.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val intent = android.content.Intent(
                                                android.content.Intent.ACTION_VIEW,
                                                android.net.Uri.parse(com.example.data.remote.AppUpdateManager.PRAYER_RELEASE_PAGE)
                                            )
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("صفحة GitHub 🌐", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            prayerUpdateStatus = com.example.ui.screens.settings.UpdateStatus.DOWNLOADING
                                            prayerDownloadJob = coroutineScope.launch {
                                                com.example.data.remote.AppUpdateManager.downloadPrayerApk(
                                                    context = context,
                                                    onProgress = { state: com.example.data.remote.DownloadState ->
                                                        when (state) {
                                                            is com.example.data.remote.DownloadState.Progress -> {
                                                                prayerDownloadProgress = state.progressPercent / 100f
                                                                prayerDownloadedMB = state.downloadedBytes / (1024f * 1024f)
                                                                prayerTotalMB = state.totalBytes / (1024f * 1024f)
                                                                prayerSpeedMBs = state.speedMBs
                                                            }
                                                            is com.example.data.remote.DownloadState.Completed -> {
                                                                prayerDownloadedApkFile = state.apkFile
                                                                prayerUpdateStatus = com.example.ui.screens.settings.UpdateStatus.DOWNLOADED
                                                                com.example.data.remote.AppUpdateManager.installApk(context, state.apkFile)
                                                            }
                                                            is com.example.data.remote.DownloadState.Error -> {
                                                                android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_LONG).show()
                                                                prayerUpdateStatus = com.example.ui.screens.settings.UpdateStatus.IDLE
                                                            }
                                                            else -> {}
                                                        }
                                                    }
                                                )
                                            }
                                        },
                                        modifier = Modifier.weight(1.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("تحميل وتثبيت التطبيق", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            com.example.ui.screens.settings.UpdateStatus.DOWNLOADING -> {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "جاري تحميل تطبيق مواقيت الصلاة...",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "${(prayerDownloadProgress * 100).toInt()}%",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        LinearProgressIndicator(
                                            progress = { prayerDownloadProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = String.format(Locale.US, "⚡ السرعة: %.1f ميغابايت/ث", prayerSpeedMBs),
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = String.format(Locale.US, "%.1f / %.1f ميغابايت", prayerDownloadedMB, prayerTotalMB),
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                prayerDownloadJob?.cancel()
                                                prayerDownloadJob = null
                                                prayerUpdateStatus = com.example.ui.screens.settings.UpdateStatus.IDLE
                                                android.widget.Toast.makeText(context, "تم إيقاف التحميل", android.widget.Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        ) {
                                            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("إلغاء التحميل", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            com.example.ui.screens.settings.UpdateStatus.DOWNLOADED -> {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.padding(14.dp)
                                    ) {
                                        Text(
                                            text = "✅ اكتمل تحميل ملف التطبيق بنجاح (جاهز للتثبيت)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            textAlign = TextAlign.Center
                                        )
                                        Button(
                                            onClick = {
                                                prayerDownloadedApkFile?.let {
                                                    com.example.data.remote.AppUpdateManager.installApk(context, it)
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("تثبيت التطبيق الآن 🚀", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Card: Developer Information Card (عن المطور والداعمين)
        var isDeveloperCardExpanded by remember { mutableStateOf(false) }
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isDeveloperCardExpanded = !isDeveloperCardExpanded }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "عن المطور والداعمين",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Icon(
                        imageVector = if (isDeveloperCardExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isDeveloperCardExpanded) "طي" else "توسيع",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }

                androidx.compose.animation.AnimatedVisibility(visible = isDeveloperCardExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // اسم المطور بتوسيط أنيق وبدون أيقونة
                        Text(
                            text = "المهندس محمد شهيد الأعاجيبي",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )

                        // أزرار التواصل المصغرة بنمط الحبوب الأنيق (Pill Buttons)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // زر واتساب المصغر
                            Button(
                                onClick = {
                                    try {
                                        val intent = android.content.Intent(
                                            android.content.Intent.ACTION_VIEW,
                                            android.net.Uri.parse("https://wa.me/9647831452279")
                                        )
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "تعذر فتح تطبيق واتساب", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = androidx.compose.ui.graphics.Color(0xFF25D366),
                                    contentColor = androidx.compose.ui.graphics.Color.White
                                ),
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("واتساب", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "واتساب",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            // زر اتصال المصغر
                            Button(
                                onClick = {
                                    try {
                                        val intent = android.content.Intent(
                                            android.content.Intent.ACTION_DIAL,
                                            android.net.Uri.parse("tel:+9647831452279")
                                        )
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "تعذر فتح تطبيق الاتصال", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = androidx.compose.ui.graphics.Color(0xFF4A90E2),
                                    contentColor = androidx.compose.ui.graphics.Color.White
                                ),
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("اتصال", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "اتصال",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            // زر بريد المصغر
                            Button(
                                onClick = {
                                    try {
                                        val intent = android.content.Intent(
                                            android.content.Intent.ACTION_SENDTO,
                                            android.net.Uri.parse("mailto:mohammed.sh.raheem@gmail.com")
                                        ).apply {
                                            putExtra(android.content.Intent.EXTRA_SUBJECT, "تطبيق نور العترة - استفسار / ملاحظة")
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "تعذر فتح تطبيق البريد الإلكتروني", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = androidx.compose.ui.graphics.Color(0xFFE5A93C),
                                    contentColor = androidx.compose.ui.graphics.Color(0xFF1E1E1E)
                                ),
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("بريد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "بريد",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // App Info, Copyright, Version & Year at the very end
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 36.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // أيقونة وشعار التطبيق (أيقونة التطبيق الرسمية)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.noor),
                        contentDescription = "أيقونة تطبيق نور العترة",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }

                Text(
                    text = "نور العترة",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Text(
                    text = "تطبيق القرآن الكريم وسيرة أهل البيت (عليهم السلام) والأدعية والزيارات والمواقيت الشرعية",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )

                // 1. اسم التطبيق
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "اسم التطبيق",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "تطبيق نور العترة",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 2. إصدار التطبيق
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "إصدار التطبيق",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "الإصدار ${com.example.BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // 3. سنة الإصدار والتوثيق
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سنة الإصدار والتوثيق",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "2026 م - 1448 هـ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 4. حقوق النشر والملكية
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "حقوق النشر والملكية",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Text(
                        text = "جميع الحقوق محفوظة © 2026 م - 1448 هـ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Text(
                        text = "وقف إسلامي خيري لوجه الله تعالى",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )

                Text(
                    text = "نسألكم الدعاء لوالدينا ولجميع المؤمنين والمؤمنات 🤲",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
@Composable
fun DetailRow(label: String, value: String, multiplier: Float = 1.0f) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, androidx.compose.ui.graphics.Color(0xFFD4AF37)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = androidx.compose.ui.graphics.Color(0xFF8B6B15),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = (15 * multiplier).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


data class WorshipCategory(
    val title: String,
    val color: androidx.compose.ui.graphics.Color,
    val iconRes: Int,
    val iconTint: androidx.compose.ui.graphics.Color? = null
)

@Composable
fun WorshipGridScreen(onCategorySelected: (String) -> Unit) {
    val currentMode = com.example.ui.theme.ThemeManager.currentThemeMode

    val circleBg = when (currentMode) {
        com.example.ui.theme.AppThemeMode.ROYAL_DARK -> androidx.compose.ui.graphics.Color(0xFF1E2128)
        com.example.ui.theme.AppThemeMode.EMERALD_DARK -> androidx.compose.ui.graphics.Color(0xFF0F261C)
        com.example.ui.theme.AppThemeMode.ICE_BLUE_LIGHT -> androidx.compose.ui.graphics.Color(0xFFEAF2F8)
    }

    val circleBorder = when (currentMode) {
        com.example.ui.theme.AppThemeMode.ROYAL_DARK -> androidx.compose.ui.graphics.Color(0xFFD4AF37)
        com.example.ui.theme.AppThemeMode.EMERALD_DARK -> androidx.compose.ui.graphics.Color(0xFF52B788)
        com.example.ui.theme.AppThemeMode.ICE_BLUE_LIGHT -> androidx.compose.ui.graphics.Color(0xFFB8D0E6)
    }

    val iconTintColor = when (currentMode) {
        com.example.ui.theme.AppThemeMode.ROYAL_DARK -> androidx.compose.ui.graphics.Color(0xFFE5C158)
        com.example.ui.theme.AppThemeMode.EMERALD_DARK -> androidx.compose.ui.graphics.Color(0xFF74C69D)
        com.example.ui.theme.AppThemeMode.ICE_BLUE_LIGHT -> androidx.compose.ui.graphics.Color(0xFF1E3A8A)
    }

    val gridItems = listOf(
        WorshipCategory("أذكار", circleBg, com.example.R.drawable.ic_worship_azkar_3d, iconTintColor),
        WorshipCategory("مناجاة", circleBg, com.example.R.drawable.ic_worship_munajat_3d, iconTintColor),
        WorshipCategory("الزيارات", circleBg, com.example.R.drawable.ic_worship_ziyarat_3d, iconTintColor),
        WorshipCategory("الادعية", circleBg, com.example.R.drawable.ic_worship_dua_3d, iconTintColor),
        WorshipCategory("الصحيفة السجادية", circleBg, com.example.R.drawable.ic_worship_sahifa_3d, iconTintColor),
        WorshipCategory("الصلاة", circleBg, com.example.R.drawable.ic_worship_salat_3d, iconTintColor),
        WorshipCategory("مواقيت الصلاة", circleBg, com.example.R.drawable.ic_mwaqet_alsalah_3d, iconTintColor),
        WorshipCategory("أحكام الصوم", circleBg, com.example.R.drawable.ic_worship_ahkam_sawm_3d, iconTintColor),
        WorshipCategory("أحكام الحجّ", circleBg, com.example.R.drawable.ic_worship_hajj, iconTintColor),
        WorshipCategory("الاعمال", circleBg, com.example.R.drawable.ic_worship_aamal, iconTintColor),
        WorshipCategory("تعقيبات الصلاة", circleBg, com.example.R.drawable.ic_worship_taqibat_3d, iconTintColor),
        WorshipCategory("المسبحة", circleBg, com.example.R.drawable.ic_worship_misbaha_3d, iconTintColor),
        WorshipCategory("الادعية المختارة", circleBg, com.example.R.drawable.ic_worship_selected_dua_3d, iconTintColor),
        WorshipCategory("عداد الركع", circleBg, com.example.R.drawable.ic_worship_rakat_counter_3d, iconTintColor),
        WorshipCategory("صلوات أهل البيت", circleBg, com.example.R.drawable.ic_worship_salawat_ahlalbayt_3d, iconTintColor)
    )

    androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(36.dp)
                        .background(circleBorder)
                ) // Vertical line
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "العبادات",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Icon(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_ornate_divider),
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.Unspecified,
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .fillMaxWidth()
                    .height(16.dp)
            )

            val rows = gridItems.chunked(4)
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { item ->
                            Column(
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onCategorySelected(item.title.replace("\n", " ")) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(
                                            circleBg, 
                                            shape = CircleShape
                                        )
                                        .border(1.dp, circleBorder, CircleShape),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    androidx.compose.foundation.Image(
                                        painter = androidx.compose.ui.res.painterResource(id = item.iconRes),
                                        contentDescription = item.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, lineHeight = 14.sp),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                        repeat(4 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
@Composable
fun WorshipScreen(navController: androidx.navigation.NavController, initialCategory: String? = null) {
    var selectedCategory by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf<String?>(initialCategory) }
    var selectedDuaSubCategory by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf("أدعية الأيام") }
    var selectedDeedsMonth by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf("محرم الحرام") }
    var selectedZiyaratSubCategory by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf("زيارة الائمة في ايام الاسبوع") }
    
    // Update selectedCategory if initialCategory changes (e.g. via bottom nav reselection while in a category)
    androidx.compose.runtime.LaunchedEffect(initialCategory) {
        if (initialCategory != null) {
            selectedCategory = initialCategory
        }
    }

    val handleBack: () -> Unit = {
        if (initialCategory != null) {
            navController.popBackStack()
        } else {
            selectedCategory = null
        }
    }

    androidx.activity.compose.BackHandler(enabled = selectedCategory != null) {
        handleBack()
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    if (selectedCategory == null) {
        WorshipGridScreen { 
            if (it == "اتجاه القبلة") {
                context.startActivity(android.content.Intent(context, com.example.qibla.QiblaCompassActivity::class.java))
            } else if (it == "أحكام الصوم") {
                selectedCategory = "أحكام الصوم"
            } else if (it == "أحكام الحجّ" || it == "أحكام الحج") {
                navController.navigate("worship_text/أحكام الحجّ")
            } else {
                selectedCategory = it 
            }
        }
    } else if (selectedCategory == "عداد الركع") {
        RakaaCounterScreen(onBack = handleBack)
    } else if (selectedCategory == "المسبحة") {
        TasbeehScreen(onBack = handleBack)
    } else if (selectedCategory == "مناجاة" || selectedCategory == "المناجاة") {
        MunajatScreen(onBack = handleBack, navController = navController)
    } else if (selectedCategory == "صلوات أهل البيت" || selectedCategory == "صلاة اهل البيت عليهم السلام") {
        AhlAlBaytPrayersScreen(onBack = handleBack, navController = navController)
    } else if (selectedCategory == "الادعية المختارة") {
        ConcisePrayersScreen(onBack = handleBack, navController = navController)
    } else if (selectedCategory == "مواقيت الصلاة") {
        PrayerTimesScreen(onBack = handleBack)
    } else {
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                IconButton(onClick = handleBack) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (selectedCategory == "أحكام الصوم") {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_worship_ahkam_sawm_3d),
                            contentDescription = "أحكام الصوم",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = selectedCategory ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                when (selectedCategory) {
                    "الادعية", "الادعية", "الأدعية" -> {
                        item {
                            val tabs = listOf("الأدعية العامة", "أدعية الأيام", "الصلوات على الحجج الطاهرين")
                            androidx.compose.material3.ScrollableTabRow(
                                selectedTabIndex = tabs.indexOf(selectedDuaSubCategory).takeIf { it >= 0 } ?: 0,
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.primary,
                                edgePadding = 8.dp,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                tabs.forEachIndexed { index, tab ->
                                    androidx.compose.material3.Tab(
                                        selected = selectedDuaSubCategory == tab,
                                        onClick = { selectedDuaSubCategory = tab },
                                        selectedContentColor = MaterialTheme.colorScheme.primary,
                                        unselectedContentColor = if (MaterialTheme.colorScheme.onBackground == androidx.compose.ui.graphics.Color.White) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        text = { Text(tab, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) }
                                    )
                                }
                            }
                        }
                        if (selectedDuaSubCategory == "أدعية الأيام") {
                            items(com.example.data.WorshipData.dailyPrayers.entries.toList()) { entry ->
                                DuaItemCard(entry) { navController.navigate("worship_text/${entry.value.first}") }
                            }
                        } else if (selectedDuaSubCategory == "الصلوات على الحجج الطاهرين") {
                            items(com.example.data.WorshipData.salawatsOnHujaj) { prayer ->
                                ZiyaratItemCard(prayer) { navController.navigate("worship_text/${prayer.first}") }
                            }
                        } else {
                            items(com.example.data.WorshipData.generalPrayers) { prayer ->
                                ZiyaratItemCard(prayer) { navController.navigate("worship_text/${prayer.first}") }
                            }
                        }
                    }
                    "تعقيبات الصلاة" -> {
                        items(com.example.data.WorshipData.taqeebat) { taqeeb ->
                            ZiyaratItemCard(taqeeb) { navController.navigate("worship_text/${taqeeb.first}") }
                        }
                    }
                    "الزيارات" -> {
                        item {
                            val tabs = listOf("الزيارات", "زيارة الائمة في ايام الاسبوع")
                            androidx.compose.material3.ScrollableTabRow(
                                selectedTabIndex = tabs.indexOf(selectedZiyaratSubCategory).takeIf { it >= 0 } ?: 0,
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.primary,
                                edgePadding = 8.dp,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                tabs.forEachIndexed { index, tab ->
                                    androidx.compose.material3.Tab(
                                        selected = selectedZiyaratSubCategory == tab,
                                        onClick = { selectedZiyaratSubCategory = tab },
                                        selectedContentColor = MaterialTheme.colorScheme.primary,
                                        unselectedContentColor = if (MaterialTheme.colorScheme.onBackground == androidx.compose.ui.graphics.Color.White) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        text = { Text(tab, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) }
                                    )
                                }
                            }
                        }
                        if (selectedZiyaratSubCategory == "الزيارات") {
                            items(com.example.data.WorshipData.ziyarats) { ziyarat ->
                                ZiyaratItemCard(ziyarat) { navController.navigate("worship_text/${ziyarat.first}") }
                            }
                        } else {
                            items(com.example.data.WorshipData.ziyaratsOfDays) { ziyarat ->
                                ZiyaratItemCard(ziyarat) { navController.navigate("worship_text/${ziyarat.first}") }
                            }
                        }
                    }
                    "أذكار", "الاذكار" -> {
                        items(com.example.data.WorshipData.azkarList) { azkar ->
                            ZiyaratItemCard(azkar) { navController.navigate("worship_text/${azkar.first}") }
                        }
                    }
                    "الاعمال", "الأعمال" -> {
                        item {
                            val tabs = listOf("محرم الحرام", "صفر", "ربيع الأول", "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة")
                            androidx.compose.material3.ScrollableTabRow(
                                selectedTabIndex = tabs.indexOf(selectedDeedsMonth).takeIf { it >= 0 } ?: 0,
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.primary,
                                edgePadding = 8.dp,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                tabs.forEachIndexed { index, tab ->
                                    androidx.compose.material3.Tab(
                                        selected = selectedDeedsMonth == tab,
                                        onClick = { selectedDeedsMonth = tab },
                                        selectedContentColor = MaterialTheme.colorScheme.primary,
                                        unselectedContentColor = if (MaterialTheme.colorScheme.onBackground == androidx.compose.ui.graphics.Color.White) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        text = { Text(tab, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) }
                                    )
                                }
                            }
                        }
                        val deedsList = com.example.data.WorshipData.monthlyDeeds[selectedDeedsMonth] ?: emptyList()
                        if (deedsList.isEmpty()) {
                            item {
                                Text("سيتم إضافة الأعمال قريباً...", modifier = Modifier.padding(32.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            items(deedsList) { deed ->
                                val uniqueTitle = "${deed.first} - $selectedDeedsMonth"
                                val uniqueDeed = Pair(uniqueTitle, deed.second)
                                DeedItemCard(uniqueDeed) { navController.navigate("worship_text/${uniqueDeed.first}") }
                            }
                        }
                    }
                    "الصلاة" -> {
                        items(com.example.data.WorshipData.obligatoryAndRecommendedPrayers) { prayer ->
                            ZiyaratItemCard(prayer) { navController.navigate("worship_text/${prayer.first}") }
                        }
                    }
                    "أحكام الصوم" -> {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(88.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                                            .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        androidx.compose.foundation.Image(
                                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_worship_ahkam_sawm_3d),
                                            contentDescription = "أحكام الصوم",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "أحكام الصوم والمسائل الفقهية",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "أحكام الصيام، المفطرات، الكفارات، أحكام القضاء، ومسائل الصوم الفقهية",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                        items(com.example.data.WorshipData.fastingRules) { rule ->
                            ZiyaratItemCard(rule) { navController.navigate("worship_text/${rule.first}") }
                        }
                        item {
                            ZiyaratItemCard(
                                Pair("صلاة العيدين (عيد الفطر المبارك وعيد الأضحى المبارك)", "")
                            ) {
                                navController.navigate("worship_text/صلاة العيدين (عيد الفطر المبارك وعيد الأضحى المبارك)")
                            }
                        }
                    }
                    "الصحيفة السجادية" -> {
                        items(com.example.data.WorshipData.sahifaSajjadiya) { supplication ->
                            ZiyaratItemCard(supplication) { navController.navigate("worship_text/${supplication.first}") }
                        }
                    }
                    else -> {
                        item {
                            Text("سيتم إضافة المحتوى قريباً...", modifier = Modifier.padding(32.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun DeedItemCard(deed: Pair<String, String>, onClick: () -> Unit) {
    val isDark = com.example.ui.theme.ThemeManager.currentThemeMode.isDark
    val textColor = if (isDark) androidx.compose.ui.graphics.Color(0xFFF2F4F7) else androidx.compose.ui.graphics.Color(0xFF0F1D30)
    val cardBg = if (isDark) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface
    var baseModifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onClick() }
    if (isDark) {
        baseModifier = baseModifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
    }
    Card(
        modifier = baseModifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = deed.first,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Right,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
        }
    }
}
@Composable
fun DuaItemCard(entry: Map.Entry<java.time.DayOfWeek, Pair<String, String>>, onClick: () -> Unit) {
    val isDark = com.example.ui.theme.ThemeManager.currentThemeMode.isDark
    val textColor = if (isDark) androidx.compose.ui.graphics.Color(0xFFF2F4F7) else androidx.compose.ui.graphics.Color(0xFF0F1D30)
    val cardBg = if (isDark) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface
    var baseModifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).clickable { onClick() }
    if (isDark) {
        baseModifier = baseModifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
    }
    Card(
        modifier = baseModifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = entry.value.first,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Right,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
        }
    }
}
@Composable
fun ZiyaratItemCard(ziyarat: Pair<String, String>, onClick: () -> Unit) {
    val isDark = com.example.ui.theme.ThemeManager.currentThemeMode.isDark
    val textColor = if (isDark) androidx.compose.ui.graphics.Color(0xFFF2F4F7) else androidx.compose.ui.graphics.Color(0xFF0F1D30)
    val cardBg = if (isDark) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface
    var baseModifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).clickable { onClick() }
    if (isDark) {
        baseModifier = baseModifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
    }
    Card(
        modifier = baseModifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = ziyarat.first,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Right,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
        }
    }
}
@Composable
fun TasbeehScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sharedPref = remember { context.getSharedPreferences("tasbeeh_prefs", Context.MODE_PRIVATE) }

    var soundEnabled by remember {
        mutableStateOf(sharedPref.getBoolean("sound_enabled", true))
    }
    var vibrationEnabled by remember {
        mutableStateOf(sharedPref.getBoolean("vibration_enabled", true))
    }

    var isZahra by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf(true) }
    var zahraStep by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf(0) }
    var zahraCount by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf(0) }
    var openCount by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf(0) }
    var openTargetCount by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf<Int?>(null) }

    var showTargetDialog by remember { androidx.compose.runtime.mutableStateOf(false) }
    var customTargetText by remember { androidx.compose.runtime.mutableStateOf("") }

    val zahraPhrases = listOf("الله أكبر", "الحمد لله", "سبحان الله", "تقبل الله الأعمال")
    val zahraTargets = listOf(34, 33, 33, 0)

    val toneGen = remember {
        try {
            android.media.ToneGenerator(android.media.AudioManager.STREAM_NOTIFICATION, 95)
        } catch (e: Exception) {
            null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                toneGen?.release()
            } catch (_: Exception) {}
        }
    }

    fun vibrateDevice(isCompletion: Boolean) {
        if (!vibrationEnabled) return
        try {
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            }
            if (vibrator != null && vibrator.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    if (isCompletion) {
                        val timings = longArrayOf(0, 120, 80, 240)
                        val amplitudes = intArrayOf(0, 240, 0, 255)
                        vibrator.vibrate(android.os.VibrationEffect.createWaveform(timings, amplitudes, -1))
                    } else {
                        vibrator.vibrate(android.os.VibrationEffect.createOneShot(18, 110))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    if (isCompletion) {
                        vibrator.vibrate(longArrayOf(0, 120, 80, 240), -1)
                    } else {
                        vibrator.vibrate(18)
                    }
                }
            }
        } catch (_: Exception) {}
    }

    fun playFeedbackSound(isCompletion: Boolean) {
        if (!soundEnabled) return
        try {
            if (isCompletion) {
                // Clear celebratory prompt tone
                toneGen?.startTone(android.media.ToneGenerator.TONE_PROP_PROMPT, 220)
            } else {
                // Instant hardware-level crisp keypress click
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
                audioManager?.playSoundEffect(android.media.AudioManager.FX_KEYPRESS_STANDARD, 0.6f)
                    ?: toneGen?.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 30)
            }
        } catch (_: Exception) {}
    }

    fun triggerFeedback(isCompletion: Boolean) {
        playFeedbackSound(isCompletion)
        vibrateDevice(isCompletion)
    }

    fun reset() {
        if (isZahra) {
            zahraStep = 0
            zahraCount = 0
        } else {
            openCount = 0
        }
        triggerFeedback(isCompletion = false)
    }

    fun onClick() {
        if (isZahra) {
            if (zahraStep < 3) {
                val target = zahraTargets[zahraStep]
                val newCount = zahraCount + 1
                if (newCount >= target) {
                    // Reached completion for this phrase! (e.g. 34 for Allahu Akbar, 33 for Alhamdulillah, 33 for Subhanallah)
                    triggerFeedback(isCompletion = true)
                    zahraCount = 0
                    zahraStep++
                } else {
                    zahraCount = newCount
                    triggerFeedback(isCompletion = false)
                }
            } else {
                // Restart cycle
                zahraStep = 0
                zahraCount = 0
                triggerFeedback(isCompletion = false)
            }
        } else {
            openCount++
            val target = openTargetCount
            if (target != null && target > 0) {
                if (openCount == target || (openCount > target && (openCount - target) % target == 0)) {
                    // Reached completion for target!
                    triggerFeedback(isCompletion = true)
                } else {
                    triggerFeedback(isCompletion = false)
                }
            } else {
                // Open count without fixed target: chime every 100
                if (openCount % 100 == 0) {
                    triggerFeedback(isCompletion = true)
                } else {
                    triggerFeedback(isCompletion = false)
                }
            }
        }
    }

    fun Int.toArabicNumerals(): String {
        val arabicNumerals = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        return this.toString().map { if (it.isDigit()) arabicNumerals[it - '0'] else it }.joinToString("")
    }

    if (showTargetDialog) {
        AlertDialog(
            onDismissRequest = { showTargetDialog = false },
            title = { Text("تحديد العدد المطلوب للتسبيح", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("أدخل أو اختر عدد التسبيحات المطلوب إكمالها لتنبيهك بنغمة عند الوصول إليه:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customTargetText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 6) {
                                customTargetText = input
                            }
                        },
                        label = { Text("العدد المطلوب") },
                        placeholder = { Text("مثلاً: 34 أو 100") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("أعداد شائعة:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(33, 34, 70, 100, 1000).forEach { num ->
                            OutlinedButton(
                                onClick = {
                                    openTargetCount = num
                                    showTargetDialog = false
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp)
                            ) {
                                Text(num.toArabicNumerals(), fontSize = 13.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = customTargetText.toIntOrNull()
                        if (num != null && num > 0) {
                            openTargetCount = num
                        } else if (customTargetText.isEmpty()) {
                            openTargetCount = null
                        }
                        showTargetDialog = false
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openTargetCount = null
                        showTargetDialog = false
                    }
                ) {
                    Text("بدون حد (مفتوح)")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 8.dp, end = 8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                // In RTL, this is on the right
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "المسبحة الإلكترونية",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    // Sound Toggle Button
                    IconButton(
                        onClick = {
                            val newValue = !soundEnabled
                            soundEnabled = newValue
                            sharedPref.edit().putBoolean("sound_enabled", newValue).apply()
                        }
                    ) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                            contentDescription = if (soundEnabled) "الصوت مفعّل" else "الصوت مكتوم",
                            tint = if (soundEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }

                    // Vibration Toggle Button
                    IconButton(
                        onClick = {
                            val newValue = !vibrationEnabled
                            vibrationEnabled = newValue
                            sharedPref.edit().putBoolean("vibration_enabled", newValue).apply()
                        }
                    ) {
                        Icon(
                            imageVector = if (vibrationEnabled) Icons.Filled.CheckCircle else Icons.Filled.CheckCircleOutline,
                            contentDescription = if (vibrationEnabled) "الاهتزاز مفعّل" else "الاهتزاز متوقف",
                            tint = if (vibrationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    
                    IconButton(onClick = { reset() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "إعادة ضبط",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mode Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isZahra) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .clickable { isZahra = true }
                        .padding(vertical = 10.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "تسبيح الزهراء (ع)",
                        color = if (isZahra) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = if (isZahra) FontWeight.Bold else FontWeight.Normal
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isZahra) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .clickable { isZahra = false }
                        .padding(vertical = 10.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "التسبيح المفتوح",
                        color = if (!isZahra) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = if (!isZahra) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Content Area
            if (isZahra) {
                // Step indicators for Tasbeeh Al-Zahra
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val stepsInfo = listOf("الله أكبر (٣٤)", "الحمد لله (٣٣)", "سبحان الله (٣٣)")
                    stepsInfo.forEachIndexed { idx, label ->
                        val isActive = zahraStep == idx
                        val isDone = zahraStep > idx
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                isActive -> MaterialTheme.colorScheme.primary
                                isDone -> androidx.compose.ui.graphics.Color(0xFF2E7D32).copy(alpha = 0.85f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) {
                            Text(
                                text = if (isDone) "✓ $label" else label,
                                fontSize = 11.sp,
                                fontWeight = if (isActive || isDone) FontWeight.Bold else FontWeight.Normal,
                                color = if (isActive || isDone) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = zahraPhrases[zahraStep],
                    fontSize = 36.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (zahraStep < 3) {
                    // Counter Display Box
                    Row(
                        modifier = Modifier
                            .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .height(72.dp)
                            .width(170.dp)
                    ) {
                        // Current Count
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                Text(
                                    text = "الحالي",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = zahraCount.toArabicNumerals(),
                                    fontSize = 26.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        // Target Count
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                Text(
                                    text = "الهدف",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = zahraTargets[zahraStep].toArabicNumerals(),
                                    fontSize = 26.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "سينبهك التطبيق بنغمة واهتزاز عند إكمال ${zahraTargets[zahraStep].toArabicNumerals()} تسبيحة",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "✨ تقبل الله طاعاتكم ✨\nتم إكمال تسبيح الزهراء عليها السلام بنجاح",
                            color = androidx.compose.ui.graphics.Color(0xFF1B5E20),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                val target = openTargetCount
                val isCompleted = target != null && target > 0 && openCount >= target

                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    if (isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = androidx.compose.ui.graphics.Color(0xFFE8F5E9),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "✨ اكتمل العدد المطلوب (${target.toArabicNumerals()}) بنجاح! ✨",
                                color = androidx.compose.ui.graphics.Color(0xFF1B5E20),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .border(
                                1.5.dp, 
                                if (isCompleted) androidx.compose.ui.graphics.Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline, 
                                RoundedCornerShape(12.dp)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .height(130.dp)
                            .width(140.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                Text(
                                    text = "العدد الحالي",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = openCount.toArabicNumerals(),
                                    fontSize = 28.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(if (isCompleted) androidx.compose.ui.graphics.Color(0xFFC8E6C9) else MaterialTheme.colorScheme.primaryContainer)
                                .clickable {
                                    customTargetText = openTargetCount?.toString() ?: ""
                                    showTargetDialog = true
                                },
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                    Text(
                                        text = "الهدف المحدد",
                                        fontSize = 10.sp,
                                        color = if (isCompleted) androidx.compose.ui.graphics.Color(0xFF1B5E20) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                        Text(
                                            text = if (target != null && target > 0) target.toArabicNumerals() else "مفتوح",
                                            fontSize = 20.sp,
                                            color = if (isCompleted) androidx.compose.ui.graphics.Color(0xFF1B5E20) else MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "تعديل الهدف",
                                            tint = (if (isCompleted) androidx.compose.ui.graphics.Color(0xFF1B5E20) else MaterialTheme.colorScheme.onPrimaryContainer).copy(alpha = 0.7f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "حدد عدد التسبيح للتنبيه عند إكماله:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp, androidx.compose.ui.Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        val options = listOf(null to "مفتوح", 33 to "٣٣", 34 to "٣٤", 70 to "٧٠", 100 to "١٠٠", 1000 to "١٠٠٠")
                        options.forEach { (targetVal, label) ->
                            val isSel = openTargetCount == targetVal
                            FilterChip(
                                selected = isSel,
                                onClick = { openTargetCount = targetVal },
                                label = { 
                                    Text(
                                        text = label, 
                                        fontSize = 11.sp, 
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        softWrap = false
                                    ) 
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                        AssistChip(
                            onClick = {
                                customTargetText = openTargetCount?.toString() ?: ""
                                showTargetDialog = true
                            },
                            label = { 
                                Text(
                                    text = "مخصص", 
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    softWrap = false
                                ) 
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Big Central Press Button
            Surface(
                modifier = Modifier
                    .size(190.dp)
                    .clip(CircleShape)
                    .clickable { onClick() },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 6.dp,
                border = BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TouchApp,
                            contentDescription = "سبّح",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isZahra && zahraStep >= 3) "إعادة البدء" else "سبّح",
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
@Composable
fun MunajatScreen(onBack: () -> Unit, navController: androidx.navigation.NavController) {
    var selectedTabIndex by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf(0) }
    val tabs = listOf("التسبيحات", "المناجاة")
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "المناجاة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
        }
        androidx.compose.material3.TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                androidx.compose.material3.TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                androidx.compose.material3.Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { 
                        Text(
                            text = title, 
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.titleMedium
                        ) 
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            if (selectedTabIndex == 0) {
                items(com.example.data.WorshipData.tasbeehat) { item ->
                    ZiyaratItemCard(item) { navController.navigate("worship_text/${item.first}") }
                }
            } else {
                items(com.example.data.WorshipData.munajat) { item ->
                    ZiyaratItemCard(item) { navController.navigate("worship_text/${item.first}") }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(navController: androidx.navigation.NavController, viewModel: FavoritesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val favorites by viewModel.allFavorites.collectAsState(initial = emptyList())
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPref = context.getSharedPreferences("AhlAlBaytPrefs", android.content.Context.MODE_PRIVATE)
    
    var textSizeMultiplier by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(sharedPref.getFloat("text_size_multiplier", 1.0f))
    }
    
    val listener = remember {
        android.content.SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == "text_size_multiplier") {
                textSizeMultiplier = prefs.getFloat(key, 1.0f)
            }
        }
    }

    androidx.compose.runtime.DisposableEffect(sharedPref) {
        sharedPref.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            sharedPref.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المحفوظات", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("لا توجد محفوظات حالياً.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
                items(favorites) { favorite ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { navController.navigate("worship_text/${favorite.title}") },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        val textColor = if (com.example.ui.theme.ThemeManager.currentThemeMode.isDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(
                                text = favorite.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Right,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun normalizeArabicText(str: String): String {
    return str
        .replace("[ًٌٍَُِّْـ]".toRegex(), "")
        .replace("[إأآٱ]", "ا")
        .replace("ى", "ي")
        .replace("ؤ", "و")
        .replace("ئ", "ي")
        .replace("ة", "ه")
        .trim()
}

private fun cleanTitleForMatching(str: String): String {
    return normalizeArabicText(str)
        .replace("رضوان الله عليه", "")
        .replace("رضوان الله عليها", "")
        .replace("عليه السلام", "")
        .replace("عليها السلام", "")
        .replace("عليهم السلام", "")
        .replace("صلى الله عليه واله", "")
        .replace("صلى الله عليه واله وسلم", "")
        .replace("رضي الله عنها", "")
        .replace("رضي الله عنه", "")
        .replace("عجل الله فرجه", "")
        .replace("دعاء", "")
        .replace("زياره", "")
        .replace("مناجاه", "")
        .replace("حديث", "")
        .replace("تسبيح", "")
        .replace("صلوات", "")
        .replace("[()（）\\[\\]]".toRegex(), " ")
        .replace("\\s+".toRegex(), " ")
        .trim()
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SingleTextScreen(title: String, onBack: () -> Unit, onNavigateToTitle: ((String) -> Unit)? = null) {
    val favoritesViewModel: FavoritesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val isFavorite by favoritesViewModel.isFavorite(title).collectAsState(initial = false)
    var text by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf("") }

    androidx.compose.runtime.LaunchedEffect(title) {
        val normTitle = normalizeArabicText(title)
        val cleanTitle = cleanTitleForMatching(title)

        val allLists = listOf(
            com.example.data.WorshipData.dailyPrayers.values.toList(),
            com.example.data.WorshipData.taqeebat,
            com.example.data.WorshipData.munajat,
            com.example.data.WorshipData.tasbeehat,
            com.example.data.WorshipData.azkarList,
            com.example.data.WorshipData.ziyarats,
            com.example.data.WorshipData.ziyaratsOfDays,
            com.example.data.WorshipData.sahifaSajjadiya,
            com.example.data.WorshipData.generalPrayers,
            com.example.data.WorshipData.salawatsOnHujaj,
            com.example.data.WorshipData.ahlAlBaytPrayers,
            com.example.data.WorshipData.concisePrayers,
            com.example.data.WorshipData.obligatoryAndRecommendedPrayers,
            com.example.data.WorshipData.fastingRules,
            com.example.data.WorshipData.hajjRules
        ).flatten()

        var found = allLists.find { it.first == title }
        if (found == null) {
            found = allLists.find { normalizeArabicText(it.first) == normTitle }
        }
        if (found == null && cleanTitle.isNotEmpty()) {
            found = allLists.find { cleanTitleForMatching(it.first) == cleanTitle }
        }
        if (found == null && cleanTitle.length >= 3) {
            found = allLists.find {
                val candidateClean = cleanTitleForMatching(it.first)
                candidateClean.isNotEmpty() && (candidateClean.contains(cleanTitle) || cleanTitle.contains(candidateClean))
            }
        }

        if (found != null) {
            text = found.second
        } else {
            val allDeeds = com.example.data.WorshipData.monthlyDeeds.flatMap { (month, deeds) -> 
                deeds.map { Pair("${it.first} - $month", it.second) } + deeds.map { Pair(it.first, it.second) }
            }
            var foundDeed = allDeeds.find { it.first == title }
            if (foundDeed == null) {
                foundDeed = allDeeds.find { normalizeArabicText(it.first) == normTitle }
            }
            if (foundDeed == null && cleanTitle.isNotEmpty()) {
                foundDeed = allDeeds.find { cleanTitleForMatching(it.first) == cleanTitle }
            }

            if (foundDeed != null) {
                text = foundDeed.second
            } else {
                // Fallback: search for any occurrence in allLists where words match
                val words = cleanTitle.split(" ").filter { it.length > 2 }
                if (words.isNotEmpty()) {
                    val candidate = allLists.find { item ->
                        val itemClean = cleanTitleForMatching(item.first)
                        words.all { w -> itemClean.contains(w) }
                    }
                    if (candidate != null) {
                        text = candidate.second
                    }
                }

                if (text.isEmpty()) {
                    text = "بِسْمِ اللهِ الرَّحْمَنِ الرَّحِيمِ\n\nنص ($title) متاح وموجود ضمن أقسام التطبيق.\n\nيمكنك القراءة والتصفح الكامل من خلال قائمة الأدعية والزيارات أو عبر محرك البحث المباشر."
                }
            }
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPref = context.getSharedPreferences("AhlAlBaytPrefs", android.content.Context.MODE_PRIVATE)

    var textSizeMultiplier by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(sharedPref.getFloat("text_size_multiplier", 1.0f))
    }

    val listener = remember {
        android.content.SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == "text_size_multiplier") {
                textSizeMultiplier = prefs.getFloat(key, 1.0f)
            }
        }
    }

    androidx.compose.runtime.DisposableEffect(sharedPref) {
        sharedPref.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            sharedPref.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        val sendIntent: android.content.Intent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, "$title\n\n$text")
                            type = "text/plain"
                        }
                        val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "مشاركة", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { 
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Text", "$title\n\n$text\n\nعبر تطبيق نور العترة...")
                        clipboard.setPrimaryClip(clip)
                        android.widget.Toast.makeText(context, "تم النسخ بنجاح", android.widget.Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "نسخ", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { 
                        favoritesViewModel.toggleFavorite(title, text, "نصوص") { _ -> }
                    }) {
                        Icon(
                            if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) androidx.compose.ui.graphics.Color.Red else MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { 
                        if (textSizeMultiplier < 2.0f) {
                            textSizeMultiplier += 0.1f
                            sharedPref.edit().putFloat("text_size_multiplier", textSizeMultiplier).apply()
                        }
                    }) {
                        Text("+", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { 
                        if (textSizeMultiplier > 0.8f) {
                            textSizeMultiplier -= 0.1f
                            sharedPref.edit().putFloat("text_size_multiplier", textSizeMultiplier).apply()
                        }
                    }) {
                        Text("-", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)
        ) {
            if (text.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("جاري التحميل...", color = MaterialTheme.colorScheme.onBackground)
                }
            } else {
                val parsedBlocks = androidx.compose.runtime.remember(text) {
                    val rawLines = text.split("\n")
                    val blocks = mutableListOf<Pair<String, String>>()
                    val currentLines = mutableListOf<String>()

                    fun flush() {
                        if (currentLines.isNotEmpty()) {
                            val combined = currentLines.joinToString(" ").trim()
                            if (combined.isNotEmpty()) {
                                blocks.add(Pair("TEXT", combined))
                            }
                            currentLines.clear()
                        }
                    }

                    for (line in rawLines) {
                        val trimmed = line.trim()
                        val cleaned = trimmed.replace(Regex("[ \\t]{2,}"), " ")
                        if (cleaned == "---") {
                            flush()
                            blocks.add(Pair("DIVIDER", "---"))
                        } else if ((cleaned.startsWith("[CENTER]") && cleaned.endsWith("[/CENTER]")) || cleaned.startsWith("#")) {
                            flush()
                            val cleanTitle = cleaned
                                .removePrefix("[CENTER]")
                                .removeSuffix("[/CENTER]")
                                .replace(Regex("^#+\\s*"), "")
                                .replace("#", "")
                                .trim()
                            if (cleanTitle.isNotEmpty()) {
                                blocks.add(Pair("TITLE", cleanTitle))
                            }
                        } else if (cleaned.isEmpty()) {
                            flush()
                        } else if (cleaned.matches(Regex("^[٠-٩0-9]+[\\.\\-]\\s*.*")) || cleaned.startsWith("- ") || cleaned.startsWith("* ")) {
                            flush()
                            blocks.add(Pair("TEXT", cleaned))
                        } else {
                            currentLines.add(cleaned)
                        }
                    }
                    flush()
                    blocks
                }

                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    item {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                lineHeight = (38 * textSizeMultiplier).sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                    parsedBlocks.forEach { (type, content) ->
                        item {
                            when (type) {
                                "DIVIDER" -> {
                                    androidx.compose.material3.HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 16.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    )
                                }
                                "TITLE" -> {
                                    Text(
                                        text = parseSimpleMarkdown(content),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = (34 * textSizeMultiplier).sp
                                        ),
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                                    )
                                }
                                "TEXT" -> {
                                    val lineToDisplay = applySmartKashida(content)
                                    Text(
                                        text = parseSimpleMarkdown(lineToDisplay),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                            lineHeight = (42 * textSizeMultiplier).sp,
                                            fontSize = (22 * textSizeMultiplier).sp,
                                            textDirection = androidx.compose.ui.text.style.TextDirection.Rtl,
                                            lineBreak = androidx.compose.ui.text.style.LineBreak.Paragraph
                                        ),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Justify,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                    if ((title == "زكاة الفطرة" || title == "أحكام الصوم") && onNavigateToTitle != null) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            androidx.compose.material3.Button(
                                onClick = {
                                    onNavigateToTitle("صلاة العيدين (عيد الفطر المبارك وعيد الأضحى المبارك)")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Row(
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                                ) {
                                    Text(
                                        text = "الانتقال إلى صلاة العيدين",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AhlAlBaytPrayersScreen(onBack: () -> Unit, navController: androidx.navigation.NavController) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "صلوات أهل البيت (عليهم السلام)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
        }
        val rightCol = listOf(
            "صلاة النبي (ص)",
            "صلاة السيدة فاطمة (س)",
            "صلاة الإمام الحسين (ع)",
            "صلاة الإمام الباقر (ع)",
            "صلاة الإمام الكاظم (ع)",
            "صلاة الإمام الجواد (ع)",
            "صلاة الإمام العسكري (ع)",
            "صلاة جعفر الطيّار (ع)"
        )
        val leftCol = listOf(
            "أعمال نهار الجُمعة",
            "صلاة أمير المؤمنين (ع)",
            "صلاة الإمام الحسن (ع)",
            "صلاة الإمام زين العابدين (ع)",
            "صلاة الإمام الصادق (ع)",
            "صلاة الإمام الرضا (ع)",
            "صلاة الإمام الهادي (ع)",
            "صلاة الإمام الحجّة (عج)",
            "أعمال يوم الجمعة"
        )
        val bottomItem = "أعمال ليلة الجُمعة"
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "صَلَوَاتُ وَأَعْمَالُ أَهْلِ الْبَيْتِ عَلَيْهِمُ السَّلَامُ وَيَوْمُ الْجُمُعَةِ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "مجموعة الصلوات المأثورة عن النبي الأكرم والأئمة المعصومين عليهم السلام بالإضافة إلى أعمال ليلة ويوم الجمعة المبارك.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
            val rowCount = maxOf(rightCol.size, leftCol.size)
            items(rowCount) { index ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (index < rightCol.size) {
                        val title = rightCol[index]
                        AhlAlBaytGridCard(
                            title = title,
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate("worship_text/$title") }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    if (index < leftCol.size) {
                        val title = leftCol[index]
                        AhlAlBaytGridCard(
                            title = title,
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate("worship_text/$title") }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            item {
                AhlAlBaytGridCard(
                    title = bottomItem,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate("worship_text/$bottomItem") }
                )
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun AhlAlBaytGridCard(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = com.example.ui.theme.ThemeManager.currentThemeMode.isDark
    val textColor = MaterialTheme.colorScheme.onSurface
    val cardBg = MaterialTheme.colorScheme.surface
    val baseModifier = modifier.height(80.dp).clickable { onClick() }
    val borderStroke = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
    
    Card(
        modifier = baseModifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = borderStroke
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp,
                    fontSize = 15.sp
                ),
                color = textColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ConcisePrayersScreen(onBack: () -> Unit, navController: androidx.navigation.NavController) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_worship_selected_dua_3d),
                    contentDescription = "الادعية المختارة",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "الادعية المختارة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(com.example.data.WorshipData.concisePrayers) { item ->
                ZiyaratItemCard(item) { navController.navigate("worship_text/${item.first}") }
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

fun parseSimpleMarkdown(text: String): androidx.compose.ui.text.AnnotatedString {
    val cleanText = text.replace("#", "")
    return androidx.compose.ui.text.buildAnnotatedString {
        val parts = cleanText.split("**")
        for ((index, part) in parts.withIndex()) {
            if (index % 2 == 1) {
                withStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) {
                    append(part)
                }
            } else {
                append(part)
            }
        }
    }
}

fun isArabicDiacritic(ch: Char): Boolean {
    return ch in '\u064B'..'\u065F' || ch == '\u0670' || ch == '\u0656' || ch in '\u06D6'..'\u06ED'
}

private val EXCLUDED_KASHIDA_WORDS = setOf(
    "الله", "لله", "بالله", "والله", "تالله", "محمد", "أحمد", "علي", "فاطمة", "حسن", "حسين",
    "إلهي", "رب", "ربي", "ربنا", "ربكم", "سبحان", "آل", "النبي", "رسول", "الإمام",
    "من", "عن", "في", "إلى", "على", "حتّى", "حتى", "مذ", "منذ",
    "ما", "لا", "لم", "لن", "إن", "أن", "إنما", "أنما", "كأن", "لكن", "ليت", "لعل",
    "ثم", "أو", "أم", "بل", "قد", "هل", "لو", "لولا", "لوما", "أما", "إما",
    "يا", "أي", "أيها", "أيتها", "أيا", "هيا", "وا", "كم", "كيف", "أين", "متى",
    "هو", "هي", "هما", "هم", "هن", "أنت", "أنتم", "أنتما", "أنتن", "أنا", "نحن",
    "هذا", "هذه", "هذان", "هاتان", "هؤلاء", "ذلك", "تلك", "ذلكم", "أولئك",
    "الذي", "التي", "اللذان", "اللتان", "الذين", "اللاتي", "اللواتي",
    "مع", "عند", "لدى", "بين", "قبل", "بعد", "فوق", "تحت", "دون", "غير", "سوى",
    "كل", "بعض", "ذو", "ذا", "ذي", "ذات",
    "به", "بها", "بهم", "بهما", "له", "لها", "لهم", "لهما", "فيه", "فيها", "فيهم", "منه", "منها", "منهم",
    "عنه", "عنها", "عنهم", "إليه", "إليها", "إليهم", "عليه", "عليها", "عليهم",
    "إليك", "عليك", "منك", "فيك", "لك", "بك", "معك", "عندك", "بنا", "لنا", "إلينا", "علينا", "منا", "عنا", "فينا", "معنا"
)

fun applySmartKashida(text: String): String {
    if (text.isBlank() || text.startsWith("---") || text.contains("[CENTER]")) return text
    if (text.contains("**")) {
        val parts = text.split("**")
        return parts.mapIndexed { idx, part ->
            if (idx % 2 == 1) part // Preserve bold markdown tags
            else applySmartKashidaToSegment(part)
        }.joinToString("**")
    }
    return applySmartKashidaToSegment(text)
}

private fun applySmartKashidaToSegment(segment: String): String {
    val tokens = segment.split(Regex("(?<=\\s)|(?=\\s)"))
    var wordsSinceLastKashida = 4
    val result = StringBuilder()

    for (token in tokens) {
        if (token.isBlank()) {
            result.append(token)
            continue
        }

        val cleanLetters = token.filter { it.isLetter() && !isArabicDiacritic(it) }
        val isEligible = cleanLetters.length >= 5 &&
                !EXCLUDED_KASHIDA_WORDS.contains(cleanLetters) &&
                !cleanLetters.contains("الله") &&
                !cleanLetters.contains("محمد") &&
                wordsSinceLastKashida >= 5

        if (isEligible) {
            val formatted = formatWordKashida(token)
            if (formatted != token) {
                result.append(formatted)
                wordsSinceLastKashida = 0
                continue
            }
        }

        result.append(token)
        wordsSinceLastKashida++
    }
    return result.toString()
}

private fun formatWordKashida(word: String): String {
    val canExtendLeft = setOf(
        'ب', 'ت', 'ث', 'ج', 'ح', 'خ', 'س', 'ش', 'ص', 'ض', 'ط', 'ظ', 'ع', 'غ', 'ف', 'ق', 'ك', 'ل', 'م', 'ن', 'ه', 'ي', 'ئ'
    )
    val canReceiveRight = setOf(
        'ب', 'ت', 'ث', 'ج', 'ح', 'خ', 'س', 'ش', 'ص', 'ض', 'ط', 'ظ', 'ع', 'غ', 'ف', 'ق', 'ك', 'ل', 'م', 'ن', 'ه', 'ي', 'ئ',
        'ا', 'أ', 'إ', 'آ', 'د', 'ذ', 'ر', 'ز', 'و', 'ؤ', 'ى', 'ة'
    )

    val baseCharIndices = mutableListOf<Int>()
    for (i in word.indices) {
        val ch = word[i]
        if (ch.isLetter() && !isArabicDiacritic(ch)) {
            baseCharIndices.add(i)
        }
    }

    val letterCount = baseCharIndices.size
    if (letterCount < 5) return word

    val validSlots = mutableListOf<Pair<Int, Char>>()
    val startsWithAl = word.startsWith("ال") || word.startsWith("وال") || word.startsWith("فال") || word.startsWith("بال")

    for (k in 0 until letterCount - 1) {
        if (startsWithAl && k <= 1) continue

        val charA = word[baseCharIndices[k]]
        val charB = word[baseCharIndices[k + 1]]

        if (canExtendLeft.contains(charA) && canReceiveRight.contains(charB)) {
            var insertPos = baseCharIndices[k] + 1
            while (insertPos < word.length && isArabicDiacritic(word[insertPos])) {
                insertPos++
            }
            validSlots.add(Pair(insertPos, charA))
        }
    }

    if (validSlots.isEmpty()) return word

    val seenSlot = validSlots.firstOrNull { it.second == 'س' || it.second == 'ش' }
    val emphSlot = validSlots.firstOrNull { it.second in setOf('ص', 'ض', 'ط', 'ظ') }
    val curveSlot = validSlots.firstOrNull { it.second in setOf('ف', 'ق', 'ك') }
    val midSlot = validSlots[validSlots.size / 2]

    val chosenSlotPos = (seenSlot ?: emphSlot ?: curveSlot ?: midSlot).first

    val sb = StringBuilder()
    for (i in word.indices) {
        sb.append(word[i])
        if (i + 1 == chosenSlotPos) {
            sb.append('ـ')
        }
    }
    return sb.toString()
}
