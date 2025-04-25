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
import ireader.i18n.LocalizeHelper
import ireader.i18n.UiText
import ireader.i18n.asString
import ireader.i18n.resources.MR
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
            onError(UiText.MStringResource(MR.strings.no_text_to_translate))
            return
        }
        
        val apiKey = readerPreferences.deepSeekApiKey().get()
        
        if (apiKey.isBlank()) {
            onError(UiText.MStringResource(MR.strings.deepseek_api_key_not_set))
            return
        }
        
        try {
            onProgress(0)
            // Combine all paragraphs into a single request
            val combinedText = texts.joinToString("\n---PARAGRAPH_BREAK---\n")
            val sourceLanguage = if (source == "auto") "the source language" else getLanguageName(source)
            val targetLanguage = getLanguageName(target)
            
            onProgress(20)
            val prompt = buildPrompt(combinedText, sourceLanguage, targetLanguage, context)
            
            onProgress(40)
            try {
                val response = client.default.post("https://api.deepseek.com/v1/chat/completions") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $apiKey")
                    }
                    contentType(ContentType.Application.Json)
                    setBody(DeepSeekRequest(
                        model = "deepseek-chat", // Use appropriate DeepSeek model
                        messages = listOf(
                            Message(role = "system", content = "You are a professional translator specializing in accurate and context-aware translations."),
                            Message(role = "user", content = prompt)
                        ),
                        temperature = 0.2, // Low temperature for more accurate translations
                        max_tokens = 4000
                    ))
                }
                
                // Check the response status code
                if (response.status.value == 402) {
                    println("DeepSeek API error: HTTP 402 Payment Required - Subscription issue or account balance")
                    onError(UiText.MStringResource(MR.strings.deepseek_payment_required))
                    return
                }
                
                onProgress(80)
                val result = response.body<DeepSeekResponse>()
                
                // Detailed debugging of response
                println("DeepSeek API response received: ${result.id}")
                println("DeepSeek choices is null: ${result.choices == null}")
                println("DeepSeek choices is empty: ${result.choices?.isEmpty() ?: true}")
                
                // Add null checks for the choices collection
                if (result.choices != null && result.choices.isNotEmpty()) {
                    // Further null check on message content
                    val choice = result.choices[0]
                    val message = choice.message
                    val messageContent = message?.content
                    
                    if (messageContent != null && messageContent.isNotEmpty()) {
                        val translatedText = messageContent.trim()
                        // Split response back into individual paragraphs
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
                        println("DeepSeek API returned empty message content")
                        onError(UiText.MStringResource(MR.strings.empty_response))
                    }
                } else {
                    println("DeepSeek API returned empty choices array")
                    onError(UiText.MStringResource(MR.strings.empty_response))
                }
            } catch (e: Exception) {
                // Log the network error for debugging
                println("DeepSeek API error: $e")
                e.printStackTrace()
                
                // Determine if it's an authentication/API key issue or a different error
                val errorMessage = when {
                    e.message?.contains("401") == true || e.message?.contains("unauthorized") == true || 
                    e.message?.contains("authentication") == true -> 
                        UiText.MStringResource(MR.strings.deepseek_api_key_invalid)
                    
                    e.message?.contains("402") == true ->
                        UiText.MStringResource(MR.strings.deepseek_api_key_invalid)
                        
                    e.message?.contains("429") == true || e.message?.contains("rate limit") == true ->
                        UiText.MStringResource(MR.strings.api_rate_limit_exceeded)
                    
                    e is NullPointerException && e.message?.contains("Collection.isEmpty()") == true ->
                        UiText.MStringResource(MR.strings.api_response_error)
                        
                    else -> UiText.ExceptionString(e)
                }
                
                onProgress(0) // Reset progress indicator
                onError(errorMessage)
            }
        } catch (e: Exception) {
            onProgress(0) // Reset progress indicator
            println("DeepSeek translation error: $e")
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
    
    private fun buildPrompt(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        context: TranslationContext
    ): String {
        val contentTypeInstruction = when (context.contentType) {
            TranslationContentType.LITERARY -> "This is literary content. DeepSeek, please preserve the literary style, metaphors, and flow."
            TranslationContentType.TECHNICAL -> "This is technical content. DeepSeek, please maintain precise terminology and clarity."
            TranslationContentType.CONVERSATION -> "This is conversational content. DeepSeek, please keep it natural and flowing as spoken language."
            TranslationContentType.POETRY -> "This is poetic content. DeepSeek, please preserve rhythm, rhyme if present, and poetic devices where possible."
            TranslationContentType.ACADEMIC -> "This is academic content. DeepSeek, please maintain formal language and precise terminology."
            else -> "DeepSeek, please translate accurately while maintaining the original meaning."
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
        
        // DeepSeek's specific instruction for better results
        return """
            Translate the following text from $sourceLanguage to $targetLanguage:
            
            $contentTypeInstruction
            $toneInstruction
            $stylePreservation
            
            Important: Maintain the original paragraph breaks (marked by ---PARAGRAPH_BREAK---).
            Only provide the translated text without explanations or additional commentary.
            
            TEXT TO TRANSLATE:
            $text
        """.trimIndent()
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
} 