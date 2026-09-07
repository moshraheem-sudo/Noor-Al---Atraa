package com.example.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.drawscope.scale

import android.app.Activity
import android.view.WindowManager
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RakaaCounterScreen(onBack: () -> Unit) {
    var selectedPrayer by remember { mutableStateOf<PrayerOption?>(null) }

    if (selectedPrayer == null) {
        PrayerSelectionScreen(
            onBack = onBack,
            onSelect = { selectedPrayer = it }
        )
    } else {
        ActiveCounterScreen(
            prayer = selectedPrayer!!,
            onBack = { selectedPrayer = null }
        )
    }
}

data class PrayerOption(val name: String, val targetRakaat: Int?)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerSelectionScreen(onBack: () -> Unit, onSelect: (PrayerOption) -> Unit) {
    val isDark = com.example.ui.theme.ThemeManager.currentThemeMode.isDark
    val titleColor = MaterialTheme.colorScheme.onBackground
    val cardBg = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val goldColor = MaterialTheme.colorScheme.primary

    val options = listOf(
        PrayerOption("صلاة الصبح", 2),
        PrayerOption("صلاة الظهر", 4),
        PrayerOption("صلاة العصر", 4),
        PrayerOption("صلاة المغرب", 3),
        PrayerOption("صلاة العشاء", 4),
        PrayerOption("عداد حر مفتوح", null)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_worship_rakat_counter_3d),
                                contentDescription = "عداد الركع",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("عداد الركع", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = titleColor,
                    navigationIconContentColor = titleColor
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(MaterialTheme.colorScheme.surface, androidx.compose.foundation.shape.CircleShape)
                                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_worship_rakat_counter_3d),
                                contentDescription = "عداد الركع",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "عداد الركعات والسجدات",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "احسب ركعات وسجدات صلاتك تلقائياً وبدقة عبر حساس التقارب أو بالنقر المباشر",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            item {
                Text(
                    text = "اختر الصلاة للبدء",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            items(options) { option ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(option) }
                        .border(1.dp, goldColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (option.targetRakaat != null) "${option.targetRakaat}" else "∞",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = option.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = if (option.targetRakaat != null) "${option.targetRakaat} ركعات" else "بدون حد أقصى للركعات",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
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

    val performSujoodStep: () -> Unit = {
        if (isStarted && !isFinished) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            try {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
            } catch (e: Exception) {
            }
            if (currentSujood == 1) {
                currentSujood = 2
                rakaaCount++
            } else {
                currentSujood = 1
            }
        }
    }

    DisposableEffect(sensorManager, proximitySensor, isStarted, isFinished, currentSujood) {
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
                        performSujoodStep()
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
    val darkShapeBg = Color(0xFF91B3D0) // Soft frosted ice shape background
    val textDark = Color(0xFF0E1F2E)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color.White.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.4f), androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_worship_rakat_counter_3d),
                                contentDescription = "عداد الركع",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
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
                    .padding(horizontal = 32.dp, vertical = 16.dp),
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
        com.example.ui.IslamicBackgroundBox(modifier = Modifier.fillMaxSize()) {
            
            // Mosque Background
            MosqueBackground(modifier = Modifier.fillMaxSize())
            
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
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

                Spacer(modifier = Modifier.height(6.dp))

                // Ornate Counter Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable(enabled = isStarted && !isFinished) {
                            performSujoodStep()
                        }
                ) {
                    // Content inside the ornate shape
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                            .drawBehind {
                                val cw = size.width
                                val ch = size.height
                                
                                val cornerRadius = 60f
                                val pointWidth = 120f
                                val pointHeight = 30f
                                
                                val path = Path().apply {
                                    // Top edge (straight)
                                    moveTo(cornerRadius, 0f)
                                    lineTo(cw - cornerRadius, 0f)
                                    
                                    // Top right corner
                                    arcTo(
                                        rect = Rect(cw - cornerRadius * 2, 0f, cw, cornerRadius * 2),
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
                                    quadraticTo(cw/2 + pointWidth/4, ch - pointHeight, cw/2, ch)
                                    quadraticTo(cw/2 - pointWidth/4, ch - pointHeight, cw/2 - pointWidth/2, ch - pointHeight)
                                    
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
                                    lineTo(0f, cornerRadius)
                                    
                                    // Top left corner
                                    arcTo(
                                        rect = Rect(0f, 0f, cornerRadius * 2, cornerRadius * 2),
                                        startAngleDegrees = 180f,
                                        sweepAngleDegrees = 90f,
                                        forceMoveTo = false
                                    )
                                    
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
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Rakaa Section
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Canvas(modifier = Modifier.size(12.dp)) { drawPath(createDiamondPath(6.dp.toPx(), 6.dp.toPx(), 6.dp.toPx()), goldColor) }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("ركعة", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(16.dp))
                            Canvas(modifier = Modifier.size(12.dp)) { drawPath(createDiamondPath(6.dp.toPx(), 6.dp.toPx(), 6.dp.toPx()), goldColor) }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Box(contentAlignment = Alignment.Center) {
                            // Diamonds on left/right of circle
                            Canvas(modifier = Modifier.fillMaxWidth()) {
                                drawPath(createDiamondPath(size.width * 0.15f, size.height / 2, 10f), goldColor)
                                drawPath(createDiamondPath(size.width * 0.85f, size.height / 2, 10f), goldColor)
                            }
                            CircleCounter(color = lightGold, borderColor = goldColor, size = 110f)
                            Text(
                                text = rakaaCount.toString(),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = textDark,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Separator line
                        Canvas(modifier = Modifier.fillMaxWidth(0.6f).height(1.dp)) {
                            drawLine(color = goldColor.copy(alpha=0.5f), start = Offset(0f, 0f), end = Offset(size.width, 0f), strokeWidth = 2f)
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Sujood Section
                        Text("سجدة", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Box(contentAlignment = Alignment.Center) {
                            CircleCounter(color = lightGold, borderColor = goldColor, size = 80f)
                            Text(
                                text = currentSujood.toString(),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = textDark,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
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

@Composable
fun MosqueBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Base dark warm color
        drawRect(Color(0xFF141210))
        
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
}