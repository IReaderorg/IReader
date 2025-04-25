package ireader.domain.usecases.translate

import io.ktor.client.call.body
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
import ireader.i18n.resources.MR
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    
    // Default Ollama API endpoint (typically local)
    private val defaultApiUrl = "http://localhost:11434/api/generate"
    
    // Get the configured Ollama URL from preferences or use default
    private fun getOllamaUrl(): String {
        val configuredUrl = readerPreferences.ollamaUrl().get()
        return if (configuredUrl.isNotBlank()) configuredUrl else defaultApiUrl
    }
    
    // Get the configured Ollama model from preferences or use default
    private fun getOllamaModel(): String {
        val configuredModel = readerPreferences.ollamaModel().get()
        return if (configuredModel.isNotBlank()) configuredModel else "mistral"
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
    private data class OllamaRequest(
        val model: String,
        val prompt: String,
        val stream: Boolean = false,
        val options: OllamaOptions? = null
    )
    
    @Serializable
    private data class OllamaOptions(
        val temperature: Float = 0.1f
    )
    
    @Serializable
    private data class OllamaResponse(
        val model: String,
        val response: String,
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
            onError(UiText.MStringResource(MR.strings.no_text_to_translate))
            return
        }
        
        try {
            onProgress(10)
            
            // Get configuration from preferences
            val url = getOllamaUrl()
            val model = getOllamaModel()
            
            // Get language names for better prompting
            val sourceLang = supportedLanguages.find { it.first == source }?.second ?: source
            val targetLang = supportedLanguages.find { it.first == target }?.second ?: target
            
            onProgress(20)
            
            // Batch texts together instead of processing individually
            // For Ollama, we need to be careful with context window limits
            // Let's set a reasonable batch size based on token count
            val results = mutableListOf<String>()
            val MAX_TOKENS_PER_BATCH = 2000 // Estimate for context window safety
            val TOKENS_PER_CHAR_ESTIMATE = 0.25 // Rough estimate of tokens per character
            
            // Create batches based on estimated token count
            val batches = mutableListOf<List<String>>()
            val currentBatch = mutableListOf<String>()
            var currentBatchTokens = 0
            
            for (text in texts) {
                val estimatedTokens = (text.length * TOKENS_PER_CHAR_ESTIMATE).toInt()
                
                if (estimatedTokens > MAX_TOKENS_PER_BATCH) {
                    // Text is too long for a batch, process individually
                    if (currentBatch.isNotEmpty()) {
                        batches.add(currentBatch.toList())
                        currentBatch.clear()
                        currentBatchTokens = 0
                    }
                    batches.add(listOf(text))
                } else if (currentBatchTokens + estimatedTokens > MAX_TOKENS_PER_BATCH) {
                    // Current batch would exceed token limit, start new batch
                    batches.add(currentBatch.toList())
                    currentBatch.clear()
                    currentBatch.add(text)
                    currentBatchTokens = estimatedTokens
                } else {
                    // Add to current batch
                    currentBatch.add(text)
                    currentBatchTokens += estimatedTokens
                }
            }
            
            // Add any remaining texts
            if (currentBatch.isNotEmpty()) {
                batches.add(currentBatch.toList())
            }
            
            println("Ollama: Created ${batches.size} batches from ${texts.size} texts")
            
            // Process each batch
            for ((batchIndex, batch) in batches.withIndex()) {
                val progressStart = 20 + (batchIndex * 70 / batches.size)
                val progressEnd = 20 + ((batchIndex + 1) * 70 / batches.size)
                onProgress(progressStart)
                
                if (batch.size == 1) {
                    // Single text in batch
                    val text = batch[0]
                    val prompt = buildPrompt(text, sourceLang, targetLang, context)
                    
                    val request = OllamaRequest(
                        model = model,
                        prompt = prompt,
                        options = OllamaOptions(temperature = 0.1f)
                    )
                    
                    // Make the API request
                    val response = client.default.post(url) {
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }.body<OllamaResponse>()
                    
                    // Add the response to results
                    results.add(response.response.trim())
                } else {
                    // Multiple texts in batch
                    val combinedText = batch.joinToString("\n---PARAGRAPH_BREAK---\n")
                    val prompt = buildPrompt(combinedText, sourceLang, targetLang, context)
                    
                    val request = OllamaRequest(
                        model = model,
                        prompt = prompt,
                        options = OllamaOptions(temperature = 0.1f)
                    )
                    
                    // Make the API request
                    val response = client.default.post(url) {
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }.body<OllamaResponse>()
                    
                    // Split the response into paragraphs
                    val responseText = response.response.trim()
                    val splitTexts = responseText.split("\n---PARAGRAPH_BREAK---\n")
                    
                    // Ensure we have the correct number of paragraphs
                    val finalTexts = if (splitTexts.size == batch.size) {
                        splitTexts
                    } else {
                        // If we didn't get the right number back, adjust
                        adjustParagraphCount(splitTexts, batch)
                    }
                    
                    results.addAll(finalTexts)
                }
                
                onProgress(progressEnd)
            }
            
            onProgress(100)
            onSuccess(results)
            
        } catch (e: Exception) {
            onProgress(0)
            println("Ollama translation error: ${e.message}")
            e.printStackTrace()
            onError(UiText.ExceptionString(e))
        }
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
     * Builds a prompt for the LLM based on the translation context
     */
    private fun buildPrompt(
        text: String,
        sourceLang: String,
        targetLang: String,
        context: TranslationContext
    ): String {
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
        
        return """
            Translate the following $contentTypeStr from $sourceLang to $targetLang.
            Use a $toneStr tone.
            $stylePreservation
            Return only the translated text without any additional comments or explanations.
            
            Text to translate:
            $text
        """.trimIndent()
    }
} 