package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class HijriRemoteData(
    val hijriDay: Int,
    val hijriMonth: Int,
    val hijriYear: Int,
    val gregorianAnchorDate: String,
    val status: String
)

object HijriRemoteSource {
    private const val HIJRI_JSON_URL = "https://raw.githubusercontent.com/moshraheem-sudo/hijri/main/hijri.json"

    suspend fun fetchHijriData(): HijriRemoteData? = withContext(Dispatchers.IO) {
        try {
            var urlStr = HIJRI_JSON_URL
            var connection = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                instanceFollowRedirects = true
                useCaches = false
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate")
                setRequestProperty("Pragma", "no-cache")
                setRequestProperty("Accept", "application/json, text/plain, */*")
            }

            var responseCode = connection.responseCode
            if (responseCode in 300..399) {
                val redirectUrl = connection.getHeaderField("Location")
                if (!redirectUrl.isNullOrEmpty()) {
                    connection.disconnect()
                    connection = (URL(redirectUrl).openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        useCaches = false
                        connectTimeout = 15000
                        readTimeout = 15000
                        setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                        setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate")
                    }
                    responseCode = connection.responseCode
                }
            }

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()

                val jsonObj = JSONObject(jsonString)

                return@withContext HijriRemoteData(
                    hijriDay = jsonObj.getInt("hijri_day"),
                    hijriMonth = jsonObj.getInt("hijri_month"),
                    hijriYear = jsonObj.getInt("hijri_year"),
                    gregorianAnchorDate = jsonObj.getString("gregorian_anchor_date"),
                    status = jsonObj.optString("status", "observed")
                )
            } else {
                connection.disconnect()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
}

