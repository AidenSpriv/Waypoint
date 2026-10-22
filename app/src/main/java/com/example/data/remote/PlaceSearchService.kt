package com.example.data.remote

import android.util.Log
import com.example.data.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class PlaceSearchResult(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val subtitle: String,
    val latLng: LatLng,
    val category: String = "Place",
    val iconEmoji: String = "📍",
    val distanceMeters: Int? = null,
    val distanceDisplay: String? = null
)

class PlaceSearchService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "PlaceSearchService"
        private const val PHOTON_URL = "https://photon.komoot.io/api/"
        private const val NOMINATIM_URL = "https://nominatim.openstreetmap.org/search"
    }

    suspend fun searchPlaces(
        query: String,
        biasLat: Double = 25.2867,
        biasLng: Double = 51.5333
    ): List<PlaceSearchResult> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return@withContext emptyList()

        // 1. Try Photon (fast, typo-tolerant, geospatial bias)
        try {
            val photonResults = searchViaPhoton(trimmed, biasLat, biasLng)
            if (photonResults.isNotEmpty()) {
                return@withContext photonResults
            }
        } catch (e: Exception) {
            Log.w(TAG, "Photon search failed: ${e.message}, falling back to Nominatim", e)
        }

        // 2. Fallback to Nominatim
        try {
            val nominatimResults = searchViaNominatim(trimmed, biasLat, biasLng)
            if (nominatimResults.isNotEmpty()) {
                return@withContext nominatimResults
            }
        } catch (e: Exception) {
            Log.w(TAG, "Nominatim fallback failed: ${e.message}", e)
        }

        emptyList()
    }

    private fun searchViaPhoton(
        query: String,
        biasLat: Double,
        biasLng: Double
    ): List<PlaceSearchResult> {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = "$PHOTON_URL?q=$encodedQuery&lat=$biasLat&lon=$biasLng&limit=10"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "WaypointNavigation/1.0 (Doha Walking Navigator)")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return emptyList()
            val body = response.body?.string() ?: return emptyList()

            val json = JSONObject(body)
            val features = json.optJSONArray("features") ?: return emptyList()

            val results = mutableListOf<PlaceSearchResult>()
            for (i in 0 until features.length()) {
                val feature = features.getJSONObject(i)
                val properties = feature.optJSONObject("properties") ?: continue
                val geometry = feature.optJSONObject("geometry") ?: continue
                val coordinates = geometry.optJSONArray("coordinates") ?: continue

                if (coordinates.length() < 2) continue
                val lng = coordinates.getDouble(0)
                val lat = coordinates.getDouble(1)

                val name = properties.optString("name").ifBlank {
                    properties.optString("street", "Location")
                }

                val street = properties.optString("street")
                val district = properties.optString("district")
                val locality = properties.optString("locality")
                val city = properties.optString("city")
                val state = properties.optString("state")
                val country = properties.optString("country")

                val subtitleParts = listOf(street, locality, district, city, state, country)
                    .filter { it.isNotBlank() && it != name }
                    .distinct()

                val subtitle = if (subtitleParts.isNotEmpty()) {
                    subtitleParts.take(3).joinToString(" · ")
                } else {
                    "Coordinates: ${String.format("%.4f, %.4f", lat, lng)}"
                }

                val osmKey = properties.optString("osm_key", "")
                val osmValue = properties.optString("osm_value", "")
                val (category, icon) = mapOsmToCategory(osmKey, osmValue, name)

                val dist = calculateDistanceMeters(biasLat, biasLng, lat, lng)
                val distDisplay = formatDistance(dist)

                results.add(
                    PlaceSearchResult(
                        id = properties.optString("osm_id", UUID.randomUUID().toString()),
                        name = name,
                        subtitle = subtitle,
                        latLng = LatLng(lat, lng),
                        category = category,
                        iconEmoji = icon,
                        distanceMeters = dist,
                        distanceDisplay = distDisplay
                    )
                )
            }

            return results
        }
    }

    private fun searchViaNominatim(
        query: String,
        biasLat: Double,
        biasLng: Double
    ): List<PlaceSearchResult> {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = "$NOMINATIM_URL?q=$encodedQuery&format=json&limit=8&addressdetails=1"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "WaypointNavigation/1.0 (Doha Walking Navigator)")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return emptyList()
            val body = response.body?.string() ?: return emptyList()

            val jsonArray = org.json.JSONArray(body)
            val results = mutableListOf<PlaceSearchResult>()

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val lat = item.optDouble("lat", 0.0)
                val lon = item.optDouble("lon", 0.0)
                if (lat == 0.0 && lon == 0.0) continue

                val displayName = item.optString("display_name", "")
                val parts = displayName.split(", ")
                val name = parts.firstOrNull() ?: query
                val subtitle = parts.drop(1).take(3).joinToString(" · ")

                val type = item.optString("type", "")
                val cls = item.optString("class", "")
                val (category, icon) = mapOsmToCategory(cls, type, name)

                val dist = calculateDistanceMeters(biasLat, biasLng, lat, lon)
                val distDisplay = formatDistance(dist)

                results.add(
                    PlaceSearchResult(
                        id = item.optString("place_id", UUID.randomUUID().toString()),
                        name = name,
                        subtitle = subtitle,
                        latLng = LatLng(lat, lon),
                        category = category,
                        iconEmoji = icon,
                        distanceMeters = dist,
                        distanceDisplay = distDisplay
                    )
                )
            }

            return results
        }
    }

    private fun mapOsmToCategory(key: String, value: String, name: String): Pair<String, String> {
        val combined = "$key $value $name".lowercase()
        return when {
            combined.contains("metro") || combined.contains("subway") || combined.contains("station") || combined.contains("transit") ->
                "Transit" to "🚇"
            combined.contains("mall") || combined.contains("shop") || combined.contains("market") || combined.contains("souq") ->
                "Shopping & Souq" to "🛍️"
            combined.contains("museum") || combined.contains("gallery") || combined.contains("historic") || combined.contains("art") ->
                "Culture & Heritage" to "🏛️"
            combined.contains("mosque") || combined.contains("masjid") || combined.contains("prayer") ->
                "Place of Worship" to "🕌"
            combined.contains("park") || combined.contains("garden") || combined.contains("corniche") || combined.contains("beach") ->
                "Park & Promenade" to "🌳"
            combined.contains("hotel") || combined.contains("resort") ->
                "Hotel & Stay" to "🏨"
            combined.contains("hospital") || combined.contains("clinic") || combined.contains("pharmacy") || combined.contains("health") ->
                "Healthcare" to "🏥"
            combined.contains("restaurant") || combined.contains("cafe") || combined.contains("dining") || combined.contains("coffee") ->
                "Food & Cafe" to "☕"
            combined.contains("school") || combined.contains("university") || combined.contains("library") ->
                "Education & Library" to "🎓"
            combined.contains("airport") ->
                "Airport" to "✈️"
            else ->
                "Landmark" to "📍"
        }
    }

    private fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (earthRadius * c).toInt()
    }

    private fun formatDistance(meters: Int): String {
        return if (meters >= 1000) {
            String.format("%.1f km", meters / 1000.0)
        } else {
            "$meters m"
        }
    }
}
