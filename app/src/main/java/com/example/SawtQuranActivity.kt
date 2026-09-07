package com.example

import androidx.compose.runtime.collectAsState
import androidx.work.OneTimeWorkRequestBuilder
import com.example.data.local.NotificationSettingsManager

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.example.worker.AyahWorker
import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.audio.AudioPlayerManager
import com.example.audio.PlayerManager
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.screens.reciters.MiniPlayer
import com.example.ui.screens.reciters.ReciterDetailScreen
import com.example.ui.screens.reciters.RecitersScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodels.HomeViewModel
import com.example.ui.viewmodels.ReaderViewModel
import kotlinx.coroutines.launch

import com.example.ui.theme.ThemeManager
import androidx.compose.ui.platform.LocalContext
import com.example.data.remote.AppReleaseInfo
import com.example.data.remote.AppUpdateManager
import com.example.ui.components.AppUpdateDialog
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.ui.SettingsScreen

class SawtQuranActivity : ComponentActivity() {
    private val audioPlayerManager = AudioPlayerManager()

    private val pendingNavigation = MutableStateFlow<String?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Update the activity's intent
        checkIntentForNavigation(intent)
    }

    private fun checkIntentForNavigation(intent: Intent?) {
        val surahId = intent?.getIntExtra("open_surah_id", -1) ?: -1
        val ayahNumber = intent?.getIntExtra("open_ayah_number", -1) ?: -1
        if (surahId != -1) {
            val route = if (ayahNumber != -1) "reader/$surahId?initialAyah=$ayahNumber" else "reader/$surahId"
            pendingNavigation.value = route
            
            // clear extras to avoid handling them again
            intent?.removeExtra("open_surah_id")
            intent?.removeExtra("open_ayah_number")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeManager.init(this)
        audioPlayerManager.initContext(this)
        
        lifecycleScope.launch {
            PlayerManager.initialize(this@SawtQuranActivity)
        }
        
        // Schedule Quran Ayah notifications & background tasks
        if (NotificationSettingsManager.areNotificationsEnabled(this)) {
            NotificationSettingsManager.scheduleAllNotifications(this)
        }


        val app = application as MainApplication
        val repository = app.repository

        val initialSurahId = intent?.getIntExtra("open_surah_id", -1) ?: -1
        val initialAyahNumber = intent?.getIntExtra("open_ayah_number", -1) ?: -1
        if (initialSurahId in 1..114) {
            lifecycleScope.launch(Dispatchers.IO) {
                repository.preloadSurah(initialSurahId)
            }
        }
        val initialStartDest = if (initialSurahId != -1) {
            if (initialAyahNumber != -1) "reader/$initialSurahId?initialAyah=$initialAyahNumber"
            else "reader/$initialSurahId"
        } else {
            "home"
        }

        setContent {
            MyApplicationTheme(themeMode = ThemeManager.currentThemeMode) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val context = LocalContext.current
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val permissionLauncher = rememberLauncherForActivityResult(
                            ActivityResultContracts.RequestPermission()
                        ) { isGranted ->
                            // Optional: Handle permission granted/denied
                        }
                        LaunchedEffect(Unit) {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }
                    
                    val navController = rememberNavController()

                    // Observe pending navigation (e.g. from onNewIntent while activity is running)
                    val pendingRoute by pendingNavigation.collectAsState(initial = null)
                    LaunchedEffect(pendingRoute) {
                        if (pendingRoute != null) {
                            navController.navigate(pendingRoute!!)
                            pendingNavigation.value = null
                        }
                    }
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    var pendingUpdateInfo by remember { mutableStateOf<AppReleaseInfo?>(null) }

                    LaunchedEffect(Unit) {
                        // 1. Check Quran App update for main screen dialog
                        val quranResult = AppUpdateManager.checkQuranUpdate()
                        val quranInfo = quranResult.getOrNull()
                        if (quranInfo != null && quranInfo.isNewerAvailable) {
                            if (!AppUpdateManager.isQuranVersionPostponed(context, quranInfo.tagName)) {
                                pendingUpdateInfo = quranInfo
                            }
                        }

                        // 2. Check Nour Al-Etra update for mobile notification & Settings (NOT main screen dialog)
                        val nourResult = AppUpdateManager.checkNourUpdate()
                        val nourInfo = nourResult.getOrNull()
                        if (nourInfo != null && nourInfo.isNewerAvailable) {
                            if (!AppUpdateManager.isNourVersionPostponed(context, nourInfo.tagName)) {
                                AppUpdateManager.sendNourUpdateNotification(context, nourInfo)
                            }
                        }
                    }

                    pendingUpdateInfo?.let { releaseInfo ->
                        AppUpdateDialog(
                            releaseInfo = releaseInfo,
                            onDismiss = { pendingUpdateInfo = null },
                            onPostpone = {
                                AppUpdateManager.postponeQuranVersion(context, releaseInfo.tagName)
                                pendingUpdateInfo = null
                            }
                        )
                    }

                    Scaffold(
                        bottomBar = {
                            if (currentRoute?.startsWith("reader") != true) {
                                Column {
                                    MiniPlayer()
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                        shadowElevation = 12.dp
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 24.dp, vertical = 10.dp)
                                                .navigationBarsPadding(),
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            NavItem(
                                                icon = Icons.Default.AutoStories,
                                                label = "المصحف",
                                                selected = currentRoute == "home" || (currentRoute == null && initialStartDest == "home")
                                            ) {
                                                navController.navigate("home") {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                            NavItem(
                                                icon = Icons.Default.Headphones,
                                                label = "القرّاء",
                                                selected = currentRoute == "reciters" || currentRoute?.startsWith("reciter_detail") == true
                                            ) {
                                                navController.navigate("reciters") {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                            NavItem(
                                                icon = Icons.Default.MenuBook,
                                                label = "الختمة",
                                                selected = currentRoute == "khatma"
                                            ) {
                                                navController.navigate("khatma") {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        var totalDragAmount by remember { mutableFloatStateOf(0f) }

                        NavHost(
                            navController = navController, 
                            startDestination = initialStartDest,
                            modifier = Modifier
                                .padding(innerPadding)
                                .pointerInput(currentRoute) {
                                    detectHorizontalDragGestures(
                                        onDragStart = { totalDragAmount = 0f },
                                        onHorizontalDrag = { change, dragAmount ->
                                            change.consume()
                                            totalDragAmount += dragAmount
                                        },
                                        onDragEnd = {
                                            if (kotlin.math.abs(totalDragAmount) > 80f) {
                                                if (currentRoute != null && (currentRoute.startsWith("reader") || currentRoute.startsWith("reciter_detail"))) {
                                                    navController.popBackStack()
                                                } else if (currentRoute == "home" && totalDragAmount < -80f) {
                                                    navController.navigate("reciters") {
                                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                } else if (currentRoute == "reciters" && totalDragAmount > 80f) {
                                                    navController.navigate("home") {
                                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                        ) {
                            composable("home") {
                                val homeViewModel: HomeViewModel = viewModel(
                                    factory = HomeViewModel.Factory(repository)
                                )
                                HomeScreen(
                                    viewModel = homeViewModel,
                                    onSurahSelected = { surahId ->
                                        navController.navigate("reader/$surahId")
                                    },
                                    onAyahSelected = { surahId, ayahNumber ->
                                        navController.navigate("reader/$surahId?initialAyah=$ayahNumber")
                                    }
                                )
                            }
                            
                            composable(
                                "reader/{surahId}?initialAyah={initialAyah}&initialPage={initialPage}&isKhatmah={isKhatmah}",
                                arguments = listOf(
                                    navArgument("surahId") { type = NavType.IntType },
                                    navArgument("initialAyah") { type = NavType.IntType; defaultValue = -1 },
                                    navArgument("initialPage") { type = NavType.IntType; defaultValue = -1 },
                                    navArgument("isKhatmah") { type = NavType.BoolType; defaultValue = false }
                                )
                            ) { backStackEntry ->
                                val surahId = backStackEntry.arguments?.getInt("surahId") ?: 1
                                val initialAyah = backStackEntry.arguments?.getInt("initialAyah") ?: -1
                                val initialPage = backStackEntry.arguments?.getInt("initialPage") ?: -1
                                val isKhatmah = backStackEntry.arguments?.getBoolean("isKhatmah") ?: false
                                val readerViewModel: ReaderViewModel = viewModel(
                                    factory = ReaderViewModel.Factory(repository, audioPlayerManager)
                                )
                                ReaderScreen(
                                    viewModel = readerViewModel,
                                    surahId = surahId,
                                    initialAyah = initialAyah,
                                    initialPage = initialPage,
                                    isKhatmah = isKhatmah,
                                    onBack = { 
                                        if (!navController.popBackStack()) {
                                            (context as? ComponentActivity)?.finish()
                                        }
                                    },
                                    onNavigateToSurah = { nextSurahId, nextInitialAyah, nextInitialPage ->
                                        var route = "reader/$nextSurahId?"
                                        if (nextInitialAyah != -1) route += "initialAyah=$nextInitialAyah&"
                                        if (nextInitialPage != -1) route += "initialPage=$nextInitialPage&"
                                        if (isKhatmah) route += "isKhatmah=true&"
                                        navController.navigate(route.removeSuffix("&").removeSuffix("?")) {
                                            popUpTo("reader/$surahId") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable("reciters") {
                                RecitersScreen(
                                    onReciterSelected = { reciterId ->
                                        navController.navigate("reciter_detail/$reciterId")
                                    }
                                )
                            }
                            composable("khatma") {
                                val coroutineScope = rememberCoroutineScope()
                                val khatmaViewModel: com.example.khatma.KhatmaViewModel = viewModel(
                                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                            return com.example.khatma.KhatmaViewModel((application as com.example.MainApplication).khatmaRepository) as T
                                        }
                                    }
                                )
                                com.example.ui.screens.KhatmaScreen(
                                    viewModel = khatmaViewModel,
                                    onJuzSelected = { juzNumber ->
                                        coroutineScope.launch {
                                            val targetPage = khatmaViewModel.openJuz(juzNumber)
                                            val surahId = com.example.ui.screens.SURAH_START_PAGES.indexOfLast { it <= targetPage } + 1
                                            navController.navigate("reader/$surahId?initialPage=$targetPage&isKhatmah=true")
                                        }
                                    }
                                )
                            }

                            composable(
                                "reciter_detail/{reciterId}",
                                arguments = listOf(navArgument("reciterId") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val reciterId = backStackEntry.arguments?.getInt("reciterId") ?: 51
                                ReciterDetailScreen(
                                    reciterId = reciterId,
                                    onBack = { navController.popBackStack() },
                                    onPlaySurah = { url, title, surahId ->
                                        PlayerManager.play(url, title)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        audioPlayerManager.stop()
    }

    override fun onStop() {
        super.onStop()
        audioPlayerManager.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayerManager.stop()
        if (!PlayerManager.isPlaying.value) {
            PlayerManager.stopAndDismiss()
        }
        PlayerManager.release()
    }
}

@Composable
fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .width(68.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (selected) {
            Spacer(modifier = Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(2.5.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        } else {
            Spacer(modifier = Modifier.height(5.5.dp))
        }
    }
}

