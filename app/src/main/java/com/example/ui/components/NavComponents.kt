package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.CityLocation
import com.example.ui.AppTab
import com.example.ui.theme.AppColors
import com.example.ui.theme.SophisticatedBorderSubtle
import com.example.utils.AppStrings

@Composable
fun TopNavBar(
    selectedCity: CityLocation,
    isLoading: Boolean,
    isGpsLocating: Boolean = false,
    onGpsClick: () -> Unit,
    onRefreshClick: () -> Unit,
    currentLanguage: AppLanguage = AppLanguage.ARABIC,
    onBackClick: (() -> Unit)? = null,
    isSettingsOpen: Boolean = false,
    onSettingsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 6.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.current.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.current.borderSubtle)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                if (onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .testTag("prayer_top_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع",
                            tint = AppColors.current.textTitle
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_mwaqet_alsalah_3d),
                        contentDescription = null,
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSettingsOpen) "إعدادات المواقيت" else AppStrings.appTitle(currentLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.current.textTitle,
                        fontSize = 18.sp
                    )
                }
                if (onSettingsClick != null) {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .testTag("prayer_top_settings_button")
                            .then(
                                if (isSettingsOpen) {
                                    Modifier
                                        .clip(CircleShape)
                                        .background(AppColors.current.tealGlow20)
                                } else Modifier
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "الإعدادات",
                            tint = if (isSettingsOpen) AppColors.current.tealAccentLight else AppColors.current.textTitle
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppBottomNavBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    onGpsClick: () -> Unit = {},
    onRefreshClick: () -> Unit = {},
    isLoading: Boolean = false,
    currentLanguage: AppLanguage = AppLanguage.ARABIC,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.border(
            width = 1.dp,
            color = AppColors.current.borderSubtle,
            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
        ),
        containerColor = AppColors.current.surface,
        tonalElevation = 8.dp
    ) {
        val selectedTab1 = currentTab == AppTab.PRAYER_TIMES
        val prayerTimesTitle = if (currentLanguage == AppLanguage.ARABIC) AppTab.PRAYER_TIMES.titleAr else AppTab.PRAYER_TIMES.titleEn
        val settingsTitle = if (currentLanguage == AppLanguage.ARABIC) AppTab.SETTINGS.titleAr else AppTab.SETTINGS.titleEn
        
        NavigationBarItem(
            selected = selectedTab1,
            onClick = { onTabSelected(AppTab.PRAYER_TIMES) },
            icon = {
                Icon(
                    imageVector = Icons.Default.AccessTimeFilled,
                    contentDescription = prayerTimesTitle,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = prayerTimesTitle,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (selectedTab1) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppColors.current.tealAccentLight,
                selectedTextColor = AppColors.current.tealAccentLight,
                indicatorColor = AppColors.current.tealGlow20,
                unselectedIconColor = AppColors.current.textSubtle,
                unselectedTextColor = AppColors.current.textSubtle
            ),
            modifier = Modifier.weight(1f).testTag("tab_prayer_times")
        )

        val selectedTab2 = currentTab == AppTab.SETTINGS
        NavigationBarItem(
            selected = selectedTab2,
            onClick = { onTabSelected(AppTab.SETTINGS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = settingsTitle,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = settingsTitle,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (selectedTab2) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppColors.current.tealAccentLight,
                selectedTextColor = AppColors.current.tealAccentLight,
                indicatorColor = AppColors.current.tealGlow20,
                unselectedIconColor = AppColors.current.textSubtle,
                unselectedTextColor = AppColors.current.textSubtle
            ),
            modifier = Modifier.weight(1f).testTag("tab_settings")
        )
    }
}
