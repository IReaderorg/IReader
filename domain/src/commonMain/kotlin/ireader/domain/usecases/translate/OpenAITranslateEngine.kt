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
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class OpenAITranslateEngine(
    private val client: HttpClients,
    private val readerPreferences: ReaderPreferences,
) : TranslateEngine() {

    override val id: Long = 2
    override val engineName: String = "OpenAI (GPT)"
    override val supportsAI: Boolean = true
    override val supportsContextAwareTranslation: Boolean = true
    override val supportsStylePreservation: Boolean = true
    override val requiresApiKey: Boolean = true
    
    // GPT-4 has 8k token context, GPT-3.5 has 4k
    // ~4 chars per token, so ~6000 chars is safe
    override val maxCharsPerRequest: Int = 6000
    
    // OpenAI rate limits vary by tier, 3 seconds is safe for most
    override val rateLimitDelayMs: Long = 3000L
    
    override val isOffline: Boolean = false
    
    /**
     * Generate content using OpenAI API
     */
    override suspend fun generateContent(
        systemPrompt: String,
        userPrompt: String,
        temperature: Float,
        maxTokens: Int
    ): Result<String> {
        val apiKey = readerPreferences.openAIApiKey().get()
        if (apiKey.isBlank()) {
            return Result.failure(Exception("OpenAI API key not configured"))
        }
        
        return try {
            val response = client.default.post("https://api.openai.com/v1/chat/completions") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $apiKey")
                }
                contentType(ContentType.Application.Json)
                setBody(OpenAIRequest(
                    model = "gpt-3.5-turbo",
                    messages = listOf(
                        Message(role = "system", content = systemPrompt),
                        Message(role = "user", content = userPrompt)
                    ),
                    temperature = temperature.toDouble(),
                    max_tokens = maxTokens
                ))
            }
            
            if (response.status.value !in 200..299) {
                return Result.failure(Exception("OpenAI API error: HTTP ${response.status.value}"))
            }
            
            val result = response.body<OpenAIResponse>()
            val generatedText = result.choices?.firstOrNull()?.message?.content
            
            if (generatedText.isNullOrBlank()) {
                Result.failure(Exception("Empty response from OpenAI API"))
            } else {
                Result.success(generatedText.trim())
            }
        } catch (e: Exception) {
            Result.failure(Exception("OpenAI API error: ${e.message}"))
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
        // Validate inputs and handle empty or null lists
        if (texts.isNullOrEmpty()) {
            onError(UiText.MStringResource(Res.string.no_text_to_translate))
            return
        }
        
        val apiKey = readerPreferences.openAIApiKey().get()
        
        if (apiKey.isBlank()) {
            onError(UiText.MStringResource(Res.string.openai_api_key_not_set))
            return
        }
        
        try {
            onProgress(0)
            // Combine all paragraphs into a single text
            val combinedText = texts.joinToString("\n---PARAGRAPH_BREAK---\n")
            val sourceLanguage = if (source == "auto") "the source language" else getLanguageName(source)
            val targetLanguage = getLanguageName(target)
            
            onProgress(20)
            val prompt = buildPrompt(combinedText, sourceLanguage, targetLanguage, context)
            
            onProgress(40)
            try {
                val response = client.default.post("https://api.openai.com/v1/chat/completions") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $apiKey")
                    }
                    contentType(ContentType.Application.Json)
                    setBody(OpenAIRequest(
                        model = "gpt-3.5-turbo", // or "gpt-4" for better quality
                        messages = listOf(
                            Message(role = "system", content = "You are a professional translator with expertise in multiple languages."),
                            Message(role = "user", content = prompt)
                        ),
                        temperature = 0.3, // Lower for more predictable translations
                        max_tokens = 4000
                    ))
                }
                
                // Check the response status code
                if (response.status.value == 402 || response.status.value == 429) {
                    println("OpenAI API error: HTTP ${response.status.value} - Quota exceeded or rate limited")
                    onError(UiText.MStringResource(Res.string.openai_quota_exceeded))
                    return
                } else if (response.status.value == 401) {
                    println("OpenAI API error: HTTP 401 - Invalid API key")
                    onError(UiText.MStringResource(Res.string.openai_api_key_invalid))
                    return
                }
                
                onProgress(80)
                val result = response.body<OpenAIResponse>()
                
                // Detailed debugging of response
                println("OpenAI API response received: ${result.id}")
                println("OpenAI choices is null: ${result.choices == null}")
                println("OpenAI choices is empty: ${result.choices?.isEmpty() ?: true}")
                
                // Add null checks for the choices collection
                if (result.choices != null && result.choices.isNotEmpty()) {
                    // Further null check on message content
                    val choice = result.choices[0]
                    val message = choice.message
                    val messageContent = message?.content
                    
                    if (messageContent != null && messageContent.isNotEmpty()) {
                        val translatedText = messageContent.trim()
                        // Split the response back into individual paragraphs
                        val splitTexts = translatedText.split("\n---PARAGRAPH_BREAK---\n")
                        
                        // Ensure we have the right number of paragraphs to match input
                        val finalTexts = if (splitTexts.size == texts.size) {
                            splitTexts
                        } else {
                            // Adjust the paragraph count to match input
                            adjustParagraphCount(splitTexts, texts)
                        }
                        
                        onProgress(100)
                        onSuccess(finalTexts)
                    } else {
                        println("OpenAI API returned empty message content")
                        onError(UiText.MStringResource(Res.string.empty_response))
                    }
                } else {
                    println("OpenAI API returned empty choices array")
                    onError(UiText.MStringResource(Res.string.empty_response))
                }
            } catch (e: Exception) {
                // Log the network error for debugging
                println("OpenAI API error: $e")
                e.printStackTrace()
                
                // Determine if it's an authentication/API key issue or a different error
                val errorMessage = when {
                    e.message?.contains("401") == true || e.message?.contains("unauthorized") == true || 
                    e.message?.contains("authentication") == true || e.message?.contains("invalid_api_key") == true -> 
                        UiText.MStringResource(Res.string.openai_api_key_invalid)
                    
                    e.message?.contains("429") == true || e.message?.contains("rate limit") == true ->
                        UiText.MStringResource(Res.string.api_rate_limit_exceeded)
                        
                    e.message?.contains("insufficient_quota") == true || e.message?.contains("402") == true ->
                        UiText.MStringResource(Res.string.openai_quota_exceeded)
                        
                    e is NullPointerException && e.message?.contains("Collection.isEmpty()") == true ->
                        UiText.MStringResource(Res.string.api_response_error)
                        
                    else -> UiText.ExceptionString(e)
                }
                
                onProgress(0) // Reset progress indicator
                onError(errorMessage)
            }
        } catch (e: Exception) {
            onProgress(0) // Reset progress indicator
            println("OpenAI translation error: $e")
            e.printStackTrace()
            onError(UiText.ExceptionString(e))
        }
    }
    
    private fun buildPrompt(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        context: TranslationContext
    ): String {
        val contentTypeInstruction = when (context.contentType) {
            TranslationContentType.LITERARY -> "This is literary content. Preserve the literary style, metaphors, and flow."
            TranslationContentType.TECHNICAL -> "This is technical content. Maintain precise terminology and clarity."
            TranslationContentType.CONVERSATION -> "This is conversational content. Keep it natural and flowing as spoken language."
            TranslationContentType.POETRY -> "This is poetic content. Preserve rhythm, rhyme if present, and poetic devices where possible."
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
        
        val formattingPreservation = if (context.preserveFormatting) {
            "Maintain the original formatting, including paragraph breaks (marked by ---PARAGRAPH_BREAK---)."
        } else {
            "You may adjust formatting for readability in the target language."
        }
        
        // Get custom prompt from preferences
        val customPrompt = readerPreferences.translationCustomPrompt().get()
        val customInstruction = if (customPrompt.isNotBlank()) {
            "\n\nAdditional instructions: $customPrompt"
        } else {
            ""
        }
        
        return """
            Translate the following text from $sourceLanguage to $targetLanguage:
            
            $contentTypeInstruction
            $toneInstruction
            $stylePreservation
            $formattingPreservation$customInstruction
            
            Please translate only the content and do not add any explanations or notes.
            
            TEXT TO TRANSLATE:
            $text
        """.trimIndent()
    }
    
    private fun getLanguageName(languageCode: String): String {
        return supportedLanguages.find { it.first == languageCode }?.second ?: languageCode
    }

    @Serializable
    private data class OpenAIRequest(
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
    private data class OpenAIResponse(
        val id: String = "",
        val choices: List<Choice>? = null
    )

    @Serializable
    private data class Choice(
        val message: Message? = null,
        @SerialName("finish_reason")
        val finishReason: String = ""
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
} 