package com.example.data.repository

import com.example.data.local.AyahEntity
import com.example.data.local.QuranDao
import com.example.data.local.SurahEntity
import com.example.data.remote.QuranApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap

class QuranRepository(
    private val dao: QuranDao,
    private val api: QuranApi
) {
    private val surahMemoryCache = ConcurrentHashMap<Int, List<AyahEntity>>()
    private val surahDetailsCache = ConcurrentHashMap<Int, SurahEntity>()

    fun getCachedAyahs(surahId: Int): List<AyahEntity>? = surahMemoryCache[surahId]
    fun getCachedSurahDetails(surahId: Int): SurahEntity? = surahDetailsCache[surahId]

    suspend fun preloadSurah(surahId: Int) {
        if (surahMemoryCache.containsKey(surahId)) return
        try {
            val dbAyahs = dao.getAyahsForSurahDirect(surahId)
            if (dbAyahs.isNotEmpty()) {
                surahMemoryCache[surahId] = dbAyahs
            } else {
                val response = api.getVersesByChapter(surahId)
                val entities = response.verses.map { verse ->
                    val ayahNum = verse.verse_key.split(":")[1].toInt()
                    AyahEntity(
                        surahId = surahId,
                        ayahNumber = ayahNum,
                        textUthmani = verse.text_uthmani
                    )
                }
                if (entities.isNotEmpty()) {
                    dao.insertAyahs(entities)
                    surahMemoryCache[surahId] = entities
                }
            }
            if (!surahDetailsCache.containsKey(surahId)) {
                val details = dao.getSurahByIdDirect(surahId)
                if (details != null) {
                    surahDetailsCache[surahId] = details
                }
            }
        } catch (e: Exception) {
            // Silently fail preload
        }
    }

    fun getAllSurahs(): Flow<List<SurahEntity>> = flow {
        val cached = dao.getAllSurahsDirect()
        if (cached.isNotEmpty()) {
            cached.forEach { surahDetailsCache[it.id] = it }
            emit(cached)
        } else {
            try {
                val response = api.getChapters()
                val entities = response.chapters.map {
                    SurahEntity(
                        id = it.id,
                        nameAr = it.name_arabic,
                        nameEn = it.name_simple,
                        revelationType = it.revelation_place,
                        ayahCount = it.verses_count
                    )
                }
                dao.insertSurahs(entities)
                entities.forEach { surahDetailsCache[it.id] = it }
                emit(entities)
            } catch (e: Exception) {
                // If offline and no cache, emit empty or handle error
                emit(emptyList())
            }
        }
        // Emit from DB to keep it reactive
        dao.getAllSurahs().collect {
            it.forEach { s -> surahDetailsCache[s.id] = s }
            emit(it)
        }
    }

    fun getSurah(surahId: Int): Flow<SurahEntity?> = flow {
        surahDetailsCache[surahId]?.let { emit(it) }
        val direct = dao.getSurahByIdDirect(surahId)
        if (direct != null) {
            surahDetailsCache[surahId] = direct
            emit(direct)
        }
        dao.getSurahById(surahId).collect {
            if (it != null) surahDetailsCache[it.id] = it
            emit(it)
        }
    }

    fun getAyahs(surahId: Int): Flow<List<AyahEntity>> = flow {
        val memCached = surahMemoryCache[surahId]
        if (memCached != null && memCached.isNotEmpty()) {
            emit(memCached)
        } else {
            val dbAyahs = dao.getAyahsForSurahDirect(surahId)
            if (dbAyahs.isNotEmpty()) {
                surahMemoryCache[surahId] = dbAyahs
                emit(dbAyahs)
            } else {
                try {
                    val response = api.getVersesByChapter(surahId)
                    val entities = response.verses.map { verse ->
                        val ayahNum = verse.verse_key.split(":")[1].toInt()
                        AyahEntity(
                            surahId = surahId,
                            ayahNumber = ayahNum,
                            textUthmani = verse.text_uthmani
                        )
                    }
                    if (entities.isNotEmpty()) {
                        dao.insertAyahs(entities)
                        surahMemoryCache[surahId] = entities
                        emit(entities)
                    } else {
                        emit(emptyList())
                    }
                } catch (e: Exception) {
                    emit(emptyList())
                }
            }
        }
        dao.getAyahsForSurah(surahId).collect {
            if (it.isNotEmpty()) {
                surahMemoryCache[surahId] = it
            }
            emit(it)
        }
    }

    suspend fun getAyahsDirect(surahId: Int): List<AyahEntity> {
        val memCached = surahMemoryCache[surahId]
        if (memCached != null && memCached.isNotEmpty()) {
            return memCached
        }
        val dbAyahs = dao.getAyahsForSurahDirect(surahId)
        if (dbAyahs.isNotEmpty()) {
            surahMemoryCache[surahId] = dbAyahs
            return dbAyahs
        }
        return try {
            val response = api.getVersesByChapter(surahId)
            val entities = response.verses.map { verse ->
                val ayahNum = verse.verse_key.split(":")[1].toInt()
                AyahEntity(
                    surahId = surahId,
                    ayahNumber = ayahNum,
                    textUthmani = verse.text_uthmani
                )
            }
            if (entities.isNotEmpty()) {
                dao.insertAyahs(entities)
                surahMemoryCache[surahId] = entities
            }
            entities
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun syncAllQuranData(onProgress: (Int) -> Unit) {
        if (dao.getSurahsCount() == 0) {
            try {
                val response = api.getChapters()
                val entities = response.chapters.map {
                    SurahEntity(
                        id = it.id,
                        nameAr = it.name_arabic,
                        nameEn = it.name_simple,
                        revelationType = it.revelation_place,
                        ayahCount = it.verses_count
                    )
                }
                dao.insertSurahs(entities)
            } catch (e: Exception) {
                // Return early if we can't even get chapters
                return
            }
        }

        // Loop over all 114 Surahs and download them to cache offline
        for (i in 1..114) {
            if (dao.getAyahsCountForSurah(i) == 0) {
                try {
                    val response = api.getVersesByChapter(i)
                    val entities = response.verses.map { verse ->
                        val ayahNum = verse.verse_key.split(":")[1].toInt()
                        AyahEntity(
                            surahId = i,
                            ayahNumber = ayahNum,
                            textUthmani = verse.text_uthmani
                        )
                    }
                    dao.insertAyahs(entities)
                } catch (e: Exception) {
                    // Silently fail and continue or retry later
                }
            }
            onProgress(i)
        }
    }

    suspend fun searchAyahs(query: String): List<AyahEntity> {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return emptyList()

        // 1. Try raw match
        val rawMatches = dao.searchAyahsRaw(trimmed)
        if (rawMatches.isNotEmpty()) {
            return rawMatches
        }

        // 2. Normalized match without diacritics
        val cleanQuery = removeArabicDiacritics(trimmed)
        if (cleanQuery.isBlank()) return emptyList()

        val allAyahs = dao.getAllAyahsForSearch()
        return allAyahs.filter { ayah ->
            removeArabicDiacritics(ayah.textUthmani).contains(cleanQuery, ignoreCase = true)
        }.take(50)
    }

    private fun removeArabicDiacritics(text: String): String {
        val diacritics = Regex("[\u064B-\u0652\u0670\u0653\u0654\u0655\u0610-\u061A\u06D6-\u06DC\u06DF-\u06E8\u06EA-\u06ED]")
        return text.replace(diacritics, "")
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ٱ', 'ا')
            .replace('ى', 'ي')
            .replace('ؤ', 'و')
            .replace('ئ', 'ي')
            .replace('ة', 'ه')
    }

    suspend fun getRandomAyah(): com.example.data.local.AyahEntity? {
        return dao.getRandomAyah()
    }

    suspend fun getRandomShortAyah(maxLength: Int = 100): com.example.data.local.AyahEntity? {
        return dao.getRandomShortAyah(maxLength)
    }

    suspend fun getSurahNameById(id: Int): String? {
        return dao.getSurahNameById(id)
    }

    suspend fun getAyahByNumber(surahId: Int, ayahNumber: Int): AyahEntity? {
        return dao.getAyahByNumber(surahId, ayahNumber)
    }

}
