package ireader.domain.usecases.translate

import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import ireader.core.http.HttpClients
import ireader.domain.data.engines.ContentType as TranslationContentType
import ireader.domain.data.engines.ToneType
import ireader.domain.data.engines.TranslateEngine
import ireader.domain.data.engines.TranslationContext
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class DeepSeekTranslateEngine(
    private val client: HttpClients,
    private val readerPreferences: ReaderPreferences,
) : TranslateEngine() {

    override val id: Long = 3
    override val engineName: String = "DeepSeek AI"
    override val supportsAI: Boolean = true
    override val supportsContextAwareTranslation: Boolean = true
    override val supportsStylePreservation: Boolean = true
    override val requiresApiKey: Boolean = true
    
    // DeepSeek has 64k context window, but we keep it conservative
    override val maxCharsPerRequest: Int = 8000
    
    // DeepSeek has generous rate limits, 3 seconds is safe
    override val rateLimitDelayMs: Long = 3000L
    
    override val isOffline: Boolean = false
    
    // DeepSeek has excellent language support similar to OpenAI
    override val supportedLanguages: List<Pair<String, String>> = listOf(
        "auto" to "Auto-detect",
        "af" to "Afrikaans",
        "ar" to "Arabic", 
        "bg" to "Bulgarian",
        "zh" to "Chinese",
        "hr" to "Croatian",
        "cs" to "Czech",
        "da" to "Danish",
        "nl" to "Dutch",
        "en" to "English",
        "et" to "Estonian",
        "fi" to "Finnish",
        "fr" to "French",
        "de" to "German",
        "el" to "Greek",
        "he" to "Hebrew",
        "hi" to "Hindi",
        "hu" to "Hungarian",
        "id" to "Indonesian",
        "it" to "Italian",
        "ja" to "Japanese",
        "ko" to "Korean",
        "lv" to "Latvian",
        "lt" to "Lithuanian",
        "ms" to "Malay",
        "no" to "Norwegian",
        "fa" to "Persian",
        "pl" to "Polish",
        "pt" to "Portuguese",
        "ro" to "Romanian",
        "ru" to "Russian",
        "sk" to "Slovak",
        "sl" to "Slovenian",
        "es" to "Spanish",
        "sv" to "Swedish",
        "th" to "Thai",
        "tr" to "Turkish",
        "uk" to "Ukrainian",
        "vi" to "Vietnamese"
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
        // Validate inputs and handle empty or null lists
        if (texts.isNullOrEmpty()) {
            onError(UiText.MStringResource(Res.string.no_text_to_translate))
            return
        }
        
        val apiKey = readerPreferences.deepSeekApiKey().get()
        
        if (apiKey.isBlank()) {
            onError(UiText.DynamicString("DeepSeek API key is not set. Please configure it in Settings > Translation."))
            return
        }
        
        try {
            onProgress(0)
            
            val sourceLanguage = if (source == "auto") "the source language" else getLanguageName(source)
            val targetLanguage = getLanguageName(target)
            
            // Chunk translation for better handling of large texts
            val maxChunkSize = MAX_CHUNK_SIZE
            val chunks = texts.chunked(maxChunkSize)
            val allResults = mutableListOf<String>()
            
            chunks.forEachIndexed { chunkIndex, chunk ->
                val chunkProgress = 10 + (chunkIndex * 80 / chunks.size)
                onProgress(chunkProgress)
                
                val combinedText = chunk.joinToString("\n---PARAGRAPH_BREAK---\n")
                val prompt = buildPrompt(combinedText, sourceLanguage, targetLanguage, context)
                
                try {
                    val translationResult = callDeepSeekApi(apiKey, prompt)
                    val splitResults = splitResponse(translationResult, chunk.size)
                    allResults.addAll(splitResults)
                } catch (e: Exception) {
                    // Rethrow to handle at outer level
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
            val errorMessage = UiText.DynamicString("DeepSeek Error: ${e.message ?: "Unknown error"}")
            onError(errorMessage)
        }
    }
    
    /**
     * Call DeepSeek API with retry logic
     */
    private suspend fun callDeepSeekApi(apiKey: String, prompt: String): String {
        
        val response = client.default.post("https://api.deepseek.com/v1/chat/completions") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $apiKey")
            }
            contentType(ContentType.Application.Json)
            setBody(DeepSeekRequest(
                model = "deepseek-chat",
                messages = listOf(
                    // Minimal system message to save tokens
                    Message(role = "system", content = "Translator."),
                    Message(role = "user", content = prompt)
                ),
                temperature = 0.1, // Lower temperature = more deterministic, fewer retries needed
                max_tokens = 2000  // Reduced from 4000 to save costs
            ))
            timeout {
                requestTimeoutMillis = 60000
                connectTimeoutMillis = 15000
                socketTimeoutMillis = 60000
            }
        }
        
        // Check the response status code
        when (response.status.value) {
            401 -> throw Exception("401 Unauthorized - Invalid API key")
            402 -> throw Exception("402 Payment Required - Check your DeepSeek account balance")
            403 -> throw Exception("403 Forbidden - API key doesn't have permission")
            429 -> throw Exception("429 Rate limit exceeded")
            in 500..599 -> throw Exception("${response.status.value} Server error - DeepSeek API is temporarily unavailable")
        }
        
        if (response.status.value !in 200..299) {
            throw Exception("HTTP ${response.status.value} - API request failed")
        }
        
        val result = response.body<DeepSeekResponse>()
        
        if (result.choices != null && result.choices.isNotEmpty()) {
            val messageContent = result.choices[0].message?.content
            if (!messageContent.isNullOrEmpty()) {
                return messageContent.trim()
            }
        }
        
        throw Exception("Empty response from DeepSeek API - no translation returned")
    }
    
    /**
     * Split response back into paragraphs
     */
    private fun splitResponse(response: String, expectedCount: Int): List<String> {
        if (response.contains("---PARAGRAPH_BREAK---")) {
            return response
                .split("---PARAGRAPH_BREAK---")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        }
        
        // If marker not found and we expect multiple paragraphs, try newlines
        if (expectedCount > 1) {
            val lines = response.lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            
            if (lines.size >= expectedCount) {
                val result = mutableListOf<String>()
                val linesPerParagraph = lines.size / expectedCount
                
                for (i in 0 until expectedCount) {
                    val start = i * linesPerParagraph
                    val end = if (i == expectedCount - 1) lines.size else (i + 1) * linesPerParagraph
                    if (start < lines.size) {
                        result.add(lines.subList(start, end.coerceAtMost(lines.size)).joinToString("\n"))
                    }
                }
                return result
            }
        }
        
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
    
    private fun buildPrompt(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        context: TranslationContext
    ): String {
        // Get custom prompt from preferences
        val customPrompt = readerPreferences.translationCustomPrompt().get()
        val customInstruction = if (customPrompt.isNotBlank()) {
            " $customPrompt"
        } else {
            ""
        }
        
        // OPTIMIZED: Minimal prompt to reduce token usage and costs
        // Each token costs money, so we use the shortest effective prompt
        return "Translate $sourceLanguage to $targetLanguage. Keep ---PARAGRAPH_BREAK--- markers.$customInstruction Output only translation:\n\n$text"
    }
    
    /**
     * Build a detailed prompt for higher quality translations (uses more tokens)
     * Use this when quality is more important than cost
     */
    private fun buildDetailedPrompt(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        context: TranslationContext
    ): String {
        val contentTypeInstruction = when (context.contentType) {
            TranslationContentType.LITERARY -> "Literary text, preserve style."
            TranslationContentType.TECHNICAL -> "Technical text, precise terms."
            TranslationContentType.CONVERSATION -> "Conversational, natural flow."
            TranslationContentType.POETRY -> "Poetry, preserve rhythm."
            TranslationContentType.ACADEMIC -> "Academic, formal language."
            else -> ""
        }
        
        val toneInstruction = when (context.toneType) {
            ToneType.FORMAL -> "Formal tone."
            ToneType.CASUAL -> "Casual tone."
            ToneType.PROFESSIONAL -> "Professional tone."
            ToneType.HUMOROUS -> "Keep humor."
            else -> ""
        }
        
        val instructions = listOfNotNull(
            contentTypeInstruction.takeIf { it.isNotEmpty() },
            toneInstruction.takeIf { it.isNotEmpty() }
        ).joinToString(" ")
        
        return if (instructions.isNotEmpty()) {
            "Translate $sourceLanguage→$targetLanguage. $instructions Keep ---PARAGRAPH_BREAK---. Output only translation:\n\n$text"
        } else {
            "Translate $sourceLanguage→$targetLanguage. Keep ---PARAGRAPH_BREAK---. Output only translation:\n\n$text"
        }
    }
    
    private fun getLanguageName(languageCode: String): String {
        return supportedLanguages.find { it.first == languageCode }?.second ?: languageCode
    }

    @Serializable
    private data class DeepSeekRequest(
        val model: String,
        val messages: List<Message>,
        val temperature: Double = 0.7,
        @SerialName("max_tokens")
        val max_tokens: Int = 2000
    )

    @Serializable
    private data class Message(
        val role: String,
        val content: String
    )

    @Serializable
    private data class DeepSeekResponse(
        val id: String = "",
        val choices: List<Choice>? = null
    )

    @Serializable
    private data class Choice(
        val message: Message? = null,
        @SerialName("finish_reason")
        val finishReason: String = ""
    )
    
    companion object {
        /** Maximum paragraphs per chunk to avoid token limits */
        const val MAX_CHUNK_SIZE = 20
    }
} 