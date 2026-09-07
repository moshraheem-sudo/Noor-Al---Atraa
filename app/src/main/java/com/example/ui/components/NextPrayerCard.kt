package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.PrayerTimesData
import com.example.data.model.PrayerType
import com.example.data.repository.PrayerTimesRepository
import com.example.utils.AppStrings
import com.example.utils.PrayerCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun NextPrayerCard(
    onOpenPrayerScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { PrayerTimesRepository(context) }
    val selectedCity by repository.selectedCity.collectAsState()
    val rawPrayerData by repository.prayerTimesData.collectAsState()

    // Fallback to offline astronomical calculations if data not yet loaded
    val prayerData: PrayerTimesData = rawPrayerData ?: remember(selectedCity) {
        PrayerCalculator.calculateOfflinePrayerData(selectedCity)
    }

    var nextPrayerInfo by remember {
        mutableStateOf(PrayerCalculator.getNextPrayerInfo(prayerData))
    }

    var isExpanded by remember { mutableStateOf(false) }

    // Real-time ticking effect: updates countdown every second
    LaunchedEffect(prayerData) {
        while (isActive) {
            nextPrayerInfo = PrayerCalculator.getNextPrayerInfo(prayerData)
            delay(1000)
        }
    }

    val next = nextPrayerInfo
    val goldColor = Color(0xFFD4AF37)
    val tealAccent = Color(0xFF14B8A6)
    val nextPrayerType = next.prayerType
    val nextPrayerName = AppStrings.prayerName(nextPrayerType, AppLanguage.ARABIC)
    val nextTime12h = PrayerCalculator.formatTo12h(next.targetTimeStr, AppLanguage.ARABIC)
    val remainingFormatted = next.remainingFormatted

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
            1.2.dp,
            Brush.horizontalGradient(
                listOf(
                    goldColor.copy(alpha = 0.65f),
                    tealAccent.copy(alpha = 0.75f),
                    goldColor.copy(alpha = 0.65f)
                )
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Row 1: Header Row with City Pill (Right) & Expand Button (Left) - Non-colliding layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Selected City Badge with bounded max width and Ellipsis
                    Row(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .clip(RoundedCornerShape(10.dp))
                            .background(tealAccent.copy(alpha = 0.15f))
                            .border(1.dp, tealAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "المدينة المختارة",
                            tint = tealAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = selectedCity.nameAr,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Expand / Collapse Pill Button with clear text and icon
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(goldColor.copy(alpha = 0.12f))
                            .border(1.dp, goldColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isExpanded) "طي" else "كافة المواقيت",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = goldColor,
                            fontSize = 11.5.sp
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "طي" else "عرض المواقيت",
                            tint = goldColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 2: Hero Next Prayer & Large Clock Time (Side by side with dedicated columns so texts never collide)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Prayer Name and subtitle
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(goldColor, CircleShape)
                            )
                            Text(
                                text = "الصلاة القادمة",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = goldColor,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = nextPrayerName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Digital Clock Time (Large 12h)
                    Text(
                        text = nextTime12h,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 26.sp,
                        letterSpacing = (-0.5).sp,
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row 3: Countdown and Status line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Countdown Remaining Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(goldColor.copy(alpha = 0.12f))
                            .border(1.dp, goldColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = goldColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "متبقي $remainingFormatted",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.5.sp
                        )
                    }

                    Text(
                        text = if (isExpanded) "انقر للطي ▲" else "انقر للتفاصيل ▼",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Bar toward next prayer
                LinearProgressIndicator(
                    progress = { next.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = tealAccent,
                    trackColor = tealAccent.copy(alpha = 0.18f)
                )

                // Expanded Section: All remaining prayer and celestial timings
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        HorizontalDivider(
                            color = goldColor.copy(alpha = 0.25f),
                            thickness = 1.dp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Header inside expanded area
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "مواقيت اليوم لمدينة ${selectedCity.nameAr}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = goldColor,
                                fontSize = 13.sp
                            )

                            Text(
                                text = prayerData.hijriDate,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Full list of 8 timings in neat 2-column grid
                        val timingsList = listOf(
                            PrayerTimingItem("الفجر", prayerData.fajir, Icons.Default.Mosque, PrayerType.FAJR),
                            PrayerTimingItem("الشروق", prayerData.sunrise, Icons.Default.WbSunny, PrayerType.SUNRISE),
                            PrayerTimingItem("الظهر", prayerData.doher, Icons.Default.Brightness5, PrayerType.DHUHR),
                            PrayerTimingItem("العصر", prayerData.asr, Icons.Default.Brightness6, PrayerType.ASR),
                            PrayerTimingItem("الغروب", prayerData.sunset, Icons.Default.WbSunny, PrayerType.SUNSET),
                            PrayerTimingItem("المغرب", prayerData.maghrib, Icons.Default.Mosque, PrayerType.MAGHRIB),
                            PrayerTimingItem("العشاء", prayerData.isha, Icons.Default.Bedtime, PrayerType.ISHA),
                            PrayerTimingItem("منتصف الليل", prayerData.midnight, Icons.Default.Bedtime, PrayerType.MIDNIGHT)
                        )

                        // Display in 4 rows of 2 columns
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (chunk in timingsList.chunked(2)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    for (item in chunk) {
                                        val isThisNext = item.prayerType == nextPrayerType
                                        PrayerTimeGridCell(
                                            item = item,
                                            isNext = isThisNext,
                                            goldColor = goldColor,
                                            tealAccent = tealAccent,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Navigation Button to full Prayer Times window
                        Button(
                            onClick = onOpenPrayerScreen,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = tealAccent
                            ),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "فتح نافذة مواقيت الصلاة والأذان بالتفصيل",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class PrayerTimingItem(
    val title: String,
    val rawTime: String,
    val icon: ImageVector,
    val prayerType: PrayerType
)

@Composable
private fun PrayerTimeGridCell(
    item: PrayerTimingItem,
    isNext: Boolean,
    goldColor: Color,
    tealAccent: Color,
    modifier: Modifier = Modifier
) {
    val formattedTime = PrayerCalculator.formatTo12h(item.rawTime, AppLanguage.ARABIC)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isNext) tealAccent.copy(alpha = 0.20f)
                else MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
            )
            .border(
                1.dp,
                if (isNext) tealAccent else goldColor.copy(alpha = 0.22f),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = if (isNext) tealAccent else goldColor,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                    color = if (isNext) tealAccent else MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
                if (isNext) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(tealAccent, CircleShape)
                    )
                }
            }
        }
    }
}
