package ireader.domain.usecases.translate

import ireader.core.http.HttpClients
import ireader.domain.data.engines.ContentType
import ireader.domain.data.engines.ToneType
import ireader.domain.data.engines.TranslateEngine
import ireader.domain.data.engines.TranslationContext
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.*

class TranslationEnginesManager(
    private val readerPreferences: ReaderPreferences,
    private val httpClients: HttpClients,
) {

    private val availableEngines = listOf(
        GoogleTranslateML(),
        TranslateDictUseCase(httpClients),
        OpenAITranslateEngine(httpClients, readerPreferences),
        DeepSeekTranslateEngine(httpClients, readerPreferences),
        LibreTranslateEngine(httpClients),
        FreeAITranslateEngine(httpClients),
        WebscrapingTranslateEngine(httpClients, readerPreferences),
        DeepSeekWebViewTranslateEngine(httpClients, readerPreferences),
        // Create a WebscrapingTranslateEngine configured for Gemini
        GeminiTranslateEngine(httpClients, readerPreferences)
    )

    fun get(): TranslateEngine {
        val engineId = readerPreferences.translatorEngine().get()
        return availableEngines.find { it.id == engineId } ?: availableEngines.first()
    }
    
    /**
     * Get all available translation engines
     */
    fun getAvailableEngines(): List<TranslateEngine> {
        return availableEngines
    }
    
    /**
     * Translate with context-aware settings for AI-powered engines
     */
    suspend fun translateWithContext(
        texts: List<String>,
        source: String,
        target: String,
        contentType: ContentType = ContentType.GENERAL,
        toneType: ToneType = ToneType.NEUTRAL,
        preserveStyle: Boolean = true,
        onProgress: (Int) -> Unit = {},
        onSuccess: (List<String>) -> Unit,
        onError: (UiText) -> Unit
    ) {
        // Check if texts is empty or null
        if (texts.isNullOrEmpty()) {
            println("Translation error: No text to translate")
            onError(UiText.MStringResource(Res.string.no_text_to_translate))
            return
        }
        
        try {
            // Get the active translation engine
            val engine = get()
            
            // Log which engine is being used
            println("Using translation engine: ${engine.engineName} (ID: ${engine.id})")
            
            // Validate API keys if required
            if (engine.requiresApiKey) {
                when (engine.id) {
                    2L -> { // OpenAI
                        val apiKey = readerPreferences.openAIApiKey().get()
                        if (apiKey.isBlank()) {
                            println("Translation error: OpenAI API key not set")
                            onError(UiText.MStringResource(Res.string.openai_api_key_not_set))
                            return
                        }
                    }
                    3L -> { // DeepSeek
                        val apiKey = readerPreferences.deepSeekApiKey().get()
                        if (apiKey.isBlank()) {
                            println("Translation error: DeepSeek API key not set")
                            onError(UiText.MStringResource(Res.string.deepseek_api_key_not_set))
                            return
                        }
                    }
                }
            }
            
            val context = TranslationContext(
                contentType = contentType,
                toneType = toneType,
                preserveStyle = preserveStyle
            )
            
            try {
                if (engine.supportsContextAwareTranslation) {
                    engine.translateWithContext(texts, source, target, context, onProgress, onSuccess, onError)
                } else {
                    engine.translate(texts, source, target, onProgress, onSuccess, onError)
                }
            } catch (e: Exception) {
                // Log the error for debugging
                println("Translation error: ${e.message}")
                e.printStackTrace()
                
                // Provide a user-friendly error message
                val errorMessage = when {
                    e.message?.contains("401") == true || e.message?.contains("unauthorized") == true -> 
                        UiText.MStringResource(Res.string.openai_api_key_invalid)
                    e.message?.contains("402") == true ->
                        UiText.MStringResource(when (engine.id) {
                            2L -> Res.string.openai_quota_exceeded
                            3L -> Res.string.deepseek_payment_required
                            else -> Res.string.api_rate_limit_exceeded
                        })
                    e.message?.contains("429") == true || e.message?.contains("rate limit") == true ->
                        UiText.MStringResource(Res.string.api_rate_limit_exceeded)
                    e is NullPointerException && e.message?.contains("isEmpty") == true ->
                        UiText.MStringResource(Res.string.api_response_error)
                    else -> UiText.ExceptionString(e)
                }
                
                onError(errorMessage)
            }
        } catch (e: Exception) {
            // Log the error for debugging
            println("Translation error: ${e}")
            e.printStackTrace()
            
            // Provide a user-friendly error message
            onError(UiText.MStringResource(Res.string.api_response_error))
        }
    }
}