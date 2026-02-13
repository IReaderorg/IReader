package ireader.domain.usecases.translate

import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ireader.core.http.HttpClients
import ireader.core.log.Log
import ireader.domain.data.engines.ToneType
import ireader.domain.data.engines.TranslateEngine
import ireader.domain.data.engines.TranslationContext
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.no_text_to_translate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import ireader.domain.data.engines.ContentType as TranslationContentType

/**
 * Ollama Translation Engine
 * Uses the Ollama API for locally-hosted LLM translation
 * Optimized to match Gemini's chunking and prompt patterns
 * https://github.com/ollama/ollama
 */
class OllamaTranslateEngine(
    private val client: HttpClients,
    private val readerPreferences: ReaderPreferences,
) : TranslateEngine() {

    override val id: Long = 5
    override val engineName: String = "Ollama (Local LLM)"
    override val supportsAI: Boolean = true
    override val supportsContextAwareTranslation: Boolean = true
    override val supportsStylePreservation: Boolean = true
    override val requiresApiKey: Boolean = false
    
    // Ollama is local, can handle larger chunks
    override val maxCharsPerRequest: Int = 10000
    
    // Local engine, minimal rate limiting
    override val rateLimitDelayMs: Long = 100L
    
    // Ollama runs locally
    override val isOffline: Boolean = true
    
    // Default Ollama API endpoint
    private val defaultApiUrl = "http://localhost:11434"
    
    // JSON parser with lenient settings
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    // Get the configured Ollama URL from preferences or use default
    private fun getOllamaUrl(): String {
        val configuredUrl = readerPreferences.ollamaServerUrl().get()
        val cleanUrl = configuredUrl.trimEnd('/')
            .removeSuffix("/api/chat")
            .removeSuffix("/api/generate")
        return if (cleanUrl.isNotBlank()) cleanUrl else defaultApiUrl
    }
    
    // Get the configured Ollama model from preferences or use default
    private fun getOllamaModel(): String {
        val configuredModel = readerPreferences.ollamaModel().get()
        return if (configuredModel.isNotBlank()) configuredModel else "mistral"
    }
    
    companion object {
        /** Maximum paragraphs per chunk (same as Gemini) */
        const val MAX_CHUNK_SIZE = 20
        
        /** Request timeout in milliseconds (local LLM can be slow) */
        const val REQUEST_TIMEOUT_MS = 800000L
        
        /** Max retries for network issues */
        const val MAX_RETRIES = 3
        
        /** Paragraph separator marker */
        const val MARKER = "---PARAGRAPH_BREAK---"
    }
    
    // Ollama supports a wide range of languages through its LLM capabilities
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

    @Serializable
    private data class OllamaMessage(
        val role: String,
        val content: String
    )

    @Serializable
    private data class OllamaChatRequest(
        val model: String,
        val messages: List<OllamaMessage>,
        val stream: Boolean = false,
        val options: OllamaOptions? = null
    )
    
    @Serializable
    private data class OllamaOptions(
        val temperature: Float = 0.1f,
        val num_predict: Int = 8192
    )
    
    @Serializable
    private data class OllamaChatResponse(
        val model: String? = null,
        val message: OllamaMessage? = null,
        val done: Boolean = false,
        val error: String? = null
    )
    
    // Response models for /api/tags endpoint
    @Serializable
    private data class OllamaTagsResponse(
        val models: List<OllamaModel> = emptyList()
    )
    
    @Serializable
    private data class OllamaModel(
        val name: String,
        val modified_at: String? = null,
        val size: Long? = null,
        val digest: String? = null,
        val details: OllamaModelDetails? = null
    )
    
    @Serializable
    private data class OllamaModelDetails(
        val format: String? = null,
        val family: String? = null,
        val parameter_size: String? = null,
        val quantization_level: String? = null
    )
    
    // Cached available models
    private var cachedModels: List<OllamaModelInfo>? = null
    private var lastModelFetchTime: Long = 0
    private val modelCacheDurationMs = 60000L // 1 minute cache
    
    /**
     * Information about an available Ollama model
     */
    data class OllamaModelInfo(
        val name: String,
        val displayName: String,
        val size: String?,
        val details: String?
    )
    
    /**
     * Fetch available models from the Ollama server.
     * Uses caching to avoid repeated API calls.
     * 
     * @param forceRefresh Force refresh the cache
     * @return List of available models, or empty list if unavailable
     */
    @OptIn(ExperimentalTime::class)
    suspend fun fetchAvailableModels(forceRefresh: Boolean = false): List<OllamaModelInfo> {
        val now = Clock.System.now().toEpochMilliseconds()
        
        // Return cached models if still valid
        if (!forceRefresh && cachedModels != null && (now - lastModelFetchTime) < modelCacheDurationMs) {
            return cachedModels!!
        }
        
        return try {
            val url = getOllamaUrl()
            val tagsUrl = "$url/api/tags"
            
            Log.info { "OllamaTranslateEngine: Fetching available models from $tagsUrl" }
            
            val response = client.default.get(tagsUrl) {
                timeout {
                    requestTimeoutMillis = 10000 // 10 seconds for model list
                    connectTimeoutMillis = 5000
                }
            }
            
            if (response.status.value !in 200..299) {
                Log.warn { "OllamaTranslateEngine: Failed to fetch models, status: ${response.status}" }
                return getCachedOrDefaultModels()
            }
            
            val responseText = response.bodyAsText()
            val tagsResponse = json.decodeFromString<OllamaTagsResponse>(responseText)
            
            val models = tagsResponse.models.map { model ->
                OllamaModelInfo(
                    name = model.name,
                    displayName = formatModelDisplayName(model),
                    size = model.size?.let { formatSize(it) },
                    details = model.details?.let { "${it.family ?: ""} ${it.parameter_size ?: ""}".trim() }
                )
            }
            
            Log.info { "OllamaTranslateEngine: Found ${models.size} models" }
            
            // Update cache
            cachedModels = models
            lastModelFetchTime = now
            
            models
        } catch (e: Exception) {
            Log.error { "OllamaTranslateEngine: Failed to fetch models: ${e.message}" }
            getCachedOrDefaultModels()
        }
    }
    
    /**
     * Get cached models or default models if cache is empty
     */
    private fun getCachedOrDefaultModels(): List<OllamaModelInfo> {
        return cachedModels ?: listOf(
            OllamaModelInfo("mistral", "Mistral (Default)", null, null),
            OllamaModelInfo("llama2", "Llama 2", null, null),
            OllamaModelInfo("llama3", "Llama 3", null, null),
            OllamaModelInfo("codellama", "Code Llama", null, null),
            OllamaModelInfo("qwen", "Qwen", null, null)
        )
    }
    
    /**
     * Format model name for display
     */
    private fun formatModelDisplayName(model: OllamaModel): String {
        val name = model.name
        // Remove trailing :latest if present
        val cleanName = name.removeSuffix(":latest")
        
        // Build display name with details if available
        return buildString {
            append(cleanName)
            model.details?.parameter_size?.let { size ->
                append(" ($size)")
            }
        }
    }
    
    /**
     * Format file size in human-readable format
     */
    private fun formatSize(bytes: Long): String {
        return when {
            bytes >= 1_073_741_824 -> {
                val gb = bytes / 1_073_741_824.0
                "${(gb * 10).toLong() / 10.0} GB"
            }
            bytes >= 1_048_576 -> {
                val mb = bytes / 1_048_576.0
                "${(mb * 10).toLong() / 10.0} MB"
            }
            bytes >= 1024 -> {
                val kb = bytes / 1024.0
                "${(kb * 10).toLong() / 10.0} KB"
            }
            else -> "$bytes B"
        }
    }
    
    /**
     * Check if the Ollama server is reachable
     */
    suspend fun isServerAvailable(): Boolean {
        return try {
            val url = getOllamaUrl()
            val response = client.default.get("$url/api/version") {
                timeout {
                    requestTimeoutMillis = 5000
                    connectTimeoutMillis = 3000
                }
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onProgress: (Int) -> Unit,
        onSuccess: (List<String>) -> Unit,
        onError: (UiText) -> Unit
    ) {
        val context = TranslationContext(
            contentType = TranslationContentType.GENERAL,
            toneType = ToneType.NEUTRAL,
            preserveStyle = true
        )
        translateWithContext(texts, source, target, context, onProgress, onSuccess, onError)
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
        
        try {
            onProgress(10)
            
            val url = getOllamaUrl()
            val model = getOllamaModel()
            
            // Get language names for prompting
            val sourceLang = supportedLanguages.find { it.first == source }?.second ?: source
            val targetLang = supportedLanguages.find { it.first == target }?.second ?: target
            
            // Chunk texts like Gemini does
            val chunks = texts.chunked(MAX_CHUNK_SIZE)
            val allResults = mutableListOf<String>()
            val totalChunks = chunks.size
            
            chunks.forEachIndexed { chunkIndex, chunk ->
                // Calculate progress: 10% start, 90% for chunks
                val chunkProgress = 10 + ((chunkIndex + 1) * 80 / totalChunks)
                onProgress(chunkProgress)
                
                val translatedChunk = translateChunkWithRetry(
                    chunk, sourceLang, targetLang, context, url, model
                )
                allResults.addAll(translatedChunk)
            }
            
            // Ensure correct paragraph count
            val finalResults = if (allResults.size == texts.size) {
                allResults
            } else {
                adjustParagraphCount(allResults, texts)
            }
            
            onProgress(100)
            onSuccess(finalResults)
            
        } catch (e: Exception) {
            onProgress(0)
            onError(UiText.ExceptionString(e))
        }
    }
    
    /**
     * Translate a chunk with retry logic (like Gemini)
     */
    private suspend fun translateChunkWithRetry(
        chunk: List<String>,
        sourceLang: String,
        targetLang: String,
        context: TranslationContext,
        url: String,
        model: String
    ): List<String> {
        var lastException: Exception? = null
        
        for (attempt in 1..MAX_RETRIES) {
            try {
                return translateChunk(chunk, sourceLang, targetLang, context, url, model)
            } catch (e: Exception) {
                lastException = e
                
                // Check if retryable
                val isRetryable = e.message?.let { msg ->
                    msg.contains("timeout", ignoreCase = true) ||
                    msg.contains("connection", ignoreCase = true) ||
                    msg.contains("reset", ignoreCase = true)
                } ?: false
                
                if (!isRetryable || attempt == MAX_RETRIES) {
                    throw e
                }
                
                // Exponential backoff
                kotlinx.coroutines.delay((1000L * attempt))
            }
        }
        
        throw lastException ?: Exception("Translation failed after $MAX_RETRIES attempts")
    }
    
    /**
     * Translate a single chunk of texts
     */
    private suspend fun translateChunk(
        chunk: List<String>,
        sourceLang: String,
        targetLang: String,
        context: TranslationContext,
        url: String,
        model: String
    ): List<String> {
        val chatUrl = url.trimEnd('/') + "/api/chat"
        
        // Join paragraphs with marker
        val combinedText = chunk.joinToString("\n$MARKER\n")
        
        // Use system message with explicit translation instruction
        val systemPrompt = """You are an expert literary translator. Your ONLY task is to translate text from $sourceLang to $targetLang.

CRITICAL RULES:
1. You MUST output ONLY in $targetLang language
2. Do NOT output any $sourceLang text
3. Do NOT explain or comment - just translate
4. Preserve paragraph structure using $MARKER as separator
5. Maintain narrative tone and style

You are translating a novel/fiction text."""

        val userPrompt = """Translate the following $sourceLang text to $targetLang.
OUTPUT MUST BE IN $targetLang ONLY.

Text to translate:

$combinedText"""
        
        val request = OllamaChatRequest(
            model = model,
            messages = listOf(
                OllamaMessage(role = "system", content = systemPrompt),
                OllamaMessage(role = "user", content = userPrompt)
            ),
            options = OllamaOptions(temperature = 0.1f, num_predict = 8192)
        )
        
        val response = client.default.post(chatUrl) {
            contentType(ContentType.Application.Json)
            setBody(request)
            timeout {
                requestTimeoutMillis = REQUEST_TIMEOUT_MS
                connectTimeoutMillis = 30000
                socketTimeoutMillis = REQUEST_TIMEOUT_MS
            }
        }
        
        // Handle response status
        if (response.status.value !in 200..299) {
            val errorBody = response.bodyAsText()
            throw Exception("Ollama API error (${response.status.value}): $errorBody")
        }
        
        val responseText = response.bodyAsText()
        val ollamaResponse = try {
            json.decodeFromString<OllamaChatResponse>(responseText)
        } catch (e: Exception) {
            throw Exception("Failed to parse Ollama response: ${e.message}")
        }
        
        // Check for errors
        if (ollamaResponse.error != null) {
            throw Exception("Ollama error: ${ollamaResponse.error}")
        }
        
        val content = ollamaResponse.message?.content?.trim()
        if (content.isNullOrEmpty()) {
            throw Exception("Empty response from Ollama")
        }
        
        return splitResponse(content, chunk.size)
    }
    

    
    /**
     * Split response back into paragraphs using PARAGRAPH_BREAK marker
     */
    private fun splitResponse(response: String, expectedCount: Int): List<String> {
        // Split by marker and clean up
        if (response.contains(MARKER)) {
            val parts = response
                .split(MARKER)
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            
            if (parts.size == expectedCount) {
                return parts
            }
            
            // If count doesn't match but we have parts, adjust
            if (parts.isNotEmpty()) {
                return adjustParagraphCount(parts, List(expectedCount) { "" })
            }
        }
        
        // Fallback: try double newlines
        if (expectedCount > 1) {
            val parts = response.split("\n\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            
            if (parts.size >= expectedCount) {
                return parts.take(expectedCount)
            }
        }
        
        // Single paragraph fallback
        return listOf(response.trim())
    }
    
    /**
     * Adjust paragraph count to match input
     */
    private fun adjustParagraphCount(translatedParagraphs: List<String>, originalTexts: List<String>): List<String> {
        val result = translatedParagraphs.toMutableList()
        
        while (result.size < originalTexts.size) {
            result.add(originalTexts[result.size])
        }
        
        if (result.size > originalTexts.size) {
            result.subList(originalTexts.size, result.size).clear()
        }
        
        return result
    }
} 