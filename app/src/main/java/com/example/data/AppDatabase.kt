package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(item: NotificationItem)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()
    
    @Query("DELETE FROM notifications")
    suspend fun deleteAll()
}

@Entity(tableName = "favorites")
data class FavoriteItem(
    @PrimaryKey val title: String,
    val content: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteItem>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE title = :title LIMIT 1)")
    fun isFavorite(title: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(item: FavoriteItem)

    @Query("DELETE FROM favorites WHERE title = :title")
    suspend fun deleteFavoriteByTitle(title: String)
}

@Entity(tableName = "hijri_date_record")
data class HijriDateRecord(
    @PrimaryKey val id: Int = 1,
    val day: Int,
    val month: Int,
    val year: Int,
    val gregorianDateStr: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "estimated"
)

@Dao
interface HijriDateRecordDao {
    @Query("SELECT * FROM hijri_date_record WHERE id = 1")
    suspend fun getRecord(): HijriDateRecord?

    @Query("SELECT * FROM hijri_date_record WHERE id = 1")
    fun getRecordFlow(): kotlinx.coroutines.flow.Flow<HijriDateRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: HijriDateRecord)
}

@Database(entities = [FavoriteItem::class, NotificationItem::class, HijriDateRecord::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun notificationDao(): NotificationDao
    abstract fun hijriDateRecordDao(): HijriDateRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ahl_al_bayt_db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
