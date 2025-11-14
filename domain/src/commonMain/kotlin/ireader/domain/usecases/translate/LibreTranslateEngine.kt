package ireader.domain.usecases.translate

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ireader.core.http.HttpClients
import ireader.domain.data.engines.TranslateEngine
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * LibreTranslate Engine
 * Uses the free and open-source LibreTranslate API for text translation
 * https://github.com/LibreTranslate/LibreTranslate
 */
class LibreTranslateEngine(
    private val client: HttpClients
) : TranslateEngine() {

    override val id: Long = 4 // Assign a unique ID that hasn't been used
    override val engineName: String = "LibreTranslate"
    override val supportsAI: Boolean = false
    override val requiresApiKey: Boolean = false // Doesn't require API key by default
    
    // LibreTranslate API endpoint - we'll use a public instance
    // You can change this to another instance if needed
    private val apiUrl = "https://lt.neat.computer/translate"
    
    // LibreTranslate supported languages (as of latest info)
    override val supportedLanguages: List<Pair<String, String>> = listOf(
        "auto" to "Auto-detect",
        "ar" to "Arabic",
        "az" to "Azerbaijani",
        "cs" to "Czech",
        "da" to "Danish",
        "de" to "German",
        "el" to "Greek",
        "en" to "English",
        "eo" to "Esperanto",
        "es" to "Spanish",
        "fa" to "Persian",
        "fi" to "Finnish",
        "fr" to "French",
        "ga" to "Irish",
        "he" to "Hebrew",
        "hi" to "Hindi",
        "hu" to "Hungarian",
        "id" to "Indonesian",
        "it" to "Italian",
        "ja" to "Japanese",
        "ko" to "Korean",
        "nl" to "Dutch",
        "pl" to "Polish",
        "pt" to "Portuguese",
        "ru" to "Russian",
        "sk" to "Slovak",
        "sv" to "Swedish",
        "th" to "Thai",
        "tr" to "Turkish",
        "uk" to "Ukrainian",
        "vi" to "Vietnamese",
        "zh" to "Chinese"
    )

    @Serializable
    private data class TranslateRequest(
        val q: String,
        val source: String,
        val target: String,
        val format: String = "text",
        @SerialName("api_key")
        val apiKey: String? = null
    )

    @Serializable
    private data class TranslateResponse(
        @SerialName("translatedText")
        val translatedText: String,
        val detectedLanguage: DetectedLanguage? = null
    )
    
    @Serializable
    private data class DetectedLanguage(
        val confidence: Double,
        val language: String
    )
    
    @Serializable
    private data class ErrorResponse(
        val error: String
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
            println("LibreTranslate error: No text to translate")
            onError(UiText.MStringResource(Res.string.no_text_to_translate))
            return
        }
        
        try {
            onProgress(0)
            val results = mutableListOf<String>()
            
            // Combine shorter texts to minimize API calls
            // LibreTranslate has a character limit, so we batch texts together
            // while keeping the total under a reasonable limit (around 1000 chars)
            val batches = mutableListOf<List<String>>()
            val currentBatch = mutableListOf<String>()
            var currentBatchLength = 0
            val BATCH_CHAR_LIMIT = 1000
            
            for (text in texts) {
                if (text.length > BATCH_CHAR_LIMIT) {
                    // If text is already too long, send it as its own batch
                    if (currentBatch.isNotEmpty()) {
                        batches.add(currentBatch.toList())
                        currentBatch.clear()
                        currentBatchLength = 0
                    }
                    batches.add(listOf(text))
                } else if (currentBatchLength + text.length > BATCH_CHAR_LIMIT) {
                    // Current batch would exceed limit, finalize it and start new one
                    batches.add(currentBatch.toList())
                    currentBatch.clear()
                    currentBatch.add(text)
                    currentBatchLength = text.length
                } else {
                    // Add to current batch
                    currentBatch.add(text)
                    currentBatchLength += text.length
                }
            }
            
            // Add any remaining texts
            if (currentBatch.isNotEmpty()) {
                batches.add(currentBatch.toList())
            }
            
            println("LibreTranslate: Created ${batches.size} batches from ${texts.size} texts")
            
            // Process each batch
            batches.forEachIndexed { batchIndex, batch ->
                try {
                    val progress = ((batchIndex + 1) * 100) / batches.size
                    onProgress(progress)
                    
                    if (batch.size == 1) {
                        // Single text in batch
                        val text = batch[0]
                        
                        // Skip empty text
                        if (text.isBlank()) {
                            results.add(text)
                            return@forEachIndexed
                        }
                        
                        // Fix source language code if "auto"
                        val sourceCode = if (source == "auto") "auto" else source
                        
                        println("LibreTranslate: Translating from $sourceCode to $target")
                        
                        val response = client.default.post(apiUrl) {
                            contentType(ContentType.Application.Json)
                            setBody(TranslateRequest(
                                q = text,
                                source = sourceCode,
                                target = target
                            ))
                        }
                        
                        // Check response status
                        if (response.status.value >= 400) {
                            try {
                                val errorBody = response.body<ErrorResponse>()
                                println("LibreTranslate API error: ${errorBody.error}")
                                throw Exception(errorBody.error)
                            } catch (e: Exception) {
                                println("LibreTranslate error parsing error response: ${e.message}")
                                throw Exception("HTTP Error ${response.status.value}")
                            }
                        }
                        
                        val result = response.body<TranslateResponse>()
                        results.add(result.translatedText)
                    } else {
                        // Multiple texts in batch
                        val combinedText = batch.joinToString("\n---PARAGRAPH_BREAK---\n")
                        
                        // Fix source language code if "auto"
                        val sourceCode = if (source == "auto") "auto" else source
                        
                        println("LibreTranslate: Translating batch from $sourceCode to $target")
                        
                        val response = client.default.post(apiUrl) {
                            contentType(ContentType.Application.Json)
                            setBody(TranslateRequest(
                                q = combinedText,
                                source = sourceCode,
                                target = target
                            ))
                        }
                        
                        // Check response status
                        if (response.status.value >= 400) {
                            try {
                                val errorBody = response.body<ErrorResponse>()
                                println("LibreTranslate API error: ${errorBody.error}")
                                throw Exception(errorBody.error)
                            } catch (e: Exception) {
                                println("LibreTranslate error parsing error response: ${e.message}")
                                throw Exception("HTTP Error ${response.status.value}")
                            }
                        }
                        
                        val result = response.body<TranslateResponse>()
                        // Split result back into separate paragraphs
                        val splitTexts = result.translatedText.split("\n---PARAGRAPH_BREAK---\n")
                        
                        // Ensure we have the correct number of paragraphs
                        val finalTexts = if (splitTexts.size == batch.size) {
                            splitTexts
                        } else {
                            // If we didn't get the right number of paragraphs back,
                            // adjust count to match the original
                            adjustParagraphCount(splitTexts, batch)
                        }
                        
                        results.addAll(finalTexts)
                    }
                    
                } catch (e: Exception) {
                    println("LibreTranslate error for batch $batchIndex: ${e.message}")
                    // Add original texts as fallback
                    results.addAll(batch)
                }
            }
            
            if (results.isEmpty()) {
                onError(UiText.MStringResource(Res.string.empty_response))
            } else {
                onProgress(100)
                onSuccess(results)
            }
        } catch (e: Exception) {
            println("LibreTranslate general error: ${e.message}")
            e.printStackTrace()
            
            val errorMessage = when {
                e.message?.contains("failed to connect") == true || 
                e.message?.contains("connection") == true -> 
                    UiText.MStringResource(Res.string.noInternetError)
                e.message?.contains("character limit") == true ->
                    UiText.DynamicString("Translation failed: Character limit exceeded")
                e.message?.contains("429") == true -> 
                    UiText.MStringResource(Res.string.api_rate_limit_exceeded)
                else -> UiText.ExceptionString(e)
            }
            
            onProgress(0)
            onError(errorMessage)
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
} 