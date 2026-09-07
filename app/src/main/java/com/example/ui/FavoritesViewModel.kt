package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FavoriteItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {
    private val favoriteDao = AppDatabase.getInstance(application).favoriteDao()

    val allFavorites: Flow<List<FavoriteItem>> = favoriteDao.getAllFavorites()

    fun isFavorite(title: String): Flow<Boolean> = favoriteDao.isFavorite(title)

    fun toggleFavorite(title: String, content: String, category: String, onCompleted: (Boolean) -> Unit) {
        viewModelScope.launch {
            val exists = favoriteDao.isFavorite(title).first()
            if (exists) {
                favoriteDao.deleteFavoriteByTitle(title)
                onCompleted(false) // removed
            } else {
                favoriteDao.insertFavorite(FavoriteItem(title = title, content = content, category = category))
                onCompleted(true) // added
            }
        }
    }
}
