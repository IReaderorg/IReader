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
 * Note: For production use, consider using:
 * - Apple's Translation framework (iOS 17.4+)
 * - DeepL API
 * - LibreTranslate (self-hosted)
 */
@OptIn(ExperimentalForeignApi::class)
actual class GoogleTranslateML actual constructor() : TranslateEngine() {
    
    override val id: Long = TranslateEngine.GOOGLE
    override val engineName: String = "Google Translate (iOS)"
    
    private val httpClient = HttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    
    // Optional: Set your Google Cloud Translation API key
    private var apiKey: String? = null
    
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
            val batchSize = 10
            val batches = texts.chunked(batchSize)
            
            batches.forEachIndexed { batchIndex, batch ->
                val batchResults = if (apiKey != null) {
                    translateWithCloudApi(batch, source, target)
                } else {
                    translateWithFreeApi(batch, source, target)
                }
                
                results.addAll(batchResults)
                
                // Update progress
                val progress = ((batchIndex + 1) * batchSize).coerceAtMost(total)
                onProgress((progress * 100) / total)
                
                // Small delay between batches to avoid rate limiting
                if (batchIndex < batches.size - 1) {
                    delay(100)
                }
            }
            
            onSuccess(results)
            
        } catch (e: Exception) {
            println("[GoogleTranslateML] Translation error: ${e.message}")
            onError(UiText.DynamicString("Translation failed: ${e.message}"))
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
        
        val responseText = response.bodyAsText()
        val jsonResponse = json.parseToJsonElement(responseText).jsonObject
        
        val translations = jsonResponse["data"]
            ?.jsonObject?.get("translations")
            ?.jsonArray
            ?.map { it.jsonObject["translatedText"]?.jsonPrimitive?.content ?: "" }
            ?: texts
        
        return translations
    }
    
    /**
     * Translate using free Google Translate web API
     * Less reliable but doesn't require API key
     */
    private suspend fun translateWithFreeApi(
        texts: List<String>,
        source: String,
        target: String
    ): List<String> {
        return texts.map { text ->
            translateSingleText(text, source, target)
        }
    }
    
    /**
     * Translate a single text using the free API
     */
    private suspend fun translateSingleText(
        text: String,
        source: String,
        target: String
    ): String {
        if (text.isBlank()) return text
        
        try {
            // Use the unofficial Google Translate API
            val encodedText = text.encodeURLParameter()
            val url = "https://translate.googleapis.com/translate_a/single" +
                    "?client=gtx" +
                    "&sl=$source" +
                    "&tl=$target" +
                    "&dt=t" +
                    "&q=$encodedText"
            
            val response = httpClient.get(url)
            val responseText = response.bodyAsText()
            
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
            val encodedText = text.take(100).encodeURLParameter()
            val url = "https://translate.googleapis.com/translate_a/single" +
                    "?client=gtx" +
                    "&sl=auto" +
                    "&tl=en" +
                    "&dt=t" +
                    "&q=$encodedText"
            
            val response = httpClient.get(url)
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
 * Data class for language information
 */
data class LanguageInfo(
    val code: String,
    val name: String
)
