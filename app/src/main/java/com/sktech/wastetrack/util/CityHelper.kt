package com.sktech.wastetrack.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.InputStreamReader

object CityHelper {
    private const val TAG = "CityHelper"
    private var citiesCache: List<String> = emptyList()

    suspend fun loadCities(context: Context) {
        if (citiesCache.isNotEmpty()) return

        withContext(Dispatchers.IO) {
            try {
                context.assets.open("indian_cities.json").use { inputStream ->
                    InputStreamReader(inputStream, "UTF-8").use { isr ->
                        android.util.JsonReader(isr).use { reader ->
                            val citiesList = mutableListOf<String>()
                            reader.beginArray()
                            while (reader.hasNext()) {
                                reader.beginObject()
                                while (reader.hasNext()) {
                                    val field = reader.nextName()
                                    if (field == "name") {
                                        citiesList.add(reader.nextString())
                                    } else {
                                        reader.skipValue()
                                    }
                                }
                                reader.endObject()
                            }
                            reader.endArray()
                            citiesCache = citiesList.distinct().sorted()
                            Log.d(TAG, "Loaded ${citiesCache.size} Indian cities from assets (Streaming).")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load cities from assets", e)
                citiesCache = emptyList()
            }
        }
    }

    fun getSuggestions(query: String): List<String> {
        if (query.isBlank()) return emptyList()
        val lowerQuery = query.lowercase()
        return citiesCache.filter { it.lowercase().startsWith(lowerQuery) }.take(5)
    }

    fun isStandardCity(cityName: String): Boolean {
        return citiesCache.any { it.equals(cityName, ignoreCase = true) }
    }
}
