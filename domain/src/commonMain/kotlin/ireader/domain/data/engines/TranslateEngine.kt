package ireader.domain.data.engines

import ireader.domain.usecases.translate.OpenAITranslateEngine
import ireader.i18n.UiText

abstract class TranslateEngine {

    open val supportedLanguages: List<Pair<String, String>> = listOf(
        "af" to "Afrikaans ",
        "sq" to "Albanian ",
        "ar" to "Arabic ",
        "be" to "Belarusian ",
        "bn" to "Bengali",
        "bg" to "Bulgarian  ",
        "ca" to "Catalan ",
        "zh" to "Chinese",
        "co" to "Corsican ",
        "hr" to "Croatian ",
        "cs" to "Czech ",
        "da" to "Danish ",
        "nl" to "Dutch ",
        "en" to "English ",
        "eo" to "Esperanto ",
        "et" to "Estonian ",
        "tl" to "Filipino ",
        "fi" to "Finnish ",
        "fr" to "French ",
        "gl" to "Galician ",
        "ka" to "Georgian ",
        "de" to "German ",
        "el" to "Greek ",
        "gu" to "Gujarati ",
        "ht" to "Haitian Creole ",
        "iw" to "Hebrew ",
        "hi" to "Hindi ",
        "hu" to "Hungarian ",
        "`is`" to "Icelandic ",
        "id" to "Indonesian ",
        "ga" to "Irish ",
        "it" to "Italian ",
        "ja" to "Japanese ",
        "jw" to "Javanese ",
        "kn" to "Kannada ",
        "ko" to "Korean ",
        "lv" to "Latvian ",
        "lt" to "Lithuanian ",
        "mk" to "Macedonian ",
        "ms" to "Malay ",
        "mt" to "Maltese ",
        "mr" to "Marathi ",
        "no" to "Norwegian ",
        "fa" to "Persian ",
        "pl" to "Polish ",
        "pt" to "Portuguese ",
        "ro" to "Romanian ",
        "ru" to "Russian ",
        "sk" to "Slovak ",
        "sl" to "Slovenian ",
        "es" to "Spanish ",
        "sw" to "Swahili ",
        "sv" to "Swedish ",
        "ta" to "Tamil ",
        "te" to "Telugu ",
        "th" to "Thai ",
        "tr" to "Turkish ",
        "uk" to "Ukrainian ",
        "ur" to "Urdu ",
        "vi" to "Vietnamese ",
        "cy" to "Welsh ",
    )

    open val id: Long = -1
    
    open val engineName: String = "Default"
    
    open val supportsAI: Boolean = false
    
    open val supportsContextAwareTranslation: Boolean = false
    
    open val supportsStylePreservation: Boolean = false
    
    open val requiresApiKey: Boolean = false
    
    /**
     * Maximum characters per request for this engine.
     * Used to chunk large texts before sending to the API.
     * Default: 4000 characters (safe for most APIs)
     */
    open val maxCharsPerRequest: Int = 4000
    
    /**
     * Minimum delay between requests in milliseconds.
     * Used to prevent rate limiting from online APIs.
     * Default: 3000ms (3 seconds) for online engines
     */
    open val rateLimitDelayMs: Long = 3000L
    
    /**
     * Whether this engine is offline/local (no rate limiting needed)
     */
    open val isOffline: Boolean = false
    
    /**
     * Whether this engine requires initialization (e.g., downloading language models)
     */
    open val requiresInitialization: Boolean = false

    abstract suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onProgress: (Int) -> Unit = {},
        onSuccess: (List<String>) -> Unit,
        onError:(UiText) -> Unit
    )
    
    /**
     * Initialize the translation engine (e.g., download language models)
     * @param sourceLanguage Source language code
     * @param targetLanguage Target language code
     * @param onProgress Progress callback (0-100)
     * @param onSuccess Success callback with message
     * @param onError Error callback
     */
    open suspend fun initialize(
        sourceLanguage: String,
        targetLanguage: String,
        onProgress: (Int) -> Unit = {},
        onSuccess: (String) -> Unit = {},
        onError: (UiText) -> Unit = {}
    ) {
        // Default implementation does nothing - override in engines that need initialization
        onSuccess("Engine ready")
    }
    
    /**
     * Enhanced version of translate for AI-powered engines that can preserve style, tone, and context
     */
    open suspend fun translateWithContext(
        texts: List<String>,
        source: String,
        target: String,
        context: TranslationContext,
        onProgress: (Int) -> Unit = {},
        onSuccess: (List<String>) -> Unit,
        onError:(UiText) -> Unit
    ) {
        // Default implementation falls back to regular translation
        translate(texts, source, target, onProgress, onSuccess, onError)
    }
    
    companion object {
        // Define engine IDs as constants for easier reference
        const val BUILT_IN = 0L
        const val GOOGLE = 1L
        const val BING = 2L
        const val OPENAI = 3L
        const val DEEPSEEK = 4L
        const val OLLAMA = 5L
        const val WEBSCRAPING = 6L
        const val DEEPSEEK_WEBVIEW = 7L
        const val GEMINI = 8L
        const val OPENROUTER = 9L
        
        /** The paragraph break marker used in translation prompts */
        const val PARAGRAPH_BREAK_MARKER = "---PARAGRAPH_BREAK---"
        
        /**
         * Sanitize translated text by removing any leftover PARAGRAPH_BREAK markers.
         * 
         * When AI models aren't smart enough, they may output the literal marker text
         * instead of using it as a proper separator. This method cleans up any
         * remaining markers from individual translated paragraphs.
         * 
         * This handles variations like:
         * - "---PARAGRAPH_BREAK---" (exact marker)
         * - "--- PARAGRAPH_BREAK ---" (with spaces)
         * - "---paragraph_break---" (case variations)
         * - Lines that are just the marker with surrounding whitespace/newlines
         */
        fun sanitizeParagraphBreakMarkers(text: String): String {
            if (!text.contains("PARAGRAPH_BREAK", ignoreCase = true)) return text
            
            // Remove the marker pattern (case-insensitive, with optional surrounding dashes/spaces)
            val sanitized = text
                .replace(Regex("""\n?-{2,}\s*PARAGRAPH_BREAK\s*-{2,}\n?""", RegexOption.IGNORE_CASE), "\n")
                .replace(Regex("""\r?\n?-{2,}\s*PARAGRAPH_BREAK\s*-{2,}\r?\n?""", RegexOption.IGNORE_CASE), "\n")
                .trim()
            
            return sanitized
        }
        
        /**
         * Sanitize a list of translated paragraphs by removing any leftover PARAGRAPH_BREAK markers
         * from each paragraph, and splitting any paragraph that still contains markers into
         * separate paragraphs.
         * 
         * @param paragraphs The translated paragraphs to sanitize
         * @return Sanitized list of paragraphs with no marker text remaining
         */
        fun sanitizeTranslatedParagraphs(paragraphs: List<String>): List<String> {
            return paragraphs.flatMap { paragraph ->
                if (paragraph.contains("PARAGRAPH_BREAK", ignoreCase = true)) {
                    // The paragraph still contains markers - split by them first, then clean
                    paragraph
                        .split(Regex("""-{2,}\s*PARAGRAPH_BREAK\s*-{2,}""", RegexOption.IGNORE_CASE))
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                } else {
                    listOf(paragraph)
                }
            }
        }
        
        // Add new engines to the values() method
        fun values(): Array<Long> {
            return arrayOf(BUILT_IN, GOOGLE, BING, OPENAI, DEEPSEEK, OLLAMA, WEBSCRAPING, DEEPSEEK_WEBVIEW, GEMINI, OPENROUTER)
        }
        
        // Map engine ID to name for display
        fun valueOf(id: Long): String {
            return when (id) {
                BUILT_IN -> "BuiltIn"
                GOOGLE -> "Google"
                BING -> "Bing"
                OPENAI -> "OpenAI"
                DEEPSEEK -> "DeepSeek"
                OLLAMA -> "Ollama"
                WEBSCRAPING -> "AI Translation (No API Key)"
                DEEPSEEK_WEBVIEW -> "DeepSeek WebView (No API Key)"
                GEMINI -> "Google Gemini"
                OPENROUTER -> "OpenRouter AI"
                else -> "Unknown"
            }
        }
    }
}

/**
 * Context information for enhanced AI translation
 */
data class TranslationContext(
    val contentType: ContentType = ContentType.GENERAL,
    val preserveStyle: Boolean = false,
    val preserveFormatting: Boolean = true,
    val toneType: ToneType = ToneType.NEUTRAL
)

enum class ContentType {
    GENERAL,
    LITERARY,
    TECHNICAL,
    CONVERSATION,
    POETRY,
    ACADEMIC,
    BUSINESS,
    CREATIVE
}

enum class ToneType {
    NEUTRAL,
    FORMAL,
    CASUAL,
    PROFESSIONAL,
    HUMOROUS,
    FRIENDLY,
    INFORMAL
}