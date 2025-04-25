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

    abstract suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onProgress: (Int) -> Unit = {},
        onSuccess: (List<String>) -> Unit,
        onError:(UiText) -> Unit
    )
    
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
        
        // Add new engines to the values() method
        fun values(): Array<Long> {
            return arrayOf(BUILT_IN, GOOGLE, BING, OPENAI, DEEPSEEK, OLLAMA, WEBSCRAPING)
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