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
 * OpenRouterTranslateEngine
 * 
 * Uses OpenRouter API to access multiple AI models for translation.
 * OpenRouter provides a unified API to access various LLM providers
 * including OpenAI, Anthropic, Google, Meta, Mistral, and more.
 * 
 * Features:
 * - Dynamic model fetching from OpenRouter API
 * - Support for multiple AI models (GPT-4, Claude, Llama, etc.)
 * - Cost-effective routing across providers
 * - Fallback model support
 * 
 * @see https://openrouter.ai/docs
 */
class OpenRouterTranslateEngine(
    private val client: HttpClients,
    private val readerPreferences: ReaderPreferences,
) : TranslateEngine() {

    override val id: Long = OPENROUTER
    override val engineName: String = "OpenRouter AI"
    override val supportsAI: Boolean = true
    override val supportsContextAwareTranslation: Boolean = true
    override val supportsStylePreservation: Boolean = true
    override val requiresApiKey: Boolean = true
    
    // OpenRouter supports larger context windows
    override val maxCharsPerRequest: Int = 8000
    
    // Rate limit varies by model, 2 seconds is safe
    override val rateLimitDelayMs: Long = 2000L
    
    override val isOffline: Boolean = false
    
    // OpenRouter API base URL
    private val baseUrl = "https://openrouter.ai/api/v1"
    
    // Default model if none selected
    private val defaultModel = "openrouter/auto"
    
    // Cached available models
    private var cachedModels: List<Pair<String, String>>? = null
    
    /**
     * Available models for OpenRouter translation
     * Format: Pair<model_id, display_name>
     * 
     * These are curated models good for translation tasks.
     * Use fetchAvailableModels() to get the full dynamic list.
     */
    val availableModels: List<Pair<String, String>> = listOf(
        "openrouter/auto" to "Auto (Best Value)",
        "openai/gpt-4o" to "GPT-4o",
        "openai/gpt-4o-mini" to "GPT-4o Mini",
        "openai/gpt-4-turbo" to "GPT-4 Turbo",
        "openai/gpt-3.5-turbo" to "GPT-3.5 Turbo",
        "anthropic/claude-3.5-sonnet" to "Claude 3.5 Sonnet",
        "anthropic/claude-3-opus" to "Claude 3 Opus",
        "anthropic/claude-3-haiku" to "Claude 3 Haiku",
        "google/gemini-pro-1.5" to "Gemini Pro 1.5",
        "google/gemini-flash-1.5" to "Gemini Flash 1.5",
        "meta-llama/llama-3.1-70b-instruct" to "Llama 3.1 70B",
        "meta-llama/llama-3.1-8b-instruct" to "Llama 3.1 8B",
        "mistralai/mistral-large" to "Mistral Large",
        "mistralai/mixtral-8x7b-instruct" to "Mixtral 8x7B",
        "qwen/qwen-2.5-72b-instruct" to "Qwen 2.5 72B",
        "deepseek/deepseek-chat" to "DeepSeek Chat",
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
        
        val apiKey = readerPreferences.openRouterApiKey().get()
        
        if (apiKey.isBlank()) {
            onError(UiText.MStringResource(Res.string.openrouter_api_key_not_set))
            return
        }
        
        val selectedModel = readerPreferences.openRouterModel().get().ifBlank { defaultModel }
        
        try {
            onProgress(0)
            
            // Combine all paragraphs into a single text with markers
            val combinedText = texts.joinToString("\n---PARAGRAPH_BREAK---\n")
            val sourceLanguage = if (source == "auto") "the source language" else getLanguageName(source)
            val targetLanguage = getLanguageName(target)
            
            onProgress(20)
            val prompt = buildPrompt(combinedText, sourceLanguage, targetLanguage, context)
            
            onProgress(40)
            
            Log.debug { "OpenRouter: Starting translation with model $selectedModel" }
            
            val response = client.default.post("$baseUrl/chat/completions") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $apiKey")
                    append("HTTP-Referer", "https://ireader.app")
                    append("X-Title", "IReader")
                }
                contentType(ContentType.Application.Json)
                timeout {
                    requestTimeoutMillis = 120000 // 2 minutes
                    connectTimeoutMillis = 30000
                }
                setBody(OpenRouterRequest(
                    model = selectedModel,
                    messages = listOf(
                        Message(role = "system", content = getSystemPrompt()),
                        Message(role = "user", content = prompt)
                    ),
                    temperature = 0.3,
                    max_tokens = 4096
                ))
            }
            
            // Handle HTTP errors
            when (response.status.value) {
                401 -> {
                    Log.error { "OpenRouter API error: HTTP 401 - Invalid API key" }
                    onError(UiText.MStringResource(Res.string.openrouter_api_key_invalid))
                    return
                }
                402 -> {
                    Log.error { "OpenRouter API error: HTTP 402 - Insufficient credits" }
                    onError(UiText.MStringResource(Res.string.openrouter_insufficient_credits))
                    return
                }
                429 -> {
                    Log.error { "OpenRouter API error: HTTP 429 - Rate limited" }
                    onError(UiText.MStringResource(Res.string.api_rate_limit_exceeded))
                    return
                }
            }
            
            onProgress(80)
            val result = response.body<OpenRouterResponse>()
            
            Log.debug { "OpenRouter API response received: ${result.id}" }
            
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
                    Log.error { "OpenRouter API returned empty message content" }
                    onError(UiText.MStringResource(Res.string.empty_response))
                }
            } else {
                Log.error { "OpenRouter API returned empty choices array" }
                onError(UiText.MStringResource(Res.string.empty_response))
            }
        } catch (e: Exception) {
            Log.error { "OpenRouter translation error: ${e.message}" }
            
            val errorMessage = when {
                e.message?.contains("401") == true || 
                e.message?.contains("unauthorized") == true ||
                e.message?.contains("invalid_api_key") == true -> 
                    UiText.MStringResource(Res.string.openrouter_api_key_invalid)
                
                e.message?.contains("429") == true || 
                e.message?.contains("rate limit") == true ->
                    UiText.MStringResource(Res.string.api_rate_limit_exceeded)
                    
                e.message?.contains("402") == true || 
                e.message?.contains("insufficient") == true ->
                    UiText.MStringResource(Res.string.openrouter_insufficient_credits)
                    
                e.message?.contains("timeout") == true ->
                    UiText.DynamicString("Request timed out. Please try again.")
                    
                else -> UiText.ExceptionString(e)
            }
            
            onProgress(0)
            onError(errorMessage)
        }
    }
    
    /**
     * Fetch available models from OpenRouter API dynamically
     * Returns a list of model ID to display name pairs
     */
    suspend fun fetchAvailableModels(): Result<List<Pair<String, String>>> {
        val apiKey = readerPreferences.openRouterApiKey().get()
        if (apiKey.isBlank()) {
            return Result.failure(Exception("OpenRouter API key not set"))
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
                
                val modelsResponse = response.body<OpenRouterModelsResponse>()
                
                val models = modelsResponse.data
                    .filter { it.id != null }
                    .sortedBy { it.id }
                    .map { model ->
                        val displayName = model.name ?: model.id ?: "Unknown"
                        (model.id ?: "") to displayName
                    }
                
                cachedModels = models
                Log.debug { "OpenRouter: Fetched ${models.size} models" }
                Result.success(models)
            }
        } catch (e: Exception) {
            Log.error { "OpenRouter: Failed to fetch models" }
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
        
        return """
            Translate the following text from $sourceLanguage to $targetLanguage:
            
            $contentTypeInstruction
            $toneInstruction
            $stylePreservation
            
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
    private data class OpenRouterRequest(
        val model: String,
        val messages: List<Message>,
        val temperature: Double = 0.3,
        @SerialName("max_tokens")
        val max_tokens: Int = 4096
    )

    @Serializable
    private data class Message(
        val role: String,
        val content: String
    )

    @Serializable
    private data class OpenRouterResponse(
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
    private data class OpenRouterModelsResponse(
        val data: List<ModelInfo> = emptyList()
    )
    
    @Serializable
    private data class ModelInfo(
        val id: String? = null,
        val name: String? = null,
        val description: String? = null,
        val context_length: Int? = null,
        val pricing: Pricing? = null
    )
    
    @Serializable
    private data class Pricing(
        val prompt: String? = null,
        val completion: String? = null
    )

    // Helper function to adjust paragraph count to match input
    private fun adjustParagraphCount(translatedParagraphs: List<String>, originalTexts: List<String>): List<String> {
        val result = translatedParagraphs.toMutableList()
        
        // If we have too few paragraphs, add original ones
        while (result.size < originalTexts.size) {
            result.add(originalTexts[result.size])
        }
        
        // If we have too many paragraphs, remove extras
        if (result.size > originalTexts.size) {
            result.subList(originalTexts.size, result.size).clear()
        }
        
        return result
    }
    
    companion object {
        const val OPENROUTER = 9L
    }
}
