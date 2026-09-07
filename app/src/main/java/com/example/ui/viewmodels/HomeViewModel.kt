package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AyahEntity
import com.example.data.local.SurahEntity
import com.example.data.repository.QuranRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: QuranRepository) : ViewModel() {

    private val _randomAyah = MutableStateFlow<AyahEntity?>(null)
    val randomAyah: StateFlow<AyahEntity?> = _randomAyah.asStateFlow()
    
    private val _randomAyahSurahName = MutableStateFlow("")
    val randomAyahSurahName: StateFlow<String> = _randomAyahSurahName.asStateFlow()

    private val _syncProgress = MutableStateFlow(0)
    val syncProgress: StateFlow<Int> = _syncProgress

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<AyahEntity>>(emptyList())
    val searchResults: StateFlow<List<AyahEntity>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _lastReadAyahText = MutableStateFlow<String?>(null)
    val lastReadAyahText: StateFlow<String?> = _lastReadAyahText.asStateFlow()

    fun loadLastReadAyahText(surahId: Int, ayahNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val ayah = repository.getAyahByNumber(surahId, ayahNumber)
            if (ayah != null) {
                _lastReadAyahText.value = ayah.textUthmani
            }
        }
    }

    init {
        // Start background sync
        viewModelScope.launch(Dispatchers.IO) {
            repository.syncAllQuranData { progress ->
                _syncProgress.value = progress
            }
        }
        startRandomAyahLoop()
    }
    
    private fun startRandomAyahLoop() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val ayah = repository.getRandomAyah()
                var delayTime = 6000L // 6 seconds by default
                
                if (ayah != null) {
                    _randomAyah.value = ayah
                    _randomAyahSurahName.value = repository.getSurahNameById(ayah.surahId) ?: ""
                    
                    // If the ayah is long (e.g. > 100 characters), give the user more time to read (10 seconds)
                    if (ayah.textUthmani.length > 100) {
                        delayTime = 10000L
                    }
                }
                
                kotlinx.coroutines.delay(delayTime)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }
        _isSearching.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val results = repository.searchAyahs(query)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    val surahs: StateFlow<List<SurahEntity>> = repository.getAllSurahs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    class Factory(private val repository: QuranRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
