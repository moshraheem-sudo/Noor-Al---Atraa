package com.example.data.remote

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChaptersResponse(
    val chapters: List<ChapterDto>
)

@JsonClass(generateAdapter = true)
data class ChapterDto(
    val id: Int,
    val name_arabic: String,
    val name_simple: String,
    val revelation_place: String,
    val verses_count: Int
)

@JsonClass(generateAdapter = true)
data class VersesResponse(
    val verses: List<VerseDto>
)

@JsonClass(generateAdapter = true)
data class VerseDto(
    val id: Int,
    val verse_key: String, // e.g., "1:1"
    val text_uthmani: String
)

interface QuranApi {
    @GET("chapters?language=ar")
    suspend fun getChapters(): ChaptersResponse

    @GET("quran/verses/uthmani")
    suspend fun getVersesByChapter(@Query("chapter_number") chapterNumber: Int): VersesResponse
}
