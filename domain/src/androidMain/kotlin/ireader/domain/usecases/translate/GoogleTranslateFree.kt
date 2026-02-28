package ireader.domain.usecases.translate

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import ireader.core.http.HttpClients
import ireader.domain.data.engines.TranslateEngine
import ireader.i18n.UiText
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.URLEncoder

/**
 * Free Google Translate Web API Engine (Android implementation)
 *
 * Uses the same free Google Translate API that Chrome and iOS use.
 * This is faster and more reliable than Google ML Kit on Android.
 *
 * Advantages over GoogleML:
 * - No model download required
 * - Better translation quality (same as Chrome)
 * - No mixed language issues
 * - Works immediately without initialization
 *
 * Note: This uses an unofficial API that may have rate limits.
 */
actual class GoogleTranslateFree actual constructor() : TranslateEngine(), KoinComponent {

    private val httpClients: HttpClients by inject()

    override val id: Long = 11L
    override val engineName: String = "Google Translate (Free)"
    override val requiresInitialization: Boolean = false
    override val requiresApiKey: Boolean = false
    
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    // Rate limiting
    private var lastRequestTime = 0L
    private val minRequestInterval = 100L // 100ms between requests
    
    override val supportedLanguages: List<Pair<String, String>> = listOf(
        "auto" to "Auto-detect",
        "af" to "Afrikaans",
        "sq" to "Albanian",
        "ar" to "Arabic",
        "be" to "Belarusian",
        "bn" to "Bengali",
        "bg" to "Bulgarian",
        "ca" to "Catalan",
        "zh" to "Chinese",
        "zh-CN" to "Chinese (Simplified)",
        "zh-TW" to "Chinese (Traditional)",
        "hr" to "Croatian",
        "cs" to "Czech",
        "da" to "Danish",
        "nl" to "Dutch",
        "en" to "English",
        "eo" to "Esperanto",
        "et" to "Estonian",
        "fi" to "Finnish",
        "fr" to "French",
        "gl" to "Galician",
        "ka" to "Georgian",
        "de" to "German",
        "el" to "Greek",
        "gu" to "Gujarati",
        "ht" to "Haitian Creole",
        "he" to "Hebrew",
        "hi" to "Hindi",
        "hu" to "Hungarian",
        "is" to "Icelandic",
        "id" to "Indonesian",
        "ga" to "Irish",
        "it" to "Italian",
        "ja" to "Japanese",
        "kn" to "Kannada",
        "ko" to "Korean",
        "lv" to "Latvian",
        "lt" to "Lithuanian",
        "mk" to "Macedonian",
        "mr" to "Marathi",
        "ms" to "Malay",
        "mt" to "Maltese",
        "no" to "Norwegian",
        "fa" to "Persian",
        "pl" to "Polish",
        "pt" to "Portuguese",
        "ro" to "Romanian",
        "ru" to "Russian",
        "sr" to "Serbian",
        "sk" to "Slovak",
        "sl" to "Slovenian",
        "es" to "Spanish",
        "sw" to "Swahili",
        "sv" to "Swedish",
        "tl" to "Tagalog",
        "ta" to "Tamil",
        "te" to "Telugu",
        "th" to "Thai",
        "tr" to "Turkish",
        "uk" to "Ukrainian",
        "ur" to "Urdu",
        "vi" to "Vietnamese",
        "cy" to "Welsh"
    )
    
    override suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onProgress: (Int) -> Unit,
        onSuccess: (List<String>) -> Unit,
        onError: (UiText) -> Unit
    ) {
        if (texts.isEmpty()) {
            onError(UiText.DynamicString("No text to translate"))
            return
        }
        
        try {
            onProgress(0)
            val results = mutableListOf<String>()
            val total = texts.size
            
            texts.forEachIndexed { index, text ->
                if (text.isBlank()) {
                    results.add(text)
                } else {
                    val translated = translateSingle(text, source, target)
                    results.add(translated ?: text)
                }
                
                val progress = ((index + 1) * 100) / total
                onProgress(progress)
            }
            
            onSuccess(results)
            
        } catch (e: Exception) {
            println("[GoogleTranslateFree] Translation error: ${e.message}")
            e.printStackTrace()
            
            val errorMessage = when {
                e.message?.contains("429") == true -> 
                    "Rate limit exceeded. Please wait a moment and try again."
                e.message?.contains("network") == true || e.message?.contains("connection") == true ->
                    "Network error. Please check your internet connection."
                else -> 
                    "Translation failed: ${e.message ?: "Unknown error"}"
            }
            
            onError(UiText.DynamicString(errorMessage))
        }
    }
    
    /**
     * Translate a single text using Google Translate free API
     */
    private suspend fun translateSingle(text: String, source: String, target: String): String? {
        if (text.isBlank()) return text
        
        // Rate limiting
        enforceRateLimit()
        
        try {
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val url = "https://translate.googleapis.com/translate_a/single" +
                    "?client=gtx" +
                    "&sl=$source" +
                    "&tl=$target" +
                    "&dt=t" +
                    "&q=$encodedText"
            
            val response = httpClients.default.get(url)
            
            // Check for rate limiting
            if (response.status.value == 429) {
                println("[GoogleTranslateFree] Rate limit exceeded")
                delay(1000) // Wait 1 second and retry once
                return translateSingle(text, source, target)
            }
            
            if (!response.status.isSuccess()) {
                println("[GoogleTranslateFree] HTTP error: ${response.status}")
                return null
            }
            
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
            
            return translations.toString().ifEmpty { null }
            
        } catch (e: Exception) {
            println("[GoogleTranslateFree] Single text translation error: ${e.message}")
            return null
        }
    }
    
    /**
     * Enforce rate limiting between requests
     */
    private suspend fun enforceRateLimit() {
        val now = System.currentTimeMillis()
        val timeSinceLastRequest = now - lastRequestTime
        
        if (timeSinceLastRequest < minRequestInterval) {
            delay(minRequestInterval - timeSinceLastRequest)
        }
        
        lastRequestTime = System.currentTimeMillis()
    }
}
