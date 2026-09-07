package com.example.ui

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AppLanguage
import com.example.data.model.ThemeMode
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.TopNavBar
import com.example.ui.prayer.screens.HomeScreen
import com.example.ui.prayer.screens.SettingsScreen
import com.example.ui.theme.PrayerAppTheme
import com.example.ui.theme.ThemeManager
import com.example.utils.*

@Composable
fun PrayerTimesScreen(
    onBack: () -> Unit,
    viewModel: PrayerTimesViewModel = viewModel()
) {
    val currentAppTheme = ThemeManager.currentThemeMode
    val appLanguage by viewModel.appLanguage.collectAsState()
    val layoutDirection = if (appLanguage == AppLanguage.ARABIC) LayoutDirection.Rtl else LayoutDirection.Ltr

    PrayerAppTheme(appThemeMode = currentAppTheme) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            PrayerApp(
                viewModel = viewModel,
                onBack = onBack
            )
        }
    }
}

@Composable
fun PrayerApp(
    viewModel: PrayerTimesViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val prayerData by viewModel.prayerTimesData.collectAsState()
    val nextPrayer by viewModel.nextPrayer.collectAsState()
    val liveTime by viewModel.currentLiveTime.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isBackgroundSyncing by viewModel.isBackgroundSyncing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()
    val notificationsMap by viewModel.notificationsMap.collectAsState()
    val prayerVisibilityMap by viewModel.prayerVisibilityMap.collectAsState()
    val calculationMethod by viewModel.calculationMethod.collectAsState()
    val midnightMethod by viewModel.midnightMethod.collectAsState()
    val showAsrSeparate by viewModel.showAsrSeparate.collectAsState()
    val showIshaSeparate by viewModel.showIshaSeparate.collectAsState()
    val isGpsLocating by viewModel.isGpsLocating.collectAsState()
    val gpsStatusMessage by viewModel.gpsStatusMessage.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            if (!LocationHelper.isLocationServiceEnabled(context)) {
                android.widget.Toast.makeText(context, AppStrings.gpsToastEnable(appLanguage), android.widget.Toast.LENGTH_LONG).show()
                LocationHelper.openLocationSettings(context)
                viewModel.setGpsStatusMessage(AppStrings.gpsStatusOpeningSettings(appLanguage))
            } else {
                viewModel.locateViaGps(context, forceRefresh = true)
            }
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    val onTriggerGps = {
        if (!LocationHelper.hasLocationPermission(context)) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else if (!LocationHelper.isLocationServiceEnabled(context)) {
            android.widget.Toast.makeText(context, AppStrings.gpsToastEnable(appLanguage), android.widget.Toast.LENGTH_LONG).show()
            LocationHelper.openLocationSettings(context)
            viewModel.setGpsStatusMessage(AppStrings.gpsStatusDisabled(appLanguage))
        } else {
            viewModel.locateViaGps(context, forceRefresh = true)
        }
    }

    LaunchedEffect(Unit) {
        PrayerNotificationHelper.createNotificationChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (!LocationHelper.hasLocationPermission(context)) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            viewModel.locateViaGps(context, forceRefresh = false)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            TopNavBar(
                selectedCity = selectedCity,
                isLoading = isLoading,
                isGpsLocating = isGpsLocating,
                onGpsClick = onTriggerGps,
                onRefreshClick = { viewModel.refreshPrayerTimes() },
                currentLanguage = appLanguage,
                onBackClick = {
                    if (currentTab == AppTab.SETTINGS) {
                        viewModel.selectTab(AppTab.PRAYER_TIMES)
                    } else {
                        onBack()
                    }
                },
                isSettingsOpen = currentTab == AppTab.SETTINGS,
                onSettingsClick = {
                    if (currentTab == AppTab.SETTINGS) {
                        viewModel.selectTab(AppTab.PRAYER_TIMES)
                    } else {
                        viewModel.selectTab(AppTab.SETTINGS)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.PRAYER_TIMES -> {
                    HomeScreen(
                        currentLanguage = appLanguage,
                        prayerData = prayerData,
                        nextPrayer = nextPrayer,
                        liveTime = liveTime,
                        selectedCity = selectedCity,
                        notificationsMap = notificationsMap,
                        prayerVisibilityMap = prayerVisibilityMap,
                        errorMessage = errorMessage,
                        isOfflineMode = isOfflineMode,
                        isBackgroundSyncing = isBackgroundSyncing,
                        isLoading = isLoading,
                        isGpsLocating = isGpsLocating,
                        gpsStatusMessage = gpsStatusMessage,
                        onGpsClick = onTriggerGps,
                        onRefreshClick = { viewModel.refreshPrayerTimes() },
                        onToggleNotification = { viewModel.toggleNotification(it) }
                    )
                }
                AppTab.SETTINGS -> {
                    val hijriSyncStatus by viewModel.hijriSyncStatus.collectAsStateWithLifecycle()
                    val githubAutoSyncEnabled by viewModel.githubAutoSyncEnabled.collectAsStateWithLifecycle()
                    val githubRepoOwner by viewModel.githubRepoOwner.collectAsStateWithLifecycle()
                    val githubRepoName by viewModel.githubRepoName.collectAsStateWithLifecycle()
                    val githubToken by viewModel.githubToken.collectAsStateWithLifecycle()
                    val githubWebhookUrl by viewModel.githubWebhookUrl.collectAsStateWithLifecycle()
                    val isSendingDispatch by viewModel.isSendingDispatch.collectAsStateWithLifecycle()
                    val dispatchStatusMessage by viewModel.dispatchStatusMessage.collectAsStateWithLifecycle()
                    val lastHijriSyncTime by viewModel.lastHijriSyncTime.collectAsStateWithLifecycle()
                    val prayerOffsets by viewModel.prayerOffsets.collectAsStateWithLifecycle()
                    val manualHijriOffset by viewModel.manualHijriOffset.collectAsStateWithLifecycle()
                    val manualHijriDateOverride by viewModel.manualHijriDateOverride.collectAsStateWithLifecycle()
                    val selectedMuezzin by viewModel.selectedMuezzin.collectAsStateWithLifecycle()
                    val isAdhanAudioEnabled by viewModel.isAdhanAudioEnabled.collectAsStateWithLifecycle()
                    val adhanPlaybackState by viewModel.adhanPlaybackState.collectAsStateWithLifecycle()
                    val muezzinDownloadStatuses by viewModel.muezzinDownloadStatuses.collectAsStateWithLifecycle()
                    val prayerAlarmConfigs by viewModel.prayerAlarmConfigs.collectAsStateWithLifecycle()

                    SettingsScreen(
                        currentLanguage = appLanguage,
                        selectedCity = selectedCity,
                        prayerData = prayerData,
                        isGpsLocating = isGpsLocating,
                        gpsStatusMessage = gpsStatusMessage,
                        onGpsLocate = onTriggerGps,
                        calculationMethod = calculationMethod,
                        onCalculationMethodChange = { viewModel.setCalculationMethod(it) },
                        midnightMethod = midnightMethod,
                        onMidnightMethodChange = { viewModel.setMidnightMethod(it) },
                        showAsrSeparate = showAsrSeparate,
                        onShowAsrSeparateChange = { viewModel.setShowAsrSeparate(it) },
                        showIshaSeparate = showIshaSeparate,
                        onShowIshaSeparateChange = { viewModel.setShowIshaSeparate(it) },
                        notificationsMap = notificationsMap,
                        onToggleNotification = { viewModel.toggleNotification(it) },
                        prayerAlarmConfigs = prayerAlarmConfigs,
                        onUpdatePrayerAlarmConfig = { viewModel.updatePrayerAlarmConfig(it) },
                        onPreviewCustomPrayerAlarm = { viewModel.previewCustomPrayerAlarm(it) },
                        prayerVisibilityMap = prayerVisibilityMap,
                        onTogglePrayerVisibility = { viewModel.togglePrayerVisibility(it) },
                        hijriSyncStatus = hijriSyncStatus,
                        onSyncHijriDate = { viewModel.syncHijriDate() },
                        githubAutoSyncEnabled = githubAutoSyncEnabled,
                        onGithubAutoSyncChange = { viewModel.setGithubAutoSyncEnabled(it) },
                        githubRepoOwner = githubRepoOwner,
                        githubRepoName = githubRepoName,
                        githubToken = githubToken,
                        githubWebhookUrl = githubWebhookUrl,
                        onSaveGithubSettings = { owner, name, token, webhook ->
                            viewModel.saveGithubSettings(owner, name, token, webhook)
                        },
                        isSendingDispatch = isSendingDispatch,
                        dispatchStatusMessage = dispatchStatusMessage,
                        onSendGithubDispatch = { viewModel.sendGithubDispatch() },
                        lastHijriSyncTime = lastHijriSyncTime,
                        onTestNotification = { viewModel.sendTestNotification() },
                        prayerOffsets = prayerOffsets,
                        onAdjustPrayerOffset = { prayerType, delta -> viewModel.adjustPrayerOffset(prayerType, delta) },
                        onResetAllPrayerOffsets = { viewModel.resetAllPrayerOffsets() },
                        manualHijriOffset = manualHijriOffset,
                        onAdjustManualHijriOffset = { delta -> viewModel.adjustManualHijriOffset(delta) },
                        manualHijriCustomDate = manualHijriDateOverride,
                        onSetManualHijriCustomDate = { customDate -> viewModel.setManualHijriCustomDate(customDate) },
                        onResetManualHijri = { viewModel.resetManualHijri() },
                        selectedMuezzin = selectedMuezzin,
                        onSelectMuezzin = { viewModel.setSelectedMuezzin(it) },
                        isAdhanAudioEnabled = isAdhanAudioEnabled,
                        onToggleAdhanAudio = { viewModel.setAdhanAudioEnabled(it) },
                        adhanPlaybackState = adhanPlaybackState,
                        onPreviewMuezzin = { viewModel.previewMuezzin(it) },
                        onStopAdhanPlayback = { viewModel.stopAdhanPlayback() },
                        muezzinDownloadStatuses = muezzinDownloadStatuses,
                        onDownloadAllMuezzins = { viewModel.downloadAllMuezzins() },
                        onDownloadMuezzin = { viewModel.downloadMuezzin(it) }
                    )
                }
            }
        }
    }
}
