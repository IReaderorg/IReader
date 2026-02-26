package ireader.data.characterart

import io.ktor.client.*
import io.ktor.client.plugins.timeout
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.random.Random

/**
 * Qwen-Image-Fast generator using multimodalart-qwen-image-fast.hf.space
 * 
 * Fast image generation (~15 seconds) using Qwen-Image-Lightning model
 * FREE - No API key required (Hugging Face Spaces)
 */
class QwenFastImageGenerator(
    private val httpClient: HttpClient
) : ImageGeneratorProvider {
    
    override val name: String = "Qwen Fast"
    override val requiresApiKey: Boolean = false
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    companion object {
        private const val BASE_URL = "https://multimodalart-qwen-image-fast.hf.space"
        private const val CALL_ENDPOINT = "$BASE_URL/gradio_api/call/infer"
        private const val RESULT_ENDPOINT = "$BASE_URL/gradio_api/call/infer"
        
        // Aspect ratio options
        private val ASPECT_RATIOS = mapOf(
            "1:1" to "1:1",
            "16:9" to "16:9",
            "9:16" to "9:16",
            "4:3" to "4:3",
            "3:4" to "3:4"
        )
    }
    
    override suspend fun generateImage(
        apiKey: String,
        prompt: String,
        characterName: String,
        bookTitle: String,
        style: String
    ): Result<GeneratedImage> {
        return try {
            // Build enhanced prompt
            val enhancedPrompt = buildPrompt(prompt, characterName, bookTitle, style)
            
            // Generate random seed
            val seed = Random.nextInt(0, Int.MAX_VALUE)
            
            // Step 1: POST request to initiate generation
            val eventId = initiateGeneration(enhancedPrompt, seed)
            
            // Step 2: GET request to fetch result (with polling)
            val imageBytes = fetchResult(eventId)
            
            Result.success(
                GeneratedImage(
                    bytes = imageBytes,
                    mimeType = "image/webp",
                    prompt = enhancedPrompt
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Qwen Fast generation failed: ${e.message}", e))
        }
    }
    
    private suspend fun initiateGeneration(prompt: String, seed: Int): String {
        // API parameters: [prompt, seed, randomize_seed, aspect_ratio, num_inference_steps, guidance_scale, enable_safety_checker]
        val requestBody = JsonObject(
            mapOf(
                "data" to JsonArray(
                    listOf(
                        JsonPrimitive(prompt),           // [0] prompt
                        JsonPrimitive(seed),             // [1] seed
                        JsonPrimitive(false),            // [2] randomize_seed (false to use our seed)
                        JsonPrimitive("1:1"),            // [3] aspect_ratio
                        JsonPrimitive(1),                // [4] num_inference_steps (1 for fast)
                        JsonPrimitive(4),                // [5] guidance_scale
                        JsonPrimitive(true)              // [6] enable_safety_checker
                    )
                )
            )
        )
        
        val response: HttpResponse = httpClient.post(CALL_ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(JsonObject.serializer(), requestBody))
            timeout {
                requestTimeoutMillis = 60000 // 60 seconds
                connectTimeoutMillis = 30000  // 30 seconds to connect
                socketTimeoutMillis = 60000   // 60 seconds socket timeout
            }
        }
        
        if (!response.status.isSuccess()) {
            throw Exception("Failed to initiate generation: ${response.status}")
        }
        
        val responseText = response.bodyAsText()
        val responseJson = json.parseToJsonElement(responseText).jsonObject
        
        // Extract event_id from response
        val eventId = responseJson["event_id"]?.jsonPrimitive?.content
            ?: throw Exception("No event_id in response")
        
        return eventId
    }
    
    private suspend fun fetchResult(eventId: String): ByteArray {
        val resultUrl = "$RESULT_ENDPOINT/$eventId"
        
        // Poll for result (max 90 seconds, check every 3 seconds)
        // Qwen Fast typically takes 12-15 seconds, but we allow extra time for network delays
        var attempts = 0
        val maxAttempts = 30 // 30 attempts × 3 seconds = 90 seconds total
        
        while (attempts < maxAttempts) {
            attempts++
            
            try {
                val response: HttpResponse = httpClient.get(resultUrl) {
                    headers {
                        append(HttpHeaders.Accept, "text/event-stream")
                    }
                    timeout {
                        requestTimeoutMillis = 90000 // 90 seconds per request
                        connectTimeoutMillis = 30000  // 30 seconds to connect
                        socketTimeoutMillis = 90000   // 90 seconds socket timeout
                    }
                }
                
                if (!response.status.isSuccess()) {
                    println("Qwen Fast: Attempt $attempts - HTTP ${response.status.value}")
                    kotlinx.coroutines.delay(3000)
                    continue
                }
                
                val responseText = response.bodyAsText()
                println("Qwen Fast: Attempt $attempts - Response length: ${responseText.length}")
                
                // Parse SSE (Server-Sent Events) response
                // Look for "data: " lines containing the result
                val dataLines = responseText.lines()
                    .filter { it.startsWith("data: ") }
                    .map { it.removePrefix("data: ") }
                
                if (dataLines.isEmpty()) {
                    println("Qwen Fast: No data lines found in response")
                    kotlinx.coroutines.delay(3000)
                    continue
                }
                
                for (dataLine in dataLines) {
                    if (dataLine.isBlank() || dataLine == "[DONE]") continue
                    
                    try {
                        val eventData = json.parseToJsonElement(dataLine)
                        
                        // Handle both JsonObject and JsonArray responses
                        val data: JsonArray? = when (eventData) {
                            is JsonObject -> {
                                println("Qwen Fast: Event data keys: ${eventData.keys}")
                                // Check if generation is complete
                                if (eventData.containsKey("output")) {
                                    val output = eventData["output"]?.jsonObject
                                    output?.get("data")?.jsonArray
                                } else {
                                    null
                                }
                            }
                            is JsonArray -> {
                                println("Qwen Fast: Received direct array response")
                                eventData
                            }
                            else -> null
                        }
                        
                        if (data != null && data.size > 0) {
                            println("Qwen Fast: Found data array, size: ${data.size}")
                            
                            val firstElement = data[0]
                            
                            // Try different response formats
                            val imageUrl = when {
                                // Format 1: data[0][0].url (nested array)
                                firstElement is JsonArray && firstElement.size > 0 -> {
                                    val imageInfo = firstElement[0].jsonObject
                                    imageInfo["url"]?.jsonPrimitive?.content
                                }
                                // Format 2: data[0].url (direct object)
                                firstElement is JsonObject -> {
                                    firstElement["url"]?.jsonPrimitive?.content
                                }
                                else -> null
                            }
                            
                            if (imageUrl != null) {
                                println("Qwen Fast: Found image URL: $imageUrl")
                                // Download the image
                                return downloadImage(imageUrl)
                            } else {
                                println("Qwen Fast: No URL found in data element")
                            }
                        }
                    } catch (e: Exception) {
                        println("Qwen Fast: Error parsing data line: ${e.message}")
                        // Continue to next line if parsing fails
                        continue
                    }
                }
                
                // If we got here, result not ready yet
                println("Qwen Fast: Result not ready, waiting...")
                kotlinx.coroutines.delay(3000)
                
            } catch (e: Exception) {
                println("Qwen Fast: Attempt $attempts failed: ${e.message}")
                if (attempts >= maxAttempts) {
                    throw Exception("Failed to fetch result after $maxAttempts attempts: ${e.message}")
                }
                kotlinx.coroutines.delay(3000)
            }
        }
        
        throw Exception("Image generation timed out after ${maxAttempts * 3} seconds")
    }
    
    private suspend fun downloadImage(imageUrl: String): ByteArray {
        // Handle relative URLs
        val fullUrl = if (imageUrl.startsWith("http")) {
            imageUrl
        } else {
            "$BASE_URL$imageUrl"
        }
        
        val response: HttpResponse = httpClient.get(fullUrl) {
            timeout {
                requestTimeoutMillis = 60000 // 60 seconds
                connectTimeoutMillis = 30000  // 30 seconds to connect
                socketTimeoutMillis = 60000   // 60 seconds socket timeout
            }
        }
        
        if (!response.status.isSuccess()) {
            throw Exception("Failed to download image: ${response.status}")
        }
        
        return response.readBytes()
    }
    
    private fun buildPrompt(
        prompt: String,
        characterName: String,
        bookTitle: String,
        style: String
    ): String {
        return buildString {
            if (prompt.isNotBlank()) {
                append(prompt)
            } else {
                append("$characterName from $bookTitle")
            }
            
            if (style.isNotBlank() && !contains(style, ignoreCase = true)) {
                append(", $style")
            }
            
            // Add quality enhancers for better results
            append(", high quality, detailed, professional")
        }
    }
    
    override suspend fun getAvailableModels(apiKey: String): Result<List<ImageModel>> {
        // Qwen Fast uses a single model (Qwen-Image-Lightning)
        return Result.success(
            listOf(
                ImageModel(
                    id = "qwen-image-lightning",
                    displayName = "Qwen Image Lightning",
                    description = "Fast image generation (~15 seconds)"
                )
            )
        )
    }
}
