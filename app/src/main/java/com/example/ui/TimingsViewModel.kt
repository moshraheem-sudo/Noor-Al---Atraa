package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AladhanResponse
import com.example.data.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

class TimingsViewModel : ViewModel() {
    private val _aladhanData = MutableStateFlow<AladhanResponse?>(null)
    val aladhanData: StateFlow<AladhanResponse?> = _aladhanData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchTimings()
    }

    fun fetchTimings(userLat: Double? = null, userLon: Double? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                var lat = userLat
                var lon = userLon
                if (lat == null || lon == null) {
                    val location = ApiClient.apiService.getLocation()
                    lat = location.latitude ?: 31.9961 // fallback to Najaf
                    lon = location.longitude ?: 44.3168
                }
                
                val response = ApiClient.apiService.getTimings(latitude = lat, longitude = lon)
                _aladhanData.value = response
            } catch (e: Exception) {
                Log.e("TimingsViewModel", "Error fetching data", e)
                _error.value = e.message ?: "حدث خطأ في الاتصال بالإنترنت"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
