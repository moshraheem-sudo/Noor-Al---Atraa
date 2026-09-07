import re

with open("sawt_al_quran-v2.30.0/app/src/main/java/com/example/MainActivity.kt", "r") as f:
    main_activity_content = f.read()

# Extract the body of setContent { ... }
# It starts from 'MyApplicationTheme {'
match = re.search(r'MyApplicationTheme\s*\{(.*?)\}\s*\}\s*\}\s*override fun onDestroy\(\)', main_activity_content, re.DOTALL)
if match:
    sawt_ui_content = match.group(1).strip()
else:
    print("Failed to find UI content")
    exit(1)

# We need to adapt it into a Composable function
composable_code = f"""package com.example.ui

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.CheckCircle
import com.example.MainApplication
import com.example.audio.AudioPlayerManager
import com.example.audio.PlayerManager
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.screens.RecitersScreen
import com.example.ui.screens.ReciterDetailScreen
import com.example.ui.screens.MiniPlayer
import com.example.ui.viewmodels.HomeViewModel
import com.example.ui.viewmodels.ReaderViewModel

@Composable
fun SawtQuranMainScreen(onBack: () -> Unit) {{
    val context = LocalContext.current
    val application = context.applicationContext as MainApplication
    val repository = application.repository
    // audioPlayerManager should be remembered or something, let's instantiate it or pass it.
    // In MainActivity it was lazy: private val audioPlayerManager by lazy {{ AudioPlayerManager(this) }}
    val audioPlayerManager = remember {{ AudioPlayerManager(context) }}
    
    // We should ensure they are released on dispose, but since it's a composable, we use DisposableEffect
    DisposableEffect(Unit) {{
        onDispose {{
            audioPlayerManager.stop()
            PlayerManager.release()
        }}
    }}

    {sawt_ui_content}
}}

@Composable
fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {{
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable(onClick = onClick)
    ) {{
        Box(
            modifier = Modifier
                .width(68.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {{
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }}
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (selected) {{
            Spacer(modifier = Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(2.5.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }} else {{
            Spacer(modifier = Modifier.height(5.5.dp))
        }}
    }}
}}
"""

# Now we need to modify sawt_ui_content slightly: replace (application as com.example.QuranApplication) with application
composable_code = composable_code.replace("(application as com.example.QuranApplication)", "application")

with open("app/src/main/java/com/example/ui/SawtQuranMainScreen.kt", "w") as f:
    f.write(composable_code)
