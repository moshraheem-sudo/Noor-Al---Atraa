package com.example.khatma

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * سجل تقدم القراءة لجزء واحد من أصل 30.
 * juzNumber هو المفتاح الأساسي (1..30) — دائماً 30 سجل موجودة مسبقاً (نضيفها عند إنشاء القاعدة).
 */
@Entity(tableName = "khatma_progress")
data class KhatmaProgress(
    @PrimaryKey val juzNumber: Int,
    val lastReadPage: Int = 0,      // 0 = لم يبدأ القراءة بعد
    val progressPercent: Int = 0,   // 0..100
    val isCompleted: Boolean = false,
    val lastUpdatedAt: Long = System.currentTimeMillis()
)

@Dao
interface KhatmaDao {

    @Query("SELECT * FROM khatma_progress ORDER BY juzNumber ASC")
    fun observeAllJuz(): Flow<List<KhatmaProgress>>

    @Query("SELECT * FROM khatma_progress WHERE juzNumber = :juz")
    suspend fun getJuz(juz: Int): KhatmaProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: KhatmaProgress)

    @Query("UPDATE khatma_progress SET lastReadPage = 0, progressPercent = 0, isCompleted = 0")
    suspend fun resetAll() // لبدء ختمة جديدة من الصفر

    @Query("UPDATE khatma_progress SET lastReadPage = 0, progressPercent = 0, isCompleted = 0 WHERE juzNumber = :juz")
    suspend fun resetJuz(juz: Int) // إعادة تصفير جزء معين

    @Query("SELECT juzNumber FROM khatma_progress WHERE isCompleted = 0 ORDER BY juzNumber ASC LIMIT 1")
    suspend fun getFirstIncompleteJuz(): Int?
}
