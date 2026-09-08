@file:OptIn(ExperimentalPermissionsApi::class)
package com.example.ui
import androidx.compose.ui.draw.rotate
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import android.Manifest
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.AhlAlBaytRepository
import com.example.data.Person
import com.example.data.AladhanResponse
import com.example.data.getHijriMonthName
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices

data class UpcomingEvent(
    val event: com.example.data.HijriEvent,
    val daysUntil: Int,
    val gregorianDateStr: String,
    val eventDate: java.time.LocalDate,
    val upcomingHijriYear: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val context = LocalContext.current
    var showNotificationDialog by remember { mutableStateOf(false) }
    val db = remember { com.example.data.AppDatabase.getInstance(context) }
    val notifications by db.notificationDao().getAllNotifications().collectAsState(initial = emptyList())
    val sharedPref = remember { context.getSharedPreferences("AhlAlBaytPrefs", android.content.Context.MODE_PRIVATE) }
    val hijriRecord by db.hijriDateRecordDao().getRecordFlow().collectAsState(initial = null)

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = { Text("الإشعارات") },
            text = {
                if (notifications.isEmpty()) {
                    Text("لا توجد إشعارات جديدة حالياً.")
                } else {
                    androidx.compose.foundation.lazy.LazyColumn {
                        items(notifications) { notification ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(notification.title, fontWeight = FontWeight.Bold, color = if (MaterialTheme.colorScheme.onBackground == androidx.compose.ui.graphics.Color.White) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(notification.message, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showNotificationDialog = false 
                    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        db.notificationDao().markAllAsRead()
                    }
                }) { Text("حسناً") }
            }
        )
    }

    // In-App Update Notification Popup & Official Permissions Consent Dialogs
    var showBackgroundOptimizationDialog by remember {
        mutableStateOf(com.example.util.BackgroundOptimizationManager.shouldShowInitialPrompt(context))
    }

    if (showBackgroundOptimizationDialog) {
        com.example.ui.components.BackgroundOptimizationDialog(
            onDismiss = {
                com.example.util.BackgroundOptimizationManager.setPromptDismissed(context, true)
                showBackgroundOptimizationDialog = false
            },
            onConfirm = {
                com.example.util.BackgroundOptimizationManager.setPromptDismissed(context, true)
                showBackgroundOptimizationDialog = false
                com.example.util.BackgroundOptimizationManager.requestIgnoreBatteryOptimization(context)
                com.example.data.local.NotificationSettingsManager.scheduleAllNotifications(context)
            }
        )
    }

    if (com.example.UpdateChecker.showInAppPopup) {
        InAppUpdateNotificationDialog(
            onDismiss = { com.example.UpdateChecker.showInAppPopup = false }
        )
    }

    if (com.example.UpdateChecker.showOfficialPermissionsDialog) {
        OfficialPermissionsConsentDialog(
            onDismiss = { com.example.UpdateChecker.showOfficialPermissionsDialog = false },
            onConfirmInstall = {
                com.example.UpdateChecker.showOfficialPermissionsDialog = false
                com.example.UpdateChecker.showInAppPopup = false
                com.example.UpdateChecker.installApk(context)
            }
        )
    }

    IslamicBackgroundBox(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            if (currentRoute == "people" || currentRoute == "worship" || currentRoute == "figures_events") {
                if (isSearchActive) {
                    TopAppBar(
                        title = {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("بحث في التطبيق...", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                    disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                    focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    cursorColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { 
                                isSearchActive = false
                                searchQuery = ""
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        },
                        actions = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "مسح", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    )
                } else {
                    TopAppBar(
                        title = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.foundation.Image(
                                    painter = painterResource(id = com.example.R.drawable.noor),
                                    contentDescription = "شعار نور العترة",
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, androidx.compose.ui.graphics.Color(0xFFD4AF37).copy(alpha = 0.6f), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "نور العترة", 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        actions = {
                            // زر اتجاه القبلة الأنيق والواضح بجانب البحث
                            Surface(
                                onClick = {
                                    context.startActivity(android.content.Intent(context, com.example.qibla.QiblaCompassActivity::class.java))
                                },
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .height(36.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = com.example.R.drawable.ic_worship_qibla),
                                        contentDescription = "اتجاه القبلة",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "القبلة",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "بحث",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { navController.navigate("favorites") }) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = "المفضلة",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        modifier = Modifier.clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    )
                }
            }
        },
        bottomBar = {
            if (currentRoute?.startsWith("worship_quran") != true) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "الرئيسية") },
                    label = { Text("الرئيسية", fontWeight = FontWeight.Bold, maxLines = 1, fontSize = 10.sp, overflow = TextOverflow.Ellipsis) },
                    selected = currentRoute == "people" || currentRoute?.startsWith("detail") == true,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSurface,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ),
                    onClick = {
                        navController.navigate("people") {
                            popUpTo(navController.graph.startDestinationId) { 
                                inclusive = false 
                            }
                            launchSingleTop = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = "الشخصيات والمناسبات") },
                    label = { Text("المناسبات", fontWeight = FontWeight.Bold, maxLines = 1, fontSize = 10.sp, overflow = TextOverflow.Ellipsis) },
                    selected = currentRoute == "figures_events",
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSurface,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ),
                    onClick = {
                        navController.navigate("figures_events") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "العبادات") },
                    label = { Text("العبادات", fontWeight = FontWeight.Bold, maxLines = 1, fontSize = 10.sp, overflow = TextOverflow.Ellipsis) },
                    selected = currentRoute == "worship",
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSurface,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ),
                    onClick = {
                        navController.navigate("worship") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { 
                        Icon(
                            imageVector = Icons.Filled.AutoStories, 
                            contentDescription = "القرآن الكريم", 
                            modifier = Modifier.size(24.dp)
                        ) 
                    },
                    label = { 
                        Text(
                            "القرآن", 
                            fontWeight = FontWeight.Bold, 
                            maxLines = 1, 
                            fontSize = 10.sp, 
                            overflow = TextOverflow.Ellipsis
                        ) 
                    },
                    selected = false,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSurface,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ),
                    onClick = {
                        context.startActivity(android.content.Intent(context, com.example.SawtQuranActivity::class.java))
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "الإعدادات") },
                    label = { Text("الإعدادات", fontWeight = FontWeight.Bold, maxLines = 1, fontSize = 10.sp, overflow = TextOverflow.Ellipsis) },
                    selected = currentRoute == "settings",
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSurface,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ),
                    onClick = {
                        navController.navigate("settings") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            }
        }
    ) { innerPadding ->
        if (isSearchActive && searchQuery.isNotBlank()) {
            SearchResultsScreen(
                query = searchQuery,
                navController = navController,
                onResultClick = { 
                    isSearchActive = false
                    searchQuery = "" 
                },
                modifier = Modifier.padding(innerPadding).fillMaxSize()
            )
        } else {
            val mainTabOrder = listOf("people", "figures_events", "worship", "settings")
            val swipeModifier = if (currentRoute in mainTabOrder) {
                Modifier.swipeGesture(
                    key = currentRoute,
                    onSwipeLeft = {
                        val currentIndex = mainTabOrder.indexOf(currentRoute)
                        if (currentIndex < mainTabOrder.size - 1) {
                            val nextRoute = mainTabOrder[currentIndex + 1]
                            navController.navigate(nextRoute) {
                                popUpTo(navController.graph.startDestinationId) { 
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    },
                    onSwipeRight = {
                        val currentIndex = mainTabOrder.indexOf(currentRoute)
                        if (currentIndex > 0) {
                            val prevRoute = mainTabOrder[currentIndex - 1]
                            navController.navigate(prevRoute) {
                                popUpTo(navController.graph.startDestinationId) { 
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            } else {
                Modifier.swipeGesture(
                    key = currentRoute,
                    requireEdge = true,
                    onSwipeRight = {
                        navController.popBackStack()
                    },
                    onSwipeLeft = {
                        navController.popBackStack()
                    }
                )
            }

            Box(modifier = Modifier.padding(innerPadding).fillMaxSize().then(swipeModifier)) {
                NavHost(
                    navController = navController,
                    startDestination = "people",
                    modifier = Modifier.fillMaxSize()
                ) {
                composable("people") { 
                    PeopleListScreen(
                        navController = navController,
                        onNavigateToTasbeeh = { navController.navigate("zahra_tasbeeh") },
                        hijriRecord = hijriRecord
                    ) 
                }
                composable("figures_events") { 
                    FiguresAndEventsScreen(
                        navController = navController,
                        hijriRecord = hijriRecord
                    ) 
                }
                composable("worship") { WorshipScreen(navController) }
                composable("worship/{category}") { backStackEntry ->
                    val category = backStackEntry.arguments?.getString("category")
                    WorshipScreen(navController, initialCategory = category)
                }
                composable(
                    route = "worship_text/{title}",
                    arguments = listOf(
                        navArgument("title") {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val rawTitle = backStackEntry.arguments?.getString("title") ?: ""
                    val title = try {
                        java.net.URLDecoder.decode(rawTitle, "UTF-8")
                    } catch (e: Exception) {
                        rawTitle
                    }
                    SingleTextScreen(
                        title = title,
                        onBack = { navController.popBackStack() },
                        onNavigateToTitle = { nextTitle ->
                            navController.navigate("worship_text/$nextTitle")
                        }
                    )
                }
                composable("worship_quran?surah={surah}&ayah={ayah}", arguments = listOf(
    navArgument("surah") { type = NavType.StringType; nullable = true },
    navArgument("ayah") { type = NavType.StringType; nullable = true }
)) { backStackEntry ->
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val surah = backStackEntry.arguments?.getString("surah")?.toIntOrNull() ?: -1
        val ayah = backStackEntry.arguments?.getString("ayah")?.toIntOrNull() ?: -1
        val intent = android.content.Intent(context, com.example.SawtQuranActivity::class.java).apply {
            putExtra("open_surah_id", surah)
            putExtra("open_ayah_number", ayah)
        }
        context.startActivity(intent)
        navController.popBackStack()
    }
}
                composable("detail/{id}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id")?.toIntOrNull()
                    val person = AhlAlBaytRepository.people.find { it.id == id } ?: AhlAlBaytRepository.famousFigures.find { it.id == id }
                    if (person != null) {
                        DetailScreen(person, onBack = { navController.popBackStack() })
                    }
                }
                composable("zahra_tasbeeh") { com.example.ui.TasbeehScreen(onBack = { navController.popBackStack() }) }
                composable("settings") { SettingsScreen() }
                composable("favorites") { FavoritesScreen(navController) }
            }
            }
        }
    }
    }
}

fun String.normalizeArabic(): String {
    return this
        // Remove Arabic diacritics / Harakat
        .replace("[\u064b\u064c\u064d\u064e\u064f\u0650\u0651\u0652\u0640]".toRegex(), "")
        // Normalize alef variants
        .replace("[\u0622\u0623\u0625\u0671]".toRegex(), "\u0627") // آ, أ, إ, ٱ -> ا
        // Normalize taa marbuta
        .replace("\u0629".toRegex(), "\u0647") // ة -> ه
        // Normalize alef maksura and yaa
        .replace("[\u0649\u064a]".toRegex(), "ي") // ى, ي -> ي
        .trim()
}

data class WorshipSearchResult(
    val title: String,
    val categoryName: String
)

@Composable
fun WorshipSearchCard(title: String, categoryName: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (categoryName == "أحكام الصوم") {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_worship_ahkam_sawm_3d),
                        contentDescription = "أحكام الصوم",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
            } else if (categoryName == "أدعية موجزة" || categoryName == "الادعية المختارة") {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_worship_selected_dua_3d),
                        contentDescription = "الادعية المختارة",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
            } else if (categoryName == "عداد الركع") {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_worship_rakat_counter_3d),
                        contentDescription = "عداد الركع",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
            } else if (categoryName == "مواقيت الصلاة") {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_mwaqet_alsalah_3d),
                        contentDescription = "مواقيت الصلاة",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title, 
                    fontWeight = FontWeight.Bold, 
                    style = MaterialTheme.typography.titleMedium, 
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = categoryName, 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "فتح",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SearchResultsScreen(
    query: String, 
    navController: NavHostController, 
    onResultClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val quranRepo = remember { (context.applicationContext as com.example.MainApplication).repository }
    var quranResults by remember { mutableStateOf<List<com.example.data.local.AyahEntity>>(emptyList()) }
    var surahNames by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    
    androidx.compose.runtime.LaunchedEffect(query) {
        if (query.isNotBlank()) {
            val results = quranRepo.searchAyahs(query)
            quranResults = results
            val names = mutableMapOf<Int, String>()
            results.forEach { ayah ->
                if (!names.containsKey(ayah.surahId)) {
                    names[ayah.surahId] = quranRepo.getSurahNameById(ayah.surahId) ?: ""
                }
            }
            surahNames = names
        } else {
            quranResults = emptyList()
            surahNames = emptyMap()
        }
    }

    val normalizedQuery = remember(query) { query.normalizeArabic() }

    val people = remember(normalizedQuery) {
        AhlAlBaytRepository.people.filter { 
            it.name.normalizeArabic().contains(normalizedQuery, ignoreCase = true) || 
            it.title.normalizeArabic().contains(normalizedQuery, ignoreCase = true)
        }
    }
    val figures = remember(normalizedQuery) {
        AhlAlBaytRepository.famousFigures.filter { 
            it.name.normalizeArabic().contains(normalizedQuery, ignoreCase = true) || 
            it.title.normalizeArabic().contains(normalizedQuery, ignoreCase = true)
        }
    }
    val events = remember(normalizedQuery) {
        AhlAlBaytRepository.events.filter { 
            it.name.normalizeArabic().contains(normalizedQuery, ignoreCase = true) 
        }
    }
    val worshipResults = remember(normalizedQuery) {
        val allWorships = listOf(
            com.example.data.WorshipData.dailyPrayers.values.map { WorshipSearchResult(it.first, "أدعية يومية") },
            com.example.data.WorshipData.taqeebat.map { WorshipSearchResult(it.first, "تعقيبات الصلاة") },
            com.example.data.WorshipData.munajat.map { WorshipSearchResult(it.first, "مناجاة") },
            com.example.data.WorshipData.tasbeehat.map { WorshipSearchResult(it.first, "تسبيحات") },
            com.example.data.WorshipData.azkarList.map { WorshipSearchResult(it.first, "أذكار") },
            com.example.data.WorshipData.ziyarats.map { WorshipSearchResult(it.first, "الزيارات") },
            com.example.data.WorshipData.ziyaratsOfDays.map { WorshipSearchResult(it.first, "زيارات الأيام") },
            com.example.data.WorshipData.monthlyDeeds.values.flatten().map { WorshipSearchResult(it.first, "أعمال الشهور") },
            com.example.data.WorshipData.sahifaSajjadiya.map { WorshipSearchResult(it.first, "الصحيفة السجادية") },
            com.example.data.WorshipData.generalPrayers.map { WorshipSearchResult(it.first, "أدعية عامة") },
            com.example.data.WorshipData.salawatsOnHujaj.map { WorshipSearchResult(it.first, "صلوات") },
            com.example.data.WorshipData.ahlAlBaytPrayers.map { WorshipSearchResult(it.first, "أدعية أهل البيت") },
            com.example.data.WorshipData.concisePrayers.map { WorshipSearchResult(it.first, "أدعية موجزة") },
            com.example.data.WorshipData.obligatoryAndRecommendedPrayers.map { WorshipSearchResult(it.first, "الصلاة") },
            com.example.data.WorshipData.fastingRules.map { WorshipSearchResult(it.first, "أحكام الصوم") },
            com.example.data.WorshipData.hajjRules.map { WorshipSearchResult(it.first, "أحكام الحجّ") },
            listOf(
                WorshipSearchResult("أحكام الصوم والمسائل الفقهية", "أحكام الصوم"),
                WorshipSearchResult("عداد الركع والسجدات", "عداد الركع"),
                WorshipSearchResult("مواقيت الصلاة", "مواقيت الصلاة")
            )
        ).flatten()

        allWorships.filter { 
            it.title.normalizeArabic().contains(normalizedQuery, ignoreCase = true)
        }
    }

    LazyColumn(modifier = modifier.background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(16.dp)) {
        if (people.isNotEmpty() || figures.isNotEmpty()) {
            item {
                Text(
                    "الشخصيات", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold, 
                    color = if (MaterialTheme.colorScheme.onBackground == androidx.compose.ui.graphics.Color.White) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(people + figures) { person ->
                PersonCard(person, onClick = { 
                    onResultClick()
                    navController.navigate("detail/${person.id}") 
                })
            }
        }
        if (worshipResults.isNotEmpty()) {
            item {
                Text(
                    "العبادات والأدعية", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold, 
                    color = if (MaterialTheme.colorScheme.onBackground == androidx.compose.ui.graphics.Color.White) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(worshipResults) { result ->
                WorshipSearchCard(
                    title = result.title,
                    categoryName = result.categoryName,
                    onClick = {
                        onResultClick()
                        if (result.categoryName == "عداد الركع") {
                            navController.navigate("worship/عداد الركع")
                        } else if (result.categoryName == "مواقيت الصلاة") {
                            navController.navigate("worship/مواقيت الصلاة")
                        } else if (result.categoryName == "أحكام الصوم" && result.title == "أحكام الصوم والمسائل الفقهية") {
                            navController.navigate("worship/أحكام الصوم")
                        } else {
                            navController.navigate("worship_text/${result.title}")
                        }
                    }
                )
            }
        }
        if (events.isNotEmpty()) {
            item {
                Text(
                    "المناسبات", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold, 
                    color = if (MaterialTheme.colorScheme.onBackground == androidx.compose.ui.graphics.Color.White) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(events) { event ->
                EventSearchCard(event)
            }
        }
        if (quranResults.isNotEmpty()) {
            item {
                Text(
                    "القرآن الكريم", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold, 
                    color = if (MaterialTheme.colorScheme.onBackground == androidx.compose.ui.graphics.Color.White) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(quranResults) { ayah ->
                QuranSearchCard(
                    ayah = ayah,
                    surahName = surahNames[ayah.surahId] ?: "",
                    onClick = {
                        onResultClick()
                        val intent = android.content.Intent(context, com.example.SawtQuranActivity::class.java).apply {
                            putExtra("open_surah_id", ayah.surahId)
                            putExtra("open_ayah_number", ayah.ayahNumber)
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
        if (people.isEmpty() && figures.isEmpty() && events.isEmpty() && worshipResults.isEmpty() && quranResults.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("لا توجد نتائج بحث", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun QuranSearchCard(ayah: com.example.data.local.AyahEntity, surahName: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "سورة $surahName - الآية ${ayah.ayahNumber}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = ayah.textUthmani,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                fontFamily = com.example.ui.screens.QuranFontFamily
            )
        }
    }
}

@Composable
fun EventSearchCard(event: com.example.data.HijriEvent) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = if (event.isMartyrdom) androidx.compose.ui.graphics.Color(0xFFEF5350) else androidx.compose.ui.graphics.Color(0xFF66BB6A),
                        shape = CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(event.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${event.day} ${com.example.data.getHijriMonthName(event.month)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
            }
        }
    }
}

private data class HourlySuggestedAyah(
    val surahId: Int,
    val surahName: String,
    val ayahNumber: Int,
    val text: String
)

private val HOURLY_SURAHS_AYAH_LIST = listOf(
    HourlySuggestedAyah(2, "البقرة", 255, "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ"),
    HourlySuggestedAyah(13, "الرعد", 28, "الَّذِينَ آمَنُوا وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ اللَّهِ ۗ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ"),
    HourlySuggestedAyah(33, "الأحزاب", 56, "إِنَّ اللَّهَ وَمَلَائِكَتَهُ يُصَلُّونَ عَلَى النَّبِيِّ ۚ يَا أَيُّهَا الَّذِينَ آمَنُوا صَلُّوا عَلَيْهِ وَسَلِّمُوا تَسْلِيمًا"),
    HourlySuggestedAyah(2, "البقرة", 186, "وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّى قَرِيبٌ ۖ أُجِيبُ دَعْوَةَ الدَّاعِ إِذَا دَعَانِ"),
    HourlySuggestedAyah(33, "الأحزاب", 33, "إِنَّمَا يُرِيدُ اللَّهُ لِيُذْهَبَ عَنكُمُ الرِّجْسَ أَهْلَ الْبَيْتِ وَيُطَهِّرَكُمْ تَطْهِيرًا"),
    HourlySuggestedAyah(20, "طه", 114, "فَتَعَالَى اللَّهُ الْمَلِكُ الْحَقُّ ۗ وَلَا تَعْجَلْ بِالْقُرْآنِ مِن قَبْلِ أَن يُقْضَىٰ إِلَيْكَ وَحْيُهُ ۖ وَقُل رَّبِّ زِدْنِي عِلْمًا"),
    HourlySuggestedAyah(94, "الشرح", 6, "إِنَّ مَعَ الْعُسْرِ يُسْرًا"),
    HourlySuggestedAyah(2, "البقرة", 152, "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ"),
    HourlySuggestedAyah(42, "الشورى", 23, "قُل لَّا أَسْأَلُكُمْ عَلَيْهِ أَجْرًا إِلَّا الْمَوَدَّةَ فِي الْقُرْبَىٰ"),
    HourlySuggestedAyah(65, "الطلاق", 2, "وَمَن يَتَّقِ اللَّهَ يَجْعَل لَّهُ مَخْرَجًا * وَيَرْزُقُهُ مِنْ حَيْثُ لَا يَحْتَسِبُ"),
    HourlySuggestedAyah(93, "الضحى", 5, "وَلَسَوْفَ يُعْطِيكَ رَبُّكَ فَتَرْضَىٰ"),
    HourlySuggestedAyah(2, "البقرة", 153, "يَا أَيُّهَا الَّذِينَ آمَنُوا اسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ ۚ إِنَّ اللَّهَ مَعَ الصَّابِرِينَ"),
    HourlySuggestedAyah(39, "الزمر", 36, "أَلَيْسَ اللَّهُ بِكَافٍ عَبْدَهُ"),
    HourlySuggestedAyah(52, "الطور", 48, "وَاصْبِرْ لِحُكْمِ رَبِّكَ فَإِنَّكَ بِأَعْيُنِنَا"),
    HourlySuggestedAyah(2, "البقرة", 137, "فَسَيَكْفِيكَهُمُ اللَّهُ ۚ وَهُوَ السَّمِيعُ الْعَلِيمُ"),
    HourlySuggestedAyah(9, "التوبة", 105, "وَقُلِ اعْمَلُوا فَسَيَرَى اللَّهُ عَمَلَكُمْ وَرَسُولُهُ وَالْمُؤْمِنُونَ"),
    HourlySuggestedAyah(28, "القصص", 24, "رَبِّ إِنِّي لِمَا أَنزَلْتَ إِلَيَّ مِنْ خَيْرٍ فَقِيرٌ"),
    HourlySuggestedAyah(21, "الأنبياء", 87, "لَّا إِلَٰهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ"),
    HourlySuggestedAyah(3, "آل عمران", 159, "إِنَّ اللَّهَ يُحِبُّ الْمُتَوَكِّلِينَ"),
    HourlySuggestedAyah(25, "الفرقان", 58, "وَتَوَكَّلْ عَلَى الْحَيِّ الَّذِي لَا يَمُوتُ"),
    HourlySuggestedAyah(11, "هود", 114, "إِنَّ الْحَسَنَاتِ يُذْهِبْنَ السَّيِّئَاتِ ۚ ذَٰلِكَ ذِكْرَىٰ لِلذَّاكِرِينَ"),
    HourlySuggestedAyah(57, "الحديد", 4, "وَهُوَ مَعَكُمْ أَيْنَ مَا كُنتُمْ ۚ وَاللَّهُ بِمَا تَعْمَلُونَ بَصِيرٌ"),
    HourlySuggestedAyah(11, "هود", 88, "وَمَا تَوْفِيقِي إِلَّا بِاللَّهِ ۚ عَلَيْهِ تَوَكَّلْتُ وَإِذَا أُنِيبُ"),
    HourlySuggestedAyah(40, "غافر", 60, "وَقَالَ رَبُّكُمُ ادْعُونِي أَسْتَجِبْ لَكُمْ")
)

@SuppressLint("MissingPermission")
@Composable
fun PeopleListScreen(navController: NavHostController, onNavigateToTasbeeh: () -> Unit = {}, hijriRecord: com.example.data.HijriDateRecord?) {
    val adjustedHijriDate = com.example.data.HijriSyncManager.getCalculatedHijriDate(hijriRecord)
    val currentYear = adjustedHijriDate.get(java.time.temporal.ChronoField.YEAR_OF_ERA)
    val monthNumber = adjustedHijriDate.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
    val monthName = com.example.data.getHijriMonthName(monthNumber)
    val weekdayAr = getArabicWeekdayName(java.time.LocalDate.now().dayOfWeek)
    val currentDay = adjustedHijriDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
    val hijriString = "$weekdayAr، $currentDay $monthName $currentYear هـ"
    
    val localDate = remember { java.time.LocalDate.now() }
    val gregDateStr = remember { "${localDate.dayOfMonth} ${getLevantineMonthName(localDate.monthValue)} ${localDate.year}" }
    val gregorianString = "$weekdayAr، $gregDateStr م"
    
    val timingsViewModel: TimingsViewModel = viewModel()
    val aladhanData by timingsViewModel.aladhanData.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("AhlAlBaytPrefs", android.content.Context.MODE_PRIVATE) }

    val homeCoroutineScope = rememberCoroutineScope()
    val quranRepo = remember {
        (context.applicationContext as? com.example.MainApplication)?.repository
    }
    var qabasSurahId by remember { mutableIntStateOf(1) }
    var qabasAyahNumber by remember { mutableIntStateOf(1) }
    var qabasSurahName by remember { mutableStateOf("الفاتحة") }
    var qabasAyahText by remember { mutableStateOf("بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ") }
    var isQabasAutoAudioEnabled by remember { mutableStateOf(false) }

    val audioPlayerManager = remember {
        com.example.audio.AyahAudioState.getOrCreate(context)
    }
    val isAudioPlaying by audioPlayerManager.isPlaying.collectAsState()
    val currentlyPlayingAyah by audioPlayerManager.currentPlayingAyah.collectAsState()
    val currentReciter by audioPlayerManager.currentReciter.collectAsState()

    var showPosterDialog by remember { mutableStateOf(false) }
    var showReciterDialog by remember { mutableStateOf(false) }

    val isQabasAyahPlaying = isAudioPlaying &&
        currentlyPlayingAyah?.first == qabasSurahId &&
        currentlyPlayingAyah?.second == qabasAyahNumber

    val isAutoChangePaused by remember {
        derivedStateOf {
            isAudioPlaying || showReciterDialog || showPosterDialog
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (currentlyPlayingAyah?.first == qabasSurahId && currentlyPlayingAyah?.second == qabasAyahNumber) {
                audioPlayerManager.stop()
            }
        }
    }

    LaunchedEffect(currentlyPlayingAyah) {
        if (isAudioPlaying && currentlyPlayingAyah != null) {
            val (sId, aNum) = currentlyPlayingAyah!!
            if (sId != qabasSurahId || aNum != qabasAyahNumber) {
                try {
                    val ayah = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        quranRepo?.getAyahByNumber(sId, aNum)
                    }
                    if (ayah != null) {
                        val sName = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            quranRepo?.getSurahNameById(ayah.surahId) ?: ""
                        }
                        qabasSurahId = ayah.surahId
                        qabasAyahNumber = ayah.ayahNumber
                        qabasSurahName = if (!sName.isNullOrBlank()) sName else "سورة رقم ${ayah.surahId}"
                        qabasAyahText = ayah.textUthmani
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    val loadAyah: suspend () -> Unit = remember(quranRepo) {
        {
            try {
                val ayah = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    quranRepo?.getRandomAyah()
                }
                if (ayah != null) {
                    val sName = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        quranRepo?.getSurahNameById(ayah.surahId) ?: ""
                    }
                    qabasSurahId = ayah.surahId
                    qabasAyahNumber = ayah.ayahNumber
                    qabasSurahName = if (!sName.isNullOrBlank()) sName else "سورة رقم ${ayah.surahId}"
                    qabasAyahText = ayah.textUthmani
                } else {
                    val fallback = HOURLY_SURAHS_AYAH_LIST.random()
                    qabasSurahId = fallback.surahId
                    qabasAyahNumber = fallback.ayahNumber
                    qabasSurahName = fallback.surahName
                    qabasAyahText = fallback.text
                }
            } catch (e: Exception) {
                val fallback = HOURLY_SURAHS_AYAH_LIST.random()
                qabasSurahId = fallback.surahId
                qabasAyahNumber = fallback.ayahNumber
                qabasSurahName = fallback.surahName
                qabasAyahText = fallback.text
            }
        }
    }

    LaunchedEffect(Unit) {
        loadAyah()
        while (true) {
            val delayTime = if (qabasAyahText.length > 100) 12000L else 8000L
            kotlinx.coroutines.delay(delayTime)
            
            // Wait if paused or if auto audio is active (auto audio handles its own loading)
            while (isAutoChangePaused || isQabasAutoAudioEnabled) {
                kotlinx.coroutines.delay(1000L)
            }
            
            // Re-check after the wait loop
            if (!isAutoChangePaused && !isQabasAutoAudioEnabled) {
                loadAyah()
            }
        }
    }

    var audioFinishedTrigger by remember { mutableIntStateOf(0) }

    DisposableEffect(audioPlayerManager) {
        audioPlayerManager.onSingleAyahComplete = {
            audioFinishedTrigger++
        }
        onDispose {
            audioPlayerManager.onSingleAyahComplete = null
        }
    }

    LaunchedEffect(audioFinishedTrigger, isQabasAutoAudioEnabled, showReciterDialog, showPosterDialog) {
        if (isQabasAutoAudioEnabled && !audioPlayerManager.isPlaying.value && !showReciterDialog && !showPosterDialog) {
            // Wait a small moment to ensure smooth transition after audio finishes
            kotlinx.coroutines.delay(1000L) 
            
            // Check again in case it was disabled during the delay or started manually
            if (isQabasAutoAudioEnabled && !audioPlayerManager.isPlaying.value) {
                try {
                    loadAyah()
                    if (isQabasAutoAudioEnabled) {
                        audioPlayerManager.playAyah(qabasSurahId, qabasAyahNumber, continuous = false)
                    }
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }

    LaunchedEffect(qabasSurahId) {
        if (qabasSurahId in 1..114) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    quranRepo?.preloadSurah(qabasSurahId)
                } catch (_: Exception) {}
            }
        }
    }

    var showDailyDeeds by remember {
        mutableStateOf(sharedPref.getBoolean("show_daily_deeds", true))
    }
    var showSuggestions by remember {
        mutableStateOf(sharedPref.getBoolean("show_suggestions", true))
    }
    var showEvents by remember {
        mutableStateOf(sharedPref.getBoolean("show_events", true))
    }
    var showQabas by remember {
        mutableStateOf(sharedPref.getBoolean("show_qabas", true))
    }

    val listener = remember {
        android.content.SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
            if (key == "show_daily_deeds") {
                showDailyDeeds = sp.getBoolean("show_daily_deeds", true)
            }
            if (key == "show_suggestions") {
                showSuggestions = sp.getBoolean("show_suggestions", true)
            }
            if (key == "show_events") {
                showEvents = sp.getBoolean("show_events", true)
            }
            if (key == "show_qabas") {
                showQabas = sp.getBoolean("show_qabas", true)
            }
        }
    }

    androidx.compose.runtime.DisposableEffect(sharedPref) {
        sharedPref.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            sharedPref.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val themeOnSurface = MaterialTheme.colorScheme.onSurface
    val themePrimaryContainer = MaterialTheme.colorScheme.primaryContainer
    val themeOnPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val themeSecondaryContainer = MaterialTheme.colorScheme.secondaryContainer
    val themeOnSecondaryContainer = MaterialTheme.colorScheme.onSecondaryContainer
    val themeSurface = MaterialTheme.colorScheme.surface
    val themeOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val themeOutline = MaterialTheme.colorScheme.outline
    val themeTertiary = MaterialTheme.colorScheme.tertiary

    IslamicBackgroundBox(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(), 
            contentPadding = PaddingValues(bottom = 56.dp, top = 12.dp, start = 12.dp, end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // شريط الصلاة على محمد وآل محمد المباركة
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 1.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = themeSurface.copy(alpha = 0.95f)),
                    border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFD4AF37).copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَآلِ مُحَمَّدٍ الطَّيِّبِينَ الطَّاهِرِينَ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }

            // Integrated Hijri & Gregorian Calendar Section (Inspired by Prayer Times Screen)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.2.dp, androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(
                            androidx.compose.ui.graphics.Color(0xFFD4AF37).copy(alpha = 0.5f),
                            androidx.compose.ui.graphics.Color(0xFF14B8A6).copy(alpha = 0.5f),
                            androidx.compose.ui.graphics.Color(0xFFD4AF37).copy(alpha = 0.5f)
                        )
                    )),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface,
                                        MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Hijri Date Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mosque,
                                    contentDescription = null,
                                    tint = androidx.compose.ui.graphics.Color(0xFFD4AF37),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = hijriString,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = "هجري",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = androidx.compose.ui.graphics.Color(0xFFD4AF37),
                                fontSize = 12.sp
                            )
                        }

                        androidx.compose.material3.HorizontalDivider(
                            color = androidx.compose.ui.graphics.Color(0xFFD4AF37).copy(alpha = 0.2f),
                            thickness = 1.dp
                        )

                        // Gregorian Date Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = gregorianString,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    fontSize = 13.5.sp
                                )
                            }
                            Text(
                                text = "ميلادي",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Ticker (Daily Deeds)
            if (showDailyDeeds) {
                item {
                    val tickerItems = remember(weekdayAr, currentDay, monthNumber) {
                        val items = mutableListOf<Pair<String, String>>()
                        items.add("تسبيح يوم $weekdayAr" to "worship_text/تسبيح يوم $weekdayAr")
                        items.add("دعاء يوم $weekdayAr" to "worship_text/دعاء يوم $weekdayAr")
                        items.add("زيارة يوم $weekdayAr" to "worship_text/زيارة يوم $weekdayAr")
                        
                        if (weekdayAr == "الخميس") items.add("قراءة دعاء كميل" to "worship_text/دعاء كميل بن زياد رضوان الله عليه")
                        if (weekdayAr == "الثلاثاء") items.add("قراءة دعاء التوسل" to "worship_text/دعاء التوسل بالأئمة (ع)")
                        if (weekdayAr == "الجمعة") items.add("قراءة دعاء الندبة" to "worship_text/دعاء الندبة")
                        
                        items
                    }
                    var currentTickerIndex by remember { mutableIntStateOf(0) }
                    
                    LaunchedEffect(tickerItems) {
                        while (true) {
                            kotlinx.coroutines.delay(7000)
                            if (tickerItems.isNotEmpty()) {
                                currentTickerIndex = (currentTickerIndex + 1) % tickerItems.size
                            }
                        }
                    }

                    if (tickerItems.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { navController.navigate(tickerItems[currentTickerIndex].second) },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(1.2.dp, androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(
                                    androidx.compose.ui.graphics.Color(0xFFD4AF37).copy(alpha = 0.65f),
                                    androidx.compose.ui.graphics.Color(0xFF14B8A6).copy(alpha = 0.75f),
                                    androidx.compose.ui.graphics.Color(0xFFD4AF37).copy(alpha = 0.65f)
                                )
                            )),
                            elevation = CardDefaults.cardElevation(3.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.surfaceVariant,
                                                MaterialTheme.colorScheme.surface
                                            )
                                        )
                                    )
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = null,
                                    tint = androidx.compose.ui.graphics.Color(0xFFD4AF37),
                                    modifier = Modifier.size(18.dp)
                                )
                                
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "أعمال اليوم",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Bold,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    androidx.compose.animation.AnimatedContent(
                                        targetState = currentTickerIndex,
                                        transitionSpec = {
                                            (androidx.compose.animation.slideInVertically { height -> height } + androidx.compose.animation.fadeIn()).togetherWith(
                                                    androidx.compose.animation.slideOutVertically { height -> -height } + androidx.compose.animation.fadeOut()
                                            )
                                        },
                                        label = "tickerTransition"
                                    ) { targetIndex ->
                                        Text(
                                            text = tickerItems[targetIndex].first,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = null,
                                    tint = androidx.compose.ui.graphics.Color(0xFFD4AF37),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // بطاقة الصلاة القادمة والمواقيت الشرعية (بديلة لبطاقة المقترحات)
            if (showSuggestions) {
                item {
                    com.example.ui.components.NextPrayerCard(
                        onOpenPrayerScreen = {
                            navController.navigate("worship/مواقيت الصلاة")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp, vertical = 2.dp)
                    )
                }
            }

            // قبس من كتاب الله الكريم - متصل مباشرة ببيانات القرآن الكريم
            if (showQabas) {
                item {
                    var isQabasExpanded by remember { mutableStateOf(false) }
                    val frameBorderColor = androidx.compose.ui.graphics.Color(0xFFB88E4C)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .animateContentSize(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = themePrimaryContainer),
                        border = BorderStroke(1.dp, frameBorderColor.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isQabasExpanded = !isQabasExpanded }
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.MenuBook,
                                        contentDescription = null,
                                        tint = frameBorderColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "قبس من كلام الله",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = themeOnPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isQabasExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                        contentDescription = if (isQabasExpanded) "طي البطاقة" else "توسيع البطاقة",
                                        tint = frameBorderColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            if (isQabasExpanded) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = {
                                            audioPlayerManager.stop()
                                            homeCoroutineScope.launch {
                                                try {
                                                    val ayah = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                                        quranRepo?.getRandomAyah()
                                                    }
                                                    if (ayah != null) {
                                                        val sName = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                                            quranRepo?.getSurahNameById(ayah.surahId) ?: ""
                                                        }
                                                        qabasSurahId = ayah.surahId
                                                        qabasAyahNumber = ayah.ayahNumber
                                                        qabasSurahName = if (!sName.isNullOrBlank()) sName else "سورة رقم ${ayah.surahId}"
                                                        qabasAyahText = ayah.textUthmani
                                                    }
                                                } catch (e: Exception) {}
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Refresh,
                                            contentDescription = "آية أخرى",
                                            tint = themeOnPrimaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Authentic Mushaf Frame Box
                                Surface(
                                    onClick = {
                                        val intent = android.content.Intent(context, com.example.SawtQuranActivity::class.java).apply {
                                            putExtra("open_surah_id", qabasSurahId)
                                            putExtra("open_ayah_number", qabasAyahNumber)
                                        }
                                        context.startActivity(intent)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.5.dp, frameBorderColor.copy(alpha = 0.65f)),
                                    shadowElevation = 1.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // Top Header of Mushaf Page
                                    val qabasJuz = com.example.ui.screens.getJuzNumber(qabasSurahId, qabasAyahNumber)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "الجزء ${qabasJuz.toArabicNumerals()}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = com.example.ui.screens.QuranFontFamily,
                                            color = frameBorderColor
                                        )
                                        Text(
                                            text = "سورة $qabasSurahName",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = com.example.ui.screens.QuranFontFamily,
                                            color = frameBorderColor
                                        )
                                    }

                                    HorizontalDivider(
                                        color = frameBorderColor.copy(alpha = 0.5f),
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(top = 6.dp, bottom = 10.dp)
                                    )

                                    if (qabasAyahNumber == 1 && qabasSurahId != 9) {
                                        Text(
                                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontFamily = com.example.ui.screens.QuranFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            ),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }

                                    AnimatedContent(
                                        targetState = qabasAyahText to qabasAyahNumber,
                                        transitionSpec = {
                                            (fadeIn() + scaleIn(initialScale = 0.95f)) togetherWith
                                            (fadeOut() + scaleOut(targetScale = 0.95f))
                                        },
                                        label = "suggested_ayah_text_transition"
                                    ) { (targetText, targetNum) ->
                                        val annotatedAyah = buildAnnotatedString {
                                            append(targetText)
                                            withStyle(
                                                SpanStyle(
                                                    color = frameBorderColor,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp
                                                )
                                            ) {
                                                append(" ۝${targetNum.toArabicNumerals()} ")
                                            }
                                        }

                                        Text(
                                            text = annotatedAyah,
                                            style = androidx.compose.ui.text.TextStyle(
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                fontSize = 21.sp,
                                                lineHeight = 40.sp,
                                                fontFamily = com.example.ui.screens.QuranFontFamily,
                                                color = MaterialTheme.colorScheme.onSurface
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 4.dp, vertical = 4.dp)
                                        )
                                    }

                                    HorizontalDivider(
                                        color = frameBorderColor.copy(alpha = 0.35f),
                                        thickness = 0.8.dp,
                                        modifier = Modifier.padding(top = 10.dp, bottom = 8.dp)
                                    )

                                    // شريط أزرار الآية: نسخ الآية، ومشاركة كصورة
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 2.dp, vertical = 2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // 1. زر مشاركة الآية كصورة
                                        Surface(
                                            onClick = { showPosterDialog = true },
                                            shape = RoundedCornerShape(10.dp),
                                            color = themeSecondaryContainer,
                                            border = BorderStroke(1.dp, frameBorderColor.copy(alpha = 0.45f)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Share,
                                                    contentDescription = "مشاركة الآية كصورة",
                                                    tint = frameBorderColor,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "مشاركة كصورة",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = themeOnSecondaryContainer,
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        // 2. زر نسخ النص
                                        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                                        Surface(
                                            onClick = {
                                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString("$qabasAyahText\n[$qabasSurahName: $qabasAyahNumber]"))
                                                android.widget.Toast.makeText(context, "تم نسخ الآية", android.widget.Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            color = themeSecondaryContainer,
                                            border = BorderStroke(1.dp, frameBorderColor.copy(alpha = 0.45f)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.ContentCopy,
                                                    contentDescription = "نسخ النص",
                                                    tint = frameBorderColor,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "نسخ النص",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = themeOnSecondaryContainer,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.MenuBook,
                                            contentDescription = null,
                                            tint = frameBorderColor.copy(alpha = 0.75f),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "اضغط على البطاقة لفتح الآية في المصحف الشريف",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = frameBorderColor.copy(alpha = 0.75f),
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

            // 3 Daily Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Ziyarat
                    Card(
                        modifier = Modifier.weight(1f).height(74.dp).clickable { navController.navigate("worship_text/زيارة يوم $weekdayAr") },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = themeSurface),
                        elevation = CardDefaults.cardElevation(1.dp),
                        border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFD4AF37).copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(themeSecondaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_worship_ziyarat_3d),
                                    contentDescription = "زيارة",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("زيارة $weekdayAr", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = themeOnSurface, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                    }
                    // Dua
                    Card(
                        modifier = Modifier.weight(1f).height(74.dp).clickable { navController.navigate("worship_text/دعاء يوم $weekdayAr") },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = themeSurface),
                        elevation = CardDefaults.cardElevation(1.dp),
                        border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFD4AF37).copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(themeSecondaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_worship_dua_3d),
                                    contentDescription = "دعاء",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("دعاء $weekdayAr", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = themeOnSurface, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                    }
                    // Tasbeeh
                    Card(
                        modifier = Modifier.weight(1f).height(74.dp).clickable { navController.navigate("worship_text/تسبيح يوم $weekdayAr") },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = themeSurface),
                        elevation = CardDefaults.cardElevation(1.dp),
                        border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFD4AF37).copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(themeSecondaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_worship_munajat_3d),
                                    contentDescription = "تسبيح",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("تسبيح $weekdayAr", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = themeOnSurface, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            // Taqeebat & Masbaha Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Taqeebat Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { navController.navigate("worship/تعقيبات الصلاة") },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = themePrimaryContainer),
                        border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFD4AF37).copy(alpha = 0.45f)),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(themeSecondaryContainer.copy(alpha = 0.3f), CircleShape)
                                    .border(1.2.dp, themeTertiary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_worship_taqibat_3d),
                                    contentDescription = "تعقيبات الصلاة",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "تعقيبات الصلاة",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = themeOnPrimaryContainer,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Masbaha Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onNavigateToTasbeeh),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = themePrimaryContainer),
                        border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFD4AF37).copy(alpha = 0.45f)),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(themeSecondaryContainer.copy(alpha = 0.3f), CircleShape)
                                    .border(1.2.dp, themeTertiary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_worship_misbaha_3d),
                                    contentDescription = "المسبحة الإلكترونية",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "المسبحة الإلكترونية",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = themeOnPrimaryContainer,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Events Bottom
            if (showEvents) {
                item {
                    AutoScrollingEventsBanner(hijriRecord = hijriRecord)
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showPosterDialog) {
        val ayahEntity = remember(qabasSurahId, qabasAyahNumber, qabasAyahText) {
            com.example.data.local.AyahEntity(
                id = 0,
                surahId = qabasSurahId,
                ayahNumber = qabasAyahNumber,
                textUthmani = qabasAyahText
            )
        }
        com.example.ui.screens.AyahPosterDialog(
            ayah = ayahEntity,
            surahName = qabasSurahName,
            onDismiss = { showPosterDialog = false }
        )
    }

    if (showReciterDialog) {
        AlertDialog(
            onDismissRequest = { showReciterDialog = false },
            modifier = Modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth(0.85f),
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.RecordVoiceOver,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color(0xFFB88E4C),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "اختر القارئ للآية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(com.example.audio.AVAILABLE_RECITERS) { reciter ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    audioPlayerManager.setReciter(reciter)
                                    if (isQabasAyahPlaying) {
                                        audioPlayerManager.playAyah(qabasSurahId, qabasAyahNumber, continuous = false)
                                    }
                                    showReciterDialog = false
                                }
                                .padding(vertical = 8.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentReciter.id == reciter.id,
                                onClick = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
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
                    Text("إغلاق", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun FiguresAndEventsScreen(navController: NavHostController, hijriRecord: com.example.data.HijriDateRecord?) {
    val timingsViewModel: TimingsViewModel = viewModel()
    val aladhanData by timingsViewModel.aladhanData.collectAsState()
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("AhlAlBaytPrefs", android.content.Context.MODE_PRIVATE) }
    var showEvents by remember { mutableStateOf(sharedPref.getBoolean("show_events", true)) }

    val listener = remember {
        android.content.SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
            if (key == "show_events") {
                showEvents = sp.getBoolean("show_events", true)
            }
        }
    }

    androidx.compose.runtime.DisposableEffect(sharedPref) {
        sharedPref.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            sharedPref.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    var showImamsDialog by remember { mutableStateOf(false) }
    var showFiguresDialog by remember { mutableStateOf(false) }
    
    IslamicBackgroundBox(modifier = Modifier.fillMaxSize()) {

        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 80.dp, top = 40.dp)) {
            item {
                Text(
                    "الشخصيات والمناسبات", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold, 
                    color = if (MaterialTheme.colorScheme.onBackground == androidx.compose.ui.graphics.Color.White) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            item { HijriCalendarCard(hijriRecord, showEvents = showEvents) }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { showImamsDialog = true },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFD4AF37).copy(alpha = 0.45f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "الأئمة المعصومين (عليهم السلام)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { showFiguresDialog = true },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFD4AF37).copy(alpha = 0.45f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "الشخصيات البارزة",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showImamsDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showImamsDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(28.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                IslamicBackgroundBox(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🕌", style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "الأئمة المعصومين (ع)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { showImamsDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "إغلاق")
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(AhlAlBaytRepository.people) { person ->
                                PersonCard(person, onClick = {
                                    showImamsDialog = false
                                    navController.navigate("detail/${person.id}")
                                })
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFiguresDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showFiguresDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(28.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                IslamicBackgroundBox(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⭐", style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "الشخصيات البارزة",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { showFiguresDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "إغلاق")
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(AhlAlBaytRepository.famousFigures) { person ->
                                PersonCard(person, onClick = {
                                    showFiguresDialog = false
                                    navController.navigate("detail/${person.id}")
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarExpandableCard(aladhanData: AladhanResponse?, hijriRecord: com.example.data.HijriDateRecord?) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val status = hijriRecord?.status ?: "estimated"

    val adjustedHijriDate = remember(hijriRecord) {
        com.example.data.HijriSyncManager.getCalculatedHijriDate(hijriRecord)
    }
    val currentYear = adjustedHijriDate.get(java.time.temporal.ChronoField.YEAR_OF_ERA)
    val currentMonth = adjustedHijriDate.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
    val currentDay = adjustedHijriDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
    val monthName = com.example.data.getHijriMonthName(currentMonth)
    val weekdayAr = getArabicWeekdayName(java.time.LocalDate.now().dayOfWeek)
    val hijriString = "$weekdayAr $currentDay $monthName $currentYear هـ"
    
    val localDate = java.time.LocalDate.now()
    val gregString = "$weekdayAr، ${localDate.dayOfMonth} ${getLevantineMonthName(localDate.monthValue)} ${localDate.year}"

    val upcomingEvents = remember { mutableStateListOf<UpcomingEvent>() }

    LaunchedEffect(currentYear, currentMonth, currentDay, hijriRecord) {
        upcomingEvents.clear()
        for (event in AhlAlBaytRepository.events) {
            val eventMonth = event.month
            val eventDay = event.day
            var eventYear = currentYear
            if (eventMonth < currentMonth || (eventMonth == currentMonth && eventDay < currentDay)) {
                eventYear = currentYear + 1
            }
            try {
                val eventHijrahDate = java.time.chrono.HijrahDate.of(eventYear, eventMonth, eventDay)
                val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(adjustedHijriDate, eventHijrahDate).toInt()
                val eventLocalDate = java.time.LocalDate.now().plusDays(daysBetween.toLong())
                val gregorianDateStr = eventLocalDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                if (daysBetween in 0..60) {
                    upcomingEvents.add(UpcomingEvent(event, daysBetween, gregorianDateStr, eventLocalDate, eventYear))
                }
            } catch(e: Exception) {}
        }
        upcomingEvents.sortBy { it.daysUntil }
    }

    var isExpanded by remember { mutableStateOf(false) }
    val rotation = if (isExpanded) 180f else 0f

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).clickable(
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            indication = null
        ) { isExpanded = !isExpanded },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFD4AF37).copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "التقويم والمناسبات",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "طي" else "توسيع",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.rotate(rotation)
                )
            }

            if (isExpanded) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = hijriString,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 10.dp)
                            ) {
                                Text(
                                    "التقويم الهجري وفق مكتب سماحة السيد علي السيستاني (دام ظله)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 10.dp)
                            ) {
                                if (status == "verified") {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF81C784), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("مؤكد رسمياً", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                                } else {
                                    Icon(Icons.Filled.Info, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFFFFB74D), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تقديري بانتظار التحديث", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(gregString, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    
                    if (upcomingEvents.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(16.dp))
                upcomingEvents.take(3).forEach { ev ->
                    val formattedGregorian = remember(ev.eventDate) {
                        "${getArabicWeekdayName(ev.eventDate.dayOfWeek)}، ${ev.eventDate.dayOfMonth} ${getLevantineMonthName(ev.eventDate.monthValue)} ${ev.eventDate.year}"
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Elegant vertical indicator strip on the start side (right side in RTL)
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .fillMaxHeight()
                                    .background(
                                        if (ev.event.isMartyrdom) androidx.compose.ui.graphics.Color(0xFFEF5350) 
                                        else androidx.compose.ui.graphics.Color(0xFF66BB6A)
                                    )
                            )
                            
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(16.dp)
                            ) {
                                // Row of badges: Type of event & Days remaining
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Event Category Tag
                                    Surface(
                                        color = if (ev.event.isMartyrdom) androidx.compose.ui.graphics.Color(0x22EF5350) 
                                                else androidx.compose.ui.graphics.Color(0x2266BB6A),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (ev.event.isMartyrdom) "ذكرى أليمة" else "ولادة مباركة",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (ev.event.isMartyrdom) androidx.compose.ui.graphics.Color(0xFFFF8A80) 
                                                    else androidx.compose.ui.graphics.Color(0xFFA5D6A7),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    
                                    // Countdown Badge
                                    Surface(
                                        color = if (ev.daysUntil == 0) MaterialTheme.colorScheme.errorContainer 
                                                else MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (ev.daysUntil == 0) "اليوم" else "متبقي ${ev.daysUntil} يوم",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (ev.daysUntil == 0) MaterialTheme.colorScheme.onErrorContainer 
                                                    else MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                // Event Title
                                Text(
                                    text = ev.event.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f))
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                // Event Dates Information List
                                // 1. Historical Date Row (stacked vertically)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "تاريخ الحدث التاريخي",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${ev.event.day} ${getHijriMonthName(ev.event.month)} ${ev.event.historicalYear?.let { "$it هـ" } ?: ""}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // 2. Next Occurrence Hijri Row (stacked vertically)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null,
                                        tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "موعد المناسبة القادمة",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${ev.event.day} ${getHijriMonthName(ev.event.month)} ${ev.upcomingHijriYear} هـ",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // 3. Corresponding Gregorian Date Row (stacked vertically)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "الموافق ميلادياً",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = formattedGregorian,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

}

}

@Composable
fun PersonCard(person: Person, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp).clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFD4AF37).copy(alpha = 0.4f))
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("👤", style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(person.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(person.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "تفاصيل", tint = MaterialTheme.colorScheme.primary)
        }
    }
}


fun getLevantineMonthName(month: Int): String {
    return when(month) {
        1 -> "كانون الثاني"
        2 -> "شباط"
        3 -> "آذار"
        4 -> "نيسان"
        5 -> "أيار"
        6 -> "حزيران"
        7 -> "تموز"
        8 -> "آب"
        9 -> "أيلول"
        10 -> "تشرين الأول"
        11 -> "تشرين الثاني"
        12 -> "كانون الأول"
        else -> ""
    }
}

fun getArabicWeekdayName(dayOfWeek: java.time.DayOfWeek): String {
    return when (dayOfWeek) {
        java.time.DayOfWeek.SATURDAY -> "السبت"
        java.time.DayOfWeek.SUNDAY -> "الأحد"
        java.time.DayOfWeek.MONDAY -> "الاثنين"
        java.time.DayOfWeek.TUESDAY -> "الثلاثاء"
        java.time.DayOfWeek.WEDNESDAY -> "الأربعاء"
        java.time.DayOfWeek.THURSDAY -> "الخميس"
        java.time.DayOfWeek.FRIDAY -> "الجمعة"
    }
}

fun Int.toArabicNumerals(): String {
    val arabicNumerals = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return this.toString().map { if (it.isDigit()) arabicNumerals[it - '0'] else it }.joinToString("")
}

@Composable
fun HijriCalendarCard(hijriRecord: com.example.data.HijriDateRecord?, showEvents: Boolean = false) {
    val themePrimary = androidx.compose.material3.MaterialTheme.colorScheme.primary
    val themeOnSurface = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
    val themeOnSurfaceVariant = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
    val themeSurface = androidx.compose.material3.MaterialTheme.colorScheme.surface
    val themeOutline = androidx.compose.material3.MaterialTheme.colorScheme.outline
    val themeOutlineVariant = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
    
    val todayHijri = androidx.compose.runtime.remember(hijriRecord) { com.example.data.HijriSyncManager.getCalculatedHijriDate(hijriRecord) }
    var currentYear by androidx.compose.runtime.remember(todayHijri) { androidx.compose.runtime.mutableStateOf(todayHijri.get(java.time.temporal.ChronoField.YEAR_OF_ERA)) }
    var currentMonth by androidx.compose.runtime.remember(todayHijri) { androidx.compose.runtime.mutableStateOf(todayHijri.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)) }
    
    val monthName = com.example.data.getHijriMonthName(currentMonth)
    
    val firstDayHijri = androidx.compose.runtime.remember(currentYear, currentMonth) { java.time.chrono.HijrahDate.of(currentYear, currentMonth, 1) }
    val lengthOfMonth = firstDayHijri.lengthOfMonth()
    
    val firstDayObservedGregorian = androidx.compose.runtime.remember(firstDayHijri, hijriRecord) {
        val safeRecord = hijriRecord ?: com.example.data.HijriDateRecord(
            day = 9, month = 2, year = 1448, gregorianDateStr = "2026-07-24", status = "estimated"
        )
        val anchorHijri = try {
            java.time.chrono.HijrahDate.of(safeRecord.year, safeRecord.month, safeRecord.day)
        } catch (e: Exception) {
            java.time.chrono.HijrahDate.now()
        }
        val anchorGregorian = try {
            java.time.LocalDate.parse(safeRecord.gregorianDateStr)
        } catch (e: Exception) {
            java.time.LocalDate.now()
        }
        val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(anchorHijri, firstDayHijri)
        anchorGregorian.plusDays(daysDiff)
    }
    
    val firstDayOfWeek = firstDayObservedGregorian.dayOfWeek.value // 1=Mon, 7=Sun
    val startDayIndex = if (firstDayOfWeek == 7) 0 else firstDayOfWeek // 0=Sun, 1=Mon...
    
    fun nextMonth() {
        if (currentMonth == 12) {
            currentMonth = 1
            currentYear++
        } else {
            currentMonth++
        }
    }
    
    fun prevMonth() {
        if (currentMonth == 1) {
            currentMonth = 12
            currentYear--
        } else {
            currentMonth--
        }
    }
    
    val currentMonthEvents = androidx.compose.runtime.remember(currentMonth) {
        com.example.data.AhlAlBaytRepository.events.filter { it.month == currentMonth }.sortedBy { it.day }
    }

    androidx.compose.material3.Card(
        modifier = androidx.compose.ui.Modifier.fillMaxWidth().padding(horizontal = if (showEvents) 24.dp else 0.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = themeSurface),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFD4AF37).copy(alpha = 0.45f))
    ) {
        androidx.compose.foundation.layout.Column(modifier = androidx.compose.ui.Modifier.fillMaxWidth().padding(16.dp)) {
            // Header
            androidx.compose.foundation.layout.Row(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                androidx.compose.material3.IconButton(onClick = { prevMonth() }) {
                    @Suppress("DEPRECATION")
                    androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Filled.KeyboardArrowRight, contentDescription = "Previous Month", tint = themePrimary)
                }
                androidx.compose.material3.Text(
                    text = "$monthName ${currentYear.toArabicNumerals()}",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = themeOnSurface
                )
                androidx.compose.material3.IconButton(onClick = { nextMonth() }) {
                    @Suppress("DEPRECATION")
                    androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Filled.KeyboardArrowLeft, contentDescription = "Next Month", tint = themePrimary)
                }
            }
            
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
            
            val daysOfWeek = listOf("ح", "ن", "ث", "ر", "خ", "ج", "س")
            androidx.compose.foundation.layout.Row(modifier = androidx.compose.ui.Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceAround) {
                daysOfWeek.forEach { day ->
                    androidx.compose.material3.Text(
                        text = day,
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                        color = themeOnSurface,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        modifier = androidx.compose.ui.Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
            
            val totalCells = startDayIndex + lengthOfMonth
            val rows = kotlin.math.ceil(totalCells / 7.0).toInt()
            
            for (row in 0 until rows) {
                androidx.compose.foundation.layout.Row(modifier = androidx.compose.ui.Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceAround) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - startDayIndex + 1
                        
                        androidx.compose.foundation.layout.Box(
                            modifier = androidx.compose.ui.Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .border(
                                    width = 0.5.dp,
                                    color = themeOutline.copy(alpha = 0.25f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            if (dayNumber in 1..lengthOfMonth) {
                                val isToday = currentYear == todayHijri.get(java.time.temporal.ChronoField.YEAR_OF_ERA) &&
                                              currentMonth == todayHijri.get(java.time.temporal.ChronoField.MONTH_OF_YEAR) &&
                                              dayNumber == todayHijri.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
                                val hasEvent = currentMonthEvents.any { it.day == dayNumber }
                                
                                androidx.compose.foundation.layout.Box(
                                    modifier = androidx.compose.ui.Modifier
                                        .fillMaxSize()
                                        .background(if (isToday) themePrimary else androidx.compose.ui.graphics.Color.Transparent, androidx.compose.foundation.shape.CircleShape),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    androidx.compose.foundation.layout.Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally, verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
                                        androidx.compose.material3.Text(
                                            text = dayNumber.toArabicNumerals(),
                                            color = if (isToday) androidx.compose.ui.graphics.Color.White else if (col == 5) themePrimary else themeOnSurface,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                                        )
                                        if (showEvents && hasEvent && !isToday) {
                                            androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.size(4.dp).background(if (currentMonthEvents.find { it.day == dayNumber }?.isMartyrdom == true) androidx.compose.ui.graphics.Color(0xFFEF5350) else androidx.compose.ui.graphics.Color(0xFF66BB6A), androidx.compose.foundation.shape.CircleShape))
                                        } else if (showEvents && hasEvent && isToday) {
                                            androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.size(4.dp).background(androidx.compose.ui.graphics.Color.White, androidx.compose.foundation.shape.CircleShape))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            if (showEvents && currentMonthEvents.isNotEmpty()) {
                var isEventsExpanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                androidx.compose.material3.HorizontalDivider(color = themeOutlineVariant.copy(alpha = 0.3f))
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(6.dp))
                
                androidx.compose.foundation.layout.Row(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .clickable { isEventsExpanded = !isEventsExpanded }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Text(
                        "أحداث هذا الشهر",
                        style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = themeOnSurface
                    )
                    
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Text(
                            text = if (isEventsExpanded) "إخفاء" else "عرض",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = themeOnSurfaceVariant,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(4.dp))
                        androidx.compose.material3.Icon(
                            imageVector = if (isEventsExpanded) androidx.compose.material.icons.Icons.Filled.KeyboardArrowUp else androidx.compose.material.icons.Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (isEventsExpanded) "إخفاء الأحداث" else "عرض الأحداث",
                            tint = themePrimary
                        )
                    }
                }

                androidx.compose.animation.AnimatedVisibility(visible = isEventsExpanded) {
                    androidx.compose.foundation.layout.Column(
                        modifier = androidx.compose.ui.Modifier.padding(top = 4.dp)
                    ) {
                        currentMonthEvents.forEach { event ->
                            androidx.compose.foundation.layout.Row(
                                modifier = androidx.compose.ui.Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                androidx.compose.foundation.layout.Box(
                                    modifier = androidx.compose.ui.Modifier.size(8.dp).background(
                                        color = if (event.isMartyrdom) androidx.compose.ui.graphics.Color(0xFFEF5350) else androidx.compose.ui.graphics.Color(0xFF66BB6A),
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(12.dp))
                                androidx.compose.material3.Text(
                                    text = "${event.day.toArabicNumerals()} $monthName - ${event.name}",
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = themeOnSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun AutoScrollingEventsBanner(hijriRecord: com.example.data.HijriDateRecord?) {
    val isDark = com.example.ui.theme.ThemeManager.currentThemeMode.isDark
    val cardBg = MaterialTheme.colorScheme.surfaceVariant
    val titleColor = MaterialTheme.colorScheme.onSurface
    val textColor = MaterialTheme.colorScheme.onSurface
    val redTextColor = if (isDark) androidx.compose.ui.graphics.Color(0xFFFF8A80) else androidx.compose.ui.graphics.Color(0xFFC62828)

    val adjustedHijriDate = remember(hijriRecord) {
        com.example.data.HijriSyncManager.getCalculatedHijriDate(hijriRecord)
    }
    val currentYear = adjustedHijriDate.get(java.time.temporal.ChronoField.YEAR_OF_ERA)
    val currentMonth = adjustedHijriDate.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
    val currentDay = adjustedHijriDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)

    val upcomingEvents = remember { mutableStateListOf<UpcomingEvent>() }
    LaunchedEffect(currentYear, currentMonth, currentDay, hijriRecord) {
        upcomingEvents.clear()
        val list = mutableListOf<UpcomingEvent>()
        for (event in AhlAlBaytRepository.events) {
            val eventMonth = event.month
            val eventDay = event.day
            var eventYear = currentYear
            if (eventMonth < currentMonth || (eventMonth == currentMonth && eventDay < currentDay)) {
                eventYear = currentYear + 1
            }
            try {
                val eventHijrahDate = java.time.chrono.HijrahDate.of(eventYear, eventMonth, eventDay)
                val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(adjustedHijriDate, eventHijrahDate).toInt()
                // Only include future events within the upcoming 2 months (~60 days)
                if (daysBetween in 0..60) {
                    val eventLocalDate = java.time.LocalDate.now().plusDays(daysBetween.toLong())
                    val gregorianDateStr = eventLocalDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                    list.add(UpcomingEvent(event, daysBetween, gregorianDateStr, eventLocalDate, eventYear))
                }
            } catch(e: Exception) {}
        }
        list.sortBy { it.daysUntil }
        
        // Fallback if no events occur in the next 60 days
        if (list.isEmpty()) {
            for (event in AhlAlBaytRepository.events) {
                val eventMonth = event.month
                val eventDay = event.day
                var eventYear = currentYear
                if (eventMonth < currentMonth || (eventMonth == currentMonth && eventDay < currentDay)) {
                    eventYear = currentYear + 1
                }
                try {
                    val eventHijrahDate = java.time.chrono.HijrahDate.of(eventYear, eventMonth, eventDay)
                    val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(adjustedHijriDate, eventHijrahDate).toInt()
                    if (daysBetween >= 0) {
                        val eventLocalDate = java.time.LocalDate.now().plusDays(daysBetween.toLong())
                        val gregorianDateStr = eventLocalDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                        list.add(UpcomingEvent(event, daysBetween, gregorianDateStr, eventLocalDate, eventYear))
                    }
                } catch(e: Exception) {}
            }
            list.sortBy { it.daysUntil }
        }
        upcomingEvents.addAll(list)
    }

    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(upcomingEvents.size) {
        if (upcomingEvents.isNotEmpty()) {
            while (true) {
                kotlinx.coroutines.delay(5000)
                currentIndex = (currentIndex + 1) % upcomingEvents.size
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "الأحداث",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (upcomingEvents.isNotEmpty()) {
                    androidx.compose.animation.AnimatedContent(
                        targetState = currentIndex,
                        transitionSpec = {
                            (androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(500)) + 
                            androidx.compose.animation.slideInVertically(animationSpec = androidx.compose.animation.core.tween(500)) { height -> height }).togetherWith(
                                androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(500)) + 
                                androidx.compose.animation.slideOutVertically(animationSpec = androidx.compose.animation.core.tween(500)) { height -> -height }
                            )
                        },
                        label = "events_banner"
                    ) { index ->
                        val event = upcomingEvents.getOrNull(index)
                        if (event != null) {
                            val hijriDateStr = "${event.event.day.toArabicNumerals()} ${com.example.data.getHijriMonthName(event.event.month)}"
                            val remainingStr = when (event.daysUntil) {
                                0 -> "اليوم!"
                                1 -> "متبقي يوم واحد"
                                2 -> "متبقي يومان"
                                in 3..10 -> "متبقي ${event.daysUntil.toArabicNumerals()} أيام"
                                else -> "متبقي ${event.daysUntil.toArabicNumerals()} يوم"
                            }
                            val fullDetailsStr = "$hijriDateStr | ${event.gregorianDateStr} م | $remainingStr"

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = event.event.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textColor,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = fullDetailsStr,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = redTextColor,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "لا توجد أحداث قريبة في الشهرين القادمين",
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

fun Modifier.swipeGesture(
    key: Any?,
    requireEdge: Boolean = false,
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null
): Modifier = this.pointerInput(key) {
    var totalDragX = 0f
    var isEligibleGesture = true
    detectHorizontalDragGestures(
        onDragStart = { offset ->
            totalDragX = 0f
            if (requireEdge) {
                val width = size.width
                val edgeThreshold = width * 0.20f
                isEligibleGesture = offset.x < edgeThreshold || offset.x > (width - edgeThreshold)
            } else {
                isEligibleGesture = true
            }
        },
        onDragEnd = {
            if (isEligibleGesture) {
                if (totalDragX > 150f) {
                    onSwipeRight?.invoke()
                } else if (totalDragX < -150f) {
                    onSwipeLeft?.invoke()
                }
            }
        },
        onDragCancel = {
            totalDragX = 0f
            isEligibleGesture = true
        },
        onHorizontalDrag = { change, dragAmount ->
            if (isEligibleGesture) {
                change.consume()
                totalDragX += dragAmount
            }
        }
    )
}
