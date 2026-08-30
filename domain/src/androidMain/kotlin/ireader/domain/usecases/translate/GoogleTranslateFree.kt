package ireader.domain.usecases.translate

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import ireader.core.http.HttpClients
import ireader.domain.data.engines.TranslateEngine
import ireader.i18n.UiText
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.URLEncoder
import java.util.concurrent.atomic.AtomicInteger

/**
 * Free Google Translate Web API Engine (Android implementation)
 *
 * Highly optimized with parallel requests, automated retries, and zero text drops.
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
    
    companion object {
        private const val MAX_CONCURRENT_HTTP = 4
        private const val MAX_RETRIES = 3
    }
    
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
            val total = texts.size
            val results = Array(total) { "" }
            val completedCount = AtomicInteger(0)
            val semaphore = Semaphore(MAX_CONCURRENT_HTTP)
            
            coroutineScope {
                val deferreds = texts.mapIndexed { index, text ->
                    async(Dispatchers.Default) {
                        if (text.isBlank()) {
                            results[index] = text
                            val done = completedCount.incrementAndGet()
                            onProgress((done * 100) / total)
                        } else {
                            val translated = semaphore.withPermit {
                                translateSingleWithRetry(text, source, target)
                            }
                            results[index] = translated ?: text
                            val done = completedCount.incrementAndGet()
                            onProgress((done * 100) / total)
                        }
                    }
                }
                deferreds.awaitAll()
            }
            
            onProgress(100)
            onSuccess(results.toList())
            
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
     * Translate a single text using Google Translate free API with automatic backoff retry
     */
    private suspend fun translateSingleWithRetry(text: String, source: String, target: String): String? {
        if (text.isBlank()) return text
        
        var attempt = 0
        while (attempt <= MAX_RETRIES) {
            try {
                val encodedText = URLEncoder.encode(text, "UTF-8")
                val url = "https://translate.googleapis.com/translate_a/single" +
                        "?client=gtx" +
                        "&sl=$source" +
                        "&tl=$target" +
                        "&dt=t" +
                        "&q=$encodedText"
                
                val response = httpClients.default.get(url)
                
                if (response.status.value == 429) {
                    println("[GoogleTranslateFree] Rate limit exceeded, backing off...")
                    attempt++
                    if (attempt <= MAX_RETRIES) {
                        delay(600L * attempt)
                        continue
                    }
                    return null
                }
                
                if (!response.status.isSuccess()) {
                    attempt++
                    if (attempt <= MAX_RETRIES) {
                        delay(300L * attempt)
                        continue
                    }
                    return null
                }
                
                val responseText = response.bodyAsText()
                val jsonArray = json.parseToJsonElement(responseText).jsonArray
                
                val translations = StringBuilder()
                jsonArray.firstOrNull()?.jsonArray?.forEach { item ->
                    item.jsonArray.firstOrNull()?.jsonPrimitive?.content?.let {
                        translations.append(it)
                    }
                }
                
                val result = translations.toString()
                if (result.isNotBlank()) {
                    return result
                }
                
            } catch (e: Exception) {
                attempt++
                if (attempt <= MAX_RETRIES) {
                    delay(300L * attempt)
                }
            }
        }
        return null
    }
}
