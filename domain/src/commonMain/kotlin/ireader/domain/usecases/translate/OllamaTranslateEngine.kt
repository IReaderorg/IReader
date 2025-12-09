package ireader.domain.usecases.translate

import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ireader.core.http.HttpClients
import ireader.domain.data.engines.ToneType
import ireader.domain.data.engines.TranslateEngine
import ireader.domain.data.engines.TranslationContext
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.no_text_to_translate
import kotlinx.serialization.Serializable
import ireader.domain.data.engines.ContentType as TranslationContentType

/**
 * Ollama Translation Engine
 * Uses the Ollama API for locally-hosted LLM translation
 * https://github.com/ollama/ollama
 */
class OllamaTranslateEngine(
    private val client: HttpClients,
    private val readerPreferences: ReaderPreferences,
) : TranslateEngine() {

    override val id: Long = 5 // Unique ID for this engine
    override val engineName: String = "Ollama (Local LLM)"
    override val supportsAI: Boolean = true
    override val supportsContextAwareTranslation: Boolean = true
    override val supportsStylePreservation: Boolean = true
    override val requiresApiKey: Boolean = false // Doesn't require an API key, but requires URL
    
    // Ollama is local, so we can handle larger chunks
    override val maxCharsPerRequest: Int = 10000
    
    // Local engine, no rate limiting needed
    override val rateLimitDelayMs: Long = 0L
    
    // Ollama runs locally
    override val isOffline: Boolean = true
    
    // Default Ollama API endpoint (typically local)
    private val defaultApiUrl = "http://localhost:11434"
    
    // Get the configured Ollama URL from preferences or use default
    private fun getOllamaUrl(): String {
        val configuredUrl = readerPreferences.ollamaServerUrl().get()
        // Handle legacy URLs that include /api/chat path
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
        /** Maximum paragraphs per chunk to avoid context window limits (same as Gemini) */
        const val MAX_CHUNK_SIZE = 20
        
        /** Request timeout in milliseconds (local LLM can be slow) */
        const val REQUEST_TIMEOUT_MS = 120000L
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
        val temperature: Float = 0.1f
    )
    
    @Serializable
    private data class OllamaChatResponse(
        val model: String,
        val message: OllamaMessage,
        val done: Boolean
    )
    
    override suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onProgress: (Int) -> Unit,
        onSuccess: (List<String>) -> Unit,
        onError: (UiText) -> Unit
    ) {
        // Create a basic context and delegate to the enhanced method
        val context = TranslationContext(
            contentType = TranslationContentType.GENERAL,
            toneType = ToneType.NEUTRAL,
            preserveStyle = false
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
            onProgress(0)
            
            // Get configuration from preferences
            val url = getOllamaUrl()
            val model = getOllamaModel()
            
            // Get language names for better prompting
            val sourceLang = supportedLanguages.find { it.first == source }?.second ?: source
            val targetLang = supportedLanguages.find { it.first == target }?.second ?: target
            
            // Chunk texts by paragraph count (similar to DeepSeek approach)
            val chunks = texts.chunked(MAX_CHUNK_SIZE)
            val allResults = mutableListOf<String>()
            
            println("Ollama: Created ${chunks.size} chunks from ${texts.size} texts (max $MAX_CHUNK_SIZE per chunk)")
            
            chunks.forEachIndexed { chunkIndex, chunk ->
                val chunkProgress = 10 + (chunkIndex * 80 / chunks.size)
                onProgress(chunkProgress)
                
                try {
                    val translatedChunk = translateChunk(chunk, sourceLang, targetLang, context, url, model)
                    allResults.addAll(translatedChunk)
                } catch (e: Exception) {
                    println("Ollama chunk $chunkIndex failed: ${e.message}")
                    throw e
                }
            }
            
            // Ensure we have the right number of results
            val finalResults = if (allResults.size == texts.size) {
                allResults
            } else {
                adjustParagraphCount(allResults, texts)
            }
            
            onProgress(100)
            onSuccess(finalResults)
            
        } catch (e: Exception) {
            onProgress(0)
            println("Ollama translation error: ${e.message}")
            e.printStackTrace()
            onError(UiText.ExceptionString(e))
        }
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
        
        // Combine texts with paragraph markers
        val combinedText = chunk.joinToString("\n---PARAGRAPH_BREAK---\n")
        val (systemPrompt, userPrompt) = buildChatPrompt(combinedText, sourceLang, targetLang, context)
        
        val request = OllamaChatRequest(
            model = model,
            messages = listOf(
                OllamaMessage(role = "system", content = systemPrompt),
                OllamaMessage(role = "user", content = userPrompt)
            ),
            options = OllamaOptions(temperature = 0.1f)
        )
        
        // Make the API request with timeout
        val response = client.default.post(chatUrl) {
            contentType(ContentType.Application.Json)
            setBody(request)
            timeout {
                requestTimeoutMillis = REQUEST_TIMEOUT_MS
                connectTimeoutMillis = 15000
                socketTimeoutMillis = REQUEST_TIMEOUT_MS
            }
        }.body<OllamaChatResponse>()
        
        // Split the response back into paragraphs
        return splitResponse(response.message.content.trim(), chunk.size)
    }
    
    /**
     * Split response back into paragraphs
     */
    private fun splitResponse(response: String, expectedCount: Int): List<String> {
        // Try to split by paragraph marker first
        if (response.contains("---PARAGRAPH_BREAK---")) {
            val parts = response
                .split("---PARAGRAPH_BREAK---")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            
            if (parts.size == expectedCount) {
                return parts
            }
        }
        
        // Also try with newline variations
        val variations = listOf(
            "\n---PARAGRAPH_BREAK---\n",
            "---PARAGRAPH_BREAK---",
            "\n\n---\n\n"
        )
        
        for (separator in variations) {
            if (response.contains(separator)) {
                val parts = response.split(separator).map { it.trim() }.filter { it.isNotEmpty() }
                if (parts.size == expectedCount) {
                    return parts
                }
            }
        }
        
        // If marker not found and we expect multiple paragraphs, try double newlines
        if (expectedCount > 1) {
            val lines = response.split("\n\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            
            if (lines.size >= expectedCount) {
                // Distribute lines evenly across expected paragraphs
                val result = mutableListOf<String>()
                val linesPerParagraph = lines.size / expectedCount
                
                for (i in 0 until expectedCount) {
                    val start = i * linesPerParagraph
                    val end = if (i == expectedCount - 1) lines.size else (i + 1) * linesPerParagraph
                    if (start < lines.size) {
                        result.add(lines.subList(start, end.coerceAtMost(lines.size)).joinToString("\n\n"))
                    }
                }
                return result
            }
        }
        
        // Fallback: return as single result
        return listOf(response.trim())
    }
    
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
    
    /**
     * Builds a chat prompt (system + user) for the LLM based on the translation context
     * Returns Pair(systemPrompt, userPrompt)
     */
    private fun buildChatPrompt(
        text: String,
        sourceLang: String,
        targetLang: String,
        context: TranslationContext
    ): Pair<String, String> {
        val contentTypeStr = when (context.contentType) {
            TranslationContentType.LITERARY -> "literary text"
            TranslationContentType.TECHNICAL -> "technical document"
            TranslationContentType.CONVERSATION -> "conversation"
            TranslationContentType.POETRY -> "poetry"
            TranslationContentType.ACADEMIC -> "academic text"
            else -> "general text"
        }
        
        val toneStr = when (context.toneType) {
            ToneType.FORMAL -> "formal"
            ToneType.CASUAL -> "casual"
            ToneType.PROFESSIONAL -> "professional"
            ToneType.HUMOROUS -> "humorous"
            else -> "neutral"
        }
        
        val stylePreservation = if (context.preserveStyle) 
            "Preserve the original writing style, formatting, and tone." 
        else 
            ""
        
        // Optimized prompt: minimal tokens while maintaining quality
        val systemPrompt = """
            Translator for $contentTypeStr. $sourceLang to $targetLang. $toneStr tone.
            $stylePreservation
            Keep ---PARAGRAPH_BREAK--- markers exactly as they appear.
            Output only the translation, no explanations.
        """.trimIndent()
        
        return Pair(systemPrompt, text)
    }
} 