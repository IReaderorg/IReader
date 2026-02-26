package ireader.domain.usecases.translate

import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import ireader.core.http.HttpClients
import ireader.core.log.Log
import ireader.domain.data.engines.ContentType as TranslationContentType
import ireader.domain.data.engines.ToneType
import ireader.domain.data.engines.TranslateEngine
import ireader.domain.data.engines.TranslationContext
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * NvidiaTranslateEngine
 * 
 * Uses NVIDIA NIM (NVIDIA Inference Microservices) API to access multiple AI models for translation.
 * NVIDIA NIM provides access to various open-source and NVIDIA-optimized models through a unified API.
 * 
 * Features:
 * - Dynamic model fetching from NVIDIA API
 * - Support for multiple AI models (Llama, Mistral, Gemma, etc.)
 * - NVIDIA-optimized inference with fast response times
 * - Enterprise-grade reliability
 * 
 * @see https://build.nvidia.com/docs
 */
class NvidiaTranslateEngine(
    private val client: HttpClients,
    private val readerPreferences: ReaderPreferences,
) : TranslateEngine() {

    override val id: Long = NVIDIA
    override val engineName: String = "NVIDIA NIM"
    override val supportsAI: Boolean = true
    override val supportsContextAwareTranslation: Boolean = true
    override val supportsStylePreservation: Boolean = true
    override val requiresApiKey: Boolean = true
    
    // NVIDIA - reduced chunk size for better reliability
    // Many models have token limits, and 4000 chars is safer
    override val maxCharsPerRequest: Int = 4000
    
    // Rate limit varies by model, 2 seconds is safe
    override val rateLimitDelayMs: Long = 2000L
    
    override val isOffline: Boolean = false
    
    /**
     * Generate content using NVIDIA NIM API
     */
    override suspend fun generateContent(
        systemPrompt: String,
        userPrompt: String,
        temperature: Float,
        maxTokens: Int
    ): Result<String> {
        val apiKey = readerPreferences.nvidiaApiKey().get()
        if (apiKey.isBlank()) {
            return Result.failure(Exception("NVIDIA API key not configured"))
        }
        
        val selectedModel = readerPreferences.nvidiaModel().get().ifBlank { defaultModel }
        
        return try {
            val response = client.default.post("$baseUrl/chat/completions") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $apiKey")
                    append("Accept", "application/json")
                }
                contentType(ContentType.Application.Json)
                timeout {
                    requestTimeoutMillis = 120000
                    connectTimeoutMillis = 30000
                }
                setBody(NvidiaRequest(
                    model = selectedModel,
                    messages = listOf(
                        Message(role = "system", content = systemPrompt),
                        Message(role = "user", content = userPrompt)
                    ),
                    temperature = temperature.toDouble(),
                    max_tokens = maxTokens,
                    top_p = 1.0,
                    stream = false
                ))
            }
            
            if (response.status.value !in 200..299) {
                return Result.failure(Exception("NVIDIA API error: HTTP ${response.status.value}"))
            }
            
            val result = response.body<NvidiaResponse>()
            val generatedText = result.choices?.firstOrNull()?.message?.content
            
            if (generatedText.isNullOrBlank()) {
                Result.failure(Exception("Empty response from NVIDIA API"))
            } else {
                Result.success(generatedText.trim())
            }
        } catch (e: Exception) {
            Result.failure(Exception("NVIDIA API error: ${e.message}"))
        }
    }
    
    // NVIDIA NIM API base URL
    private val baseUrl = "https://integrate.api.nvidia.com/v1"
    
    // Default model if none selected - using a good translation model
    private val defaultModel = "meta/llama-3.1-8b-instruct"
    
    // Cached available models
    private var cachedModels: List<Pair<String, String>>? = null
    
    /**
     * Available models for NVIDIA NIM translation
     * Format: Pair<model_id, display_name>
     * 
     * These are curated models good for translation tasks.
     * Use fetchAvailableModels() to get the full dynamic list.
     */
    val availableModels: List<Pair<String, String>> = listOf(
        "meta/llama-3.1-8b-instruct" to "Llama 3.1 8B",
        "meta/llama-3.1-70b-instruct" to "Llama 3.1 70B",
        "meta/llama-3.1-405b-instruct" to "Llama 3.1 405B",
        "meta/llama-3.2-1b-instruct" to "Llama 3.2 1B",
        "meta/llama-3.2-3b-instruct" to "Llama 3.2 3B",
        "meta/llama-3.2-11b-vision-instruct" to "Llama 3.2 11B Vision",
        "meta/llama-3.2-90b-vision-instruct" to "Llama 3.2 90B Vision",
        "mistralai/mistral-large" to "Mistral Large",
        "mistralai/mixtral-8x7b-instruct" to "Mixtral 8x7B",
        "mistralai/mistral-7b-instruct" to "Mistral 7B",
        "google/gemma-2-9b-it" to "Gemma 2 9B",
        "google/gemma-2-27b-it" to "Gemma 2 27B",
        "microsoft/phi-3-medium-128k-instruct" to "Phi-3 Medium",
        "microsoft/phi-3-mini-128k-instruct" to "Phi-3 Mini",
        "nvidia/nemotron-4-340b-instruct" to "Nemotron 4 340B",
        "qwen/qwen2.5-7b-instruct" to "Qwen 2.5 7B",
        "qwen/qwen2.5-72b-instruct" to "Qwen 2.5 72B",
        "qwen/qwen2-7b-instruct" to "Qwen 2 7B",
        "deepseek-ai/deepseek-coder" to "DeepSeek Coder",
        "moonshotai/kimi-k2.5" to "Kimi K2.5",
    )
    
    override val supportedLanguages: List<Pair<String, String>> = listOf(
        "auto" to "Auto-detect",
        "af" to "Afrikaans",
        "sq" to "Albanian",
        "am" to "Amharic",
        "ar" to "Arabic",
        "hy" to "Armenian",
        "az" to "Azerbaijani",
        "bn" to "Bengali",
        "bs" to "Bosnian",
        "bg" to "Bulgarian",
        "ca" to "Catalan",
        "zh" to "Chinese",
        "hr" to "Croatian",
        "cs" to "Czech",
        "da" to "Danish",
        "nl" to "Dutch",
        "en" to "English",
        "et" to "Estonian",
        "fa" to "Farsi/Persian",
        "tl" to "Filipino",
        "fi" to "Finnish",
        "fr" to "French",
        "ka" to "Georgian",
        "de" to "German",
        "el" to "Greek",
        "gu" to "Gujarati",
        "ht" to "Haitian Creole",
        "ha" to "Hausa",
        "he" to "Hebrew",
        "hi" to "Hindi",
        "hu" to "Hungarian",
        "is" to "Icelandic",
        "id" to "Indonesian",
        "ga" to "Irish",
        "it" to "Italian",
        "ja" to "Japanese",
        "kn" to "Kannada",
        "kk" to "Kazakh",
        "ko" to "Korean",
        "lv" to "Latvian",
        "lt" to "Lithuanian",
        "mk" to "Macedonian",
        "ms" to "Malay",
        "ml" to "Malayalam",
        "mt" to "Maltese",
        "mr" to "Marathi",
        "mn" to "Mongolian",
        "ne" to "Nepali",
        "no" to "Norwegian",
        "ps" to "Pashto",
        "pl" to "Polish",
        "pt" to "Portuguese",
        "pa" to "Punjabi",
        "ro" to "Romanian",
        "ru" to "Russian",
        "sr" to "Serbian",
        "si" to "Sinhala",
        "sk" to "Slovak",
        "sl" to "Slovenian",
        "so" to "Somali",
        "es" to "Spanish",
        "sw" to "Swahili",
        "sv" to "Swedish",
        "tg" to "Tajik",
        "ta" to "Tamil",
        "te" to "Telugu",
        "th" to "Thai",
        "tr" to "Turkish",
        "uk" to "Ukrainian",
        "ur" to "Urdu",
        "uz" to "Uzbek",
        "vi" to "Vietnamese",
        "cy" to "Welsh",
        "yi" to "Yiddish",
        "yo" to "Yoruba",
        "zu" to "Zulu"
    )

    override suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onProgress: (Int) -> Unit,
        onSuccess: (List<String>) -> Unit,
        onError: (UiText) -> Unit
    ) {
        translateWithContext(
            texts, 
            source, 
            target, 
            TranslationContext(), 
            onProgress,
            onSuccess, 
            onError
        )
    }

    override suspend fun translateWithContext(
        texts: List<String>,
        source: String,
        target: String,
        context: TranslationContext,
        onProgress: (Int) -> Unit,
        onSuccess: (List<String>) -> Unit,
        onError: (UiText) -> Unit
    ) {
        if (texts.isEmpty()) {
            onError(UiText.MStringResource(Res.string.no_text_to_translate))
            return
        }
        
        val apiKey = readerPreferences.nvidiaApiKey().get()
        
        if (apiKey.isBlank()) {
            onError(UiText.MStringResource(Res.string.nvidia_api_key_not_set))
            return
        }
        
        val selectedModel = readerPreferences.nvidiaModel().get().ifBlank { defaultModel }
        
        try {
            onProgress(0)
            
            // Combine all paragraphs into a single text with markers
            val combinedText = texts.joinToString("\n---PARAGRAPH_BREAK---\n")
            val sourceLanguage = if (source == "auto") "the source language" else getLanguageName(source)
            val targetLanguage = getLanguageName(target)
            
            onProgress(20)
            val prompt = buildPrompt(combinedText, sourceLanguage, targetLanguage, context)
            
            onProgress(40)
            
            Log.debug { "NVIDIA NIM: Starting translation with model $selectedModel" }
            
            val response = client.default.post("$baseUrl/chat/completions") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $apiKey")
                    append("Accept", "application/json")
                }
                contentType(ContentType.Application.Json)
                timeout {
                    requestTimeoutMillis = 120000 // 2 minutes
                    connectTimeoutMillis = 30000
                }
                setBody(NvidiaRequest(
                    model = selectedModel,
                    messages = listOf(
                        Message(role = "system", content = getSystemPrompt()),
                        Message(role = "user", content = prompt)
                    ),
                    temperature = 0.3,
                    max_tokens = 16384,
                    top_p = 1.0,
                    stream = false
                ))
            }
            
            // Handle HTTP errors
            when (response.status.value) {
                401 -> {
                    Log.error { "NVIDIA API error: HTTP 401 - Invalid API key" }
                    onError(UiText.MStringResource(Res.string.nvidia_api_key_invalid))
                    return
                }
                402 -> {
                    Log.error { "NVIDIA API error: HTTP 402 - Insufficient credits" }
                    onError(UiText.MStringResource(Res.string.nvidia_insufficient_credits))
                    return
                }
                429 -> {
                    Log.error { "NVIDIA API error: HTTP 429 - Rate limited" }
                    onError(UiText.MStringResource(Res.string.api_rate_limit_exceeded))
                    return
                }
            }
            
            onProgress(80)
            val result = response.body<NvidiaResponse>()
            
            Log.debug { "NVIDIA API response received: ${result.id}" }
            
            if (result.choices != null && result.choices.isNotEmpty()) {
                val choice = result.choices[0]
                val messageContent = choice.message?.content
                
                if (!messageContent.isNullOrBlank()) {
                    val translatedText = messageContent.trim()
                    // Split the response back into individual paragraphs
                    val splitTexts = translatedText.split("\n---PARAGRAPH_BREAK---\n")
                    
                    // Ensure we have the right number of paragraphs to match input
                    val finalTexts = if (splitTexts.size == texts.size) {
                        splitTexts
                    } else {
                        adjustParagraphCount(splitTexts, texts)
                    }
                    
                    onProgress(100)
                    onSuccess(finalTexts)
                } else {
                    Log.error { "NVIDIA API returned empty message content" }
                    onError(UiText.MStringResource(Res.string.empty_response))
                }
            } else {
                Log.error { "NVIDIA API returned empty choices array" }
                onError(UiText.MStringResource(Res.string.empty_response))
            }
        } catch (e: Exception) {
            Log.error { "NVIDIA translation error: ${e.message}" }
            
            val errorMessage = when {
                e.message?.contains("401") == true || 
                e.message?.contains("unauthorized") == true ||
                e.message?.contains("invalid_api_key") == true -> 
                    UiText.MStringResource(Res.string.nvidia_api_key_invalid)
                
                e.message?.contains("429") == true || 
                e.message?.contains("rate limit") == true ->
                    UiText.MStringResource(Res.string.api_rate_limit_exceeded)
                    
                e.message?.contains("402") == true || 
                e.message?.contains("insufficient") == true ->
                    UiText.MStringResource(Res.string.nvidia_insufficient_credits)
                    
                e.message?.contains("timeout") == true ->
                    UiText.DynamicString("Request timed out. Please try again.")
                    
                else -> UiText.ExceptionString(e)
            }
            
            onProgress(0)
            onError(errorMessage)
        }
    }
    
    /**
     * Fetch available models from NVIDIA NIM API dynamically
     * Returns a list of model ID to display name pairs
     */
    suspend fun fetchAvailableModels(): Result<List<Pair<String, String>>> {
        val apiKey = readerPreferences.nvidiaApiKey().get()
        if (apiKey.isBlank()) {
            return Result.failure(Exception("NVIDIA API key not set"))
        }
        
        return try {
            withContext(Dispatchers.IO) {
                val response = client.default.get("$baseUrl/models") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $apiKey")
                    }
                    timeout {
                        requestTimeoutMillis = 30000
                    }
                }
                
                if (response.status.value != 200) {
                    return@withContext Result.failure(Exception("Failed to fetch models: HTTP ${response.status.value}"))
                }
                
                val modelsResponse = response.body<NvidiaModelsResponse>()
                
                val models = modelsResponse.data
                    .filter { it.id != null }
                    .sortedBy { it.id }
                    .map { model ->
                        val displayName = model.id?.substringAfterLast("/") ?: model.id ?: "Unknown"
                        (model.id ?: "") to displayName
                    }
                
                cachedModels = models
                Log.debug { "NVIDIA NIM: Fetched ${models.size} models" }
                Result.success(models)
            }
        } catch (e: Exception) {
            Log.error { "NVIDIA NIM: Failed to fetch models" }
            Result.failure(e)
        }
    }
    
    /**
     * Get cached models or return static list if not cached
     */
    fun getCachedModels(): List<Pair<String, String>> {
        return cachedModels ?: availableModels
    }
    
    private fun getSystemPrompt(): String {
        return """You are a professional translator with expertise in multiple languages and literature. 
Your task is to translate text accurately while preserving the original meaning, style, and tone.
Maintain paragraph structure by keeping the ---PARAGRAPH_BREAK--- markers between paragraphs.
Do not add any explanations, notes, or commentary - only provide the translation."""
    }
    
    private fun buildPrompt(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        context: TranslationContext
    ): String {
        val contentTypeInstruction = when (context.contentType) {
            TranslationContentType.LITERARY -> "This is literary content. Preserve the literary style, metaphors, and narrative flow."
            TranslationContentType.TECHNICAL -> "This is technical content. Maintain precise terminology and clarity."
            TranslationContentType.CONVERSATION -> "This is conversational content. Keep it natural and flowing as spoken language."
            TranslationContentType.POETRY -> "This is poetic content. Preserve rhythm and poetic devices where possible."
            TranslationContentType.ACADEMIC -> "This is academic content. Maintain formal language and precise terminology."
            else -> "Translate accurately while maintaining the original meaning."
        }
        
        val toneInstruction = when (context.toneType) {
            ToneType.FORMAL -> "Use formal language appropriate for professional or academic contexts."
            ToneType.CASUAL -> "Use casual, everyday language as if speaking to a friend."
            ToneType.PROFESSIONAL -> "Use professional language suitable for business contexts."
            ToneType.HUMOROUS -> "Maintain any humor or light tone in the original where appropriate."
            else -> "Use a neutral tone that's appropriate for general reading."
        }
        
        val stylePreservation = if (context.preserveStyle) {
            "Carefully preserve the original style, voice, and narrative techniques."
        } else {
            "Focus on accuracy of meaning rather than preserving the exact style."
        }
        
        // Get custom prompt from preferences
        val customPrompt = readerPreferences.translationCustomPrompt().get()
        val customInstruction = if (customPrompt.isNotBlank()) {
            "\n\nAdditional instructions: $customPrompt"
        } else {
            ""
        }
        
        return """
            Translate the following text from $sourceLanguage to $targetLanguage:
            
            $contentTypeInstruction
            $toneInstruction
            $stylePreservation$customInstruction
            
            IMPORTANT: Keep the ---PARAGRAPH_BREAK--- markers between paragraphs exactly as they appear.
            Do not add any explanations or notes - only provide the translation.
            
            TEXT TO TRANSLATE:
            $text
        """.trimIndent()
    }
    
    private fun getLanguageName(languageCode: String): String {
        return supportedLanguages.find { it.first == languageCode }?.second ?: languageCode
    }

    // Request/Response data classes
    
    @Serializable
    private data class NvidiaRequest(
        val model: String,
        val messages: List<Message>,
        val temperature: Double = 0.3,
        @SerialName("max_tokens")
        val max_tokens: Int = 16384,
        @SerialName("top_p")
        val top_p: Double = 1.0,
        val stream: Boolean = false
    )

    @Serializable
    private data class Message(
        val role: String,
        val content: String
    )

    @Serializable
    private data class NvidiaResponse(
        val id: String = "",
        val choices: List<Choice>? = null,
        val usage: Usage? = null
    )

    @Serializable
    private data class Choice(
        val index: Int = 0,
        val message: Message? = null,
        @SerialName("finish_reason")
        val finishReason: String = ""
    )
    
    @Serializable
    private data class Usage(
        @SerialName("prompt_tokens")
        val promptTokens: Int = 0,
        @SerialName("completion_tokens")
        val completionTokens: Int = 0,
        @SerialName("total_tokens")
        val totalTokens: Int = 0
    )
    
    // Models API response
    @Serializable
    private data class NvidiaModelsResponse(
        val data: List<ModelInfo> = emptyList()
    )
    
    @Serializable
    private data class ModelInfo(
        val id: String? = null,
        val `object`: String? = null,
        val created: Long? = null,
        val owned_by: String? = null
    )
    
    /**
     * Adjust paragraph count to match expected size
     * Handles cases where the AI merges or splits paragraphs
     */
    private fun adjustParagraphCount(
        splitTexts: List<String>,
        originalTexts: List<String>
    ): List<String> {
        // If we have fewer paragraphs, try to distribute content
        if (splitTexts.size < originalTexts.size) {
            val result = mutableListOf<String>()
            var currentIndex = 0
            
            for (original in originalTexts) {
                if (currentIndex < splitTexts.size) {
                    // Estimate if this paragraph should take more content
                    val ratio = original.length.toDouble() / originalTexts.sumOf { it.length }
                    val expectedParagraphs = maxOf(1, (ratio * splitTexts.size).toInt())
                    
                    if (expectedParagraphs > 1 && currentIndex + expectedParagraphs <= splitTexts.size) {
                        result.add(splitTexts.subList(currentIndex, currentIndex + expectedParagraphs).joinToString("\n"))
                        currentIndex += expectedParagraphs
                    } else {
                        result.add(splitTexts.getOrNull(currentIndex++) ?: "")
                    }
                } else {
                    result.add("")
                }
            }
            
            return result
        }
        
        // If we have more paragraphs, merge extras
        if (splitTexts.size > originalTexts.size) {
            val extraCount = splitTexts.size - originalTexts.size
            val mergeCount = extraCount / originalTexts.size + 1
            
            return originalTexts.indices.map { index ->
                val start = index * mergeCount
                val end = minOf(start + mergeCount, splitTexts.size)
                if (start < splitTexts.size) {
                    splitTexts.subList(start, end).joinToString("\n")
                } else {
                    ""
                }
            }
        }
        
        return splitTexts
    }
    
    companion object {
        /**
         * Engine ID for NVIDIA NIM
         */
        const val NVIDIA: Long = 10L
    }
}
