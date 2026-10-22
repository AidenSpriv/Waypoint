package com.example.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AiAnalysisResult
import com.example.data.model.BarrierCategory
import com.example.data.model.ReviewPriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiVisionService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "GeminiVisionService"
        private const val MODEL_NAME = "gemini-3.5-flash"
    }

    suspend fun analyzeReport(
        context: Context,
        description: String,
        imageUri: Uri?
    ): AiAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }

        // If no valid key is provided or default placeholder is used, return honest labeled demo fallback
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.i(TAG, "No Gemini API key configured. Using deterministic demo classifier.")
            return@withContext runDeterministicDemoClassifier(description, isOfflineDemo = true)
        }

        try {
            val base64Image = imageUri?.let { uri ->
                loadAndCompressBitmap(context, uri)
            }

            val systemInstruction = """
                You are an objective, civic-focused Pedestrian Infrastructure Assistant for WAYPOINT.
                Analyze the user's description and optional photo of a pedestrian obstacle or barrier.
                
                Rules:
                1. NEVER certify a sidewalk as unconditionally safe or wheelchair accessible from an image alone.
                2. Pick the most accurate category from: BLOCKED_SIDEWALK, DAMAGED_PAVEMENT, MISSING_CURB_RAMP, UNCLEAR_CROSSING, LACK_OF_SHADE, CONSTRUCTION_DETOUR.
                3. Provide a concise, neutral 1-sentence summary.
                4. Explicitly state uncertainties (e.g. curb height cannot be measured from photo, temporary vs permanent, lighting conditions).
                5. Suggest a review priority: LOW, MEDIUM, HIGH, URGENT with a 1-sentence reason.
                
                Format your response as strict valid JSON:
                {
                  "category": "BLOCKED_SIDEWALK",
                  "summary": "Brief neutral summary",
                  "uncertainty": "Caveat or observation limitation",
                  "priority": "HIGH",
                  "rationale": "Reason for priority"
                }
            """.trimIndent()

            val contentsArray = JSONArray()
            val partsArray = JSONArray()

            val promptText = if (description.isNotBlank()) {
                "User Report Description: $description"
            } else {
                "User attached an image of a pedestrian barrier. Classify and describe."
            }

            partsArray.put(JSONObject().put("text", promptText))

            if (base64Image != null) {
                val inlineData = JSONObject()
                    .put("mimeType", "image/jpeg")
                    .put("data", base64Image)
                partsArray.put(JSONObject().put("inlineData", inlineData))
            }

            contentsArray.put(JSONObject().put("parts", partsArray))

            val rootJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemInstruction))))
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.2)
                })
            }

            val requestUrl = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"
            val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(requestUrl)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val parsedResult = parseGeminiResponse(responseBody)
                if (parsedResult != null) {
                    return@withContext parsedResult
                }
            } else {
                Log.w(TAG, "Gemini API error ${response.code}: ${response.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception calling Gemini API", e)
        }

        // On failure or exception, fall back gracefully
        return@withContext runDeterministicDemoClassifier(description, isOfflineDemo = true)
    }

    private fun parseGeminiResponse(rawJson: String): AiAnalysisResult? {
        try {
            val root = JSONObject(rawJson)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null
            val text = parts.getJSONObject(0).optString("text", "")
            if (text.isBlank()) return null

            val resultJson = JSONObject(text.trim())
            val categoryStr = resultJson.optString("category", "BLOCKED_SIDEWALK")
            val summary = resultJson.optString("summary", "Pedestrian barrier report submitted.")
            val uncertainty = resultJson.optString("uncertainty", "Requires in-person surveyor verification to confirm measurements.")
            val priorityStr = resultJson.optString("priority", "MEDIUM")
            val rationale = resultJson.optString("rationale", "Standard neighborhood review queue.")

            val category = try { BarrierCategory.valueOf(categoryStr) } catch (e: Exception) { BarrierCategory.BLOCKED_SIDEWALK }
            val priority = try { ReviewPriority.valueOf(priorityStr) } catch (e: Exception) { ReviewPriority.MEDIUM }

            return AiAnalysisResult(
                suggestedCategory = category,
                neutralSummary = summary,
                uncertaintyNotice = uncertainty,
                suggestedPriority = priority,
                priorityRationale = rationale,
                isDemoFallback = false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Gemini structured JSON", e)
            return null
        }
    }

    private fun runDeterministicDemoClassifier(text: String, isOfflineDemo: Boolean): AiAnalysisResult {
        val lower = text.lowercase()
        val (category, priority, rationale) = when {
            lower.contains("curb") || lower.contains("ramp") || lower.contains("wheelchair") || lower.contains("step") ->
                Triple(
                    BarrierCategory.MISSING_CURB_RAMP,
                    ReviewPriority.HIGH,
                    "Missing curb ramps present an impassable barrier for wheeled mobility and strollers."
                )
            lower.contains("block") || lower.contains("fence") || lower.contains("generator") || lower.contains("scaffold") || lower.contains("hoarding") ->
                Triple(
                    BarrierCategory.BLOCKED_SIDEWALK,
                    ReviewPriority.HIGH,
                    "Sidewalk obstruction forces pedestrians to detour into vehicular travel lanes."
                )
            lower.contains("crack") || lower.contains("broken") || lower.contains("pothole") || lower.contains("cobble") || lower.contains("tile") ->
                Triple(
                    BarrierCategory.DAMAGED_PAVEMENT,
                    ReviewPriority.MEDIUM,
                    "Damaged pavement creates a trip hazard and uneven walking surface."
                )
            lower.contains("cross") || lower.contains("zebra") || lower.contains("signal") || lower.contains("marking") ->
                Triple(
                    BarrierCategory.UNCLEAR_CROSSING,
                    ReviewPriority.HIGH,
                    "Unclear or faded crossing markers heighten conflict between pedestrians and motorists."
                )
            lower.contains("shade") || lower.contains("sun") || lower.contains("heat") || lower.contains("hot") || lower.contains("tree") ->
                Triple(
                    BarrierCategory.LACK_OF_SHADE,
                    ReviewPriority.LOW,
                    "Extreme solar exposure impacts thermal comfort and duration of walking journeys."
                )
            lower.contains("detour") || lower.contains("construction") || lower.contains("roadwork") ->
                Triple(
                    BarrierCategory.CONSTRUCTION_DETOUR,
                    ReviewPriority.URGENT,
                    "Active construction detour requires prompt route coordination and signage verification."
                )
            else ->
                Triple(
                    BarrierCategory.BLOCKED_SIDEWALK,
                    ReviewPriority.MEDIUM,
                    "General pedestrian condition report categorized for municipal or survey triage."
                )
        }

        val summary = if (text.isNotBlank()) {
            "Reported $category: ${text.take(80)}${if (text.length > 80) "..." else ""}"
        } else {
            "Reported ${category.displayName} in pilot neighborhood."
        }

        val uncertainty = "Classification based on deterministic heuristic rules. Physical verification required to confirm obstruction dimensions and municipal jurisdiction."

        return AiAnalysisResult(
            suggestedCategory = category,
            neutralSummary = summary,
            uncertaintyNotice = uncertainty,
            suggestedPriority = priority,
            priorityRationale = rationale,
            isDemoFallback = isOfflineDemo
        )
    }

    private fun loadAndCompressBitmap(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) return null

            // Scale down if large (max 1024px)
            val maxDim = Math.max(bitmap.width, bitmap.height)
            val scaled = if (maxDim > 1024) {
                val scale = 1024f / maxDim
                Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
            } else {
                bitmap
            }

            val outputStream = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load/compress bitmap", e)
            null
        }
    }
}
