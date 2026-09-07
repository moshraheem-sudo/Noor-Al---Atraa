package com.example.ui.prayer.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Remove
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AdhanSoundMode
import com.example.data.model.AlarmRepeatMode
import com.example.data.model.AppLanguage
import com.example.data.model.CalculationMethod
import com.example.data.model.CityLocation
import com.example.data.model.MidnightMethod
import com.example.data.model.Muezzin
import com.example.data.model.PrayerCustomAlarmConfig
import com.example.data.model.PrayerTimesData
import com.example.data.model.PrayerType
import com.example.data.model.ThemeMode
import com.example.ui.theme.AppColors
import com.example.ui.theme.IslamicGold
import com.example.utils.AdhanPlaybackState
import com.example.utils.AdhanPlaybackStatus
import com.example.utils.AppStrings
import com.example.utils.MuezzinDownloadManager
import com.example.utils.MuezzinDownloadStatus
import com.example.utils.PrayerCalculator

@Composable
fun SettingsScreen(
    currentLanguage: AppLanguage = AppLanguage.ARABIC,
    onLanguageChange: (AppLanguage) -> Unit = {},
    currentThemeMode: ThemeMode = ThemeMode.LIGHT,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    selectedCity: CityLocation,
    prayerData: PrayerTimesData?,
    isGpsLocating: Boolean,
    gpsStatusMessage: String?,
    onGpsLocate: () -> Unit,
    calculationMethod: CalculationMethod = CalculationMethod.JAFARI_KAFEEL,
    onCalculationMethodChange: (CalculationMethod) -> Unit = {},
    midnightMethod: MidnightMethod = MidnightMethod.SUNSET_TO_FAJR,
    onMidnightMethodChange: (MidnightMethod) -> Unit = {},
    showAsrSeparate: Boolean = false,
    onShowAsrSeparateChange: (Boolean) -> Unit = {},
    showIshaSeparate: Boolean = false,
    onShowIshaSeparateChange: (Boolean) -> Unit = {},
    notificationsMap: Map<PrayerType, Boolean>,
    onToggleNotification: (PrayerType) -> Unit,
    prayerAlarmConfigs: Map<PrayerType, PrayerCustomAlarmConfig> = emptyMap(),
    onUpdatePrayerAlarmConfig: (PrayerCustomAlarmConfig) -> Unit = {},
    onPreviewCustomPrayerAlarm: (PrayerCustomAlarmConfig) -> Unit = {},
    prayerVisibilityMap: Map<PrayerType, Boolean> = emptyMap(),
    onTogglePrayerVisibility: (PrayerType) -> Unit = {},
    hijriSyncStatus: String? = null,
    onSyncHijriDate: () -> Unit = {},
    githubAutoSyncEnabled: Boolean = true,
    onGithubAutoSyncChange: (Boolean) -> Unit = {},
    githubRepoOwner: String = "moshraheem-sudo",
    githubRepoName: String = "hijri",
    githubToken: String = "",
    githubWebhookUrl: String = "",
    onSaveGithubSettings: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    isSendingDispatch: Boolean = false,
    dispatchStatusMessage: String? = null,
    onSendGithubDispatch: () -> Unit = {},
    lastHijriSyncTime: Long = 0L,
    onTestNotification: () -> Unit = {},
    prayerOffsets: Map<PrayerType, Int> = emptyMap(),
    onAdjustPrayerOffset: (PrayerType, Int) -> Unit = { _, _ -> },
    onResetAllPrayerOffsets: () -> Unit = {},
    manualHijriOffset: Int = 0,
    onAdjustManualHijriOffset: (Int) -> Unit = {},
    manualHijriCustomDate: String? = null,
    onSetManualHijriCustomDate: (String?) -> Unit = {},
    onResetManualHijri: () -> Unit = {},
    selectedMuezzin: Muezzin = Muezzin.defaultMuezzin,
    onSelectMuezzin: (Muezzin) -> Unit = {},
    isAdhanAudioEnabled: Boolean = true,
    onToggleAdhanAudio: (Boolean) -> Unit = {},
    adhanPlaybackState: AdhanPlaybackState = AdhanPlaybackState(),
    onPreviewMuezzin: (Muezzin) -> Unit = {},
    onStopAdhanPlayback: () -> Unit = {},
    muezzinDownloadStatuses: Map<String, MuezzinDownloadStatus> = emptyMap(),
    onDownloadAllMuezzins: () -> Unit = {},
    onDownloadMuezzin: (Muezzin) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Collapsible states for settings sections
    var isJafariCalcExpanded by remember { mutableStateOf(false) }
    var isPrayerOffsetsExpanded by remember { mutableStateOf(false) }
    var isVisibilityExpanded by remember { mutableStateOf(false) }
    var isNotificationsExpanded by remember { mutableStateOf(false) }
    var isMuezzinSectionExpanded by remember { mutableStateOf(false) }
    var isHijriExpanded by remember { mutableStateOf(false) }

    var isCustomHijriDialogOpen by remember { mutableStateOf(false) }
    var customHijriInputText by remember { mutableStateOf(manualHijriCustomDate ?: (prayerData?.hijriDate ?: "")) }

    // Dialog state for custom prayer alert customization
    var activeConfigPrayerType by remember { mutableStateOf<PrayerType?>(null) }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: Jafari Calculation & Fiqh Settings (إعدادات الحساب الفقهي والمواقيت الجعفرية - قابل للطي)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_jafari_calc_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { isJafariCalcExpanded = !isJafariCalcExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.current.tealGlow20),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = AppColors.current.tealAccentLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = AppStrings.jafariCalcSettingsTitle(currentLanguage),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.current.textTitle
                            )
                            Text(
                                text = if (currentLanguage == AppLanguage.ARABIC) 
                                    (if (isJafariCalcExpanded) "انقر للطي والتصغير" else "طرق الحساب وحساب منتصف الليل الشرعي")
                                else 
                                    (if (isJafariCalcExpanded) "Tap to collapse" else "Calculation methods & midnight calculation"),
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.current.tealAccentLight,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isJafariCalcExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isJafariCalcExpanded) "Collapse" else "Expand",
                        tint = AppColors.current.tealAccentLight,
                        modifier = Modifier.size(24.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isJafariCalcExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 1.dp
                        )

                        // 1. Calculation Method Options
                        Text(
                            text = AppStrings.calcMethodLabel(currentLanguage),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.current.textTitle,
                            fontSize = 13.sp
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            CalculationMethod.values().forEach { method ->
                                val isSelected = calculationMethod == method
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) AppColors.current.tealGlow10 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) AppColors.current.tealAccentLight else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onCalculationMethodChange(method) }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                        .testTag("calc_method_${method.name.lowercase()}"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isSelected) AppColors.current.tealAccentLight else AppColors.current.textSubtle,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (currentLanguage == AppLanguage.ARABIC) method.titleAr else method.titleEn,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) AppColors.current.textTitle else AppColors.current.textMain,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = if (currentLanguage == AppLanguage.ARABIC) method.descriptionAr else method.descriptionEn,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AppColors.current.textSubtle,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            thickness = 1.dp
                        )

                        // 2. Midnight Calculation Method Options
                        Text(
                            text = AppStrings.midnightMethodLabel(currentLanguage),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.current.textTitle,
                            fontSize = 13.sp
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            MidnightMethod.values().forEach { method ->
                                val isSelected = midnightMethod == method
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) AppColors.current.tealGlow10 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) AppColors.current.tealAccentLight else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onMidnightMethodChange(method) }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                        .testTag("midnight_method_${method.name.lowercase()}"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isSelected) AppColors.current.tealAccentLight else AppColors.current.textSubtle,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (currentLanguage == AppLanguage.ARABIC) method.titleAr else method.titleEn,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) AppColors.current.textTitle else AppColors.current.textMain,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = if (currentLanguage == AppLanguage.ARABIC) method.descriptionAr else method.descriptionEn,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AppColors.current.textSubtle,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Manual Prayer Time Adjustments (التعديل اليدوي لمواقيت الصلاة - قابل للطي)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_manual_prayer_offsets_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { isPrayerOffsetsExpanded = !isPrayerOffsetsExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.current.tealGlow20),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTimeFilled,
                                contentDescription = null,
                                tint = AppColors.current.tealAccentLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = AppStrings.manualPrayerOffsetsTitle(currentLanguage),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.current.textTitle
                            )
                            Text(
                                text = if (currentLanguage == AppLanguage.ARABIC) 
                                    (if (isPrayerOffsetsExpanded) "انقر للطي والتصغير" else "تقديم أو تأخير دقائق كل صلاة بشكل منفرد")
                                else 
                                    (if (isPrayerOffsetsExpanded) "Tap to collapse" else "Independently adjust minutes for each prayer"),
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.current.tealAccentLight,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isPrayerOffsetsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isPrayerOffsetsExpanded) "Collapse" else "Expand",
                        tint = AppColors.current.tealAccentLight,
                        modifier = Modifier.size(24.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isPrayerOffsetsExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 1.dp
                        )

                        Text(
                            text = if (currentLanguage == AppLanguage.ARABIC)
                                "يمكنك زيادة أو إنقاص وقت أي صلاة بالدقائق بشكل يدوي (مع الحفاظ الكامل على دقة باقي الحسابات الفلكية والمعادلات):"
                            else
                                "You can manually shift any prayer time forward or backward in minutes:",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.current.textSubtle,
                            fontSize = 12.sp
                        )

                        val prayersList = listOf(
                            Triple(PrayerType.FAJR, "🌅", prayerData?.fajir ?: "04:12"),
                            Triple(PrayerType.SUNRISE, "☀️", prayerData?.sunrise ?: "05:37"),
                            Triple(PrayerType.DHUHR, "🕛", prayerData?.doher ?: "11:57"),
                            Triple(PrayerType.ASR, "🌤️", prayerData?.asr ?: "15:28"),
                            Triple(PrayerType.SUNSET, "🌇", prayerData?.sunset ?: "18:17"),
                            Triple(PrayerType.MAGHRIB, "🌙", prayerData?.maghrib ?: "18:32"),
                            Triple(PrayerType.ISHA, "✨", prayerData?.isha ?: "19:35"),
                            Triple(PrayerType.MIDNIGHT, "🌌", prayerData?.midnight ?: "23:15")
                        )

                        prayersList.forEach { (type, emoji, time) ->
                            val offset = prayerOffsets[type] ?: 0
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                    .border(
                                        width = 1.dp,
                                        color = if (offset != 0) AppColors.current.tealAccentLight.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("prayer_offset_row_${type.name.lowercase()}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(emoji, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = AppStrings.prayerName(type, currentLanguage),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.current.textTitle,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = time,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AppColors.current.tealAccentLight,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Decrement -1 min
                                    IconButton(
                                        onClick = { onAdjustPrayerOffset(type, -1) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), CircleShape)
                                            .testTag("btn_minus_offset_${type.name.lowercase()}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Minus 1 min",
                                            tint = AppColors.current.textMain,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Offset badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when {
                                                    offset > 0 -> AppColors.current.tealGlow20
                                                    offset < 0 -> Color(0xFFEF5350).copy(alpha = 0.15f)
                                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when {
                                                offset > 0 -> "+$offset د"
                                                offset < 0 -> "$offset د"
                                                else -> "0 د"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                offset > 0 -> AppColors.current.tealAccentLight
                                                offset < 0 -> Color(0xFFEF5350)
                                                else -> AppColors.current.textSubtle
                                            },
                                            fontSize = 11.sp
                                        )
                                    }

                                    // Increment +1 min
                                    IconButton(
                                        onClick = { onAdjustPrayerOffset(type, 1) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), CircleShape)
                                            .testTag("btn_plus_offset_${type.name.lowercase()}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Plus 1 min",
                                            tint = AppColors.current.textMain,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Master reset button for prayer offsets
                        val hasAnyOffset = prayerOffsets.values.any { it != 0 }
                        if (hasAnyOffset) {
                            OutlinedButton(
                                onClick = onResetAllPrayerOffsets,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFEF5350)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .testTag("btn_reset_all_offsets")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RestartAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = AppStrings.resetAllOffsetsButton(currentLanguage),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Prayer Visibility Settings (إظهار وإخفاء المواقيت في الشاشة الرئيسية - قابل للطي)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_prayer_visibility_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { isVisibilityExpanded = !isVisibilityExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.current.tealGlow20),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = AppColors.current.tealAccentLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = AppStrings.prayerVisibilityTitle(currentLanguage),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.current.textTitle
                            )
                            Text(
                                text = if (currentLanguage == AppLanguage.ARABIC) 
                                    (if (isVisibilityExpanded) "انقر للطي والتصغير" else "تخصيص البطاقات الظاهرة في الواجهة")
                                else 
                                    (if (isVisibilityExpanded) "Tap to collapse" else "Customize visible prayer cards"),
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.current.tealAccentLight,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isVisibilityExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isVisibilityExpanded) "Collapse" else "Expand",
                        tint = AppColors.current.tealAccentLight,
                        modifier = Modifier.size(24.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isVisibilityExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 1.dp
                        )

                        val prayerList = listOf(
                            Triple(PrayerType.FAJR, "🌅", AppStrings.prayerDescription(PrayerType.FAJR, currentLanguage)),
                            Triple(PrayerType.SUNRISE, "☀️", AppStrings.prayerDescription(PrayerType.SUNRISE, currentLanguage)),
                            Triple(PrayerType.DHUHR, "☀️", AppStrings.prayerDescription(PrayerType.DHUHR, currentLanguage)),
                            Triple(PrayerType.ASR, "🌤️", AppStrings.prayerDescription(PrayerType.ASR, currentLanguage)),
                            Triple(PrayerType.SUNSET, "🌇", AppStrings.prayerDescription(PrayerType.SUNSET, currentLanguage)),
                            Triple(PrayerType.MAGHRIB, "🌙", AppStrings.prayerDescription(PrayerType.MAGHRIB, currentLanguage)),
                            Triple(PrayerType.ISHA, "✨", AppStrings.prayerDescription(PrayerType.ISHA, currentLanguage)),
                            Triple(PrayerType.MIDNIGHT, "🌌", AppStrings.prayerDescription(PrayerType.MIDNIGHT, currentLanguage))
                        )

                        prayerList.forEach { (prayer, emoji, desc) ->
                            val isVisible = prayerVisibilityMap[prayer] ?: true
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isVisible) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                                    .clickable { onTogglePrayerVisibility(prayer) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("visibility_toggle_${prayer.name.lowercase()}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = emoji,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = AppStrings.prayerName(prayer, currentLanguage),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isVisible) AppColors.current.textTitle else AppColors.current.textSubtle,
                                            fontWeight = if (isVisible) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AppColors.current.textSubtle,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Switch(
                                    checked = isVisible,
                                    onCheckedChange = { onTogglePrayerVisibility(prayer) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = AppColors.current.tealAccentLight,
                                        checkedTrackColor = AppColors.current.tealGlow40,
                                        uncheckedThumbColor = AppColors.current.textSubtle,
                                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Prayer Notification Settings (تنبيهات وإشعارات الصلوات - قابل للطي)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_notifications_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { isNotificationsExpanded = !isNotificationsExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.current.tealGlow20),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = AppColors.current.tealAccentLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = AppStrings.notificationsTitle(currentLanguage),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.current.textTitle
                            )
                            Text(
                                text = if (currentLanguage == AppLanguage.ARABIC) 
                                    (if (isNotificationsExpanded) "انقر للطي والتصغير" else "إعداد تنبيهات الأذان والأصوات")
                                else 
                                    (if (isNotificationsExpanded) "Tap to collapse" else "Configure Adhan notifications and sounds"),
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.current.tealAccentLight,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isNotificationsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isNotificationsExpanded) "Collapse" else "Expand",
                        tint = AppColors.current.tealAccentLight,
                        modifier = Modifier.size(24.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isNotificationsExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 1.dp
                        )

                        val prayerNotifications = listOf(
                            Triple(PrayerType.FAJR, "🌅", AppStrings.prayerDescription(PrayerType.FAJR, currentLanguage)),
                            Triple(PrayerType.SUNRISE, "☀️", AppStrings.prayerDescription(PrayerType.SUNRISE, currentLanguage)),
                            Triple(PrayerType.DHUHR, "☀️", AppStrings.prayerDescription(PrayerType.DHUHR, currentLanguage)),
                            Triple(PrayerType.ASR, "🌤️", AppStrings.prayerDescription(PrayerType.ASR, currentLanguage)),
                            Triple(PrayerType.SUNSET, "🌇", AppStrings.prayerDescription(PrayerType.SUNSET, currentLanguage)),
                            Triple(PrayerType.MAGHRIB, "🌙", AppStrings.prayerDescription(PrayerType.MAGHRIB, currentLanguage)),
                            Triple(PrayerType.ISHA, "✨", AppStrings.prayerDescription(PrayerType.ISHA, currentLanguage)),
                            Triple(PrayerType.MIDNIGHT, "🌌", AppStrings.prayerDescription(PrayerType.MIDNIGHT, currentLanguage))
                        )

                        prayerNotifications.forEach { (prayer, emoji, desc) ->
                            val config = prayerAlarmConfigs[prayer] ?: PrayerCustomAlarmConfig(prayerType = prayer)
                            val isEnabled = config.isEnabled
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isEnabled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                                    .border(
                                        1.dp,
                                        if (isEnabled) AppColors.current.tealAccentLight.copy(alpha = 0.25f) else Color.Transparent,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable { activeConfigPrayerType = prayer }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("notification_row_${prayer.name.lowercase()}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = emoji,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = AppStrings.prayerName(prayer, currentLanguage),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isEnabled) AppColors.current.textTitle else AppColors.current.textSubtle,
                                        fontWeight = if (isEnabled) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                
                                // Customize icon button
                                IconButton(
                                    onClick = { activeConfigPrayerType = prayer },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(AppColors.current.tealGlow10)
                                        .testTag("btn_custom_alert_${prayer.name.lowercase()}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Custom Alert",
                                        tint = AppColors.current.tealAccentLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                Switch(
                                    checked = isEnabled,
                                    onCheckedChange = { onToggleNotification(prayer) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = AppColors.current.tealAccentLight,
                                        checkedTrackColor = AppColors.current.tealGlow40,
                                        uncheckedThumbColor = AppColors.current.textSubtle,
                                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Test Notification Button
                        Button(
                            onClick = onTestNotification,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.current.tealGlow20,
                                contentColor = AppColors.current.tealAccentLight
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("test_prayer_notification_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = AppStrings.testNotificationButton(currentLanguage),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Official Muezzins & Adhan Audio (المؤذنون الرسميون وصوت الأذان - قابل للطي)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_muezzins_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { isMuezzinSectionExpanded = !isMuezzinSectionExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.current.tealGlow20),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = AppColors.current.tealAccentLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = AppStrings.muezzinSectionTitle(currentLanguage),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.current.textTitle
                            )
                            Text(
                                text = "${selectedMuezzin.nameAr} • " + if (isAdhanAudioEnabled) "مفعّل تلقائياً" else "معطّل",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.current.tealAccentLight,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isMuezzinSectionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isMuezzinSectionExpanded) "Collapse" else "Expand",
                        tint = AppColors.current.tealAccentLight,
                        modifier = Modifier.size(24.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isMuezzinSectionExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 1.dp
                        )

                        // Master Auto-Play Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = AppStrings.muezzinAutoPlayLabel(currentLanguage),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.current.textTitle
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = AppStrings.muezzinAutoPlayDesc(currentLanguage),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.current.textSubtle,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Switch(
                                checked = isAdhanAudioEnabled,
                                onCheckedChange = onToggleAdhanAudio,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AppColors.current.tealAccentLight,
                                    checkedTrackColor = AppColors.current.tealGlow40,
                                    uncheckedThumbColor = AppColors.current.textSubtle,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }

                        // Download All / Offline Status Header Card
                        val allDownloaded = Muezzin.values().all { muezzin ->
                            muezzinDownloadStatuses[muezzin.id] is MuezzinDownloadStatus.Downloaded
                        }
                        val anyDownloading = Muezzin.values().any { muezzin ->
                            muezzinDownloadStatuses[muezzin.id] is MuezzinDownloadStatus.Downloading
                        }

                        if (allDownloaded) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AppColors.current.tealGlow20)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AppColors.current.tealAccentLight,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = if (currentLanguage == AppLanguage.ARABIC) "تم تحميل جميع الأصوات بنجاح" else "All Audios Downloaded",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.current.textTitle
                                    )
                                    Text(
                                        text = if (currentLanguage == AppLanguage.ARABIC) "الأذان جاهز للعمل بشكل كامل وبدون إنترنت" else "Adhan is ready to work 100% offline",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppColors.current.tealAccentLight,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = onDownloadAllMuezzins,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.current.tealAccentLight,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("download_all_muezzins_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    if (anyDownloading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.CloudDownload,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Text(
                                        text = if (anyDownloading) {
                                            if (currentLanguage == AppLanguage.ARABIC) "جارٍ تحميل الصوتيات في الخلفية..." else "Downloading in background..."
                                        } else {
                                            AppStrings.downloadAllButton(currentLanguage)
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // Muezzins List
                        Text(
                            text = if (currentLanguage == AppLanguage.ARABIC) "قائمة المؤذنين المعتمدين:" else "Select Active Muezzin:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.current.textTitle,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Muezzin.values().forEach { muezzin ->
                            val isSelected = selectedMuezzin == muezzin
                            val isThisPlaying = adhanPlaybackState.currentMuezzinId == muezzin.id &&
                                    adhanPlaybackState.status == AdhanPlaybackStatus.PLAYING
                            val isThisBuffering = adhanPlaybackState.currentMuezzinId == muezzin.id &&
                                    adhanPlaybackState.status == AdhanPlaybackStatus.BUFFERING
                            val downloadStatus = muezzinDownloadStatuses[muezzin.id] ?: MuezzinDownloadStatus.NotDownloaded

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) AppColors.current.tealAccentLight else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { onSelectMuezzin(muezzin) }
                                    .testTag("muezzin_card_${muezzin.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) AppColors.current.tealGlow10 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) AppColors.current.tealAccentLight else MaterialTheme.colorScheme.surfaceVariant),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Mosque,
                                                    contentDescription = null,
                                                    tint = if (isSelected) Color.White else AppColors.current.textSubtle,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    text = if (currentLanguage == AppLanguage.ARABIC) muezzin.nameAr else muezzin.nameEn,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AppColors.current.textTitle,
                                                    fontSize = 15.sp,
                                                    lineHeight = 20.sp
                                                )
                                                Text(
                                                    text = if (currentLanguage == AppLanguage.ARABIC) muezzin.descriptionAr else muezzin.descriptionEn,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = AppColors.current.textSubtle,
                                                    fontSize = 11.sp,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (isSelected) AppColors.current.tealAccentLight else AppColors.current.textSubtle,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    // Download Status & Progress Row
                                    when (downloadStatus) {
                                        is MuezzinDownloadStatus.Downloaded -> {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(AppColors.current.tealGlow20)
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DownloadDone,
                                                    contentDescription = null,
                                                    tint = AppColors.current.tealAccentLight,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Text(
                                                    text = "${AppStrings.downloadedOfflineTag(currentLanguage)} (${MuezzinDownloadManager.formatBytes(downloadStatus.fileSizeBytes)})",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = AppColors.current.tealAccentLight,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                        is MuezzinDownloadStatus.Downloading -> {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = AppStrings.downloadingTag(downloadStatus.progressPercent, currentLanguage),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = AppColors.current.tealAccentLight,
                                                        fontSize = 10.sp
                                                    )
                                                    if (downloadStatus.progressPercent > 0) {
                                                        Text(
                                                            text = "${downloadStatus.progressPercent}%",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = AppColors.current.tealAccentLight,
                                                            fontSize = 10.sp
                                                        )
                                                    }
                                                }
                                                if (downloadStatus.progressPercent > 0) {
                                                    LinearProgressIndicator(
                                                        progress = { downloadStatus.progressPercent / 100f },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(4.dp)
                                                            .clip(RoundedCornerShape(2.dp)),
                                                        color = AppColors.current.tealAccentLight,
                                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                                    )
                                                } else {
                                                    LinearProgressIndicator(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(4.dp)
                                                            .clip(RoundedCornerShape(2.dp)),
                                                        color = AppColors.current.tealAccentLight,
                                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                        is MuezzinDownloadStatus.Failed -> {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = downloadStatus.error,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.error,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                TextButton(
                                                    onClick = { onDownloadMuezzin(muezzin) },
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.RestartAlt,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(13.dp),
                                                        tint = AppColors.current.tealAccentLight
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = if (currentLanguage == AppLanguage.ARABIC) "إعادة المحاولة" else "Retry",
                                                        fontSize = 10.sp,
                                                        color = AppColors.current.tealAccentLight,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                        is MuezzinDownloadStatus.NotDownloaded -> {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = AppStrings.notDownloadedTag(currentLanguage),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = AppColors.current.textSubtle,
                                                    fontSize = 10.sp
                                                )
                                                TextButton(
                                                    onClick = { onDownloadMuezzin(muezzin) },
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Download,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(13.dp),
                                                        tint = AppColors.current.tealAccentLight
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "تحميل الآن",
                                                        fontSize = 10.sp,
                                                        color = AppColors.current.tealAccentLight
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Preview / Audio Action Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        if (isThisBuffering) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp,
                                                    color = AppColors.current.tealAccentLight
                                                )
                                                Text(
                                                    text = AppStrings.bufferingAdhanText(currentLanguage),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = AppColors.current.tealAccentLight,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        } else if (isThisPlaying) {
                                            Text(
                                                text = AppStrings.playingAdhanText(currentLanguage),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = AppColors.current.tealAccentLight,
                                                fontSize = 11.sp
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }

                                        Button(
                                            onClick = { onPreviewMuezzin(muezzin) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isThisPlaying) MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else AppColors.current.tealGlow20,
                                                contentColor = if (isThisPlaying) MaterialTheme.colorScheme.error else AppColors.current.tealAccentLight
                                            ),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isThisPlaying) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Text(
                                                    text = if (isThisPlaying) AppStrings.stopAdhanButton(currentLanguage) else AppStrings.previewAdhanButton(currentLanguage),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Informational Footer Note
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.current.tealGlow10)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = AppStrings.offlineStreamingNotice(currentLanguage),
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.current.tealAccentLight,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Hijri Calendar Sync Settings (مزامنة التقويم الهجري - قابل للطي)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_hijri_sync_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { isHijriExpanded = !isHijriExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.current.tealGlow20),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = AppColors.current.tealAccentLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = AppStrings.hijriSyncTitle(currentLanguage),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.current.textTitle
                            )
                            Text(
                                text = if (currentLanguage == AppLanguage.ARABIC) 
                                    (if (isHijriExpanded) "انقر للطي والتصغير" else "التاريخ المعتمد والمزامنة التلقائية")
                                else 
                                    (if (isHijriExpanded) "Tap to collapse" else "Approved date and auto-sync"),
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.current.tealAccentLight,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isHijriExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isHijriExpanded) "Collapse" else "Expand",
                        tint = AppColors.current.tealAccentLight,
                        modifier = Modifier.size(24.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isHijriExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 1.dp
                        )

                        // Current Active Hijri Date Display
                        prayerData?.hijriDate?.let { hDate ->
                            val isManual = manualHijriOffset != 0 || manualHijriCustomDate != null
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(1.dp, if (isManual) IslamicGold.copy(alpha = 0.5f) else AppColors.current.tealGlow20, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("🌙", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (currentLanguage == AppLanguage.ARABIC) "التاريخ الهجري الحالي:" else "Current Hijri Date:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AppColors.current.textSubtle,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = PrayerCalculator.formatFullHijriDate(hDate, currentLanguage),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isManual) IslamicGold else AppColors.current.tealAccentLight,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    if (isManual) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (manualHijriCustomDate != null) {
                                                if (currentLanguage == AppLanguage.ARABIC) "(تعديل مخصص)" else "(Custom override)"
                                            } else {
                                                if (currentLanguage == AppLanguage.ARABIC) "(معدّل: ${if (manualHijriOffset > 0) "+$manualHijriOffset" else "$manualHijriOffset"} يوم)"
                                                else "(Adjusted: ${if (manualHijriOffset > 0) "+$manualHijriOffset" else "$manualHijriOffset"} days)"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = IslamicGold,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        // Sub-section: Manual Hijri Date Adjustments (تعديل يدوي بالأيام والتاريخ)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = AppStrings.manualHijriAdjustmentTitle(currentLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.current.textTitle,
                                fontSize = 13.sp
                            )
                            Text(
                                text = AppStrings.manualHijriAdjustmentSubtitle(currentLanguage),
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.current.textSubtle,
                                fontSize = 11.sp
                            )

                            // Day Offset Stepper
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (currentLanguage == AppLanguage.ARABIC) "تقديم / تأخير اليوم:" else "Shift Day:",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.current.textMain,
                                    fontSize = 12.sp
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Minus 1 Day
                                    IconButton(
                                        onClick = { onAdjustManualHijriOffset(-1) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), CircleShape)
                                            .testTag("btn_minus_hijri_day")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Minus 1 day",
                                            tint = AppColors.current.textMain,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Offset badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when {
                                                    manualHijriOffset > 0 -> AppColors.current.tealGlow20
                                                    manualHijriOffset < 0 -> Color(0xFFEF5350).copy(alpha = 0.15f)
                                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                }
                                            )
                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when {
                                                manualHijriOffset > 0 -> "+$manualHijriOffset يوم"
                                                manualHijriOffset < 0 -> "$manualHijriOffset يوم"
                                                else -> "اليوم (0)"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                manualHijriOffset > 0 -> AppColors.current.tealAccentLight
                                                manualHijriOffset < 0 -> Color(0xFFEF5350)
                                                else -> AppColors.current.textSubtle
                                            },
                                            fontSize = 11.sp
                                        )
                                    }

                                    // Plus 1 Day
                                    IconButton(
                                        onClick = { onAdjustManualHijriOffset(1) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), CircleShape)
                                            .testTag("btn_plus_hijri_day")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Plus 1 day",
                                            tint = AppColors.current.textMain,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Custom Text edit button & reset
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        customHijriInputText = manualHijriCustomDate ?: (prayerData?.hijriDate ?: "")
                                        isCustomHijriDialogOpen = true
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = AppColors.current.tealAccentLight
                                    )
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (currentLanguage == AppLanguage.ARABIC) "تعديل نص التاريخ" else "Custom Date Text",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (manualHijriOffset != 0 || manualHijriCustomDate != null) {
                                    OutlinedButton(
                                        onClick = onResetManualHijri,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(38.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Color(0xFFEF5350)
                                        )
                                    ) {
                                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = AppStrings.resetToAutoButton(currentLanguage),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Auto-sync Toggle Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onGithubAutoSyncChange(!githubAutoSyncEnabled) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = AppStrings.githubAutoSyncLabel(currentLanguage),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.current.textTitle,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = AppStrings.githubAutoSyncSubtitle(currentLanguage),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.current.textSubtle,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = githubAutoSyncEnabled,
                                onCheckedChange = onGithubAutoSyncChange,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AppColors.current.tealAccentLight,
                                    uncheckedThumbColor = AppColors.current.textSubtle,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.testTag("switch_github_auto_sync")
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Action Button: Sync Hijri Calendar Now
                        Button(
                            onClick = onSyncHijriDate,
                            enabled = hijriSyncStatus == null || !hijriSyncStatus.contains("جارٍ"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.current.tealGlow20,
                                contentColor = AppColors.current.tealAccentLight,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor = AppColors.current.textSubtle
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("settings_sync_hijri_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (hijriSyncStatus?.contains("جارٍ") == true) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = AppColors.current.tealAccentLight,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                } else {
                                    Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = AppStrings.syncHijriButton(currentLanguage, hijriSyncStatus?.contains("جارٍ") == true),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Sync status message
                        if (hijriSyncStatus != null) {
                            Text(
                                text = hijriSyncStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (hijriSyncStatus.contains("فشل") || hijriSyncStatus.contains("Failed")) MaterialTheme.colorScheme.error else AppColors.current.tealAccentLight,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

    }

    if (isCustomHijriDialogOpen) {
        AlertDialog(
            onDismissRequest = { isCustomHijriDialogOpen = false },
            title = {
                Text(
                    text = if (currentLanguage == AppLanguage.ARABIC) "تعديل التاريخ الهجري يدوياً" else "Manual Hijri Date Text",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.current.textTitle
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (currentLanguage == AppLanguage.ARABIC)
                            "أدخل التاريخ الهجري المطلوب عرضه (مثال: 15 شعبان 1448 هـ):"
                        else
                            "Enter the custom Hijri date text to display (e.g. 15 Shaban 1448 AH):",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.current.textSubtle
                    )
                    OutlinedTextField(
                        value = customHijriInputText,
                        onValueChange = { customHijriInputText = it },
                        modifier = Modifier.fillMaxWidth().testTag("input_custom_hijri_text"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.current.tealAccentLight,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customHijriInputText.isNotBlank()) {
                            onSetManualHijriCustomDate(customHijriInputText.trim())
                        }
                        isCustomHijriDialogOpen = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.current.tealAccentLight,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = if (currentLanguage == AppLanguage.ARABIC) "حفظ وتطبيق" else "Save & Apply",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { isCustomHijriDialogOpen = false }) {
                    Text(
                        text = if (currentLanguage == AppLanguage.ARABIC) "إلغاء" else "Cancel",
                        color = AppColors.current.textSubtle
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Custom Prayer Alarm Configuration Dialog
    activeConfigPrayerType?.let { prayer ->
        val currentConfig = prayerAlarmConfigs[prayer] ?: PrayerCustomAlarmConfig(prayerType = prayer)
        CustomPrayerAlarmDialog(
            prayerType = prayer,
            config = currentConfig,
            currentLanguage = currentLanguage,
            onDismiss = { activeConfigPrayerType = null },
            onSave = { updatedConfig ->
                onUpdatePrayerAlarmConfig(updatedConfig)
                activeConfigPrayerType = null
            },
            onPreview = { previewConfig ->
                onPreviewCustomPrayerAlarm(previewConfig)
            },
            onStopPreview = onStopAdhanPlayback
        )
    }
}

@Composable
private fun CustomPrayerAlarmDialog(
    prayerType: PrayerType,
    config: PrayerCustomAlarmConfig,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (PrayerCustomAlarmConfig) -> Unit,
    onPreview: (PrayerCustomAlarmConfig) -> Unit,
    onStopPreview: () -> Unit
) {
    val isNonPrayerEvent = prayerType == PrayerType.SUNRISE || prayerType == PrayerType.SUNSET || prayerType == PrayerType.MIDNIGHT

    var isEnabled by remember(config) { mutableStateOf(config.isEnabled) }
    var selectedSoundMode by remember(config) { 
        mutableStateOf(
            if (isNonPrayerEvent && (config.soundMode == AdhanSoundMode.FULL_ADHAN || config.soundMode == AdhanSoundMode.SHORT_TAKBIR))
                AdhanSoundMode.BEEP_ALERT
            else config.soundMode
        ) 
    }
    var selectedRepeatMode by remember(config) { mutableStateOf(config.repeatMode) }
    var volume by remember(config) { mutableStateOf(config.customVolumePercent / 100f) }
    var selectedMuezzinId by remember(config) { mutableStateOf(config.specificMuezzinId) }

    AlertDialog(
        onDismissRequest = {
            onStopPreview()
            onDismiss()
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppColors.current.tealGlow20),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = AppColors.current.tealAccentLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = if (currentLanguage == AppLanguage.ARABIC)
                            "تخصيص تنبيه ${AppStrings.prayerName(prayerType, currentLanguage)}"
                        else
                            "Custom Alert: ${AppStrings.prayerName(prayerType, currentLanguage)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.current.textTitle
                    )
                    Text(
                        text = if (isNonPrayerEvent) {
                            if (currentLanguage == AppLanguage.ARABIC) "خيارات التنبيه والتكرار" else "Alert and Repeat Options"
                        } else {
                            if (currentLanguage == AppLanguage.ARABIC) "خيارات الصوت، الأذان والتكرار" else "Sound, Adhan and Repeat Options"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.current.tealAccentLight,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Main toggle for this prayer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isNonPrayerEvent) {
                                if (currentLanguage == AppLanguage.ARABIC) "تفعيل التنبيه لهذا الوقت" else "Enable Alert for this Time"
                            } else {
                                if (currentLanguage == AppLanguage.ARABIC) "تفعيل التنبيه لهذه الصلاة" else "Enable Alert for this Prayer"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.current.textTitle
                        )
                        Text(
                            text = if (isEnabled) {
                                if (isNonPrayerEvent) {
                                    if (currentLanguage == AppLanguage.ARABIC) "التنبيه مفعل بالصوت المختار" else "Alert enabled with selected sound"
                                } else {
                                    if (currentLanguage == AppLanguage.ARABIC) "التنبيه مفعل ويشمل الأذان أو الصوت المختار" else "Alert enabled with Adhan or selected sound"
                                }
                            } else {
                                if (isNonPrayerEvent) {
                                    if (currentLanguage == AppLanguage.ARABIC) "التنبيه معطل لهذا الوقت" else "Alert disabled for this time"
                                } else {
                                    if (currentLanguage == AppLanguage.ARABIC) "التنبيه معطل لهذه الصلاة" else "Alert disabled for this prayer"
                                }
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.current.textSubtle,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppColors.current.tealAccentLight,
                            checkedTrackColor = AppColors.current.tealGlow40
                        )
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Section: Adhan Sound Mode
                Text(
                    text = if (isNonPrayerEvent) {
                        if (currentLanguage == AppLanguage.ARABIC) "صوت التنبيه:" else "Alert Sound:"
                    } else {
                        if (currentLanguage == AppLanguage.ARABIC) "صوت التنبيه والأذان:" else "Alert & Adhan Sound:"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.current.textTitle
                )

                val availableModes = if (isNonPrayerEvent) {
                    listOf(AdhanSoundMode.BEEP_ALERT, AdhanSoundMode.VIBRATE_ONLY)
                } else {
                    AdhanSoundMode.entries
                }

                availableModes.forEach { mode ->
                    val isSelected = selectedSoundMode == mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) AppColors.current.tealGlow10 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .border(
                                1.dp,
                                if (isSelected) AppColors.current.tealAccentLight else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedSoundMode = mode }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedSoundMode = mode },
                            colors = RadioButtonDefaults.colors(selectedColor = AppColors.current.tealAccentLight)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (currentLanguage == AppLanguage.ARABIC) mode.titleAr else mode.titleEn,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AppColors.current.tealAccentLight else AppColors.current.textMain
                            )
                            Text(
                                text = if (currentLanguage == AppLanguage.ARABIC) mode.subtitleAr else mode.subtitleEn,
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.current.textSubtle,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // If FULL_ADHAN is chosen, allow choosing specific Muezzin
                if (selectedSoundMode == AdhanSoundMode.FULL_ADHAN) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (currentLanguage == AppLanguage.ARABIC) "المؤذن المخصص لهذه الصلاة:" else "Muezzin for this Prayer:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.current.textTitle
                    )

                    // Default (General App Muezzin) option
                    val isDefaultSelected = selectedMuezzinId == null
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDefaultSelected) AppColors.current.tealGlow10 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                            .border(1.dp, if (isDefaultSelected) AppColors.current.tealAccentLight else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable { selectedMuezzinId = null }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isDefaultSelected,
                            onClick = { selectedMuezzinId = null },
                            colors = RadioButtonDefaults.colors(selectedColor = AppColors.current.tealAccentLight)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentLanguage == AppLanguage.ARABIC) "المؤذن العام المختار بالتطبيق" else "Default App Muezzin",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isDefaultSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isDefaultSelected) AppColors.current.tealAccentLight else AppColors.current.textMain
                        )
                    }

                    // Specific muezzins list
                    Muezzin.values().forEach { muezzin ->
                        val isMuezzinSelected = selectedMuezzinId == muezzin.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isMuezzinSelected) AppColors.current.tealGlow10 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                                .border(1.dp, if (isMuezzinSelected) AppColors.current.tealAccentLight else Color.Transparent, RoundedCornerShape(10.dp))
                                .clickable { selectedMuezzinId = muezzin.id }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isMuezzinSelected,
                                onClick = { selectedMuezzinId = muezzin.id },
                                colors = RadioButtonDefaults.colors(selectedColor = AppColors.current.tealAccentLight)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (currentLanguage == AppLanguage.ARABIC) muezzin.nameAr else muezzin.nameEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isMuezzinSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isMuezzinSelected) AppColors.current.tealAccentLight else AppColors.current.textMain
                                )
                                Text(
                                    text = if (currentLanguage == AppLanguage.ARABIC) muezzin.descriptionAr else muezzin.descriptionEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppColors.current.textSubtle,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Section: Alert Repetition (تكرار التنبيه)
                Text(
                    text = if (currentLanguage == AppLanguage.ARABIC) "تكرار التنبيه والتذكير:" else "Alert Repetition:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.current.textTitle
                )

                AlarmRepeatMode.entries.forEach { repeat ->
                    val isRepeatSelected = selectedRepeatMode == repeat
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isRepeatSelected) AppColors.current.tealGlow10 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .border(
                                1.dp,
                                if (isRepeatSelected) AppColors.current.tealAccentLight else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedRepeatMode = repeat }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isRepeatSelected,
                            onClick = { selectedRepeatMode = repeat },
                            colors = RadioButtonDefaults.colors(selectedColor = AppColors.current.tealAccentLight)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (currentLanguage == AppLanguage.ARABIC) repeat.titleAr else repeat.titleEn,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isRepeatSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isRepeatSelected) AppColors.current.tealAccentLight else AppColors.current.textMain
                            )
                            Text(
                                text = if (currentLanguage == AppLanguage.ARABIC) repeat.subtitleAr else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.current.textSubtle,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Volume slider (if sound is not VIBRATE_ONLY)
                if (selectedSoundMode != AdhanSoundMode.VIBRATE_ONLY) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (currentLanguage == AppLanguage.ARABIC) "مستوى الصوت:" else "Volume:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.current.textTitle
                            )
                            Text(
                                text = "${(volume * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.current.tealAccentLight
                            )
                        }

                        Slider(
                            value = volume,
                            onValueChange = { volume = it },
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = AppColors.current.tealAccentLight,
                                activeTrackColor = AppColors.current.tealAccentLight,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }

                // Preview Button
                OutlinedButton(
                    onClick = {
                        val previewConfig = PrayerCustomAlarmConfig(
                            prayerType = prayerType,
                            isEnabled = isEnabled,
                            soundMode = selectedSoundMode,
                            specificMuezzinId = selectedMuezzinId,
                            repeatMode = selectedRepeatMode,
                            customVolumePercent = (volume * 100).toInt().coerceIn(10, 100)
                        )
                        onPreview(previewConfig)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.current.tealAccentLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentLanguage == AppLanguage.ARABIC) "تجربة صوت التنبيه الآن 🔊" else "Test Sound Alert Now 🔊",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onStopPreview()
                    val newConfig = PrayerCustomAlarmConfig(
                        prayerType = prayerType,
                        isEnabled = isEnabled,
                        soundMode = selectedSoundMode,
                        specificMuezzinId = selectedMuezzinId,
                        repeatMode = selectedRepeatMode,
                        customVolumePercent = (volume * 100).toInt().coerceIn(10, 100)
                    )
                    onSave(newConfig)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.current.tealAccentLight,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (currentLanguage == AppLanguage.ARABIC) "حفظ الإعدادات" else "Save Settings",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onStopPreview()
                    onDismiss()
                }
            ) {
                Text(
                    text = if (currentLanguage == AppLanguage.ARABIC) "إلغاء" else "Cancel",
                    color = AppColors.current.textSubtle
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(22.dp)
    )
}
