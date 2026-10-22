package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.BarrierReport
import com.example.data.model.ChatMessage
import com.example.data.model.Facility
import com.example.data.model.MessageSender
import com.example.data.model.PilotArea
import com.example.data.model.WalkingRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OpenRouterAiService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "OpenRouterAiService"
        private const val OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions"
        private const val PRIMARY_MODEL = "openai/gpt-4o-mini"
        private const val SECONDARY_MODEL = "meta-llama/llama-3.3-70b-instruct"
    }

    suspend fun askAssistant(
        userPrompt: String,
        history: List<ChatMessage>,
        pilotArea: PilotArea,
        activeRoute: WalkingRoute?,
        originName: String,
        destinationName: String,
        facilities: List<Facility>,
        barriers: List<BarrierReport>,
        isWalkingActive: Boolean
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            val keyFromConfig = BuildConfig::class.java.getField("OPENROUTER_API_KEY").get(null) as? String
            if (!keyFromConfig.isNullOrBlank()) {
                keyFromConfig
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }

        if (apiKey.isNullOrBlank()) {
            Log.w(TAG, "OPENROUTER_API_KEY is not configured; using local fallback without calling OpenRouter")
            return@withContext generateSmartFallback(userPrompt, activeRoute, pilotArea, facilities, barriers)
        }

        val systemPrompt = buildSystemPrompt(
            pilotArea = pilotArea,
            activeRoute = activeRoute,
            originName = originName,
            destinationName = destinationName,
            facilities = facilities,
            barriers = barriers,
            isWalkingActive = isWalkingActive
        )

        // Try primary model first, then fallback
        try {
            return@withContext callOpenRouter(apiKey, PRIMARY_MODEL, systemPrompt, history, userPrompt)
        } catch (e: Exception) {
            Log.w(TAG, "Primary model ($PRIMARY_MODEL) failed, trying secondary ($SECONDARY_MODEL): ${e.message}")
            try {
                return@withContext callOpenRouter(apiKey, SECONDARY_MODEL, systemPrompt, history, userPrompt)
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "All models failed on OpenRouter: ${fallbackEx.message}", fallbackEx)
                return@withContext generateSmartFallback(userPrompt, activeRoute, pilotArea, facilities, barriers)
            }
        }
    }

    private fun callOpenRouter(
        apiKey: String,
        modelName: String,
        systemPrompt: String,
        history: List<ChatMessage>,
        userPrompt: String
    ): String {
        val messagesArray = JSONArray()

        // 1. System instruction
        val systemObj = JSONObject().apply {
            put("role", "system")
            put("content", systemPrompt)
        }
        messagesArray.put(systemObj)

        // 2. Recent conversation history (last 8 messages for context)
        val recentHistory = history.takeLast(8)
        for (msg in recentHistory) {
            val role = when (msg.sender) {
                MessageSender.USER -> "user"
                MessageSender.AI -> "assistant"
                MessageSender.SYSTEM -> "system"
            }
            val msgObj = JSONObject().apply {
                put("role", role)
                put("content", msg.text)
            }
            messagesArray.put(msgObj)
        }

        // 3. Current user message
        val currentObj = JSONObject().apply {
            put("role", "user")
            put("content", userPrompt)
        }
        messagesArray.put(currentObj)

        val requestPayload = JSONObject().apply {
            put("model", modelName)
            put("messages", messagesArray)
            put("temperature", 0.7)
            put("max_tokens", 800)
        }

        val requestBody = requestPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(OPENROUTER_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "https://waypoint.doha.app")
            .addHeader("X-Title", "WAYPOINT Walking Navigation")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("OpenRouter returned HTTP ${response.code}: $responseBody")
            }

            val json = JSONObject(responseBody)
            val choices = json.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val firstChoice = choices.getJSONObject(0)
                val message = firstChoice.optJSONObject("message")
                val text = message?.optString("content").orEmpty().trim()
                if (text.isNotEmpty()) {
                    return text
                }
            }

            throw IllegalStateException("Empty or malformed response from OpenRouter: $responseBody")
        }
    }

    private fun buildSystemPrompt(
        pilotArea: PilotArea,
        activeRoute: WalkingRoute?,
        originName: String,
        destinationName: String,
        facilities: List<Facility>,
        barriers: List<BarrierReport>,
        isWalkingActive: Boolean
    ): String {
        val routeSection = if (activeRoute != null) {
            """
            === ACTIVE USER ROUTE ===
            - Route Name: ${activeRoute.name}
            - Origin: ${originName.ifBlank { "Current Location" }}
            - Destination: ${destinationName.ifBlank { "Selected Destination" }}
            - Walking Distance: ${activeRoute.distanceDisplay} (${activeRoute.distanceMeters} meters)
            - Walking Duration: ${activeRoute.walkingTimeDisplay}
            - Shade & Shelter Coverage: ${activeRoute.shadeShelterCoveragePercent}%
            - Confidence Score: ${activeRoute.confidenceScore}%
            - Thermal & Corridor Notes: ${activeRoute.comparisonExplanation.ifBlank { "Routes through covered sikkats and shaded arcades." }}
            - Active Walking Mode: ${if (isWalkingActive) "USER IS CURRENTLY WALKING THIS ROUTE" else "User is planning / previewing route"}
            """.trimIndent()
        } else {
            """
            === ACTIVE USER ROUTE ===
            No specific route is selected at this moment. The user is currently browsing the map and facilities in ${pilotArea.name}.
            """.trimIndent()
        }

        val facilitiesSection = facilities.take(8).joinToString("\n") { fac ->
            "- ${fac.name} [${fac.category.displayName}]: Location (${fac.latitude}, ${fac.longitude}). Status: ${fac.verificationStatus.displayName}. Notes: ${fac.notes} (${fac.caveats})"
        }

        val barriersSection = if (barriers.isNotEmpty()) {
            barriers.take(5).joinToString("\n") { bar ->
                "- ${bar.category.displayName} at (${bar.latitude}, ${bar.longitude}): ${bar.description} (Priority: ${bar.priority.displayName}, Status: ${bar.status.displayName})"
            }
        } else {
            "No active community barriers reported currently in this zone."
        }

        return """
            You are Wayfinder, the intelligent pedestrian navigation AI assistant for WAYPOINT in Doha, Qatar.
            
            WAYPOINT MISSION & PRODUCT KNOWLEDGE:
            - WAYPOINT is a specialized pedestrian navigation application designed specifically for walking in extreme desert heat (initially launched in Doha, Qatar).
            - It helps people walk comfortably by routing through shaded sikkats (traditional pedestrian alleys), covered building arcades, air-conditioned cut-throughs, and outdoor evaporative mist corridors.
            - It provides pause points (free chilled water bottle refilling stations, AC rest pods, prayer rooms, metro links).
            - It allows crowd-sourced reporting of accessibility barriers, broken sidewalks, missing shade, and construction detours.
            
            CURRENT APPLICATION ENVIRONMENT:
            - Active District: ${pilotArea.name} (Center: ${pilotArea.centerLat}, ${pilotArea.centerLng})
            - District Description: ${pilotArea.description}
            
            $routeSection
            
            NEARBY FACILITIES & PAUSE POINTS IN ${pilotArea.name}:
            $facilitiesSection
            
            ACTIVE COMMUNITY BARRIERS & OBSTACLES:
            $barriersSection
            
            HOW TO RESPOND:
            1. Be warm, concise, authoritative, and helpful. Keep responses easily readable on a mobile screen.
            2. When the user asks about their route, use the exact route metrics (distance, minutes, shade %) provided above.
            3. When asked for drinking water, restrooms, or AC rest, cite the specific nearby facilities listed above.
            4. When asked about obstacles or safety, reference the community barriers listed above.
            5. Use bullet points and bold highlights for legibility.
            6. If the user asks about the app itself or how WAYPOINT works, explain its thermal navigation algorithms and civic accessibility focus.
        """.trimIndent()
    }

    private fun generateSmartFallback(
        userPrompt: String,
        activeRoute: WalkingRoute?,
        pilotArea: PilotArea,
        facilities: List<Facility>,
        barriers: List<BarrierReport>
    ): String {
        val lower = userPrompt.lowercase()
        return when {
            lower.contains("shade") || lower.contains("sun") || lower.contains("hot") -> {
                if (activeRoute != null) {
                    "🌿 **Route Shade Telemetry**\n\nYour selected path (**${activeRoute.name}**) features **${activeRoute.shadeShelterCoveragePercent}% shade & shelter coverage** through covered sikkat corridors and arcades. Direct sun exposure is minimized."
                } else {
                    "🌿 **Shade in ${pilotArea.name}**\n\nWAYPOINT routes prioritize Doha's covered sikkats and building overhangs, keeping you in shaded walkways up to 88% of the time. Select a destination on the map to calculate your shaded corridor."
                }
            }
            lower.contains("water") || lower.contains("drink") || lower.contains("hydrate") -> {
                val waterPoint = facilities.firstOrNull { it.name.contains("Water", ignoreCase = true) }
                if (waterPoint != null) {
                    "💧 **Nearest Drinking Water**\n\nFound **${waterPoint.name}** in ${pilotArea.name}. It provides free chilled filtered water for bottle refilling."
                } else {
                    "💧 Free chilled water refill stations are marked along Msheireb Downtown pedestrian corridors. Look for the blue water droplet icons on your map."
                }
            }
            lower.contains("barrier") || lower.contains("obstacle") || lower.contains("construction") -> {
                if (barriers.isNotEmpty()) {
                    val b = barriers.first()
                    "⚠️ **Active Obstacle in Area**\n\n${b.category.displayName} reported: ${b.description}. WAYPOINT routing steers around this spot."
                } else {
                    "✅ No severe barriers reported along the primary shaded walking arteries right now."
                }
            }
            else -> {
                "🌿 **Wayfinder Navigator**\n\nI'm connected to your live walk in **${pilotArea.name}**.\n\n" +
                (if (activeRoute != null) "• **Active Route:** ${activeRoute.name} (${activeRoute.distanceDisplay}, ${activeRoute.walkingTimeDisplay}, ${activeRoute.shadeShelterCoveragePercent}% shaded)\n" else "• Ready to guide your walk through shaded corridors.\n") +
                "• ${facilities.size} pause points (water, AC, transit) available nearby.\n\nFeel free to ask about shade coverage, water refill points, or upcoming turns!"
            }
        }
    }
}
