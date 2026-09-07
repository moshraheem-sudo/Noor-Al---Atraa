import re

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "r") as f:
    content = f.read()

# Make sure we have necessary imports
if "import androidx.compose.foundation.layout.BoxWithConstraints" not in content:
    content = content.replace("import androidx.compose.foundation.layout.Box", "import androidx.compose.foundation.layout.BoxWithConstraints\nimport androidx.compose.foundation.layout.Box")

new_active_screen = """@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveCounterScreen(prayer: PrayerOption, onBack: () -> Unit) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val proximitySensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) }
    val haptic = LocalHapticFeedback.current
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100) }

    DisposableEffect(Unit) {
        onDispose {
            toneGenerator.release()
        }
    }

    var rakaaCount by remember { mutableStateOf(0) }
    var currentSujood by remember { mutableStateOf(0) }
    var isNear by remember { mutableStateOf(false) }
    var isStarted by remember { mutableStateOf(false) }
    
    val isFinished = prayer.targetRakaat != null && rakaaCount >= prayer.targetRakaat

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
                    val threshold = Math.min(maximumRange.toDouble(), 5.0).toFloat()
                    
                    val currentlyNear = distance < threshold

                    if (currentlyNear && !isNear) {
                        isNear = true
                    } else if (!currentlyNear && isNear) {
                        isNear = false
                        if (isStarted && !isFinished) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            try {
                                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                            } catch (e: Exception) {
                            }
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

    // Theme Colors
    val goldColor = Color(0xFFC8A261)
    val lightGold = Color(0xFFF1DEB9) // Lighter for better contrast
    val darkShapeBg = Color(0xFF132A25) // Deep green
    val textDark = Color(0xFF0D1B17)

    Scaffold(
        containerColor = Color.Transparent,
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
                    IconButton(onClick = { 
                        rakaaCount = 0
                        currentSujood = 0
                        isStarted = false
                        try {
                            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                        } catch (e: Exception) {}
                    }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reset", tint = Color.White)
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
                    .padding(horizontal = 32.dp, vertical = 24.dp),
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
                        containerColor = Color.Transparent,
                        contentColor = textDark
                    ),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(64.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(listOf(lightGold, goldColor, lightGold)),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(2.dp, goldColor, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Left/Right diamonds on button
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawPath(
                                path = createDiamondPath(30f, size.height / 2f, 10f),
                                color = darkShapeBg
                            )
                            drawPath(
                                path = createDiamondPath(size.width - 30f, size.height / 2f, 10f),
                                color = darkShapeBg
                            )
                        }
                        Text(
                            text = if (isFinished) "إعادة الصلاة" else if (isStarted) "إيقاف الصلاة" else "ابدأ صلاتك",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E1A17))) {
            
            // Mosque Background
            MosqueBackground(modifier = Modifier.fillMaxSize())
            
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Prayer Name Pill
                Box(
                    modifier = Modifier
                        .border(1.dp, goldColor, RoundedCornerShape(32.dp))
                        .background(darkShapeBg.copy(alpha = 0.9f), RoundedCornerShape(32.dp))
                        .padding(horizontal = 32.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawPath(createDiamondPath(16f, size.height / 2f, 8f), goldColor)
                        drawPath(createDiamondPath(size.width - 16f, size.height / 2f, 8f), goldColor)
                    }
                    Text(prayer.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Ornate Counter Container
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val w = maxWidth.value
                    val h = maxHeight.value
                    
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        // Draw Ornate Background
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cw = size.width
                            val ch = size.height
                            
                            val cornerRadius = 60f
                            val pointWidth = 120f
                            val pointHeight = 50f
                            
                            val path = Path().apply {
                                // Top point and top edge
                                moveTo(cw/2, 0f)
                                quadraticBezierTo(cw/2 + pointWidth/4, pointHeight, cw/2 + pointWidth/2, pointHeight)
                                lineTo(cw - cornerRadius, pointHeight)
                                
                                // Top right corner
                                arcTo(
                                    rect = Rect(cw - cornerRadius * 2, pointHeight, cw, pointHeight + cornerRadius * 2),
                                    startAngleDegrees = -90f,
                                    sweepAngleDegrees = 90f,
                                    forceMoveTo = false
                                )
                                
                                // Right edge
                                lineTo(cw, ch - pointHeight - cornerRadius)
                                
                                // Bottom right corner
                                arcTo(
                                    rect = Rect(cw - cornerRadius * 2, ch - pointHeight - cornerRadius * 2, cw, ch - pointHeight),
                                    startAngleDegrees = 0f,
                                    sweepAngleDegrees = 90f,
                                    forceMoveTo = false
                                )
                                
                                // Bottom edge and bottom point
                                lineTo(cw/2 + pointWidth/2, ch - pointHeight)
                                quadraticBezierTo(cw/2 + pointWidth/4, ch - pointHeight, cw/2, ch)
                                quadraticBezierTo(cw/2 - pointWidth/4, ch - pointHeight, cw/2 - pointWidth/2, ch - pointHeight)
                                
                                // Bottom edge left
                                lineTo(cornerRadius, ch - pointHeight)
                                
                                // Bottom left corner
                                arcTo(
                                    rect = Rect(0f, ch - pointHeight - cornerRadius * 2, cornerRadius * 2, ch - pointHeight),
                                    startAngleDegrees = 90f,
                                    sweepAngleDegrees = 90f,
                                    forceMoveTo = false
                                )
                                
                                // Left edge
                                lineTo(0f, pointHeight + cornerRadius)
                                
                                // Top left corner
                                arcTo(
                                    rect = Rect(0f, pointHeight, cornerRadius * 2, pointHeight + cornerRadius * 2),
                                    startAngleDegrees = 180f,
                                    sweepAngleDegrees = 90f,
                                    forceMoveTo = false
                                )
                                
                                // Top edge left
                                lineTo(cw/2 - pointWidth/2, pointHeight)
                                quadraticBezierTo(cw/2 - pointWidth/4, pointHeight, cw/2, 0f)
                                
                                close()
                            }
                            
                            // Outer dark fill
                            drawPath(path = path, color = darkShapeBg)
                            
                            // Outer gold border
                            drawPath(path = path, color = goldColor, style = Stroke(width = 6f))
                            
                            // Inner gold border
                            val innerScale = 0.94f
                            scale(innerScale, innerScale) {
                                drawPath(path = path, color = goldColor.copy(alpha = 0.5f), style = Stroke(width = 3f))
                            }
                        }
                        
                        // Content inside the ornate shape
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Rakaa Section
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Canvas(modifier = Modifier.size(16.dp)) { drawPath(createDiamondPath(8.dp.toPx(), 8.dp.toPx(), 8.dp.toPx()), goldColor) }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("ركعة", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(16.dp))
                                Canvas(modifier = Modifier.size(16.dp)) { drawPath(createDiamondPath(8.dp.toPx(), 8.dp.toPx(), 8.dp.toPx()), goldColor) }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Box(contentAlignment = Alignment.Center) {
                                // Diamonds on left/right of circle
                                Canvas(modifier = Modifier.fillMaxWidth()) {
                                    drawPath(createDiamondPath(size.width * 0.15f, size.height / 2, 14f), goldColor)
                                    drawPath(createDiamondPath(size.width * 0.85f, size.height / 2, 14f), goldColor)
                                }
                                CircleCounter(color = lightGold, borderColor = goldColor, size = 180f)
                                Text(
                                    text = rakaaCount.toString(),
                                    fontSize = 90.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textDark,
                                    textAlign = TextAlign.Center
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Separator line
                            Canvas(modifier = Modifier.fillMaxWidth(0.7f).height(1.dp)) {
                                drawLine(color = goldColor.copy(alpha=0.5f), start = Offset(0f, 0f), end = Offset(size.width, 0f), strokeWidth = 2f)
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Sujood Section
                            Text("سجدة", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Box(contentAlignment = Alignment.Center) {
                                CircleCounter(color = lightGold, borderColor = goldColor, size = 140f)
                                Text(
                                    text = currentSujood.toString(),
                                    fontSize = 70.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textDark,
                                    textAlign = TextAlign.Center
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
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

@Composable
fun MosqueBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Base dark warm color
        drawRect(Color(0xFF141210))
        
        // Background wall texture
        val archPath = Path().apply {
            moveTo(w * 0.05f, h)
            lineTo(w * 0.05f, h * 0.4f)
            
            // Pointed arch
            quadraticBezierTo(w * 0.05f, h * 0.2f, w * 0.2f, h * 0.15f)
            quadraticBezierTo(w * 0.5f, h * 0.05f, w * 0.8f, h * 0.15f)
            quadraticBezierTo(w * 0.95f, h * 0.2f, w * 0.95f, h * 0.4f)
            
            lineTo(w * 0.95f, h)
            close()
        }
        
        drawPath(
            path = archPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0F1A17), Color(0xFF132A25), Color(0xFF132A25))
            )
        )
        
        // Inner glowing border of the arch
        drawPath(
            path = archPath,
            color = Color(0xFFC8A261).copy(alpha = 0.4f),
            style = Stroke(width = 8f)
        )
        drawPath(
            path = archPath,
            color = Color(0xFFC8A261).copy(alpha = 0.2f),
            style = Stroke(width = 16f)
        )
        
        // A floor line
        drawLine(
            color = Color(0xFFC8A261).copy(alpha = 0.2f),
            start = Offset(0f, h * 0.85f),
            end = Offset(w, h * 0.85f),
            strokeWidth = 4f
        )
        
        // Simple rug shape
        val rugPath = Path().apply {
            moveTo(w * 0.2f, h)
            lineTo(w * 0.35f, h * 0.85f)
            lineTo(w * 0.65f, h * 0.85f)
            lineTo(w * 0.8f, h)
            close()
        }
        drawPath(
            path = rugPath,
            color = Color(0xFF0D1B17).copy(alpha = 0.8f)
        )
        
        // Rug border
        drawPath(
            path = rugPath,
            color = Color(0xFFC8A261).copy(alpha = 0.3f),
            style = Stroke(width = 4f)
        )
    }
}

fun createDiamondPath(cx: Float, cy: Float, size: Float): Path {
    return Path().apply {
        moveTo(cx, cy - size)
        lineTo(cx + size, cy)
        lineTo(cx, cy + size)
        lineTo(cx - size, cy)
        close()
    }
}

@Composable
fun CircleCounter(color: Color, borderColor: Color, size: Float) {
    Canvas(modifier = Modifier.size(size.dp)) {
        drawCircle(
            color = color,
            radius = size.dp.toPx() / 2f
        )
        drawCircle(
            color = borderColor,
            radius = size.dp.toPx() / 2f,
            style = Stroke(width = 6f)
        )
        // Inner thin border
        drawCircle(
            color = borderColor.copy(alpha=0.6f),
            radius = size.dp.toPx() / 2f - 10f,
            style = Stroke(width = 3f)
        )
    }
}"""

pattern = r"@OptIn\(ExperimentalMaterial3Api::class\)\n@Composable\nfun ActiveCounterScreen.*"
content = re.sub(pattern, new_active_screen, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "w") as f:
    f.write(content)
