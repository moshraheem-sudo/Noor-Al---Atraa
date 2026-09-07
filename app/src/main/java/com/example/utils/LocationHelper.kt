package com.example.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.model.CityLocation
import com.example.data.model.PredefinedCities
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume

object LocationHelper {

    private const val TAG = "LocationHelper"

    fun hasLocationPermission(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    fun isLocationServiceEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            val isGpsEnabled = try { locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) } catch (e: Exception) { false }
            val isNetworkEnabled = try { locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) } catch (e: Exception) { false }
            isGpsEnabled || isNetworkEnabled
        }
    }

    fun openLocationSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to open settings", e2)
            }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getDeviceLocation(context: Context, forceRefresh: Boolean = false): CityLocation? = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("location_cache", Context.MODE_PRIVATE)
        val lastUpdateTime = prefs.getLong("last_update_time", 0L)
        val cachedGpsJson = prefs.getString("last_gps_city", null)
        val now = System.currentTimeMillis()

        // --- BATTERY OPTIMIZATION ---
        // If not forced, check if we have a very fresh cache (e.g. < 2 hours). If so, skip GPS entirely.
        if (!forceRefresh && cachedGpsJson != null && (now - lastUpdateTime) < 2 * 60 * 60 * 1000L) {
            try {
                val json = JSONObject(cachedGpsJson)
                return@withContext CityLocation(
                    id = json.getString("id"),
                    nameAr = json.getString("nameAr"),
                    nameEn = json.getString("nameEn"),
                    countryAr = json.getString("countryAr"),
                    latitude = json.getDouble("latitude"),
                    longitude = json.getDouble("longitude"),
                    timezone = json.getString("timezone"),
                    isHolyCity = json.optBoolean("isHolyCity", false),
                    province = json.optString("province", "")
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse cached location", e)
            }
        }

        var detectedLat: Double? = null
        var detectedLon: Double? = null
        var sourceLabel = "GPS"

        // 1. Try GPS & Network Providers if permission is granted
        if (hasLocationPermission(context)) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager != null) {
                var bestLocation: Location? = null

                // Check last known locations
                try {
                    val providers = locationManager.getProviders(true)
                    for (provider in providers) {
                        val l = locationManager.getLastKnownLocation(provider) ?: continue
                        // Filter out extremely stale passive locations (> 4 hours)
                        if (now - l.time > 4 * 60 * 60 * 1000L) continue

                        if (bestLocation == null || (l.accuracy > 0 && l.accuracy < bestLocation.accuracy)) {
                            bestLocation = l
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading last known location", e)
                }

                // If no recent last known location, request active fast update
                if (bestLocation == null || forceRefresh) {
                    bestLocation = requestLocationUpdateFast(locationManager)
                }

                if (bestLocation != null) {
                    // Check if coordinates are standard default US emulator (Mountain View ~37.42, -122.08)
                    if (!isDefaultEmulatorCoordinates(bestLocation.latitude, bestLocation.longitude)) {
                        detectedLat = bestLocation.latitude
                        detectedLon = bestLocation.longitude
                        sourceLabel = "GPS"
                    }
                }
            }
        }

        // 2. If GPS was unavailable or in emulator with US default coords, fallback to IP Geolocation
        if (detectedLat == null || detectedLon == null) {
            val ipLocation = fetchIpLocation()
            if (ipLocation != null) {
                detectedLat = ipLocation.first
                detectedLon = ipLocation.second
                sourceLabel = "IP"
            }
        }

        // 3. If still null, return closest match or default city
        if (detectedLat == null || detectedLon == null) {
            return@withContext PredefinedCities.defaultCity
        }

        // 4. Check Cache against new coordinates
        if (cachedGpsJson != null) {
            try {
                val json = JSONObject(cachedGpsJson)
                val cachedCity = CityLocation(
                    id = json.getString("id"),
                    nameAr = json.getString("nameAr"),
                    nameEn = json.getString("nameEn"),
                    countryAr = json.getString("countryAr"),
                    latitude = json.getDouble("latitude"),
                    longitude = json.getDouble("longitude"),
                    timezone = json.getString("timezone"),
                    isHolyCity = json.optBoolean("isHolyCity", false),
                    province = json.optString("province", "")
                )
                val cacheDist = distanceBetweenKm(detectedLat, detectedLon, cachedCity.latitude, cachedCity.longitude)
                // Battery Optimization: if user is still in the same 10km area, reuse cache to avoid reverse geocoding
                // and reduce data consumption
                if (cacheDist <= 10.0) {
                    prefs.edit().putLong("last_update_time", now).apply()
                    return@withContext cachedCity
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse cached location", e)
            }
        }

        // Resolve city name: try reverse geocode first for exact local Arabic name
        val reverseName = reverseGeocode(context, detectedLat, detectedLon)
        val nearestCity = findNearestPredefinedCity(detectedLat, detectedLon)
        val distKm = distanceBetweenKm(detectedLat, detectedLon, nearestCity.latitude, nearestCity.longitude)

        val cityName = when {
            reverseName.isNotBlank() && reverseName != "موقعك الحالي" -> reverseName
            distKm <= 40.0 -> nearestCity.nameAr
            nearestCity.countryAr == "العراق" && distKm <= 75.0 -> nearestCity.nameAr
            else -> nearestCity.nameAr
        }

        val timezoneStr = calculateTimezone(detectedLon)

        val resultCity = CityLocation(
            id = "gps_${System.currentTimeMillis()}",
            nameAr = cityName,
            nameEn = if (nearestCity.nameEn.isNotEmpty()) nearestCity.nameEn else "Current Location",
            countryAr = if (nearestCity.countryAr.isNotEmpty()) nearestCity.countryAr else "العراق",
            latitude = Math.round(detectedLat * 10000.0) / 10000.0,
            longitude = Math.round(detectedLon * 10000.0) / 10000.0,
            timezone = timezoneStr,
            isHolyCity = nearestCity.isHolyCity,
            province = nearestCity.province
        )

        // Save to cache
        val resultJson = JSONObject().apply {
            put("id", resultCity.id)
            put("nameAr", resultCity.nameAr)
            put("nameEn", resultCity.nameEn)
            put("countryAr", resultCity.countryAr)
            put("latitude", resultCity.latitude)
            put("longitude", resultCity.longitude)
            put("timezone", resultCity.timezone)
            put("isHolyCity", resultCity.isHolyCity)
            put("province", resultCity.province)
        }
        prefs.edit()
            .putString("last_gps_city", resultJson.toString())
            .putLong("last_update_time", now)
            .apply()
        
        return@withContext resultCity
    }

    private fun isDefaultEmulatorCoordinates(latitude: Double, longitude: Double): Boolean {
        // Android SDK default virtual location (Mountain View, California)
        return (latitude in 37.40..37.45 && longitude in -122.10..-122.05)
    }

    private fun fetchIpLocation(): Pair<Double, Double>? {
        return try {
            val url = URL("http://ip-api.com/json/")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")

            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val json = JSONObject(response.toString())
                if (json.optString("status") == "success") {
                    val lat = json.optDouble("lat", Double.NaN)
                    val lon = json.optDouble("lon", Double.NaN)
                    if (!lat.isNaN() && !lon.isNaN()) {
                        Pair(lat, lon)
                    } else null
                } else null
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch IP location", e)
            null
        }
    }

    private fun distanceBetweenKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return 6371.0 * c
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestLocationUpdateFast(locationManager: LocationManager): Location? {
        return withTimeoutOrNull(4000L) {
            suspendCancellableCoroutine { continuation ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(loc: Location) {
                        try {
                            locationManager.removeUpdates(this)
                        } catch (_: Exception) {}
                        if (continuation.isActive) {
                            continuation.resume(loc)
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }

                var requested = false
                val preferredProviders = listOf(
                    LocationManager.GPS_PROVIDER,
                    LocationManager.NETWORK_PROVIDER,
                    LocationManager.PASSIVE_PROVIDER
                )

                for (provider in preferredProviders) {
                    if (locationManager.isProviderEnabled(provider)) {
                        try {
                            locationManager.requestLocationUpdates(
                                provider,
                                0L,
                                0f,
                                listener,
                                Looper.getMainLooper()
                            )
                            requested = true
                        } catch (_: Exception) {}
                    }
                }

                if (!requested) {
                    if (continuation.isActive) continuation.resume(null)
                }

                continuation.invokeOnCancellation {
                    try {
                        locationManager.removeUpdates(listener)
                    } catch (_: Exception) {}
                }
            }
        }
    }

    private fun reverseGeocode(context: Context, latitude: Double, longitude: Double): String {
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale("ar"))
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val city = addr.locality
                        ?: addr.subAdminArea
                        ?: addr.adminArea
                        ?: addr.featureName
                    if (!city.isNullOrBlank()) return city
                }
            }
        } catch (_: Exception) {}

        val nearest = findNearestPredefinedCity(latitude, longitude)
        return nearest.nameAr
    }

    fun findNearestPredefinedCity(latitude: Double, longitude: Double): CityLocation {
        return PredefinedCities.list.minByOrNull { city ->
            val dLat = city.latitude - latitude
            val dLon = city.longitude - longitude
            dLat * dLat + dLon * dLon
        } ?: PredefinedCities.defaultCity
    }

    private fun calculateTimezone(longitude: Double): String {
        val tz = TimeZone.getDefault()
        val now = System.currentTimeMillis()
        val offsetMillis = tz.getOffset(now)
        val offsetHours = offsetMillis.toDouble() / (1000 * 60 * 60)

        val sign = if (offsetHours >= 0) "+" else "-"
        val absHours = Math.abs(offsetHours)
        val intHours = absHours.toInt()
        val remainder = absHours - intHours

        return if (remainder == 0.0) {
            "$sign$intHours"
        } else if (remainder in 0.4..0.6) {
            "$sign$intHours.5"
        } else {
            val estTz = Math.round(longitude / 15.0).toInt()
            if (estTz >= 0) "+$estTz" else "$estTz"
        }
    }
}
