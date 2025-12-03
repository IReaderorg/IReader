package ireader.domain.usecases.translate

import ireader.domain.data.engines.TranslateEngine
import ireader.i18n.UiText
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

/**
 * iOS implementation of GoogleTranslateML
 * 
 * Since ML Kit is not available on iOS, this implementation uses:
 * 1. Google Cloud Translation API (if API key is provided)
 * 2. Free Google Translate web API as fallback
 * 
 * ## Rate Limiting
 * - Implements exponential backoff for rate limit errors (HTTP 429)
 * - Adds delays between batches to avoid triggering rate limits
 * - Retries failed requests up to 3 times
 * 
 * ## Recommendations for Production
 * - Use Google Cloud Translation API with an API key for reliability
 * - Consider Apple's Translation framework (iOS 17.4+)
 * - Consider LibreTranslate (self-hosted) for privacy
 */
@OptIn(ExperimentalForeignApi::class)
actual class GoogleTranslateML actual constructor() : TranslateEngine() {
    
    override val id: Long = TranslateEngine.GOOGLE
    override val engineName: String = "Google Translate (iOS)"
    
    private val httpClient = HttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    
    // Optional: Set your Google Cloud Translation API key
    private var apiKey: String? = null
    
    // Rate limiting configuration
    private var lastRequestTime: Long = 0
    private val minRequestInterval: Long = 100 // Minimum ms between requests
    private val maxRetries = 3
    private val baseRetryDelay: Long = 1000 // Base delay for exponential backoff
    
    /**
     * Set Google Cloud Translation API key for better reliability
     */
    fun setApiKey(key: String) {
        apiKey = key
    }
    
    actual override suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onProgress: (Int) -> Unit,
        onSuccess: (List<String>) -> Unit,
        onError: (UiText) -> Unit
    ) {
        if (texts.isEmpty()) {
            onSuccess(emptyList())
            return
        }
        
        try {
            val results = mutableListOf<String>()
            val total = texts.size
            
            // Translate texts in batches to avoid rate limiting
            val batchSize = if (apiKey != null) 50 else 5 // Smaller batches for free API
            val batches = texts.chunked(batchSize)
            
            batches.forEachIndexed { batchIndex, batch ->
                // Ensure minimum interval between requests
                enforceRateLimit()
                
                val batchResults = try {
                    if (apiKey != null) {
                        translateWithCloudApiRetry(batch, source, target)
                    } else {
                        translateWithFreeApiRetry(batch, source, target)
                    }
                } catch (e: RateLimitException) {
                    println("[GoogleTranslateML] Rate limit exceeded, returning original text")
                    batch // Return original texts on rate limit
                }
                
                results.addAll(batchResults)
                
                // Update progress
                val progress = ((batchIndex + 1) * batchSize).coerceAtMost(total)
                onProgress((progress * 100) / total)
                
                // Delay between batches (longer for free API)
                if (batchIndex < batches.size - 1) {
                    val delayMs = if (apiKey != null) 50L else 500L
                    delay(delayMs)
                }
            }
            
            onSuccess(results)
            
        } catch (e: Exception) {
            println("[GoogleTranslateML] Translation error: ${e.message}")
            onError(UiText.DynamicString("Translation failed: ${e.message}"))
        }
    }
    
    /**
     * Enforce rate limiting by waiting if necessary
     */
    private suspend fun enforceRateLimit() {
        val now = currentTimeMillis()
        val elapsed = now - lastRequestTime
        if (elapsed < minRequestInterval) {
            delay(minRequestInterval - elapsed)
        }
        lastRequestTime = currentTimeMillis()
    }
    
    /**
     * Translate with Cloud API with retry logic
     */
    private suspend fun translateWithCloudApiRetry(
        texts: List<String>,
        source: String,
        target: String
    ): List<String> {
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return translateWithCloudApi(texts, source, target)
            } catch (e: RateLimitException) {
                lastException = e
                val delayMs = baseRetryDelay * (1 shl attempt) // Exponential backoff
                println("[GoogleTranslateML] Rate limited, retrying in ${delayMs}ms (attempt ${attempt + 1}/$maxRetries)")
                delay(delayMs)
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    delay(baseRetryDelay)
                }
            }
        }
        
        throw lastException ?: Exception("Translation failed after $maxRetries attempts")
    }
    
    /**
     * Translate with free API with retry logic
     */
    private suspend fun translateWithFreeApiRetry(
        texts: List<String>,
        source: String,
        target: String
    ): List<String> {
        return texts.map { text ->
            var lastException: Exception? = null
            
            repeat(maxRetries) { attempt ->
                try {
                    return@map translateSingleTextWithRateLimit(text, source, target)
                } catch (e: RateLimitException) {
                    lastException = e
                    val delayMs = baseRetryDelay * (1 shl attempt)
                    println("[GoogleTranslateML] Rate limited, retrying in ${delayMs}ms")
                    delay(delayMs)
                } catch (e: Exception) {
                    lastException = e
                    if (attempt < maxRetries - 1) {
                        delay(baseRetryDelay / 2)
                    }
                }
            }
            
            // Return original text if all retries failed
            println("[GoogleTranslateML] Failed to translate after $maxRetries attempts: ${lastException?.message}")
            text
        }
    }
    
    /**
     * Translate using Google Cloud Translation API
     * Requires API key
     */
    private suspend fun translateWithCloudApi(
        texts: List<String>,
        source: String,
        target: String
    ): List<String> {
        val key = apiKey ?: throw IllegalStateException("API key not set")
        
        val response = httpClient.post("https://translation.googleapis.com/language/translate/v2") {
            parameter("key", key)
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                putJsonArray("q") {
                    texts.forEach { add(it) }
                }
                put("source", source)
                put("target", target)
                put("format", "text")
            }.toString())
        }
        
        // Check for rate limiting
        if (response.status.value == 429) {
            throw RateLimitException("Rate limit exceeded")
        }
        
        if (!response.status.isSuccess()) {
            throw Exception("API error: ${response.status}")
        }
        
        val responseText = response.bodyAsText()
        val jsonResponse = json.parseToJsonElement(responseText).jsonObject
        
        // Check for error in response
        jsonResponse["error"]?.let { error ->
            val errorMessage = error.jsonObject["message"]?.jsonPrimitive?.content ?: "Unknown error"
            val errorCode = error.jsonObject["code"]?.jsonPrimitive?.intOrNull ?: 0
            if (errorCode == 429) {
                throw RateLimitException(errorMessage)
            }
            throw Exception(errorMessage)
        }
        
        val translations = jsonResponse["data"]
            ?.jsonObject?.get("translations")
            ?.jsonArray
            ?.map { it.jsonObject["translatedText"]?.jsonPrimitive?.content ?: "" }
            ?: texts
        
        return translations
    }
    
    /**
     * Translate a single text with rate limit handling
     */
    private suspend fun translateSingleTextWithRateLimit(
        text: String,
        source: String,
        target: String
    ): String {
        if (text.isBlank()) return text
        
        enforceRateLimit()
        
        try {
            val encodedText = text.encodeURLParameter()
            val url = "https://translate.googleapis.com/translate_a/single" +
                    "?client=gtx" +
                    "&sl=$source" +
                    "&tl=$target" +
                    "&dt=t" +
                    "&q=$encodedText"
            
            val response = httpClient.get(url)
            
            // Check for rate limiting
            if (response.status.value == 429) {
                throw RateLimitException("Rate limit exceeded")
            }
            
            if (!response.status.isSuccess()) {
                throw Exception("HTTP error: ${response.status}")
            }
            
            val responseText = response.bodyAsText()
            
            // Check for error responses
            if (responseText.contains("\"error\"") || responseText.startsWith("{")) {
                try {
                    val errorJson = json.parseToJsonElement(responseText).jsonObject
                    errorJson["error"]?.let { error ->
                        val code = error.jsonObject["code"]?.jsonPrimitive?.intOrNull
                        if (code == 429) {
                            throw RateLimitException("Rate limit exceeded")
                        }
                    }
                } catch (e: RateLimitException) {
                    throw e
                } catch (e: Exception) {
                    // Not a JSON error, continue parsing
                }
            }
            
            // Parse the response (it's a nested JSON array)
            val jsonArray = json.parseToJsonElement(responseText).jsonArray
            
            // Extract translated text from the response
            val translations = StringBuilder()
            jsonArray.firstOrNull()?.jsonArray?.forEach { item ->
                item.jsonArray.firstOrNull()?.jsonPrimitive?.content?.let {
                    translations.append(it)
                }
            }
            
            return translations.toString().ifEmpty { text }
            
        } catch (e: RateLimitException) {
            throw e
        } catch (e: Exception) {
            println("[GoogleTranslateML] Single text translation error: ${e.message}")
            return text // Return original text on error
        }
    }
    
    /**
     * URL encode a string
     */
    private fun String.encodeURLParameter(): String {
        return NSString.create(string = this)
            .stringByAddingPercentEncodingWithAllowedCharacters(
                NSCharacterSet.URLQueryAllowedCharacterSet
            ) ?: this
    }
    
    private fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
    
    /**
     * Get supported languages
     */
    suspend fun getSupportedLanguages(): List<LanguageInfo> {
        return listOf(
            LanguageInfo("en", "English"),
            LanguageInfo("es", "Spanish"),
            LanguageInfo("fr", "French"),
            LanguageInfo("de", "German"),
            LanguageInfo("it", "Italian"),
            LanguageInfo("pt", "Portuguese"),
            LanguageInfo("ru", "Russian"),
            LanguageInfo("ja", "Japanese"),
            LanguageInfo("ko", "Korean"),
            LanguageInfo("zh", "Chinese (Simplified)"),
            LanguageInfo("zh-TW", "Chinese (Traditional)"),
            LanguageInfo("ar", "Arabic"),
            LanguageInfo("hi", "Hindi"),
            LanguageInfo("th", "Thai"),
            LanguageInfo("vi", "Vietnamese"),
            LanguageInfo("id", "Indonesian"),
            LanguageInfo("ms", "Malay"),
            LanguageInfo("fil", "Filipino"),
            LanguageInfo("tr", "Turkish"),
            LanguageInfo("pl", "Polish"),
            LanguageInfo("nl", "Dutch"),
            LanguageInfo("sv", "Swedish"),
            LanguageInfo("da", "Danish"),
            LanguageInfo("no", "Norwegian"),
            LanguageInfo("fi", "Finnish"),
            LanguageInfo("cs", "Czech"),
            LanguageInfo("el", "Greek"),
            LanguageInfo("he", "Hebrew"),
            LanguageInfo("uk", "Ukrainian"),
            LanguageInfo("ro", "Romanian"),
            LanguageInfo("hu", "Hungarian"),
            LanguageInfo("bg", "Bulgarian"),
            LanguageInfo("hr", "Croatian"),
            LanguageInfo("sk", "Slovak"),
            LanguageInfo("sl", "Slovenian"),
            LanguageInfo("lt", "Lithuanian"),
            LanguageInfo("lv", "Latvian"),
            LanguageInfo("et", "Estonian")
        )
    }
    
    /**
     * Detect language of text
     */
    suspend fun detectLanguage(text: String): String? {
        if (text.isBlank()) return null
        
        try {
            enforceRateLimit()
            
            val encodedText = text.take(100).encodeURLParameter()
            val url = "https://translate.googleapis.com/translate_a/single" +
                    "?client=gtx" +
                    "&sl=auto" +
                    "&tl=en" +
                    "&dt=t" +
                    "&q=$encodedText"
            
            val response = httpClient.get(url)
            
            if (response.status.value == 429) {
                println("[GoogleTranslateML] Rate limited during language detection")
                return null
            }
            
            val responseText = response.bodyAsText()
            val jsonArray = json.parseToJsonElement(responseText).jsonArray
            
            // The detected language is usually in the second element
            return jsonArray.getOrNull(2)?.jsonPrimitive?.content
            
        } catch (e: Exception) {
            println("[GoogleTranslateML] Language detection error: ${e.message}")
            return null
        }
    }
}

/**
 * Exception for rate limit errors
 */
private class RateLimitException(message: String) : Exception(message)

/**
 * Data class for language information
 */
data class LanguageInfo(
    val code: String,
    val name: String
)
