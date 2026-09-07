package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AdhanSoundMode
import com.example.data.model.AlarmRepeatMode
import com.example.data.model.AppLanguage
import com.example.data.model.CalculationMethod
import com.example.data.model.CityLocation
import com.example.data.model.MidnightMethod
import com.example.data.model.Muezzin
import com.example.data.model.NextPrayerInfo
import com.example.data.model.PrayerCustomAlarmConfig
import com.example.data.model.PrayerTimesData
import com.example.data.model.PrayerType
import com.example.data.model.PredefinedCities
import com.example.data.model.ThemeMode
import com.example.data.repository.PrayerTimesRepository
import com.example.utils.AdhanAudioService
import com.example.utils.AdhanPlaybackState
import com.example.utils.AdhanPlaybackStatus
import com.example.utils.MuezzinDownloadManager
import com.example.utils.MuezzinDownloadStatus
import com.example.utils.PrayerCalculator
import com.example.utils.PrayerNotificationHelper
import com.example.utils.PrayerNotificationScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppTab(val titleAr: String, val titleEn: String) {
    PRAYER_TIMES("المواقيت", "Times"),
    SETTINGS("الإعدادات", "Settings")
}

class PrayerTimesViewModel(application: Application) : AndroidViewModel(application) {

    val repository = PrayerTimesRepository(application)

    val themeMode: StateFlow<ThemeMode> = repository.themeMode

    fun setThemeMode(mode: ThemeMode) {
        repository.setThemeMode(mode)
    }

    val appLanguage: StateFlow<AppLanguage> = repository.appLanguage

    fun setAppLanguage(language: AppLanguage) {
        repository.setAppLanguage(language)
    }

    val calculationMethod: StateFlow<CalculationMethod> = repository.calculationMethod

    fun setCalculationMethod(method: CalculationMethod) {
        repository.setCalculationMethod(method)
        updatePrayerCountdown()
    }

    val midnightMethod: StateFlow<MidnightMethod> = repository.midnightMethod

    fun setMidnightMethod(method: MidnightMethod) {
        repository.setMidnightMethod(method)
        updatePrayerCountdown()
    }

    val showAsrSeparate: StateFlow<Boolean> = repository.showAsrSeparate

    fun setShowAsrSeparate(show: Boolean) {
        repository.setShowAsrSeparate(show)
    }

    val showIshaSeparate: StateFlow<Boolean> = repository.showIshaSeparate

    fun setShowIshaSeparate(show: Boolean) {
        repository.setShowIshaSeparate(show)
    }

    private val _currentTab = MutableStateFlow(AppTab.PRAYER_TIMES)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    val selectedCity = repository.selectedCity
    val prayerTimesData = repository.prayerTimesData
    val isLoading = repository.isLoading
    val isBackgroundSyncing = repository.isBackgroundSyncing
    val isOfflineMode = repository.isOfflineMode
    val errorMessage = repository.errorMessage

    private val _nextPrayer = MutableStateFlow<NextPrayerInfo?>(null)
    val nextPrayer: StateFlow<NextPrayerInfo?> = _nextPrayer.asStateFlow()

    private val _currentLiveTime = MutableStateFlow("")
    val currentLiveTime: StateFlow<String> = _currentLiveTime.asStateFlow()

    private val _notificationsMap = MutableStateFlow<Map<PrayerType, Boolean>>(emptyMap())
    val notificationsMap: StateFlow<Map<PrayerType, Boolean>> = _notificationsMap.asStateFlow()

    private val _prayerAlarmConfigs = MutableStateFlow<Map<PrayerType, PrayerCustomAlarmConfig>>(emptyMap())
    val prayerAlarmConfigs: StateFlow<Map<PrayerType, PrayerCustomAlarmConfig>> = _prayerAlarmConfigs.asStateFlow()

    private val _editingAlarmPrayerType = MutableStateFlow<PrayerType?>(null)
    val editingAlarmPrayerType: StateFlow<PrayerType?> = _editingAlarmPrayerType.asStateFlow()

    private val _prayerVisibilityMap = MutableStateFlow<Map<PrayerType, Boolean>>(emptyMap())
    val prayerVisibilityMap: StateFlow<Map<PrayerType, Boolean>> = _prayerVisibilityMap.asStateFlow()

    private val _isGpsLocating = MutableStateFlow(false)
    val isGpsLocating: StateFlow<Boolean> = _isGpsLocating.asStateFlow()

    private val _gpsStatusMessage = MutableStateFlow<String?>(null)
    val gpsStatusMessage: StateFlow<String?> = _gpsStatusMessage.asStateFlow()

    init {
        loadNotifications()
        loadPrayerVisibility()
        loadInitialData()
        startLiveTicker()
        MuezzinDownloadManager.initAndAutoDownloadAll(getApplication())
        viewModelScope.launch {
            prayerTimesData.collect {
                updatePrayerCountdown()
            }
        }
    }

    fun locateViaGps(context: android.content.Context, forceRefresh: Boolean = false, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val lang = appLanguage.value
            _isGpsLocating.value = true
            _gpsStatusMessage.value = com.example.utils.AppStrings.gpsStatusLocating(lang)
            try {
                val location = com.example.utils.LocationHelper.getDeviceLocation(context, forceRefresh)
                    ?: com.example.data.model.PredefinedCities.defaultCity
                val cityName = if (lang == AppLanguage.ARABIC) location.nameAr else location.nameEn
                _gpsStatusMessage.value = com.example.utils.AppStrings.gpsStatusDetermined(cityName, lang)
                changeCity(location)
                onComplete?.invoke(true)
            } catch (e: Exception) {
                val defaultCity = com.example.data.model.PredefinedCities.defaultCity
                val cityName = if (lang == AppLanguage.ARABIC) defaultCity.nameAr else defaultCity.nameEn
                _gpsStatusMessage.value = com.example.utils.AppStrings.gpsStatusSetTo(cityName, lang)
                changeCity(defaultCity)
                onComplete?.invoke(true)
            } finally {
                _isGpsLocating.value = false
            }
        }
    }

    fun setGpsStatusMessage(msg: String?) {
        _gpsStatusMessage.value = msg
    }

    fun clearGpsStatusMessage() {
        _gpsStatusMessage.value = null
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Immediate countdown display from cache
            updatePrayerCountdown()
            // Background sync with trusted Haqibat Al-Momen API
            repository.fetchPrayerTimes(selectedCity.value, forceRefresh = false)
            updatePrayerCountdown()
        }
    }

    private fun startLiveTicker() {
        viewModelScope.launch {
            val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale("ar"))
            while (isActive) {
                _currentLiveTime.value = timeFormat.format(Date())
                updatePrayerCountdown()
                delay(1000)
            }
        }
    }

    private fun updatePrayerCountdown() {
        val data = prayerTimesData.value ?: return
        val next = PrayerCalculator.getNextPrayerInfo(data)
        _nextPrayer.value = next
        rescheduleAlarms(data)
    }

    private fun rescheduleAlarms(data: PrayerTimesData) {
        val context = getApplication<Application>()
        val city = selectedCity.value
        PrayerNotificationScheduler.scheduleAllPrayerNotifications(
            context = context,
            prayerData = data,
            cityName = city.nameAr
        )
    }

    fun sendTestNotification() {
        val context = getApplication<Application>()
        PrayerNotificationHelper.showTestNotification(context)
    }

    fun refreshPrayerTimes() {
        viewModelScope.launch {
            repository.fetchPrayerTimes(selectedCity.value, forceRefresh = true)
            updatePrayerCountdown()
        }
    }

    fun changeCity(city: CityLocation) {
        viewModelScope.launch {
            repository.selectCity(city)
            repository.fetchPrayerTimes(city, forceRefresh = true)
            updatePrayerCountdown()
        }
    }

    private fun loadNotifications() {
        val notifMap = mutableMapOf<PrayerType, Boolean>()
        val configMap = mutableMapOf<PrayerType, PrayerCustomAlarmConfig>()
        PrayerType.values().forEach { type ->
            val config = repository.getPrayerAlarmConfig(type)
            notifMap[type] = config.isEnabled
            configMap[type] = config
        }
        _notificationsMap.value = notifMap
        _prayerAlarmConfigs.value = configMap
    }

    fun openAlarmCustomization(prayerType: PrayerType) {
        _editingAlarmPrayerType.value = prayerType
    }

    fun closeAlarmCustomization() {
        _editingAlarmPrayerType.value = null
    }

    fun updatePrayerAlarmConfig(config: PrayerCustomAlarmConfig) {
        repository.savePrayerAlarmConfig(config)
        _prayerAlarmConfigs.value = _prayerAlarmConfigs.value.toMutableMap().apply {
            put(config.prayerType, config)
        }
        _notificationsMap.value = _notificationsMap.value.toMutableMap().apply {
            put(config.prayerType, config.isEnabled)
        }
        prayerTimesData.value?.let { rescheduleAlarms(it) }
    }

    fun toggleNotification(prayerType: PrayerType) {
        val currentConfig = _prayerAlarmConfigs.value[prayerType] ?: repository.getPrayerAlarmConfig(prayerType)
        val updatedConfig = currentConfig.copy(isEnabled = !currentConfig.isEnabled)
        updatePrayerAlarmConfig(updatedConfig)
    }

    private fun loadPrayerVisibility() {
        val map = mutableMapOf<PrayerType, Boolean>()
        PrayerType.values().forEach { type ->
            map[type] = repository.isPrayerVisible(type.name)
        }
        _prayerVisibilityMap.value = map
    }

    fun togglePrayerVisibility(prayerType: PrayerType) {
        val current = _prayerVisibilityMap.value[prayerType] ?: true
        val updated = !current
        repository.setPrayerVisible(prayerType.name, updated)
        _prayerVisibilityMap.value = _prayerVisibilityMap.value.toMutableMap().apply {
            put(prayerType, updated)
        }
    }

    fun setPrayerVisibility(prayerType: PrayerType, visible: Boolean) {
        repository.setPrayerVisible(prayerType.name, visible)
        _prayerVisibilityMap.value = _prayerVisibilityMap.value.toMutableMap().apply {
            put(prayerType, visible)
        }
    }

    val prayerOffsets: StateFlow<Map<PrayerType, Int>> = repository.prayerOffsets
    val manualHijriOffset: StateFlow<Int> = repository.manualHijriOffset
    val manualHijriDateOverride: StateFlow<String?> = repository.manualHijriDateOverride

    fun setPrayerOffset(prayerType: PrayerType, offsetMinutes: Int) {
        repository.setPrayerOffset(prayerType, offsetMinutes)
        updatePrayerCountdown()
    }

    fun adjustPrayerOffset(prayerType: PrayerType, deltaMinutes: Int) {
        val current = repository.prayerOffsets.value[prayerType] ?: 0
        setPrayerOffset(prayerType, current + deltaMinutes)
    }

    fun resetAllPrayerOffsets() {
        repository.resetAllPrayerOffsets()
        updatePrayerCountdown()
    }

    fun setManualHijriOffset(offsetDays: Int) {
        repository.setManualHijriOffset(offsetDays)
        updatePrayerCountdown()
    }

    fun adjustManualHijriOffset(deltaDays: Int) {
        val current = repository.manualHijriOffset.value
        setManualHijriOffset(current + deltaDays)
    }

    fun setManualHijriCustomDate(customDate: String?) {
        repository.setManualHijriCustomDate(customDate)
        updatePrayerCountdown()
    }

    fun resetManualHijri() {
        repository.resetManualHijri()
        updatePrayerCountdown()
    }

    private val _hijriSyncStatus = MutableStateFlow<String?>(null)
    val hijriSyncStatus: StateFlow<String?> = _hijriSyncStatus.asStateFlow()

    val githubAutoSyncEnabled: StateFlow<Boolean> = repository.githubAutoSyncEnabled
    val githubRepoOwner: StateFlow<String> = repository.githubRepoOwner
    val githubRepoName: StateFlow<String> = repository.githubRepoName
    val githubToken: StateFlow<String> = repository.githubToken
    val githubWebhookUrl: StateFlow<String> = repository.githubWebhookUrl
    val lastHijriSyncTime: StateFlow<Long> = repository.lastHijriSyncTime

    private val _isSendingDispatch = MutableStateFlow(false)
    val isSendingDispatch: StateFlow<Boolean> = _isSendingDispatch.asStateFlow()

    private val _dispatchStatusMessage = MutableStateFlow<String?>(null)
    val dispatchStatusMessage: StateFlow<String?> = _dispatchStatusMessage.asStateFlow()

    fun setGithubAutoSyncEnabled(enabled: Boolean) {
        repository.setGithubAutoSyncEnabled(enabled)
    }

    fun saveGithubSettings(owner: String, name: String, token: String, webhookUrl: String) {
        repository.saveGithubSettings(owner, name, token, webhookUrl)
    }

    fun syncHijriDate() {
        viewModelScope.launch {
            _hijriSyncStatus.value = "جارٍ مزامنة التاريخ الهجري من GitHub..."
            val result = repository.syncHijriDateWithGithub(isAuto = false, notifyUser = true)
            if (result.isSuccess) {
                _hijriSyncStatus.value = "تم تحديث التاريخ الهجري بنجاح: ${result.getOrNull()}"
            } else {
                _hijriSyncStatus.value = "فشل تحديث التاريخ الهجري (${result.exceptionOrNull()?.message ?: "خطأ في الشبكة"})"
            }
            delay(3500)
            _hijriSyncStatus.value = null
        }
    }

    fun sendGithubDispatch() {
        viewModelScope.launch {
            _isSendingDispatch.value = true
            _dispatchStatusMessage.value = "جارٍ إرسال إشعار التحديث إلى GitHub..."
            val result = repository.sendGithubNotificationDispatch()
            if (result.isSuccess) {
                _dispatchStatusMessage.value = result.getOrNull() ?: "تم إرسال إشعار التحديث بنجاح"
            } else {
                _dispatchStatusMessage.value = "فشل الإرسال: ${result.exceptionOrNull()?.message}"
            }
            _isSendingDispatch.value = false
            delay(4000)
            _dispatchStatusMessage.value = null
        }
    }

    // Muezzins & Official Adhan Settings
    val selectedMuezzin: StateFlow<Muezzin> = repository.selectedMuezzin
    val isAdhanAudioEnabled: StateFlow<Boolean> = repository.isAdhanAudioEnabled
    val adhanPlaybackState: StateFlow<AdhanPlaybackState> = AdhanAudioService.playbackState
    val muezzinDownloadStatuses: StateFlow<Map<String, MuezzinDownloadStatus>> = MuezzinDownloadManager.downloadStatuses

    fun setSelectedMuezzin(muezzin: Muezzin) {
        repository.setSelectedMuezzin(muezzin)
        val context = getApplication<Application>()
        MuezzinDownloadManager.downloadMuezzin(context, muezzin)
    }

    fun setAdhanAudioEnabled(enabled: Boolean) {
        repository.setAdhanAudioEnabled(enabled)
    }

    fun previewMuezzin(muezzin: Muezzin) {
        val context = getApplication<Application>()
        if (adhanPlaybackState.value.status == AdhanPlaybackStatus.PLAYING &&
            adhanPlaybackState.value.currentMuezzinId == muezzin.id
        ) {
            AdhanAudioService.stopAdhan(context)
        } else {
            AdhanAudioService.previewMuezzin(context, muezzin)
        }
    }

    fun previewCustomPrayerAlarm(config: PrayerCustomAlarmConfig) {
        val context = getApplication<Application>()
        val muezzin = if (!config.specificMuezzinId.isNullOrBlank()) {
            Muezzin.fromId(config.specificMuezzinId)
        } else {
            selectedMuezzin.value
        }
        if (adhanPlaybackState.value.status == AdhanPlaybackStatus.PLAYING) {
            AdhanAudioService.stopAdhan(context)
        } else {
            AdhanAudioService.previewMuezzin(
                context = context,
                muezzin = muezzin,
                soundMode = config.soundMode,
                volumePercent = config.customVolumePercent
            )
        }
    }

    fun stopAdhanPlayback() {
        val context = getApplication<Application>()
        AdhanAudioService.stopAdhan(context)
    }

    fun downloadAllMuezzins() {
        val context = getApplication<Application>()
        MuezzinDownloadManager.downloadAllInBackground(context)
    }

    fun downloadMuezzin(muezzin: Muezzin) {
        val context = getApplication<Application>()
        MuezzinDownloadManager.downloadMuezzin(context, muezzin)
    }

    val updateStatus: StateFlow<com.example.utils.UpdateCheckStatus> = com.example.utils.AppUpdateManager.updateStatus

    fun checkForAppUpdates() {
        val context = getApplication<Application>()
        viewModelScope.launch {
            com.example.utils.AppUpdateManager.checkForUpdates(context)
        }
    }

    fun downloadAndInstallAppUpdate(downloadUrl: String, apkFileName: String) {
        val context = getApplication<Application>()
        viewModelScope.launch {
            com.example.utils.AppUpdateManager.downloadAndInstallUpdate(context, downloadUrl, apkFileName)
        }
    }

    fun cancelAppUpdateDownload() {
        com.example.utils.AppUpdateManager.cancelDownload()
    }

    fun resetAppUpdateStatus() {
        com.example.utils.AppUpdateManager.resetStatus()
    }
}
