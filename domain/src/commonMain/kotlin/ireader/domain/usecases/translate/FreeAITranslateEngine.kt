package ireader.domain.usecases.translate

import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import ireader.core.http.HttpClients
import ireader.domain.data.engines.TranslateEngine
import ireader.domain.data.engines.TranslationContext
import ireader.i18n.UiText
import ireader.i18n.resources.MR
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * FreeAITranslateEngine
 * Uses the Hugging Face Inference API to access free translation models
 */
class FreeAITranslateEngine(
    private val client: HttpClients
) : TranslateEngine() {

    override val id: Long = 5 // Assign a unique ID that hasn't been used
    override val engineName: String = "Free AI Translation"
    override val supportsAI: Boolean = true
    override val requiresApiKey: Boolean = false
    
    // We're using a smaller set of the most common languages for this free API
    override val supportedLanguages: List<Pair<String, String>> = listOf(
        "auto" to "Auto-detect",
        "en" to "English",
        "zh" to "Chinese",
        "es" to "Spanish",
        "fr" to "French",
        "de" to "German",
        "ja" to "Japanese",
        "ko" to "Korean",
        "pt" to "Portuguese",
        "ru" to "Russian",
        "it" to "Italian"
    )

    // Use the Helsinki-NLP free model hosted on Hugging Face
    private val apiUrl = "https://api-inference.huggingface.co/models/Helsinki-NLP/opus-mt-{SRC}-{TGT}"
    
    // Map of language codes for the Helsinki-NLP models
    private val languageMappings = mapOf(
        "en" to "en",
        "zh" to "zh",
        "es" to "es",
        "fr" to "fr",
        "de" to "de",
        "ja" to "ja",
        "ko" to "ko",
        "pt" to "pt",
        "ru" to "ru",
        "it" to "it"
    )

    @Serializable
    private data class TranslationRequest(
        val inputs: String
    )

    @Serializable
    private data class TranslationResponse(
        @SerialName("translation_text")
        val translationText: String? = null,
        @SerialName("generated_text")
        val generatedText: String? = null
    )

    override suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onProgress: (Int) -> Unit,
        onSuccess: (List<String>) -> Unit,
        onError: (UiText) -> Unit
    ) {
        // Validate inputs
        if (texts.isNullOrEmpty()) {
            onError(UiText.MStringResource(MR.strings.no_text_to_translate))
            return
        }
        
        try {
            onProgress(0)
            val results = mutableListOf<String>()
            val total = texts.size
            
            // Handle auto-detection (in a real implementation, this would use a language detection service)
            val sourceCode = if (source == "auto") "en" else source
            
            // Get the appropriate language codes for the model
            val srcLang = languageMappings[sourceCode] ?: "en"
            val tgtLang = languageMappings[target] ?: "en"
            
            // Skip translation if source and target are the same
            if (srcLang == tgtLang) {
                onSuccess(texts)
                return
            }
            
            // Replace placeholders in the URL
            val modelUrl = apiUrl
                .replace("{SRC}", srcLang)
                .replace("{TGT}", tgtLang)
            
            // Translate each text individually
            texts.forEachIndexed { index, text ->
                try {
                    val progress = ((index + 1) * 100) / total
                    onProgress(progress)
                    
                    // Skip empty text
                    if (text.isBlank()) {
                        results.add(text)
                        return@forEachIndexed
                    }
                    
                    println("FreeAI: Translating from $srcLang to $tgtLang")
                    
                    // Use Hugging Face Inference API (free tier)
                    val response = client.default.post(modelUrl) {
                        contentType(ContentType.Application.Json)
                        setBody(TranslationRequest(inputs = text))
                    }
                    
                    // Try to parse as array first, then as single object
                    val translatedText = try {
                        val resultList = response.body<List<TranslationResponse>>()
                        resultList.firstOrNull()?.let { 
                            it.translationText ?: it.generatedText 
                        }
                    } catch (e: Exception) {
                        // If array parsing fails, try single object
                        try {
                            val result = response.body<TranslationResponse>()
                            result.translationText ?: result.generatedText
                        } catch (e2: Exception) {
                            println("Failed to parse response: ${e2.message}")
                            null
                        }
                    }
                    
                    if (translatedText != null && translatedText.isNotBlank()) {
                        results.add(translatedText)
                    } else {
                        // If response parsing fails, fall back to original text
                        results.add(text)
                    }
                    
                } catch (e: Exception) {
                    println("FreeAI translation error for text at index $index: ${e.message}")
                    e.printStackTrace()
                    // Add original text as fallback
                    results.add(text)
                }
            }
            
            if (results.isEmpty()) {
                onError(UiText.MStringResource(MR.strings.empty_response))
            } else {
                onProgress(100)
                onSuccess(results)
            }
        } catch (e: Exception) {
            println("FreeAI general error: ${e.message}")
            e.printStackTrace()
            
            val errorMessage = when {
                e.message?.contains("failed to connect") == true || 
                e.message?.contains("connection") == true -> 
                    UiText.MStringResource(MR.strings.noInternetError)
                e.message?.contains("429") == true -> 
                    UiText.MStringResource(MR.strings.api_rate_limit_exceeded)
                else -> UiText.ExceptionString(e)
            }
            
            onProgress(0)
            onError(errorMessage)
        }
    }
    
    // This engine doesn't support context-aware translation so we'll keep the default implementation
    // which falls back to the regular translate method
} 