package com.example.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.NotificationSettingsManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.example.worker.AyahWorker

import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.ThemeManager
import com.example.data.remote.AppReleaseInfo
import com.example.data.remote.AppUpdateManager
import com.example.data.remote.DownloadState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // App Theme State
    var isThemeCardExpanded by remember { mutableStateOf(false) }

    // 1. Quran App Update State
    val quranCurrentVersion = AppUpdateManager.QURAN_CURRENT_VERSION
    var quranUpcomingVersion by remember { mutableStateOf(quranCurrentVersion) }
    var quranUpdateInfo by remember { mutableStateOf<AppReleaseInfo?>(null) }
    var isQuranUpdateExpanded by remember { mutableStateOf(false) }
    var quranUpdateStatus by remember { mutableStateOf(UpdateStatus.IDLE) }
    var quranDownloadProgress by remember { mutableStateOf(0f) }
    var quranDownloadedMB by remember { mutableStateOf(0f) }
    var quranTotalMB by remember { mutableStateOf(0f) }
    var quranSpeedMBs by remember { mutableStateOf(0f) }
    var quranDownloadedApkFile by remember { mutableStateOf<File?>(null) }
    var quranDownloadJob by remember { mutableStateOf<Job?>(null) }

    // Prayer Times App Update State
    val prayerCurrentVersion = AppUpdateManager.PRAYER_CURRENT_VERSION
    var prayerUpcomingVersion by remember { mutableStateOf(prayerCurrentVersion) }
    var prayerUpdateInfo by remember { mutableStateOf<AppReleaseInfo?>(null) }
    var isPrayerUpdateExpanded by remember { mutableStateOf(false) }
    var prayerUpdateStatus by remember { mutableStateOf(UpdateStatus.IDLE) }
    var prayerDownloadProgress by remember { mutableStateOf(0f) }
    var prayerDownloadedMB by remember { mutableStateOf(0f) }
    var prayerTotalMB by remember { mutableStateOf(0f) }
    var prayerSpeedMBs by remember { mutableStateOf(0f) }
    var prayerDownloadedApkFile by remember { mutableStateOf<File?>(null) }
    var prayerDownloadJob by remember { mutableStateOf<Job?>(null) }

    var isNourUpdateExpanded by remember { mutableStateOf(false) }
    var isDeveloperCardExpanded by remember { mutableStateOf(false) }
    var isAppInfoExpanded by remember { mutableStateOf(false) }
    var isNotificationsExpanded by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(NotificationSettingsManager.areNotificationsEnabled(context)) }
    // 2. Nour Al-Itrah App Update State

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets.statusBars
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "إعدادات التطبيق",
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        val isRtl = androidx.compose.ui.platform.LocalLayoutDirection.current == androidx.compose.ui.unit.LayoutDirection.Rtl
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.align(if (isRtl) Alignment.CenterEnd else Alignment.CenterStart)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    // NOTIFICATIONS CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isNotificationsExpanded = !isNotificationsExpanded }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "إعدادات الإشعارات",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = if (isNotificationsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isNotificationsExpanded) "طي" else "توسيع",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            AnimatedVisibility(visible = isNotificationsExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "باقة الإشعارات والتذكيرات اليومية",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "إرسال إشعارات بالآيات القرآنية بالرسم العثماني والعبادات وتنبيهات المناسبات عند اقترابها",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Switch(
                                            checked = notificationsEnabled,
                                            onCheckedChange = { enabled ->
                                                notificationsEnabled = enabled
                                                NotificationSettingsManager.setNotificationsEnabled(context, enabled)
                                                if (enabled) {
                                                    Toast.makeText(context, "تم تفعيل باقة الإشعارات والتذكيرات", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "تم إيقاف الإشعارات", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Hourly Limit Explanatory Card
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AccessTime,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "جدولة منتظمة: بحد أقصى إشعار واحد فقط كل ساعة لمنع التراكم أو الإزعاج.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Battery Optimization & Background Execution
                                    var isBatteryOptIgnored by remember {
                                        mutableStateOf(com.example.util.BackgroundOptimizationManager.isIgnoringBatteryOptimizations(context))
                                    }
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isBatteryOptIgnored) 
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                            else 
                                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (isBatteryOptIgnored) Icons.Default.BatteryChargingFull else Icons.Default.BatteryAlert,
                                                    contentDescription = null,
                                                    tint = if (isBatteryOptIgnored) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = if (isBatteryOptIgnored) 
                                                        "العمل بالخلفية: مستثنى من قيود البطارية ✅" 
                                                    else 
                                                        "العمل بالخلفية: قيود البطارية قد تؤخر الإشعارات ⚠️",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = if (isBatteryOptIgnored)
                                                    "التطبيق يتمتع بصلاحية العمل في الخلفية لإيصال التذكيرات في مواعيدها بدقة."
                                                else
                                                    "إذا كانت الإشعارات لا تصل إلا عند فتح التطبيق، يرجى استثناء التطبيق من تحسين البطارية ليعمل بشكل سليم.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (!isBatteryOptIgnored) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Button(
                                                    onClick = {
                                                        com.example.util.BackgroundOptimizationManager.requestIgnoreBatteryOptimization(context)
                                                        isBatteryOptIgnored = com.example.util.BackgroundOptimizationManager.isIgnoringBatteryOptimizations(context)
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary
                                                    )
                                                ) {
                                                    Text("استثناء التطبيق من قيود توفير البطارية", fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                                        // CARD 1: App Theme Selection Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isThemeCardExpanded = !isThemeCardExpanded }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "مظهر وثيمات التطبيق",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(ThemeManager.currentThemeMode.primaryColor)
                                            )
                                            Text(
                                                text = if (ThemeManager.currentThemeMode.isDark) "داكن" else "فاتح",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = if (isThemeCardExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isThemeCardExpanded) "طي" else "توسيع",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            AnimatedVisibility(visible = isThemeCardExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .padding(bottom = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "اختر الثيم والمظهر المفضل للواجهة والقراءة:",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    val modes = AppThemeMode.entries
                                    modes.chunked(2).forEach { rowModes ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            rowModes.forEach { mode ->
                                                val isSelected = ThemeManager.currentThemeMode == mode
                                                Surface(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable { ThemeManager.setTheme(context, mode) },
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                    border = BorderStroke(
                                                        if (isSelected) 2.dp else 1.dp,
                                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                                    )
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(10.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(20.dp)
                                                                .clip(CircleShape)
                                                                .background(mode.backgroundColor)
                                                                .border(1.dp, mode.primaryColor, CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(7.dp)
                                                                    .clip(CircleShape)
                                                                    .background(mode.primaryColor)
                                                            )
                                                        }

                                                        Text(
                                                            text = mode.titleAr.split(" ").firstOrNull() ?: mode.titleAr,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                            modifier = Modifier.weight(1f),
                                                            maxLines = 1
                                                        )

                                                        if (isSelected) {
                                                            Icon(
                                                                imageVector = Icons.Default.CheckCircle,
                                                                contentDescription = "محدد",
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            if (rowModes.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // CARD 2: Quran App Update Card (تحديث تطبيق صوت القرءان)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isQuranUpdateExpanded = !isQuranUpdateExpanded }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SystemUpdate,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "تحديث تطبيق صوت القرءان",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Icon(
                                    imageVector = if (isQuranUpdateExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isQuranUpdateExpanded) "طي" else "توسيع",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            AnimatedVisibility(visible = isQuranUpdateExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .padding(bottom = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = "الحالي: $quranCurrentVersion",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text(
                                                text = "القادم: $quranUpcomingVersion",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    when (quranUpdateStatus) {
                                        UpdateStatus.IDLE -> {
                                            Text(
                                                text = "اضغط للبحث عن تحديثات أحدث لتطبيق صوت القرءان عبر GitHub API",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                            Button(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        quranUpdateStatus = UpdateStatus.CHECKING
                                                        val result = AppUpdateManager.checkQuranUpdate()
                                                        val info = result.getOrNull()
                                                        if (info != null) {
                                                            quranUpdateInfo = info
                                                            quranUpcomingVersion = info.tagName
                                                            if (info.isNewerAvailable) {
                                                                quranUpdateStatus = UpdateStatus.AVAILABLE
                                                            } else {
                                                                quranUpdateStatus = UpdateStatus.UP_TO_DATE
                                                            }
                                                        } else {
                                                            quranUpdateStatus = UpdateStatus.UP_TO_DATE
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "افحص عن تحديثات",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }

                                        UpdateStatus.CHECKING -> {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    strokeWidth = 2.dp
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "جاري الفحص عن إصدارات أحدث...",
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }

                                        UpdateStatus.AVAILABLE -> {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(12.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = "🎉 تم العثور على إصدار جديد أحدث ($quranUpcomingVersion)!",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        textAlign = TextAlign.Center
                                                    )
                                                    Text(
                                                        text = quranUpdateInfo?.releaseNotes ?: "يتضمن التحديث الجديد تحسينات في التلاوات والأداء وإصلاحات عامة.",
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }

                                            Button(
                                                onClick = {
                                                    quranDownloadJob = coroutineScope.launch {
                                                        quranUpdateStatus = UpdateStatus.DOWNLOADING
                                                        quranDownloadProgress = 0f
                                                        quranDownloadedMB = 0f

                                                        AppUpdateManager.downloadQuranApk(
                                                            context = context,
                                                            onProgress = { state ->
                                                                when (state) {
                                                                    is DownloadState.Progress -> {
                                                                        quranDownloadProgress = state.progressPercent / 100f
                                                                        quranDownloadedMB = state.downloadedBytes / (1024f * 1024f)
                                                                        quranTotalMB = state.totalBytes / (1024f * 1024f)
                                                                        quranSpeedMBs = state.speedMBs
                                                                    }
                                                                    is DownloadState.Completed -> {
                                                                        quranDownloadedApkFile = state.apkFile
                                                                        quranUpdateStatus = UpdateStatus.DOWNLOADED
                                                                        installApk(context, state.apkFile)
                                                                    }
                                                                    is DownloadState.Error -> {
                                                                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                                                                        quranUpdateStatus = UpdateStatus.AVAILABLE
                                                                    }
                                                                    else -> {}
                                                                }
                                                            }
                                                        )
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.GetApp,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "تحميل وتثبيت التحديث الآن",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }

                                            TextButton(
                                                onClick = {
                                                    AppUpdateManager.postponeQuranVersion(context, quranUpcomingVersion)
                                                    quranUpdateStatus = UpdateStatus.UP_TO_DATE
                                                    Toast.makeText(context, "تم تأجيل التحديث", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Text(
                                                    text = "تأجيل التحديث",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        UpdateStatus.DOWNLOADING -> {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "جاري تحميل تحديث صوت القرءان...",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(
                                                        text = "${(quranDownloadProgress * 100).toInt()}%",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
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
                                                        text = String.format(Locale.US, "السرعة: %.1f ميغابايت/ث", quranSpeedMBs),
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = String.format(Locale.US, "%.1f / %.1f ميغابايت", quranDownloadedMB, quranTotalMB),
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                // Stop Download Button (زر إيقاف التحميل)
                                                OutlinedButton(
                                                    onClick = {
                                                        quranDownloadJob?.cancel()
                                                        quranDownloadJob = null
                                                        quranUpdateStatus = UpdateStatus.AVAILABLE
                                                        Toast.makeText(context, "تم إيقاف تحميل تحديث صوت القرءان", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        contentColor = MaterialTheme.colorScheme.error
                                                    ),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                                    shape = RoundedCornerShape(10.dp),
                                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Cancel,
                                                        contentDescription = "إيقاف",
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "إيقاف التحميل",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        UpdateStatus.DOWNLOADED -> {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "✅ اكتمل تحميل الملف التحديث بنجاح!",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    textAlign = TextAlign.Center
                                                )
                                                Button(
                                                    onClick = {
                                                        quranDownloadedApkFile?.let { apk ->
                                                            installApk(context, apk)
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary,
                                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                                    ),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.SystemUpdate,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "تثبيت تحديث التطبيق الآن",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }
                                        }

                                        UpdateStatus.UP_TO_DATE -> {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "تطبيقك محدّث بأحدث إصدار متوفر بالفعل ($quranCurrentVersion)",
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    textAlign = TextAlign.Center
                                                )
                                                OutlinedButton(
                                                    onClick = {
                                                        val intent = android.content.Intent(
                                                            android.content.Intent.ACTION_VIEW,
                                                            android.net.Uri.parse(AppUpdateManager.QURAN_RELEASE_PAGE)
                                                        )
                                                        context.startActivity(intent)
                                                    },
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("صفحة الإصدارات على GitHub 🌐", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // CARD 2.5: Prayer Times App Update & Download Card (تحديث وتنزيل تطبيق مواقيت الصلاة)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isPrayerUpdateExpanded = !isPrayerUpdateExpanded }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "تحديث وتنزيل تطبيق مواقيت الصلاة",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Icon(
                                    imageVector = if (isPrayerUpdateExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isPrayerUpdateExpanded) "طي" else "توسيع",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            AnimatedVisibility(visible = isPrayerUpdateExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .padding(bottom = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = "الحالي: $prayerCurrentVersion",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text(
                                                text = "القادم: $prayerUpcomingVersion",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    when (prayerUpdateStatus) {
                                        UpdateStatus.IDLE -> {
                                            Text(
                                                text = "اضغط للبحث عن تحديثات أحدث لتطبيق مواقيت الصلاة عبر GitHub API",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        prayerUpdateStatus = UpdateStatus.CHECKING
                                                        coroutineScope.launch {
                                                            val result = AppUpdateManager.checkPrayerUpdate()
                                                            val info = result.getOrNull()
                                                            if (info != null) {
                                                                prayerUpcomingVersion = info.tagName
                                                                prayerUpdateInfo = info
                                                                if (info.isNewerAvailable) {
                                                                    prayerUpdateStatus = UpdateStatus.AVAILABLE
                                                                } else {
                                                                    prayerUpdateStatus = UpdateStatus.UP_TO_DATE
                                                                }
                                                            } else {
                                                                prayerUpdateStatus = UpdateStatus.UP_TO_DATE
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("فحص التحديث 🔍", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }

                                                Button(
                                                    onClick = {
                                                        prayerUpdateStatus = UpdateStatus.DOWNLOADING
                                                        prayerDownloadJob = coroutineScope.launch {
                                                            AppUpdateManager.downloadPrayerApk(
                                                                context = context,
                                                                onProgress = { state ->
                                                                    when (state) {
                                                                        is DownloadState.Progress -> {
                                                                            prayerDownloadProgress = state.progressPercent / 100f
                                                                            prayerDownloadedMB = state.downloadedBytes / (1024f * 1024f)
                                                                            prayerTotalMB = state.totalBytes / (1024f * 1024f)
                                                                            prayerSpeedMBs = state.speedMBs
                                                                        }
                                                                        is DownloadState.Completed -> {
                                                                            prayerDownloadedApkFile = state.apkFile
                                                                            prayerUpdateStatus = UpdateStatus.DOWNLOADED
                                                                            AppUpdateManager.installApk(context, state.apkFile)
                                                                        }
                                                                        is DownloadState.Error -> {
                                                                            android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_LONG).show()
                                                                            prayerUpdateStatus = UpdateStatus.IDLE
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
                                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("تحميل APK مباشر ⬇️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        UpdateStatus.CHECKING -> {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center,
                                                modifier = Modifier.padding(8.dp)
                                            ) {
                                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("جارٍ فحص خوادم GitHub API...", fontSize = 13.sp)
                                            }
                                        }

                                        UpdateStatus.UP_TO_DATE -> {
                                            Text(
                                                text = "أنت تستخدم أحدث إصدار متاح من تطبيق مواقيت الصلاة 👍",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        prayerUpdateStatus = UpdateStatus.CHECKING
                                                        coroutineScope.launch {
                                                            val result = AppUpdateManager.checkPrayerUpdate()
                                                            val info = result.getOrNull()
                                                            if (info != null) {
                                                                prayerUpcomingVersion = info.tagName
                                                                prayerUpdateInfo = info
                                                                if (info.isNewerAvailable) {
                                                                    prayerUpdateStatus = UpdateStatus.AVAILABLE
                                                                } else {
                                                                    prayerUpdateStatus = UpdateStatus.UP_TO_DATE
                                                                }
                                                            } else {
                                                                prayerUpdateStatus = UpdateStatus.UP_TO_DATE
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text("إعادة الفحص", fontSize = 12.sp)
                                                }
                                                Button(
                                                    onClick = {
                                                        prayerUpdateStatus = UpdateStatus.DOWNLOADING
                                                        prayerDownloadJob = coroutineScope.launch {
                                                            AppUpdateManager.downloadPrayerApk(
                                                                context = context,
                                                                onProgress = { state ->
                                                                    when (state) {
                                                                        is DownloadState.Progress -> {
                                                                            prayerDownloadProgress = state.progressPercent / 100f
                                                                            prayerDownloadedMB = state.downloadedBytes / (1024f * 1024f)
                                                                            prayerTotalMB = state.totalBytes / (1024f * 1024f)
                                                                            prayerSpeedMBs = state.speedMBs
                                                                        }
                                                                        is DownloadState.Completed -> {
                                                                            prayerDownloadedApkFile = state.apkFile
                                                                            prayerUpdateStatus = UpdateStatus.DOWNLOADED
                                                                            AppUpdateManager.installApk(context, state.apkFile)
                                                                        }
                                                                        is DownloadState.Error -> {
                                                                            android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_LONG).show()
                                                                            prayerUpdateStatus = UpdateStatus.IDLE
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
                                                    Text("تنزيل APK مجدداً ⬇️", fontSize = 12.sp)
                                                }
                                            }
                                        }

                                        UpdateStatus.AVAILABLE -> {
                                            prayerUpdateInfo?.let { info ->
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(12.dp),
                                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(
                                                            text = "✨ ${info.releaseName}",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                        Text(
                                                            text = info.releaseNotes,
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            lineHeight = 16.sp
                                                        )
                                                    }
                                                }

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            AppUpdateManager.postponePrayerVersion(context, info.tagName)
                                                            prayerUpdateStatus = UpdateStatus.IDLE
                                                            android.widget.Toast.makeText(context, "تم تأجيل التحديث", android.widget.Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        shape = RoundedCornerShape(12.dp)
                                                    ) {
                                                        Text("تأجيل", fontSize = 12.sp)
                                                    }

                                                    Button(
                                                        onClick = {
                                                            prayerUpdateStatus = UpdateStatus.DOWNLOADING
                                                            prayerDownloadJob = coroutineScope.launch {
                                                                AppUpdateManager.downloadPrayerApk(
                                                                    context = context,
                                                                    onProgress = { state ->
                                                                        when (state) {
                                                                            is DownloadState.Progress -> {
                                                                            prayerDownloadProgress = state.progressPercent / 100f
                                                                            prayerDownloadedMB = state.downloadedBytes / (1024f * 1024f)
                                                                            prayerTotalMB = state.totalBytes / (1024f * 1024f)
                                                                            prayerSpeedMBs = state.speedMBs
                                                                        }
                                                                        is DownloadState.Completed -> {
                                                                            prayerDownloadedApkFile = state.apkFile
                                                                            prayerUpdateStatus = UpdateStatus.DOWNLOADED
                                                                            AppUpdateManager.installApk(context, state.apkFile)
                                                                        }
                                                                        is DownloadState.Error -> {
                                                                            android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_LONG).show()
                                                                            prayerUpdateStatus = UpdateStatus.IDLE
                                                                        }
                                                                        else -> {}
                                                                    }
                                                                }
                                                            )
                                                        }
                                                    },
                                                    modifier = Modifier.weight(1.5f),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text("تنزيل وتثبيت APK ⬇️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                        UpdateStatus.DOWNLOADING -> {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "جاري التحميل...",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(
                                                        text = "${(prayerDownloadProgress * 100).toInt()}%",
                                                        fontSize = 12.sp,
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
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = String.format(Locale.US, "السرعة: %.1f MB/s", prayerSpeedMBs),
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = String.format(Locale.US, "%.1f / %.1f MB", prayerDownloadedMB, prayerTotalMB),
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        prayerDownloadJob?.cancel()
                                                        prayerDownloadJob = null
                                                        prayerUpdateStatus = UpdateStatus.IDLE
                                                        android.widget.Toast.makeText(context, "تم إلغاء التحميل", android.widget.Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                                ) {
                                                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("إلغاء التحميل", fontSize = 12.sp)
                                                }
                                            }
                                        }

                                        UpdateStatus.DOWNLOADED -> {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "✅ تم تنزيل حزمة تطبيق مواقيت الصلاة بنجاح",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Button(
                                                    onClick = {
                                                        prayerDownloadedApkFile?.let {
                                                            AppUpdateManager.installApk(context, it)
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

                                    OutlinedButton(
                                        onClick = {
                                            val intent = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(AppUpdateManager.PRAYER_RELEASE_PAGE)
                                            )
                                            context.startActivity(intent)
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("صفحة الإصدارات على GitHub 🌐", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // CARD 3: Nour Al-Itrah App (تطبيق نور العترة)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isNourUpdateExpanded = !isNourUpdateExpanded }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "تطبيق نور العترة",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Icon(
                                    imageVector = if (isNourUpdateExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isNourUpdateExpanded) "طي" else "توسيع",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            AnimatedVisibility(visible = isNourUpdateExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .padding(bottom = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "تطبيق نور العترة هو تطبيق شقيق لتطبيق صوت القرآن. يمكنك تحميله أو تحديثه عبر الصفحة الرسمية.",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Button(
                                        onClick = {
                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(com.example.data.remote.AppUpdateManager.NOUR_RELEASE_PAGE))
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "تنزيل تطبيق نور العترة",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // CARD 4: Developer Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isDeveloperCardExpanded = !isDeveloperCardExpanded }
                                    .padding(16.dp)
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
                            AnimatedVisibility(visible = isDeveloperCardExpanded) {
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
                                                    android.widget.Toast.makeText(context, "تعذر فتح واتساب", android.widget.Toast.LENGTH_SHORT).show()
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

                    // App Info & Copyright Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isAppInfoExpanded = !isAppInfoExpanded }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(
                                    imageVector = if (isAppInfoExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isAppInfoExpanded) "طي" else "توسيع",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "عن التطبيق",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            AnimatedVisibility(visible = isAppInfoExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
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
                                        text = "تطبيق نور العترة",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "تطبيق القرآن الكريم وسيرة أهل البيت (عليهم السلام) والأدعية والزيارات والمواقيت الشرعية",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
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
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "جميع الحقوق محفوظة © 2026 م - 1448 هـ",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "وقف إسلامي خيري لوجه الله تعالى",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = "نسألكم الدعاء لوالدينا ولجميع المؤمنين والمؤمنات 🤲",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Extra Bottom Spacing outside cards to keep entire dialog scrollable above Android navigation bar
                    Spacer(modifier = Modifier.height(100.dp))
                }
        }
    }
}
}
