package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "surahs")
data class SurahEntity(
    @PrimaryKey val id: Int,
    val nameAr: String,
    val nameEn: String,
    val revelationType: String,
    val ayahCount: Int
)

@Entity(
    tableName = "ayahs",
    foreignKeys = [
        ForeignKey(
            entity = SurahEntity::class,
            parentColumns = ["id"],
            childColumns = ["surahId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["surahId"])]
)
data class AyahEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val surahId: Int,
    val ayahNumber: Int,
    val textUthmani: String
)

@Dao
interface QuranDao {
    @Query("SELECT * FROM surahs ORDER BY id ASC")
    fun getAllSurahs(): Flow<List<SurahEntity>>

    @Query("SELECT * FROM surahs ORDER BY id ASC")
    suspend fun getAllSurahsDirect(): List<SurahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurahs(surahs: List<SurahEntity>)

    @Query("SELECT * FROM ayahs WHERE surahId = :surahId ORDER BY ayahNumber ASC")
    fun getAyahsForSurah(surahId: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs WHERE surahId = :surahId ORDER BY ayahNumber ASC")
    suspend fun getAyahsForSurahDirect(surahId: Int): List<AyahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<AyahEntity>)

    @Query("SELECT * FROM surahs WHERE id = :id LIMIT 1")
    fun getSurahById(id: Int): Flow<SurahEntity?>

    @Query("SELECT * FROM surahs WHERE id = :id LIMIT 1")
    suspend fun getSurahByIdDirect(id: Int): SurahEntity?

    @Query("SELECT COUNT(*) FROM surahs")
    suspend fun getSurahsCount(): Int
    

    @Query("SELECT COUNT(*) FROM ayahs WHERE surahId = :surahId")
    suspend fun getAyahsCountForSurah(surahId: Int): Int

    @Query("SELECT * FROM ayahs ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomAyah(): AyahEntity?

    @Query("SELECT * FROM ayahs WHERE LENGTH(textUthmani) < :maxLength AND LENGTH(textUthmani) > 15 ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomShortAyah(maxLength: Int = 100): AyahEntity?

    @Query("SELECT nameAr FROM surahs WHERE id = :id LIMIT 1")
    suspend fun getSurahNameById(id: Int): String?

    @Query("SELECT * FROM ayahs WHERE surahId = :surahId AND ayahNumber = :ayahNumber LIMIT 1")
    suspend fun getAyahByNumber(surahId: Int, ayahNumber: Int): AyahEntity?



    @Query("SELECT * FROM ayahs WHERE textUthmani LIKE '%' || :query || '%' ORDER BY surahId ASC, ayahNumber ASC LIMIT 100")
    suspend fun searchAyahsRaw(query: String): List<AyahEntity>

    @Query("SELECT * FROM ayahs ORDER BY surahId ASC, ayahNumber ASC")
    suspend fun getAllAyahsForSearch(): List<AyahEntity>
}

@Database(entities = [SurahEntity::class, AyahEntity::class, com.example.khatma.KhatmaProgress::class], version = 2, exportSchema = false)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao
    abstract fun khatmaDao(): com.example.khatma.KhatmaDao
}
