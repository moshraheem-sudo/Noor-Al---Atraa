package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.CityLocation
import com.example.data.model.NextPrayerInfo
import com.example.data.model.PrayerTimesData
import com.example.data.model.PrayerType
import com.example.ui.theme.AppColors
import com.example.utils.AppStrings
import com.example.utils.PrayerCalculator
import java.util.Locale

@Composable
fun HomeScreen(
    currentLanguage: AppLanguage = AppLanguage.ARABIC,
    prayerData: PrayerTimesData?,
    nextPrayer: NextPrayerInfo?,
    liveTime: String,
    selectedCity: CityLocation,
    notificationsMap: Map<PrayerType, Boolean>,
    prayerVisibilityMap: Map<PrayerType, Boolean> = emptyMap(),
    errorMessage: String?,
    isOfflineMode: Boolean = false,
    isBackgroundSyncing: Boolean = false,
    isLoading: Boolean = false,
    isGpsLocating: Boolean = false,
    gpsStatusMessage: String? = null,
    onGpsClick: () -> Unit,
    onRefreshClick: () -> Unit = {},
    onToggleNotification: (PrayerType) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAr = currentLanguage == AppLanguage.ARABIC
    val cityName = if (isAr) selectedCity.nameAr else selectedCity.nameEn

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Title & Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = AppStrings.appTitle(currentLanguage),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = AppColors.current.textTitle,
                    fontSize = 28.sp
                )
            }
        }

        // Beautiful Card with City Name, Refresh & GPS Buttons, & Integrated Hijri & Gregorian Dates
        item {
            val hijriDisplay = PrayerCalculator.formatFullHijriDate(prayerData?.hijriDate, currentLanguage)
            val gregorianDisplay = PrayerCalculator.formatFullGregorianDate(prayerData?.gregorianDate, currentLanguage)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_location_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.current.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.current.tealGlow40)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Top Row: Selected City Name & Location / Refresh Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.current.tealGlow20)
                                    .border(1.5.dp, AppColors.current.tealAccentLight.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isGpsLocating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = AppColors.current.tealAccentLight,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Location Pin",
                                        tint = AppColors.current.tealAccentLight,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Clean City Name
                            Column {
                                Text(
                                    text = cityName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AppColors.current.textTitle,
                                    fontSize = 22.sp
                                )
                            }
                        }

                        // Action buttons: Refresh Button & GPS Locator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Refresh Button (زر التحديث)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AppColors.current.tealGlow10)
                                    .border(1.dp, AppColors.current.tealGlow20, RoundedCornerShape(12.dp))
                                    .clickable { onRefreshClick() }
                                    .testTag("home_refresh_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading || isBackgroundSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = AppColors.current.tealAccentLight,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = if (isAr) "تحديث" else "Refresh",
                                        tint = AppColors.current.tealAccentLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // GPS Quick Locate button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, AppColors.current.borderSubtle, RoundedCornerShape(12.dp))
                                    .clickable { onGpsClick() }
                                    .testTag("home_gps_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GpsFixed,
                                    contentDescription = "GPS",
                                    tint = AppColors.current.textMain,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }

                    // Elegant Divider
                    androidx.compose.material3.HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 1.dp
                    )

                    // GPS Status / feedback message if locating
                    if (gpsStatusMessage != null) {
                        Text(
                            text = AppStrings.translateGpsMessage(gpsStatusMessage, currentLanguage),
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.current.tealAccentLight,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Integrated Hijri & Gregorian Calendar Section (Full names and years, no truncation, no city button)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppColors.current.tealGlow10)
                            .border(1.dp, AppColors.current.tealGlow40, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Hijri Date Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mosque,
                                    contentDescription = null,
                                    tint = AppColors.current.tealAccentLight,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = hijriDisplay,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.current.textTitle,
                                    fontSize = 13.5.sp
                                )
                            }
                            Text(
                                text = if (isAr) "هجري" else "Hijri",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.current.tealAccentLight,
                                fontSize = 11.sp
                            )
                        }

                        androidx.compose.material3.HorizontalDivider(
                            color = AppColors.current.tealGlow20,
                            thickness = 0.8.dp
                        )

                        // Gregorian Date Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = AppColors.current.textMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = gregorianDisplay,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.current.textMain,
                                    fontSize = 12.5.sp
                                )
                            }
                            Text(
                                text = if (isAr) "ميلادي" else "Gregorian",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Normal,
                                color = AppColors.current.textMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        if (isOfflineMode) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.current.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.current.tealGlow20)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Offline",
                            tint = AppColors.current.tealAccentLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = AppStrings.offlineBanner(currentLanguage),
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.current.textMain,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        if (errorMessage != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.current.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Hero Card: Next Prayer & Real-Time Countdown (Compact & Elegant)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("next_prayer_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.current.heroGradientEnd),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.current.border)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    AppColors.current.heroGradientStart,
                                    AppColors.current.heroGradientEnd
                                )
                            )
                        )
                ) {
                    // Decorative ambient circle
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .align(Alignment.TopEnd)
                            .alpha(0.06f)
                            .background(
                                color = AppColors.current.tealAccentLight,
                                shape = CircleShape
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top row inside card: Notification icon & Next prayer label (Centered)
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            val nextType = nextPrayer?.prayerType ?: PrayerType.FAJR
                            val isNotifEnabled = notificationsMap[nextType] ?: true

                            // Centered Next Prayer Label
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(AppColors.current.tealAccentLight, CircleShape)
                                )

                                Text(
                                    text = AppStrings.nextPrayerLabel(currentLanguage, AppStrings.prayerName(nextType, currentLanguage)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.current.tealAccentLight,
                                    fontSize = 13.sp
                                )

                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(AppColors.current.tealAccentLight, CircleShape)
                                )
                            }

                            // Notification toggle icon on the side
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.current.tealGlow10)
                                    .clickable { onToggleNotification(nextType) }
                                    .align(if (isAr) Alignment.CenterStart else Alignment.CenterEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isNotifEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                                    contentDescription = "Toggle Notification",
                                    tint = if (isNotifEnabled) AppColors.current.tealAccentLight else AppColors.current.textMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Digital Clock Time (More compact & well proportioned)
                        val nextPrayerTimeStr = nextPrayer?.let { PrayerCalculator.formatTo12h(it.targetTimeStr, currentLanguage) } ?: (if (isAr) "04:35 ص" else "04:35 AM")
                        Text(
                            text = nextPrayerTimeStr,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = AppColors.current.textTitle,
                            fontSize = 32.sp,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Countdown text
                        val remainingText = nextPrayer?.remainingFormatted ?: "00:00"
                        Text(
                            text = AppStrings.countdownRemaining(currentLanguage, remainingText),
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.current.textMuted,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Smooth Glowing Progress Bar
                        val progress = nextPrayer?.progress ?: 0.5f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(AppColors.current.tealGlow10)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress.coerceIn(0.02f, 1f))
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                AppColors.current.tealAccent,
                                                AppColors.current.tealAccentLight
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }

        // Section Title: 5 Prayers & Phenomena
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = AppStrings.prayerSectionTitle(currentLanguage, cityName),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.current.textTitle,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                if (isBackgroundSyncing) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = AppColors.current.tealAccentLight,
                            strokeWidth = 1.5.dp
                        )
                        Text(
                            text = AppStrings.updatingStatus(currentLanguage),
                            fontSize = 10.sp,
                            color = AppColors.current.tealAccentLight
                        )
                    }
                }
            }
        }

        // Prayer and Astronomical Phenomena Items (filtered by user visibility preference)
        if (prayerData != null) {
            val allPrayerCards = listOfNotNull(
                PrayerCardInfo(
                    type = PrayerType.FAJR,
                    rawTime = prayerData.fajir,
                    emoji = "🌅",
                    icon = Icons.Default.Bedtime
                ),
                PrayerCardInfo(
                    type = PrayerType.SUNRISE,
                    rawTime = prayerData.sunrise,
                    emoji = "☀️",
                    icon = Icons.Default.Brightness5,
                    isMuted = true
                ),
                PrayerCardInfo(
                    type = PrayerType.DHUHR,
                    rawTime = prayerData.doher,
                    emoji = "☀️",
                    icon = Icons.Default.WbSunny
                ),
                if (prayerData.asr.isNotBlank()) {
                    PrayerCardInfo(
                        type = PrayerType.ASR,
                        rawTime = prayerData.asr,
                        emoji = "🌤️",
                        icon = Icons.Default.WbSunny
                    )
                } else null,
                PrayerCardInfo(
                    type = PrayerType.SUNSET,
                    rawTime = prayerData.sunset,
                    emoji = "🌇",
                    icon = Icons.Default.Brightness6
                ),
                PrayerCardInfo(
                    type = PrayerType.MAGHRIB,
                    rawTime = prayerData.maghrib,
                    emoji = "🌙",
                    icon = Icons.Default.Mosque
                ),
                if (prayerData.isha.isNotBlank()) {
                    PrayerCardInfo(
                        type = PrayerType.ISHA,
                        rawTime = prayerData.isha,
                        emoji = "✨",
                        icon = Icons.Default.Bedtime
                    )
                } else null,
                PrayerCardInfo(
                    type = PrayerType.MIDNIGHT,
                    rawTime = prayerData.midnight,
                    emoji = "🌌",
                    icon = Icons.Default.AccessTime
                )
            )

            val visiblePrayerCards = allPrayerCards.filter { prayerVisibilityMap[it.type] ?: true }

            items(visiblePrayerCards.size) { index ->
                val card = visiblePrayerCards[index]
                val isNext = nextPrayer?.prayerType == card.type
                val isNotif = notificationsMap[card.type] ?: true

                SophisticatedPrayerItemCard(
                    currentLanguage = currentLanguage,
                    prayerInfo = card,
                    isNext = isNext,
                    isNotificationEnabled = isNotif,
                    onToggleNotification = { onToggleNotification(card.type) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

data class PrayerCardInfo(
    val type: PrayerType,
    val rawTime: String,
    val emoji: String,
    val icon: ImageVector,
    val description: String = "",
    val isMuted: Boolean = false
)

@Composable
fun SophisticatedPrayerItemCard(
    currentLanguage: AppLanguage = AppLanguage.ARABIC,
    prayerInfo: PrayerCardInfo,
    isNext: Boolean,
    isNotificationEnabled: Boolean,
    onToggleNotification: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prayer_card_${prayerInfo.type.name.lowercase()}"),
        shape = RoundedCornerShape(if (isNext) 20.dp else 18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNext) AppColors.current.heroGradientStart else AppColors.current.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNext) 3.dp else 0.dp),
        border = if (isNext) {
            androidx.compose.foundation.BorderStroke(1.5.dp, AppColors.current.tealAccentLight)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, AppColors.current.borderSubtle)
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Center Indicator for Next Prayer (شريط ومؤشر الصلاة القادمة بأعلى البطاقة)
            if (isNext) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    AppColors.current.tealAccentLight.copy(alpha = 0.05f),
                                    AppColors.current.tealAccentLight.copy(alpha = 0.22f),
                                    AppColors.current.tealAccentLight.copy(alpha = 0.22f),
                                    AppColors.current.tealAccentLight.copy(alpha = 0.05f)
                                )
                            )
                        )
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(AppColors.current.tealAccentLight, CircleShape)
                        )
                        Text(
                            text = AppStrings.nextPrayerBadge(currentLanguage),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AppColors.current.tealAccentLight,
                            letterSpacing = 0.3.sp
                        )
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(AppColors.current.tealAccentLight, CircleShape)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = if (isNext) 12.dp else 14.dp)
                    .alpha(if (prayerInfo.isMuted && !isNext) 0.7f else 1f),
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
                            .background(
                                if (isNext) AppColors.current.tealGlow20 else AppColors.current.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = prayerInfo.emoji,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = AppStrings.prayerName(prayerInfo.type, currentLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                        color = if (isNext) AppColors.current.tealAccentLight else if (prayerInfo.isMuted) AppColors.current.textMuted else AppColors.current.textTitle,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = PrayerCalculator.formatTo12h(prayerInfo.rawTime, currentLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isNext) FontWeight.Black else FontWeight.Bold,
                            color = if (isNext) AppColors.current.tealAccentLight else if (prayerInfo.isMuted) AppColors.current.textMuted else AppColors.current.textMain,
                            fontSize = if (isNext) 17.sp else 16.sp
                        )
                        Text(
                            text = "${prayerInfo.rawTime} (24h)",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.current.textSubtle,
                            fontSize = 10.sp
                        )
                    }

                    IconButton(
                        onClick = onToggleNotification,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isNotificationEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                            contentDescription = "Notification Toggle",
                            tint = if (isNotificationEnabled) (if (isNext) AppColors.current.tealAccentLight else AppColors.current.textMuted) else AppColors.current.textSubtle.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
