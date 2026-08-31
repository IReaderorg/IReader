package ireader.domain.usecases.translate

import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ireader.core.http.HttpClients
import ireader.domain.data.engines.ContentType as TranslationContentType
import ireader.domain.data.engines.ToneType
import ireader.domain.data.engines.TranslateEngine
import ireader.domain.data.engines.TranslationContext
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.no_text_to_translate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Claude AI Translation Engine
 * Uses Anthropic Messages API for high-fidelity literary and contextual novel translation.
 */
class ClaudeTranslateEngine(
    private val client: HttpClients,
    private val readerPreferences: ReaderPreferences,
) : TranslateEngine() {

    override val id: Long = 13
    override val engineName: String = "Claude AI (Anthropic)"
    override val supportsAI: Boolean = true
    override val supportsContextAwareTranslation: Boolean = true
    override val supportsStylePreservation: Boolean = true
    override val requiresApiKey: Boolean = true

    override val maxCharsPerRequest: Int = 10000
    override val rateLimitDelayMs: Long = 2000L
    override val isOffline: Boolean = false

    override suspend fun generateContent(
        systemPrompt: String,
        userPrompt: String,
        temperature: Float,
        maxTokens: Int
    ): Result<String> {
        val apiKey = readerPreferences.claudeApiKey().get()
        if (apiKey.isBlank()) {
            return Result.failure(Exception("Claude API key not configured"))
        }

        val modelName = readerPreferences.claudeModel().get().ifBlank { "claude-3-5-sonnet-20241022" }

        return try {
            val response = client.default.post("https://api.anthropic.com/v1/messages") {
                headers {
                    append("x-api-key", apiKey)
                    append("anthropic-version", "2023-06-01")
                }
                contentType(ContentType.Application.Json)
                setBody(
                    ClaudeRequest(
                        model = modelName,
                        system = systemPrompt,
                        messages = listOf(
                            ClaudeMessage(role = "user", content = userPrompt)
                        ),
                        temperature = temperature.toDouble().coerceIn(0.0, 1.0),
                        max_tokens = maxTokens
                    )
                )
                timeout {
                    requestTimeoutMillis = 60000
                    connectTimeoutMillis = 15000
                    socketTimeoutMillis = 60000
                }
            }

            if (response.status.value !in 200..299) {
                return Result.failure(Exception("Claude API error: HTTP ${response.status.value}"))
            }

            val result = response.body<ClaudeResponse>()
            val generatedText = result.content?.firstOrNull { it.type == "text" }?.text

            if (generatedText.isNullOrBlank()) {
                Result.failure(Exception("Empty response from Claude API"))
            } else {
                Result.success(generatedText.trim())
            }
        } catch (e: Exception) {
            Result.failure(Exception("Claude API error: ${e.message}"))
        }
    }

    override val supportedLanguages: List<Pair<String, String>> = listOf(
        "auto" to "Auto-detect",
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
        "zh" to "Chinese",
        "hr" to "Croatian",
        "cs" to "Czech",
        "da" to "Danish",
        "nl" to "Dutch",
        "en" to "English",
        "et" to "Estonian",
        "tl" to "Filipino",
        "fi" to "Finnish",
        "fr" to "French",
        "de" to "German",
        "el" to "Greek",
        "gu" to "Gujarati",
        "he" to "Hebrew",
        "hi" to "Hindi",
        "hu" to "Hungarian",
        "is" to "Icelandic",
        "id" to "Indonesian",
        "it" to "Italian",
        "ja" to "Japanese",
        "jv" to "Javanese",
        "kn" to "Kannada",
        "kk" to "Kazakh",
        "km" to "Khmer",
        "ko" to "Korean",
        "la" to "Latin",
        "lv" to "Latvian",
        "lt" to "Lithuanian",
        "ms" to "Malay",
        "ml" to "Malayalam",
        "mr" to "Marathi",
        "mn" to "Mongolian",
        "ne" to "Nepali",
        "no" to "Norwegian",
        "fa" to "Persian",
        "pl" to "Polish",
        "pt" to "Portuguese",
        "pa" to "Punjabi",
        "ro" to "Romanian",
        "ru" to "Russian",
        "sr" to "Serbian",
        "sk" to "Slovak",
        "sl" to "Slovene",
        "es" to "Spanish",
        "sw" to "Swahili",
        "sv" to "Swedish",
        "ta" to "Tamil",
        "te" to "Telugu",
        "th" to "Thai",
        "tr" to "Turkish",
        "uk" to "Ukrainian",
        "ur" to "Urdu",
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
            texts = texts,
            source = source,
            target = target,
            context = TranslationContext(),
            onProgress = onProgress,
            onSuccess = onSuccess,
            onError = onError
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
            onSuccess(emptyList())
            return
        }

        val apiKey = readerPreferences.claudeApiKey().get()
        if (apiKey.isBlank()) {
            onError(UiText.DynamicString("Claude API key is not configured. Please set it in Settings > Translation."))
            return
        }

        try {
            onProgress(10)
            val sourceLangName = if (source == "auto") "Auto-detect" else getLanguageName(source)
            val targetLangName = getLanguageName(target)

            val joinedText = texts.joinToString("\n$PARAGRAPH_BREAK_MARKER\n")
            val systemPrompt = "You are a professional literary translator. Translate accurately while preserving formatting, style, and tone. Maintain the $PARAGRAPH_BREAK_MARKER separator exactly between paragraphs. Output ONLY the translated text without notes or commentary."
            val userPrompt = buildPrompt(joinedText, sourceLangName, targetLangName, context)

            onProgress(40)
            val result = generateContent(systemPrompt, userPrompt, temperature = 0.3f, maxTokens = 4096)
            if (result.isSuccess) {
                val translatedText = result.getOrNull() ?: ""
                val paragraphs = translatedText.split(PARAGRAPH_BREAK_MARKER)
                    .map { sanitizeParagraphBreakMarkers(it).trim() }
                    .filter { it.isNotEmpty() }

                onProgress(100)
                if (paragraphs.size == texts.size) {
                    onSuccess(paragraphs)
                } else {
                    onSuccess(sanitizeTranslatedParagraphs(listOf(translatedText)))
                }
            } else {
                onError(UiText.DynamicString(result.exceptionOrNull()?.message ?: "Unknown Claude translation error"))
            }
        } catch (e: Exception) {
            onError(UiText.DynamicString(e.message ?: "Failed to translate with Claude"))
        }
    }

    private fun buildPrompt(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        context: TranslationContext
    ): String {
        val contentTypeInstruction = when (context.contentType) {
            TranslationContentType.LITERARY -> "Maintain novelistic tone, metaphors, and narrative pacing."
            TranslationContentType.CONVERSATION -> "Keep dialogue fluent, natural, and character-driven."
            TranslationContentType.POETRY -> "Preserve poetic rhythm and imagery."
            else -> "Translate clearly and accurately."
        }

        val toneInstruction = when (context.toneType) {
            ToneType.FORMAL -> "Use a formal tone."
            ToneType.CASUAL -> "Use a casual tone."
            ToneType.HUMOROUS -> "Preserve comedic nuance and wordplay."
            else -> ""
        }

        return "Translate this text from $sourceLanguage to $targetLanguage.\n$contentTypeInstruction $toneInstruction\nKeep $PARAGRAPH_BREAK_MARKER between paragraphs.\n\n$text"
    }

    private fun getLanguageName(languageCode: String): String {
        return supportedLanguages.find { it.first == languageCode }?.second ?: languageCode
    }

    @Serializable
    private data class ClaudeRequest(
        val model: String,
        val system: String,
        val messages: List<ClaudeMessage>,
        val temperature: Double = 0.3,
        val max_tokens: Int = 4096
    )

    @Serializable
    private data class ClaudeMessage(
        val role: String,
        val content: String
    )

    @Serializable
    private data class ClaudeResponse(
        val id: String = "",
        val content: List<ContentBlock>? = null
    )

    @Serializable
    private data class ContentBlock(
        val type: String = "text",
        val text: String = ""
    )
}
