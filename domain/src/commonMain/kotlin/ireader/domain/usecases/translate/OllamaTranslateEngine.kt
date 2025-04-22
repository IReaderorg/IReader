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
            
            // Process each text chunk
            val results = mutableListOf<String>()
            val totalTexts = texts.size
            
            for ((index, text) in texts.withIndex()) {
                // Generate the appropriate prompt based on context
                val prompt = buildPrompt(text, sourceLang, targetLang, context)
                
                val request = OllamaRequest(
                    model = model,
                    prompt = prompt,
                    options = OllamaOptions(temperature = 0.1f)
                )
                
                // Calculate progress percentage
                val progressStart = 20 + (index * 70 / totalTexts)
                val progressEnd = 20 + ((index + 1) * 70 / totalTexts)
                onProgress(progressStart)
                
                // Make the API request
                val response = client.default.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body<OllamaResponse>()
                
                // Add the response to results
                results.add(response.response.trim())
                
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