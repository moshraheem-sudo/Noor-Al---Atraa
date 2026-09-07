package com.example.qibla

import com.example.R
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.common.api.ResolvableApiException
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.IslamicBackgroundBox
import androidx.activity.enableEdgeToEdge
import kotlin.math.abs

class QiblaCompassActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var rotationVectorSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var magnetometerSensor: Sensor? = null
    private var orientationSensor: Sensor? = null
    
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var currentAzimuth by mutableStateOf(0f)
    private var qiblaBearing by mutableStateOf(196f) // Default fallback
    private var permissionGranted by mutableStateOf(false)
    private var isLocationEnabled by mutableStateOf(false)
    private var locationFetched by mutableStateOf(false)
    private var isTrackingLocation = false
    
    private var currentLocation: Location? = null
    
    private var lastAccelerometer = FloatArray(3)
    private var lastMagnetometer = FloatArray(3)
    private var lastAccelerometerSet = false
    private var lastMagnetometerSet = false
    
    private var accumulatedAzimuth = 0f
    private var isFirstAzimuth = true

    // Kaaba location
    private val kaabaLat = 21.422487
    private val kaabaLng = 39.826206

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        permissionGranted = fineGranted || coarseGranted
        if (permissionGranted) {
            checkLocationEnabledState()
        }
    }

    private val resolutionForResult = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            checkLocationEnabledState()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val prefs = getSharedPreferences("qibla_prefs", Context.MODE_PRIVATE)
        if (prefs.contains("cached_qibla_bearing")) {
            qiblaBearing = prefs.getFloat("cached_qibla_bearing", 0f)
            locationFetched = true
        }

        @Suppress("DEPRECATION")
        orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    calculateQibla(location)
                }
            }
        }

        checkLocationPermission()

        setContent {
            var showCalibrationDialog by remember { mutableStateOf(false) }
            
            androidx.activity.compose.BackHandler {
                finish()
            }

            androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                MyApplicationTheme {
                    QiblaScreenUI(
                        currentAzimuth = currentAzimuth,
                        qiblaBearing = qiblaBearing,
                        permissionGranted = permissionGranted,
                        isLocationEnabled = isLocationEnabled,
                        locationFetched = locationFetched,
                        showCalibrationDialog = showCalibrationDialog,
                        onDismissCalibration = { showCalibrationDialog = false },
                        onRequestPermission = { checkLocationPermission() },
                        onEnableLocation = { promptEnableLocation() },
                        onBack = { finish() },
                        onShowCalibration = { showCalibrationDialog = true }
                    )
                }
            }
        }
    }

    private fun checkLocationPermission() {
        val fineLocationGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        
        if (fineLocationGranted || coarseLocationGranted) {
            permissionGranted = true
            checkLocationEnabledState()
        } else {
            requestPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }
    
    private fun checkLocationEnabledState() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || 
                            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (isLocationEnabled && permissionGranted) {
            startLocationUpdates()
        }
    }

    private fun promptEnableLocation() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())
        
        task.addOnSuccessListener {
            checkLocationEnabledState()
        }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                    resolutionForResult.launch(intentSenderRequest)
                } catch (sendEx: Exception) {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            } else {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }
    }

    private fun startLocationUpdates() {
        if (isTrackingLocation) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                val priority = if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Priority.PRIORITY_HIGH_ACCURACY
                } else {
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY
                }
                val locationRequest = LocationRequest.Builder(priority, 5000)
                    .setMinUpdateIntervalMillis(2000)
                    .build()
                    
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                ).addOnFailureListener {
                    // Ignore failure
                    isTrackingLocation = false
                }
                isTrackingLocation = true
                
                // Also try to get last location quickly
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        calculateQibla(location)
                    }
                }.addOnFailureListener {
                    // Ignore location failure
                }
            } catch (e: Exception) {
                isTrackingLocation = false
            }
        }
    }
    
    private fun stopLocationUpdates() {
        if (isTrackingLocation) {
            try {
                fusedLocationClient.removeLocationUpdates(locationCallback).addOnFailureListener {
                    // Ignore
                }
            } catch (e: Exception) {
                // Ignore
            }
            isTrackingLocation = false
        }
    }

    private fun calculateQibla(location: Location) {
        currentLocation = location
        
        // Exact mathematical equation for Qibla calculation
        val phiK = Math.toRadians(kaabaLat)
        val lambdaK = Math.toRadians(kaabaLng)
        val phi = Math.toRadians(location.latitude)
        val lambda = Math.toRadians(location.longitude)

        val deltaLambda = lambdaK - lambda

        val y = Math.sin(deltaLambda)
        val x = Math.cos(phi) * Math.tan(phiK) - Math.sin(phi) * Math.cos(deltaLambda)

        var bearing = Math.toDegrees(Math.atan2(y, x)).toFloat()
        if (bearing < 0) {
            bearing += 360f
        }
        
        qiblaBearing = bearing
        getSharedPreferences("qibla_prefs", Context.MODE_PRIVATE).edit().putFloat("cached_qibla_bearing", qiblaBearing).apply()
        locationFetched = true
    }

    override fun onResume() {
        super.onResume()
        checkLocationEnabledState()
        
        var registered = false
        // Using SENSOR_DELAY_GAME (20ms) for smoother compass
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME)
            registered = true
        }
        if (accelerometerSensor != null && magnetometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)
            sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_GAME)
            registered = true
        }
        if (!registered && orientationSensor != null) {
            sensorManager.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        stopLocationUpdates()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        
        var azimuthInDegrees = -1f
        var success = false

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuthInRadians = orientation[0]
            azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
            success = true
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
            lastAccelerometerSet = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
            lastMagnetometerSet = true
        } else if (event.sensor.type == Sensor.TYPE_ORIENTATION && !success && !lastAccelerometerSet) {
            azimuthInDegrees = event.values[0]
            success = true
        }

        if (!success && lastAccelerometerSet && lastMagnetometerSet) {
            val rotationMatrix = FloatArray(9)
            val rSuccess = SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)
            if (rSuccess) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                val azimuthInRadians = orientation[0]
                azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
                success = true
            }
        }

        if (success) {
            // Apply magnetic declination to get true north instead of magnetic north
            currentLocation?.let { loc ->
                val geoField = GeomagneticField(
                    loc.latitude.toFloat(),
                    loc.longitude.toFloat(),
                    loc.altitude.toFloat(),
                    System.currentTimeMillis()
                )
                azimuthInDegrees += geoField.declination
            }

            if (azimuthInDegrees < 0) azimuthInDegrees += 360f
            
            if (isFirstAzimuth) {
                accumulatedAzimuth = azimuthInDegrees
                isFirstAzimuth = false
                currentAzimuth = accumulatedAzimuth
            } else {
                var delta = azimuthInDegrees - (accumulatedAzimuth % 360f)
                if (delta > 180f) delta -= 360f
                if (delta < -180f) delta += 360f
                accumulatedAzimuth += delta
                
                // Butter-smooth low-pass filter to eliminate sensor jitter
                val alpha = 0.12f
                currentAzimuth = currentAzimuth + alpha * (accumulatedAzimuth - currentAzimuth)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

private fun getDirectionName(azimuth: Float): String {
    val norm = ((azimuth % 360f) + 360f) % 360f
    return when {
        norm >= 337.5 || norm < 22.5 -> "الشمال"
        norm >= 22.5 && norm < 67.5 -> "شمال شرق"
        norm >= 67.5 && norm < 112.5 -> "الشرق"
        norm >= 112.5 && norm < 157.5 -> "جنوب شرق"
        norm >= 157.5 && norm < 202.5 -> "الجنوب"
        norm >= 202.5 && norm < 247.5 -> "جنوب غرب"
        norm >= 247.5 && norm < 292.5 -> "الغرب"
        else -> "شمال غرب"
    }
}

@Composable
fun QiblaScreenUI(
    currentAzimuth: Float,
    qiblaBearing: Float,
    permissionGranted: Boolean,
    isLocationEnabled: Boolean,
    locationFetched: Boolean,
    showCalibrationDialog: Boolean,
    onDismissCalibration: () -> Unit,
    onRequestPermission: () -> Unit,
    onEnableLocation: () -> Unit,
    onBack: () -> Unit,
    onShowCalibration: () -> Unit = {}
) {
    androidx.activity.compose.BackHandler {
        onBack()
    }

    if (showCalibrationDialog) {
        AlertDialog(
            onDismissRequest = onDismissCalibration,
            title = {
                Text(
                    text = "معايرة البوصلة",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Calibrate",
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFF1CB0F6)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "للحصول على دقة أفضل في تحديد إتجاه القبلة، يرجى تحريك هاتفك على شكل رقم 8 في الهواء عدة مرات لتفعيل مستشعرات الجهاز بدقة.",
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissCalibration,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1CB0F6))
                ) {
                    Text("تم", color = Color.White)
                }
            }
        )
    }

    // Smoother animation for fast sensor updates
    val animatedAzimuth by animateFloatAsState(
        targetValue = currentAzimuth,
        animationSpec = tween(durationMillis = 100),
        label = "compass_rotation"
    )

    val normalizedAzimuth = ((currentAzimuth % 360f) + 360f) % 360f
    val normalizedQibla = ((qiblaBearing % 360f) + 360f) % 360f
    val diff = abs(normalizedAzimuth - normalizedQibla)
    val finalDiff = if (diff > 180f) 360f - diff else diff
    val isAligned = permissionGranted && isLocationEnabled && locationFetched && (finalDiff <= 6.5f)
    
    val haptic = LocalHapticFeedback.current
    var lastHapticTime by remember { mutableStateOf(0L) }
    LaunchedEffect(isAligned) {
        if (isAligned) {
            val now = System.currentTimeMillis()
            if (now - lastHapticTime > 1500L) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                lastHapticTime = now
            }
        }
    }

    val displayDegree = ((currentAzimuth % 360f) + 360f) % 360f

    IslamicBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
                .verticalScroll(androidx.compose.foundation.rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Calibration / GPS icon on top-left
                IconButton(onClick = { if (!isLocationEnabled) onEnableLocation() else if (!permissionGranted) onRequestPermission() }) {
                    Icon(
                        imageVector = if (isLocationEnabled && permissionGranted) Icons.Default.GpsFixed else Icons.Default.GpsOff,
                        contentDescription = "GPS",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Back button on top-right
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))

            // Premium 3D Kaaba Illustration
            Kaaba3D(
                modifier = Modifier.size(68.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Arabic title "اتجاه القبلة"
            Text(
                text = "اتجاه القبلة",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Compass Dial - Prominent full size
            Box(
                modifier = Modifier
                    .sizeIn(maxWidth = 290.dp, maxHeight = 290.dp)
                    .fillMaxWidth(0.78f)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                CompassDial(
                    azimuth = animatedAzimuth,
                    qiblaBearing = qiblaBearing,
                    isReady = permissionGranted && isLocationEnabled && locationFetched,
                    isAligned = isAligned
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Alignment Status Pill
            Surface(
                color = if (isAligned) Color(0xFF1B5E20).copy(alpha = 0.85f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, if (isAligned) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isAligned) Icons.Default.Check else Icons.Default.NearMe,
                        contentDescription = null,
                        tint = if (isAligned) Color(0xFF81C784) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAligned) "أنت الآن باتجاه القبلة المباركة" else "حرك الهاتف لإيجاد القبلة",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Permissions or GPS disabled warnings
            if (!permissionGranted) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إذن الموقع مطلوب", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("يجب منح صلاحية الموقع لمعرفة إتجاه القبلة.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("منح الصلاحية", fontSize = 12.sp)
                        }
                    }
                }
            } else if (!isLocationEnabled) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("الموقع الجغرافي مغلق", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("يرجى تفعيل الموقع (GPS) لمعرفة إتجاه القبلة بشكل دقيق.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = onEnableLocation,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("تفعيل الموقع", fontSize = 12.sp)
                        }
                    }
                }
            } else if (!locationFetched) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "جاري تحديد الموقع...",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Detailed Cards Section (Qibla Angle + Phone Direction)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Qibla Angle Card
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${qiblaBearing.toInt()}°",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "زاويـة القبلة",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Phone Direction Card
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = null,
                            tint = Color(0xFF1CB0F6),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${displayDegree.toInt()}°",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = getDirectionName(displayDegree),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calibration & Sensor Info Card
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = Color(0xFF1CB0F6),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "معايرة دقة البوصلة",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "للحصول على أدق نتيجة، ابتعد عن المغناطيس والمعادن، وقم بتحريك الجهاز على شكل رقم 8.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onShowCalibration,
                        border = BorderStroke(1.dp, Color(0xFF1CB0F6).copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("إعادة المعايرة", fontSize = 12.sp, color = Color(0xFF1CB0F6))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun Kaaba3D(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val cx = width / 2f
        val cy = height / 2f + 10.dp.toPx() // Shift down slightly

        // 1. Draw soft white background glow
        drawCircle(
            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                center = Offset(cx, cy - 10.dp.toPx()),
                radius = width * 0.7f
            ),
            radius = width * 0.7f,
            center = Offset(cx, cy - 10.dp.toPx())
        )

        // Tall cube dimensions
        val hCube = height * 0.45f
        val wLeft = width * 0.32f
        val wRight = width * 0.32f
        val slant = height * 0.10f

        // 2. White Marble Base (Shazarwan)
        val wBaseLeft = wLeft * 1.08f
        val wBaseRight = wRight * 1.08f
        val hBase = 6.dp.toPx()

        // Base Top Points
        val bTopFront = Offset(cx, cy + hCube / 2f + hBase)
        val bTopLeft = Offset(cx - wBaseLeft, cy + hCube / 2f - slant + hBase)
        val bTopRight = Offset(cx + wBaseRight, cy + hCube / 2f - slant + hBase)
        val bTopBack = Offset(cx, cy + hCube / 2f - slant * 2f + hBase)

        // Base Bottom Points
        val bBottomFront = Offset(cx, cy + hCube / 2f + hBase * 2.5f)
        val bBottomLeft = Offset(cx - wBaseLeft, cy + hCube / 2f - slant + hBase * 2.5f)
        val bBottomRight = Offset(cx + wBaseRight, cy + hCube / 2f - slant + hBase * 2.5f)

        // Draw Base Left and Right Vertical sides
        val baseLeftPath = Path().apply {
            moveTo(bTopLeft.x, bTopLeft.y)
            lineTo(bTopFront.x, bTopFront.y)
            lineTo(bBottomFront.x, bBottomFront.y)
            lineTo(bBottomLeft.x, bBottomLeft.y)
            close()
        }
        val baseRightPath = Path().apply {
            moveTo(bTopFront.x, bTopFront.y)
            lineTo(bTopRight.x, bTopRight.y)
            lineTo(bBottomRight.x, bBottomRight.y)
            lineTo(bBottomFront.x, bBottomFront.y)
            close()
        }
        val baseTopPath = Path().apply {
            moveTo(bTopFront.x, bTopFront.y)
            lineTo(bTopLeft.x, bTopLeft.y)
            lineTo(bTopBack.x, bTopBack.y)
            lineTo(bTopRight.x, bTopRight.y)
            close()
        }

        drawPath(baseTopPath, Color(0xFFEBEBEB))
        drawPath(baseLeftPath, Color(0xFFCFCFCF))
        drawPath(baseRightPath, Color(0xFFDFDFDF))

        // 3. Main Kaaba Cube Body
        val topFront = Offset(cx, cy - hCube / 2f)
        val topLeft = Offset(cx - wLeft, cy - hCube / 2f - slant)
        val topRight = Offset(cx + wRight, cy - hCube / 2f - slant)
        val topBack = Offset(cx, cy - hCube / 2f - slant * 2f)

        val bottomFront = Offset(cx, cy + hCube / 2f)
        val bottomLeft = Offset(cx - wLeft, cy + hCube / 2f - slant)
        val bottomRight = Offset(cx + wRight, cy + hCube / 2f - slant)

        // Draw top face
        val topFacePath = Path().apply {
            moveTo(topFront.x, topFront.y)
            lineTo(topLeft.x, topLeft.y)
            lineTo(topBack.x, topBack.y)
            lineTo(topRight.x, topRight.y)
            close()
        }
        drawPath(topFacePath, Color(0xFF1E1E1E))

        // Draw left face (shadowed)
        val leftFacePath = Path().apply {
            moveTo(topLeft.x, topLeft.y)
            lineTo(topFront.x, topFront.y)
            lineTo(bottomFront.x, bottomFront.y)
            lineTo(bottomLeft.x, bottomLeft.y)
            close()
        }
        drawPath(leftFacePath, Color(0xFF121212))

        // Draw right face
        val rightFacePath = Path().apply {
            moveTo(topFront.x, topFront.y)
            lineTo(topRight.x, topRight.y)
            lineTo(bottomRight.x, bottomRight.y)
            lineTo(bottomFront.x, bottomFront.y)
            close()
        }
        drawPath(rightFacePath, Color(0xFF1B1B1B))

        // 4. Kiswa Golden Band (Wraps around the top part)
        val bandTopRatio = 0.12f
        val bandBottomRatio = 0.24f

        val kLeftTopL = Offset(topLeft.x, topLeft.y + hCube * bandTopRatio)
        val kLeftTopR = Offset(topFront.x, topFront.y + hCube * bandTopRatio)
        val kLeftBottomR = Offset(topFront.x, topFront.y + hCube * bandBottomRatio)
        val kLeftBottomL = Offset(topLeft.x, topLeft.y + hCube * bandBottomRatio)

        val kRightTopL = Offset(topFront.x, topFront.y + hCube * bandTopRatio)
        val kRightTopR = Offset(topRight.x, topRight.y + hCube * bandTopRatio)
        val kRightBottomR = Offset(topRight.x, topRight.y + hCube * bandBottomRatio)
        val kRightBottomL = Offset(topFront.x, topFront.y + hCube * bandBottomRatio)

        val leftBandPath = Path().apply {
            moveTo(kLeftTopL.x, kLeftTopL.y)
            lineTo(kLeftTopR.x, kLeftTopR.y)
            lineTo(kLeftBottomR.x, kLeftBottomR.y)
            lineTo(kLeftBottomL.x, kLeftBottomL.y)
            close()
        }
        val rightBandPath = Path().apply {
            moveTo(kRightTopL.x, kRightTopL.y)
            lineTo(kRightTopR.x, kRightTopR.y)
            lineTo(kRightBottomR.x, kRightBottomR.y)
            lineTo(kRightBottomL.x, kRightBottomL.y)
            close()
        }

        val goldColor = Color(0xFFFFD700)
        val goldShadow = Color(0xFFD4AF37)
        drawPath(leftBandPath, goldShadow)
        drawPath(rightBandPath, goldColor)

        // Draw vertical stripes inside the gold bands
        val stripesCount = 8
        for (i in 1..stripesCount) {
            val t = i.toFloat() / (stripesCount + 1)
            val pTop = Offset(
                kLeftTopL.x + (kLeftTopR.x - kLeftTopL.x) * t,
                kLeftTopL.y + (kLeftTopR.y - kLeftTopL.y) * t
            )
            val pBottom = Offset(
                kLeftBottomL.x + (kLeftBottomR.x - kLeftBottomL.x) * t,
                kLeftBottomL.y + (kLeftBottomR.y - kLeftBottomL.y) * t
            )
            drawLine(
                color = Color(0xFF8B7500),
                start = pTop,
                end = pBottom,
                strokeWidth = 1.5.dp.toPx()
            )
        }

        for (i in 1..stripesCount) {
            val t = i.toFloat() / (stripesCount + 1)
            val pTop = Offset(
                kRightTopL.x + (kRightTopR.x - kRightTopL.x) * t,
                kRightTopL.y + (kRightTopR.y - kRightTopL.y) * t
            )
            val pBottom = Offset(
                kRightBottomL.x + (kRightBottomR.x - kRightBottomL.x) * t,
                kRightBottomL.y + (kRightBottomR.y - kRightBottomL.y) * t
            )
            drawLine(
                color = Color(0xFF9B8510),
                start = pTop,
                end = pBottom,
                strokeWidth = 1.5.dp.toPx()
            )
        }

        // 5. Golden Door of Kaaba (Bab Al-Kaaba) on the right face
        val doorL = 0.20f
        val doorR = 0.52f
        val doorB = 0.12f
        val doorT = 0.62f

        fun mapRightFace(u: Float, v: Float): Offset {
            val x = topFront.x + u * wRight
            val yTop = topFront.y + u * (-slant)
            val yBottom = bottomFront.y + u * (-slant)
            val y = yBottom - v * hCube
            return Offset(x, y)
        }

        val doorBottomLeft = mapRightFace(doorL, doorB)
        val doorBottomRight = mapRightFace(doorR, doorB)
        val doorTopRight = mapRightFace(doorR, doorT)
        val doorTopLeft = mapRightFace(doorL, doorT)

        val doorPath = Path().apply {
            moveTo(doorBottomLeft.x, doorBottomLeft.y)
            lineTo(doorBottomRight.x, doorBottomRight.y)
            lineTo(doorTopRight.x, doorTopRight.y)
            lineTo(doorTopLeft.x, doorTopLeft.y)
            close()
        }

        drawPath(doorPath, Color(0xFFE5C158))
        drawPath(doorPath, Color(0xFF8B6508), style = Stroke(width = 1.5.dp.toPx()))

        val innerDoorPath = Path().apply {
            val dBL = mapRightFace(doorL + 0.05f, doorB + 0.05f)
            val dBR = mapRightFace(doorR - 0.05f, doorB + 0.05f)
            val dTR = mapRightFace(doorR - 0.05f, doorT - 0.05f)
            val dTL = mapRightFace(doorL + 0.05f, doorT - 0.05f)
            moveTo(dBL.x, dBL.y)
            lineTo(dBR.x, dBR.y)
            lineTo(dTR.x, dTR.y)
            lineTo(dTL.x, dTL.y)
            close()
        }
        drawPath(innerDoorPath, Color(0xFF8B6508), style = Stroke(width = 1.dp.toPx()))
    }
}

@Composable
fun CompassDial(
    azimuth: Float,
    qiblaBearing: Float,
    isReady: Boolean,
    isAligned: Boolean
) {
    val textMeasurer = rememberTextMeasurer()
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val padding = 8.dp.toPx()
            val radius = (size.width / 2) - padding
            
            drawCircle(color = Color(0xFF1E2638), radius = radius)
            
            // Draw a smooth green ring glow if aligned, otherwise normal dark ring
            if (isAligned) {
                drawCircle(
                    color = Color(0xFF4CAF50).copy(alpha = 0.25f),
                    radius = radius - 8.dp.toPx(),
                    style = Stroke(width = 14.dp.toPx())
                )
            } else {
                drawCircle(
                    color = Color(0xFF2C3549),
                    radius = radius - 8.dp.toPx(),
                    style = Stroke(width = 14.dp.toPx())
                )
            }
            
            // Inner circle shifts to elegant forest green when aligned, otherwise remains dark
            if (isAligned) {
                drawCircle(color = Color(0xFF0F3820), radius = radius - 18.dp.toPx())
            } else {
                drawCircle(color = Color(0xFF141924), radius = radius - 18.dp.toPx())
            }

            // Fixed top indicator
            drawLine(
                color = if (isAligned) Color(0xFF4CAF50) else Color.White,
                start = Offset(center.x, center.y - radius - 6.dp.toPx()),
                end = Offset(center.x, center.y - radius + 8.dp.toPx()),
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )
            
            rotate(-azimuth) {
                // Ticks
                for (i in 0 until 360 step 2) {
                    val isMajor = i % 30 == 0
                    val isMedium = i % 10 == 0
                    val lineLength = when {
                        isMajor -> 12.dp.toPx()
                        isMedium -> 8.dp.toPx()
                        else -> 4.dp.toPx()
                    }
                    val strokeWidth = if (isMajor) 1.8.dp.toPx() else 1.dp.toPx()
                    val lineColor = if (isMajor) Color.White else Color.White.copy(alpha = 0.5f)
                    
                    rotate(i.toFloat()) {
                        drawLine(
                            color = lineColor,
                            start = Offset(center.x, center.y - radius + 4.dp.toPx()),
                            end = Offset(center.x, center.y - radius + 4.dp.toPx() + lineLength),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }
                
                // Labels
                val labels = listOf("N", "E", "S", "W")
                val subLabels = listOf("NE", "SE", "SW", "NW")
                val styleMajor = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                val styleMinor = TextStyle(color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                
                for (i in 0 until 4) {
                    rotate(i * 90f) {
                        val textLayoutResult = textMeasurer.measure(labels[i], styleMajor)
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(center.x - textLayoutResult.size.width / 2, center.y - radius + 22.dp.toPx())
                        )
                    }
                    rotate(i * 90f + 45f) {
                        val textLayoutResult = textMeasurer.measure(subLabels[i], styleMinor)
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(center.x - textLayoutResult.size.width / 2, center.y - radius + 26.dp.toPx())
                        )
                    }
                }
                
                // Qibla Marker on the Dial
                if (isReady) {
                    rotate(qiblaBearing) {
                        val markerColor = if (isAligned) Color(0xFF4CAF50) else Color(0xFFFFD700)
                        drawCircle(
                            color = markerColor,
                            radius = 7.dp.toPx(),
                            center = Offset(center.x, center.y - radius + 32.dp.toPx())
                        )
                        drawLine(
                            color = markerColor,
                            start = Offset(center.x, center.y - radius + 39.dp.toPx()),
                            end = Offset(center.x, center.y - radius + 55.dp.toPx()),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
            
            // Fixed Pointer (always points UP to the Kaaba icon)
            if (isReady) {
                val pointerColor = if (isAligned) Color(0xFF4CAF50) else Color.White
                
                val needleWidth = 14.dp.toPx()
                val needlePath = Path().apply {
                    moveTo(center.x, center.y - radius + 50.dp.toPx()) // Tip
                    lineTo(center.x - needleWidth/2, center.y) // Left base
                    lineTo(center.x + needleWidth/2, center.y) // Right base
                    close()
                }
                drawPath(needlePath, pointerColor.copy(alpha = 0.9f))
                
                drawCircle(
                    color = pointerColor,
                    radius = 8.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = Color(0xFF1E2638),
                    radius = 3.dp.toPx(),
                    center = center
                )
            }
        }
    }
}
