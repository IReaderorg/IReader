package ireader.domain.data.engines

import ireader.domain.usecases.translate.OpenAITranslateEngine
import ireader.i18n.UiText

abstract class TranslateEngine {

    open val supportedLanguages: List<Pair<String, String>> = listOf(
        "af" to "Afrikaans",
        "sq" to "Albanian",
        "am" to "Amharic",
        "ar" to "Arabic",
        "hy" to "Armenian",
        "az" to "Azerbaijani",
        "eu" to "Basque",
        "be" to "Belarusian",
        "bn" to "Bengali",
        "bs" to "Bosnian",
        "bg" to "Bulgarian",
        "my" to "Burmese",
        "ca" to "Catalan",
        "ny" to "Chichewa",
        "zh" to "Chinese",
        "co" to "Corsican",
        "hr" to "Croatian",
        "cs" to "Czech",
        "da" to "Danish",
        "nl" to "Dutch",
        "en" to "English",
        "eo" to "Esperanto",
        "et" to "Estonian",
        "tl" to "Filipino",
        "fi" to "Finnish",
        "fr" to "French",
        "gl" to "Galician",
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
        "jv" to "Javanese",
        "kn" to "Kannada",
        "kk" to "Kazakh",
        "km" to "Khmer",
        "rw" to "Kinyarwanda",
        "ko" to "Korean",
        "ku" to "Kurdish",
        "ky" to "Kyrgyz",
        "lo" to "Lao",
        "la" to "Latin",
        "lv" to "Latvian",
        "lt" to "Lithuanian",
        "lb" to "Luxembourgish",
        "mk" to "Macedonian",
        "mg" to "Malagasy",
        "ms" to "Malay",
        "ml" to "Malayalam",
        "mt" to "Maltese",
        "mi" to "Māori",
        "mr" to "Marathi",
        "mn" to "Mongolian",
        "ne" to "Nepali",
        "no" to "Norwegian",
        "or" to "Oriya",
        "ps" to "Pashto",
        "fa" to "Persian",
        "pl" to "Polish",
        "pt" to "Portuguese",
        "pa" to "Punjabi",
        "ro" to "Romanian",
        "ru" to "Russian",
        "sm" to "Samoan",
        "sr" to "Serbian",
        "st" to "Sesotho",
        "sn" to "Shona",
        "sd" to "Sindhi",
        "si" to "Sinhala",
        "sk" to "Slovak",
        "sl" to "Slovene",
        "so" to "Somali",
        "es" to "Spanish",
        "su" to "Sundanese",
        "sw" to "Swahili",
        "sv" to "Swedish",
        "tg" to "Tajik",
        "ta" to "Tamil",
        "tt" to "Tatar",
        "te" to "Telugu",
        "th" to "Thai",
        "tr" to "Turkish",
        "tk" to "Turkmen",
        "uk" to "Ukrainian",
        "ur" to "Urdu",
        "ug" to "Uyghur",
        "uz" to "Uzbek",
        "vi" to "Vietnamese",
        "cy" to "Welsh",
        "fy" to "Western Frisian",
        "xh" to "Xhosa",
        "yi" to "Yiddish",
        "yo" to "Yoruba",
        "zu" to "Zulu",
    ).sortedBy { it.second }

    open val id: Long = -1
    
    open val engineName: String = "Default"
    
    open val supportsAI: Boolean = false
    
    open val supportsContextAwareTranslation: Boolean = false
    
    open val supportsStylePreservation: Boolean = false
    
    open val requiresApiKey: Boolean = false
    
    /**
     * Default maximum characters per request for this engine.
     */
    open val defaultMaxCharsPerRequest: Int = 4000

    /**
     * Maximum characters per request for this engine.
     * Used to chunk large texts before sending to the API.
     * Default: 4000 characters (safe for most APIs)
     */
    open val maxCharsPerRequest: Int = 4000
    
    /**
     * Maximum paragraphs per request for this engine.
     * Used in conjunction with maxCharsPerRequest to prevent sending too many
     * paragraphs at once to LLMs, which causes token limit exhaustion, timeouts,
     * or dropped paragraph break markers.
     * Default: 12 paragraphs per chunk.
     */
    open val maxParagraphsPerRequest: Int = DEFAULT_MAX_PARAGRAPHS_PER_CHUNK
    
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
    
    /**
     * Generate content using AI (for engines that support it)
     * This is used for tasks like generating image prompts from text
     * 
     * @param systemPrompt The system instruction for the AI
     * @param userPrompt The user's request/input
     * @param temperature Creativity level (0.0-1.0, default 0.7)
     * @param maxTokens Maximum tokens to generate (default 500)
     * @return Result with generated text or error
     */
    open suspend fun generateContent(
        systemPrompt: String,
        userPrompt: String,
        temperature: Float = 0.7f,
        maxTokens: Int = 500
    ): Result<String> {
        // Default implementation returns error - only AI engines should override this
        return Result.failure(Exception("Content generation not supported by this engine"))
    }
    
    /**
     * Adjusts the number of translated paragraphs to match the original count.
     * Default implementation delegates to companion adjustParagraphCount.
     */
    open fun adjustParagraphCount(translatedParagraphs: List<String>, originalTexts: List<String>): List<String> {
        return Companion.adjustParagraphCount(translatedParagraphs, originalTexts)
    }

    companion object {
        // Define engine IDs as constants for easier reference
        const val BUILT_IN = 0L
        const val ML_KIT = 0L
        const val GOOGLE = 1L
        const val BING = 2L
        const val OPENAI = 2L
        const val DEEPSEEK = 3L
        const val LIBRE = 4L
        const val OLLAMA = 5L
        const val WEBSCRAPING = 6L
        const val DEEPSEEK_WEBVIEW = 7L
        const val FREE_AI = 7L
        const val GEMINI = 8L
        const val OPENROUTER = 9L
        const val NVIDIA = 10L
        const val GOOGLE_FREE = 11L
        const val GEMINI_NANO = 12L
        const val CLAUDE = 13L
        
        /** The paragraph break marker used in translation prompts */
        const val PARAGRAPH_BREAK_MARKER = "---PARAGRAPH_BREAK---"
        
        /**
         * Comprehensive regex matching any variation of paragraph break markers:
         * e.g. "---PARAGRAPH_BREAK---", "--PARAGRAPH_BREAK__", "__PARAGRAPH_BREAK__",
         * "[PARAGRAPH_BREAK]", "--- PARAGRAPH BREAK ---", "---paragraph_break---", etc.
         */
        val PARAGRAPH_BREAK_REGEX = Regex(
            """\r?\n?[-_*#=\s\[\]]*PARAGRAPH[\s_-]*BREAK[-_*#=\s\[\]]*\r?\n?""",
            RegexOption.IGNORE_CASE
        )
        
        /**
         * Sanitize translated text by removing any leftover PARAGRAPH_BREAK markers.
         * 
         * When AI models output the literal marker text instead of using it as a
         * proper separator, this cleans up any remaining markers from individual paragraphs.
         * 
         * This handles variations like:
         * - "---PARAGRAPH_BREAK---" (exact marker)
         * - "--PARAGRAPH_BREAK__" (mixed dashes and underscores)
         * - "__PARAGRAPH_BREAK__" (underscores)
         * - "--- PARAGRAPH BREAK ---" (with spaces)
         * - "[PARAGRAPH_BREAK]" (with brackets)
         * - Lines that are just the marker with surrounding whitespace/newlines
         */
        fun sanitizeParagraphBreakMarkers(text: String): String {
            if (!text.contains("PARAGRAPH", ignoreCase = true) || !text.contains("BREAK", ignoreCase = true)) return text
            
            val sanitized = text
                .replace(PARAGRAPH_BREAK_REGEX, "\n")
                .replace(Regex("""[-_*#=\s\[\]]*PARAGRAPH[\s_-]*BREAK[-_*#=\s\[\]]*""", RegexOption.IGNORE_CASE), "")
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
                if (paragraph.contains("PARAGRAPH", ignoreCase = true) && paragraph.contains("BREAK", ignoreCase = true)) {
                    // The paragraph still contains markers - split by them first, then clean
                    paragraph
                        .split(PARAGRAPH_BREAK_REGEX)
                        .map { sanitizeParagraphBreakMarkers(it).trim() }
                        .filter { it.isNotEmpty() }
                } else {
                    listOf(sanitizeParagraphBreakMarkers(paragraph).trim()).filter { it.isNotEmpty() }
                }
            }
        }
        
        /**
         * Splits a raw AI translation response by paragraph break markers,
         * handling all symbol and whitespace variations.
         * Falls back to newline splitting if markers were stripped by the model.
         */
        fun splitByParagraphMarkers(response: String, expectedCount: Int = 0): List<String> {
            val text = response.trim()
            if (text.contains("PARAGRAPH", ignoreCase = true) && text.contains("BREAK", ignoreCase = true)) {
                val split = text
                    .split(PARAGRAPH_BREAK_REGEX)
                    .map { sanitizeParagraphBreakMarkers(it).trim() }
                    .filter { it.isNotEmpty() }
                if (split.size == expectedCount || expectedCount <= 0 || split.size > 1) {
                    return split
                }
            }
            
            // If markers weren't used or splitting yielded 1 item when multiple were expected,
            // check if double newlines or single newlines can split the text into expectedCount
            if (expectedCount > 1) {
                val byDoubleNewline = text.split(Regex("""\r?\n\s*\r?\n"""))
                    .map { sanitizeParagraphBreakMarkers(it).trim() }
                    .filter { it.isNotEmpty() }
                if (byDoubleNewline.size == expectedCount) {
                    return byDoubleNewline
                }
                val bySingleNewline = text.lines()
                    .map { sanitizeParagraphBreakMarkers(it).trim() }
                    .filter { it.isNotEmpty() }
                if (bySingleNewline.size == expectedCount) {
                    return bySingleNewline
                }
                if (bySingleNewline.size >= expectedCount) {
                    val result = mutableListOf<String>()
                    val linesPerParagraph = bySingleNewline.size / expectedCount
                    for (i in 0 until expectedCount) {
                        val start = i * linesPerParagraph
                        val end = if (i == expectedCount - 1) bySingleNewline.size else (i + 1) * linesPerParagraph
                        if (start < bySingleNewline.size) {
                            result.add(bySingleNewline.subList(start, end.coerceAtMost(bySingleNewline.size)).joinToString("\n"))
                        }
                    }
                    if (result.size == expectedCount) {
                        return result
                    }
                }
            }
            
            return listOf(sanitizeParagraphBreakMarkers(text).trim()).filter { it.isNotEmpty() }
        }
        
        /**
         * Adjusts the number of translated paragraphs to match the original count.
         * If too few paragraphs were produced, attempts to split multi-line paragraphs
         * first before falling back to original text.
         */
        fun adjustParagraphCount(translatedParagraphs: List<String>, originalTexts: List<String>): List<String> {
            if (translatedParagraphs.size == originalTexts.size) return translatedParagraphs
            if (translatedParagraphs.isEmpty()) return originalTexts
            
            val result = translatedParagraphs.toMutableList()
            
            // If we have only 1 paragraph but expected multiple, try to split lines
            if (result.size == 1 && originalTexts.size > 1) {
                val lines = result[0].lines().map { it.trim() }.filter { it.isNotEmpty() }
                if (lines.size >= originalTexts.size) {
                    val splitResult = mutableListOf<String>()
                    val linesPerParagraph = lines.size / originalTexts.size
                    for (i in 0 until originalTexts.size) {
                        val start = i * linesPerParagraph
                        val end = if (i == originalTexts.size - 1) lines.size else (i + 1) * linesPerParagraph
                        if (start < lines.size) {
                            splitResult.add(lines.subList(start, end.coerceAtMost(lines.size)).joinToString("\n"))
                        }
                    }
                    if (splitResult.size == originalTexts.size) {
                        return splitResult
                    }
                }
            }
            
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
        
        /** Default maximum paragraphs per chunk for AI/LLM translation */
        const val DEFAULT_MAX_PARAGRAPHS_PER_CHUNK = 12
        
        /**
         * Chunks a list of paragraphs so that each chunk's combined character count
         * does not exceed [maxChars], AND the number of paragraphs does not exceed [maxParagraphs].
         * Always includes at least one paragraph per chunk.
         */
        fun chunkTexts(
            texts: List<String>,
            maxChars: Int,
            maxParagraphs: Int = DEFAULT_MAX_PARAGRAPHS_PER_CHUNK
        ): List<List<String>> {
            if (texts.isEmpty()) return emptyList()
            val chunks = mutableListOf<List<String>>()
            var currentChunk = mutableListOf<String>()
            var currentLen = 0
            val effectiveMaxParagraphs = if (maxParagraphs > 0) maxParagraphs else DEFAULT_MAX_PARAGRAPHS_PER_CHUNK

            for (text in texts) {
                val exceedsChars = currentChunk.isNotEmpty() && currentLen + text.length > maxChars
                val exceedsParagraphs = currentChunk.size >= effectiveMaxParagraphs

                if (exceedsChars || exceedsParagraphs) {
                    chunks.add(currentChunk)
                    currentChunk = mutableListOf()
                    currentLen = 0
                }
                currentChunk.add(text)
                currentLen += text.length
            }
            if (currentChunk.isNotEmpty()) {
                chunks.add(currentChunk)
            }
            return chunks
        }
        
        /**
         * Chunks a list of paragraphs so that each chunk's combined character count
         * does not exceed [maxChars], and paragraph count does not exceed [maxParagraphs].
         * Always includes at least one paragraph per chunk.
         */
        fun chunkTextsByMaxChars(
            texts: List<String>,
            maxChars: Int,
            maxParagraphs: Int = DEFAULT_MAX_PARAGRAPHS_PER_CHUNK
        ): List<List<String>> {
            return chunkTexts(texts, maxChars, maxParagraphs)
        }
        
        // Add new engines to the values() method
        fun values(): Array<Long> {
            return arrayOf(
                BUILT_IN,
                GOOGLE_FREE,
                GEMINI_NANO,
                CLAUDE,
                GEMINI,
                OPENAI,
                DEEPSEEK,
                OPENROUTER,
                NVIDIA,
                OLLAMA,
                LIBRE,
                FREE_AI
            )
        }
        
        // Map engine ID to name for display
        fun valueOf(id: Long): String {
            return when (id) {
                BUILT_IN -> "Google ML Kit (Offline)"
                GOOGLE_FREE -> "Google Translate (Free)"
                GEMINI_NANO -> "Gemini Nano (Offline)"
                CLAUDE -> "Claude AI (Anthropic)"
                GEMINI -> "Google Gemini"
                OPENAI -> "OpenAI (ChatGPT)"
                DEEPSEEK -> "DeepSeek AI"
                OPENROUTER -> "OpenRouter AI"
                NVIDIA -> "NVIDIA NIM"
                OLLAMA -> "Ollama (Local LLM)"
                LIBRE -> "LibreTranslate"
                FREE_AI -> "AI Translation (No API Key)"
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