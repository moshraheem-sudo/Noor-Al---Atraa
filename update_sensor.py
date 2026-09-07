import re

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "r") as f:
    content = f.read()

# 1. Add imports
imports_to_add = """import android.app.Activity
import android.view.WindowManager
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
"""
content = content.replace("import android.content.Context\n", imports_to_add + "import android.content.Context\n")


# 2. Rewrite ActiveCounterScreen
new_active_counter = """@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveCounterScreen(prayer: PrayerOption, onBack: () -> Unit) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val proximitySensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) }
    val haptic = LocalHapticFeedback.current

    var rakaaCount by remember { mutableStateOf(0) }
    var currentSujood by remember { mutableStateOf(0) }
    var isNear by remember { mutableStateOf(false) }
    var isStarted by remember { mutableStateOf(false) }
    
    val isFinished = prayer.targetRakaat != null && rakaaCount >= prayer.targetRakaat

    // Keep screen on while this screen is active
    val activity = context as? Activity
    DisposableEffect(activity) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    DisposableEffect(sensorManager, proximitySensor, isStarted, isFinished) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
                    val distance = event.values[0]
                    val maximumRange = proximitySensor?.maximumRange ?: 5f
                    // If maximumRange is very large (e.g., 100cm), we still want a small threshold for sujood (e.g. 5cm)
                    val threshold = min(maximumRange, 5f)
                    
                    val currentlyNear = distance < threshold

                    if (currentlyNear && !isNear) {
                        isNear = true
                    } else if (!currentlyNear && isNear) {
                        isNear = false
                        if (isStarted && !isFinished) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (currentSujood == 1) {
                                currentSujood = 0
                                rakaaCount++
                            } else {
                                currentSujood++
                            }
                        }
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (proximitySensor != null && isStarted && !isFinished) {
            sensorManager.registerListener(listener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    val bgColor = Color(0xFF132A35)
    val goldColor = Color(0xFFFDD55E)
    val darkElementColor = Color(0xFF1C3845)

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = { 
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("عداد الركع", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.TouchApp, contentDescription = "Touch", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        if (isFinished) {
                            rakaaCount = 0
                            currentSujood = 0
                            isStarted = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } else {
                            isStarted = !isStarted
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = goldColor,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(0.6f).height(56.dp)
                ) {
                    Text(
                        text = if (isFinished) "إعادة الصلاة" else if (isStarted) "إيقاف الصلاة" else "ابدأ صلاتك",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            // Prayer Rug background
            PrayerRugShape(color = goldColor, modifier = Modifier.fillMaxSize(0.85f).padding(bottom = 20.dp))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize().padding(bottom = 40.dp)
            ) {
                // Prayer Name Pill
                Box(
                    modifier = Modifier
                        .background(darkElementColor, RoundedCornerShape(32.dp))
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(prayer.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Rakaa Section
                Box(
                    modifier = Modifier
                        .background(darkElementColor, RoundedCornerShape(24.dp))
                        .padding(horizontal = 32.dp, vertical = 6.dp)
                ) {
                    Text("ركعة", color = Color.White, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(contentAlignment = Alignment.Center) {
                    IslamicStar(color = goldColor, size = 120f)
                    Text(
                        text = rakaaCount.toString(),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = bgColor
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Sujood Section
                Box(
                    modifier = Modifier
                        .background(darkElementColor, RoundedCornerShape(24.dp))
                        .padding(horizontal = 32.dp, vertical = 6.dp)
                ) {
                    Text("سجدة", color = Color.White, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(contentAlignment = Alignment.Center) {
                    IslamicStar(color = goldColor, size = 100f)
                    Text(
                        text = currentSujood.toString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = bgColor
                    )
                }
            }
            
            if (proximitySensor == null) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "عذراً، جهازك لا يحتوي على حساس التقارب اللازم.",
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            }
        }
    }
}
"""

content = re.sub(r"@OptIn\(ExperimentalMaterial3Api::class\)\n@Composable\nfun ActiveCounterScreen.*?\}\n}\n\n@Composable\nfun PrayerRugShape", new_active_counter + "\n@Composable\nfun PrayerRugShape", content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "w") as f:
    f.write(content)
