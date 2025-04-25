package ireader.domain.usecases.translate

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.content.TextContent
import io.ktor.http.contentType
import ireader.core.http.HttpClients
import ireader.domain.data.engines.ContentType
import ireader.domain.data.engines.ToneType
import ireader.domain.data.engines.TranslateEngine
import ireader.domain.data.engines.TranslationContext
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.i18n.UiText
import ireader.i18n.resources.MR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * WebscrapingTranslateEngine
 * Uses WebView to access ChatGPT and other AI models directly
 * without API keys
 */
open class WebscrapingTranslateEngine(
    private val client: HttpClients,
    val readerPreferences: ReaderPreferences
) : TranslateEngine() {

    override val id: Long = 6L
    override val engineName: String
        get() = when (currentService) {
            AI_SERVICE.CHATGPT -> "ChatGPT WebView (No API Key)"
            AI_SERVICE.DEEPSEEK -> "DeepSeek WebView (No API Key)"
            AI_SERVICE.GEMINI -> "Google Gemini API"
        }
    override val supportsAI: Boolean = true
    override val supportsContextAwareTranslation: Boolean = true
    override val supportsStylePreservation: Boolean = true
    override val requiresApiKey: Boolean = false

    // Various states for webview login
    private val _loginState = MutableStateFlow(LoginState.UNKNOWN)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _captchaRequired = MutableStateFlow(false)
    val captchaRequired: StateFlow<Boolean> = _captchaRequired.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _translationInProgress = MutableStateFlow(false)
    val translationInProgress: StateFlow<Boolean> = _translationInProgress.asStateFlow()
    
    // ChatGPT URLs
    private val chatGptUrl = "https://chat.openai.com"
    private val chatGptDirectUrl = "https://chat.openai.com/c/"
    
    // DeepSeek URLs
    private val deepSeekUrl = "https://chat.deepseek.com"
    private val deepSeekDirectUrl = "https://chat.deepseek.com/chat"
    
    // Current service being used
    private var currentService = AI_SERVICE.CHATGPT
    
    // AI service enum
    enum class AI_SERVICE {
        CHATGPT,
        DEEPSEEK,
        GEMINI
    }
    
    // Function to set the current AI service
    fun setAIService(service: AI_SERVICE) {
        currentService = service
    }
    
    // Function to get the base URL for the current service
    fun getBaseUrl(): String {
        return when (currentService) {
            AI_SERVICE.CHATGPT -> chatGptUrl
            AI_SERVICE.DEEPSEEK -> deepSeekUrl
            AI_SERVICE.GEMINI -> chatGptUrl // Gemini uses API so we just use ChatGPT WebView as fallback
        }
    }
    
    // Cookie storage keys
    private val CHATGPT_COOKIE_STORAGE_KEY = "chatgpt_cookies"
    private val DEEPSEEK_COOKIE_STORAGE_KEY = "deepseek_cookies"
    private val GEMINI_API_KEY = "gemini_api_key"
    
    // Function to get the current cookie storage key
    private fun getCurrentCookieKey(): String {
        return when (currentService) {
            AI_SERVICE.CHATGPT -> CHATGPT_COOKIE_STORAGE_KEY
            AI_SERVICE.DEEPSEEK -> DEEPSEEK_COOKIE_STORAGE_KEY
            AI_SERVICE.GEMINI -> GEMINI_API_KEY
        }
    }
    
    // Support all major languages
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

    // Login states for the WebView
    enum class LoginState {
        UNKNOWN,
        LOGGED_OUT,
        LOGGING_IN,
        CAPTCHA_REQUIRED,
        LOGGED_IN,
        ERROR
    }
    
    // Last message from ChatGPT for translation
    private var lastRawMessage: String? = null
    
    // Function to check login status
    fun checkLoginStatus() {
        // This will be called from the WebView to update the login state
    }
    
    // Function to update login state (called from platform code)
    fun updateLoginState(state: LoginState) {
        _loginState.value = state
        if (state == LoginState.CAPTCHA_REQUIRED) {
            _captchaRequired.value = true
        } else {
            _captchaRequired.value = false
        }
        
        if (state == LoginState.ERROR) {
            _errorMessage.value = "Error logging in to ChatGPT. Please try again."
        } else {
            _errorMessage.value = null
        }
    }
    
    // Function to set cookies (called from platform code)
    fun saveCookies(cookies: String) {
        when (currentService) {
            AI_SERVICE.CHATGPT -> readerPreferences.chatGptCookies().set(cookies)
            AI_SERVICE.DEEPSEEK -> readerPreferences.deepSeekCookies().set(cookies)
            AI_SERVICE.GEMINI -> readerPreferences.geminiApiKey().set(cookies)
        }
    }
    
    // Function to get cookies
    fun getCookies(): String {
        return when (currentService) {
            AI_SERVICE.CHATGPT -> readerPreferences.chatGptCookies().get()
            AI_SERVICE.DEEPSEEK -> readerPreferences.deepSeekCookies().get()
            AI_SERVICE.GEMINI -> readerPreferences.geminiApiKey().get()
        }
    }
    
    // Function to update the last raw message (called from platform code)
    fun updateLastRawMessage(message: String) {
        lastRawMessage = message
    }
    
    // Function to clear login data
    fun clearLoginData() {
        when (currentService) {
            AI_SERVICE.CHATGPT -> readerPreferences.chatGptCookies().set("")
            AI_SERVICE.DEEPSEEK -> readerPreferences.deepSeekCookies().set("")
            AI_SERVICE.GEMINI -> readerPreferences.geminiApiKey().set("")
        }
        _loginState.value = LoginState.LOGGED_OUT
    }
    
    // Helper function to determine if we're logged in
    fun isLoggedIn(): Boolean {
        return when (currentService) {
            AI_SERVICE.CHATGPT, AI_SERVICE.DEEPSEEK -> _loginState.value == LoginState.LOGGED_IN && getCookies().isNotEmpty()
            AI_SERVICE.GEMINI -> getCookies().isNotEmpty() // For API-based services, we just need the API key
        }
    }

    override suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onProgress: (Int) -> Unit,
        onSuccess: (List<String>) -> Unit,
        onError: (UiText) -> Unit
    ) {
        // Use default context
        val context = TranslationContext(
            contentType = ContentType.GENERAL,
            toneType = ToneType.NEUTRAL,
            preserveStyle = true
        )
        
        translateWithContext(texts, source, target, context, onProgress, onSuccess, onError)
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
            onError(UiText.MStringResource(MR.strings.no_text_to_translate))
            return
        }
        
        try {
            _translationInProgress.value = true
            onProgress(10)
            
            // Check login state
            if (!isLoggedIn()) {
                onError(UiText.MStringResource(MR.strings.sign_in_to_chatgpt))
                _loginState.value = LoginState.LOGGED_OUT
                return
            }
            
            // Get language names for better prompting
            val sourceLang = supportedLanguages.find { it.first == source }?.second ?: source
            val targetLang = supportedLanguages.find { it.first == target }?.second ?: target
            
            // Skip translation if source and target are the same
            if (source == target || (source == "auto" && target == "en")) {
                onSuccess(texts)
                return
            }
            
            onProgress(20)
            
            // For Gemini API, send all content in one request instead of batches
            if (currentService == AI_SERVICE.GEMINI) {
                val combinedText = texts.joinToString("\n---PARAGRAPH_BREAK---\n")
                val prompt = buildPrompt(combinedText, sourceLang, targetLang, context)
                
                onProgress(30)
                val translationResult = sendMessageToChatGPT(prompt)
                onProgress(90)
                
                if (translationResult.isNullOrEmpty()) {
                    throw Exception("Empty response from API")
                }
                
                // Split the translation back into paragraphs
                val translatedParagraphs = splitResponse(translationResult, texts.size)
                
                // Match the number of input paragraphs
                val finalParagraphs = if (translatedParagraphs.size == texts.size) {
                    translatedParagraphs
                } else {
                    adjustParagraphCount(translatedParagraphs, texts)
                }
                
                onProgress(100)
                _translationInProgress.value = false
                onSuccess(finalParagraphs)
                return
            }
            
            // Process text in batches for WebView-based translators
            val results = mutableListOf<String>()
            val batchSize = 5 // Process 5 paragraphs at a time
            val batches = texts.chunked(batchSize)
            
            withContext(Dispatchers.IO) {
                for ((batchIndex, batch) in batches.withIndex()) {
                    val combinedText = batch.joinToString("\n---PARAGRAPH_BREAK---\n")
                    
                    // Calculate progress percentage
                    val progressStart = 20 + (batchIndex * 70 / batches.size)
                    val progressEnd = 20 + ((batchIndex + 1) * 70 / batches.size)
                    onProgress(progressStart)
                    
                    // Create the prompt based on context
                    val prompt = buildPrompt(combinedText, sourceLang, targetLang, context)
                    
                    // Signal to UI that we need to send a message to ChatGPT
                    // This will be handled by platform-specific code
                    val translationResult = sendMessageToChatGPT(prompt)
                    
                    if (translationResult.isNullOrEmpty()) {
                        throw Exception("Empty response from ChatGPT")
                    }
                    
                    // Split the translation back into paragraphs
                    val translatedParagraphs = splitResponse(translationResult, batch.size)
                    
                    // Match the number of input paragraphs
                    val finalParagraphs = if (translatedParagraphs.size == batch.size) {
                        translatedParagraphs
                    } else {
                        adjustParagraphCount(translatedParagraphs, batch)
                    }
                    
                    results.addAll(finalParagraphs)
                    onProgress(progressEnd)
                }
            }
            
            onProgress(100)
            _translationInProgress.value = false
            onSuccess(results)
            
        } catch (e: Exception) {
            _translationInProgress.value = false
            println("Translation error: ${e.message}")
            e.printStackTrace()
            onError(UiText.ExceptionString(e))
        }
    }
    
    // This function will send a message to the selected AI service and retrieve the response
    private suspend fun sendMessageToChatGPT(message: String): String {
        // For Gemini, we use direct API calls instead of WebView
        if (currentService == AI_SERVICE.GEMINI) {
            return translateWithGeminiApi(message)
        }
        
        // For WebView-based services (ChatGPT, DeepSeek)
        // Make sure we're tracking that translation is in progress
        _translationInProgress.value = true
        
        // This is the message that the platform code will use in the WebView
        // We'll store it in a way that the platform code can retrieve it
        readerPreferences.chatGptPrompt().set(message)
        
        // Wait for a response from the WebView or timeout
        var attempts = 0
        val maxAttempts = 30 // 30 seconds timeout
        
        while (lastRawMessage == null && attempts < maxAttempts) {
            delay(1000) // Wait 1 second between checks
            attempts++
        }
        
        // Reset the translation in progress flag
        _translationInProgress.value = false
        
        // Get the response and clear it for next time
        val response = lastRawMessage
        lastRawMessage = null
        
        // Also clear the prompt
        readerPreferences.chatGptPrompt().set("")
        
        return response ?: throw Exception("No response from service after $attempts seconds")
    }
    
    // Helper function to adjust paragraph count
    private fun adjustParagraphCount(translatedParagraphs: List<String>, originalParagraphs: List<String>): List<String> {
        val result = translatedParagraphs.toMutableList()
        
        // If we have too few paragraphs, add original ones
        while (result.size < originalParagraphs.size) {
            result.add(originalParagraphs[result.size])
        }
        
        // If we have too many paragraphs, remove extras
        if (result.size > originalParagraphs.size) {
            result.subList(originalParagraphs.size, result.size).clear()
        }
        
        return result
    }
    
    /**
     * Builds a prompt for the AI model based on the translation context
     */
    private fun buildPrompt(
        text: String,
        sourceLang: String,
        targetLang: String,
        context: TranslationContext
    ): String {
        val contentTypeStr = when (context.contentType) {
            ContentType.LITERARY -> "literary text"
            ContentType.TECHNICAL -> "technical document"
            ContentType.CONVERSATION -> "conversation"
            ContentType.POETRY -> "poetry"
            ContentType.ACADEMIC -> "academic text"
            ContentType.BUSINESS -> "business document"
            ContentType.CREATIVE -> "creative writing"
            else -> "general text"
        }
        
        val toneStr = when (context.toneType) {
            ToneType.FORMAL -> "formal"
            ToneType.CASUAL -> "casual"
            ToneType.PROFESSIONAL -> "professional"
            ToneType.HUMOROUS -> "humorous"
            ToneType.FRIENDLY -> "friendly"
            ToneType.INFORMAL -> "informal"
            else -> "neutral"
        }
        
        val stylePreservation = if (context.preserveStyle) 
            "Please preserve the original writing style, formatting, and tone." 
        else 
            ""
        
        return """
            Translate the following $contentTypeStr from $sourceLang to $targetLang.
            Use a $toneStr tone.
            $stylePreservation
            
            IMPORTANT: Return ONLY the translated text. 
            Do not include any explanations, notes, or additional text.
            Preserve paragraph breaks indicated by ---PARAGRAPH_BREAK---.
            
            Text to translate:
            $text
        """.trimIndent()
    }
    
    /**
     * Split the response back into paragraphs
     */
    private fun splitResponse(response: String, expectedCount: Int): List<String> {
        // First attempt to split by the special marker
        if (response.contains("---PARAGRAPH_BREAK---")) {
            return response
                .split("---PARAGRAPH_BREAK---")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        }
        
        // If the marker isn't found and we expect multiple paragraphs,
        // try to split by newlines instead
        if (expectedCount > 1) {
            val lines = response.lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            
            if (lines.size >= expectedCount) {
                // Group lines into paragraphs based on expected count
                val result = mutableListOf<String>()
                val linesPerParagraph = lines.size / expectedCount
                
                for (i in 0 until expectedCount) {
                    val start = i * linesPerParagraph
                    val end = if (i == expectedCount - 1) lines.size else (i + 1) * linesPerParagraph
                    
                    if (start < lines.size) {
                        val paragraph = lines.subList(start, end.coerceAtMost(lines.size)).joinToString("\n")
                        result.add(paragraph)
                    }
                }
                
                return result
            }
        }
        
        // If all else fails, treat the entire response as a single paragraph
        return listOf(response.trim())
    }

    // Data class for Gemini API request
    @Serializable
    private data class GeminiRequest(
        val contents: List<GeminiContent>,
        val generationConfig: GeminiGenerationConfig = GeminiGenerationConfig()
    )
    
    @Serializable
    private data class GeminiContent(
        val parts: List<GeminiPart>
    )
    
    @Serializable
    private data class GeminiPart(
        val text: String
    )
    
    @Serializable
    private data class GeminiGenerationConfig(
        val temperature: Float = 0.2f,
        val topK: Int = 40,
        val topP: Float = 0.95f,
        val maxOutputTokens: Int = 8192
    )
    
    // Data class for Gemini API response
    @Serializable
    private data class GeminiResponse(
        val candidates: List<GeminiCandidate> = emptyList()
    )
    
    @Serializable
    private data class GeminiCandidate(
        val content: GeminiContent
    )
    
    // Define available Gemini models
    companion object {
        // List of available Gemini models with display names
        val AVAILABLE_GEMINI_MODELS = listOf(
            "gemini-2.0-flash" to "Gemini 2.0 Flash (Recommended)",
            "gemini-2.5-pro-exp-03-25" to "Gemini 2.5 Pro (Experimental)",
            "gemini-2.5-flash-preview-04-17" to "Gemini 2.5 Flash (Preview)",
            "gemini-2.0-flash-lite" to "Gemini 2.0 Flash Lite",
            "gemini-1.5-pro" to "Gemini 1.5 Pro",
            "gemini-1.5-flash" to "Gemini 1.5 Flash"
        )
        
        // Default model to use if none is selected
        const val DEFAULT_GEMINI_MODEL = "gemini-2.0-flash"
    }

    // Function to send text to Gemini API and get response
    private suspend fun translateWithGeminiApi(prompt: String): String {
        val apiKey = getCookies()
        if (apiKey.isEmpty()) {
            throw Exception(MR.strings.gemini_api_key_not_set.toString())
        }
        
        // Get user-selected model or use fallback sequence
        val userSelectedModel = readerPreferences.geminiModel().get()
        
        // Create a prioritized list starting with user selection if available
        val geminiModels = if (userSelectedModel.isNotBlank()) {
            // Start with user's preferred model, then fallback to others
            listOf(userSelectedModel) + AVAILABLE_GEMINI_MODELS.map { it.first }.filter { it != userSelectedModel }
        } else {
            // Default order if no user preference
            listOf(
                "gemini-2.0-flash", // Latest stable, versatile model
                "gemini-2.5-pro-exp-03-25", // Newest with enhanced thinking and reasoning (experimental version with free quota)
                "gemini-2.5-flash-preview-04-17", // Best price-performance model
                "gemini-2.0-flash-lite", // Cost efficient model
                "gemini-1.5-pro", // Older model for complex reasoning
                "gemini-1.5-flash" // Older versatile model
            )
        }
        
        // Try each model until one succeeds or all fail
        var lastException: Exception? = null
        
        for (modelName in geminiModels) {
            try {
                return tryGeminiModel(apiKey, modelName, prompt)
            } catch (e: Exception) {
                println("Failed with model $modelName: ${e.message}")
                lastException = e
                
                // Special error handling for model-specific issues
                if (e.message.toString().contains("doesn't have a free quota tier")) {
                    // This model requires payment, try the next one
                    println("Model $modelName doesn't have a free quota tier, trying next model")
                    continue
                }
                
                // If this is not a quota error, don't try other models
                if (!e.message.toString().contains("quota") && 
                    !e.message.toString().contains("rate limit") &&
                    !e.message.toString().contains("429") &&
                    !e.message.toString().contains("RESOURCE_EXHAUSTED")) {
                    throw e
                }
                
                // If it's a quota issue, continue to the next model
                println("Quota exceeded for $modelName, trying next model")
            }
        }
        
        // If we've exhausted all models, throw the last exception
        throw lastException ?: Exception("All Gemini models failed or exceeded quota limits")
    }
    
    // Try a specific Gemini model with retry logic
    private suspend fun tryGeminiModel(apiKey: String, modelName: String, prompt: String): String {
        // Construct API endpoint in the exact format used in Google's examples
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
        
        // Ensure request matches the curl example format
        val requestBody = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = prompt)
                    )
                )
            )
        )
        
        // Add retry mechanism for network issues
        val maxRetries = 3
        var retryCount = 0
        var lastException: Exception? = null
        
        while (retryCount < maxRetries) {
            try {
                println("Sending request to Gemini API with model: $modelName (attempt ${retryCount + 1}/$maxRetries)")
                val response = client.default.post(endpoint) {
                    contentType(io.ktor.http.ContentType.Application.Json)
                    setBody(requestBody)
                }
                
                println("Gemini API response status: ${response.status.value}")
                
                if (response.status.value in 200..299) {
                    val geminiResponse = response.body<GeminiResponse>()
                    if (geminiResponse.candidates.isNotEmpty()) {
                        // Extract text from the first candidate's content
                        val textParts = geminiResponse.candidates[0].content.parts
                            .mapNotNull { it.text }
                        
                        return textParts.joinToString("")
                    }
                    println("Gemini API returned empty candidates list")
                    throw Exception(MR.strings.empty_response.toString())
                } else {
                    val errorBody = response.bodyAsText()
                    println("Gemini API error: Status ${response.status.value}, Body: $errorBody")
                    
                    // Check for specific error codes
                    when (response.status.value) {
                        400 -> throw Exception("Invalid request: $errorBody")
                        401 -> throw Exception(MR.strings.gemini_api_key_not_set.toString())
                        403 -> throw Exception("API key does not have permission")
                        404 -> throw Exception("Model $modelName not found or not available")
                        429 -> {
                            val errorBody = response.bodyAsText()
                            if (errorBody.contains("You exceeded your current quota") || errorBody.contains("quota exceeded")) {
                                throw Exception("You exceeded your current Gemini API quota. Please check your plan and billing details.")
                            } else {
                                throw Exception(MR.strings.gemini_payment_required.toString())
                            }
                        }
                        500, 501, 502, 503 -> {
                            // Server errors might be temporary, so we'll retry
                            retryCount++
                            lastException = Exception("Google AI API server error (${response.status.value})")
                            // Add a delay before retrying
                            delay((1000 * retryCount).toLong()) // Exponential backoff
                            continue
                        }
                        else -> throw Exception("Gemini API error (${response.status.value}): $errorBody")
                    }
                }
            } catch (e: Exception) {
                println("Gemini API exception with model $modelName: ${e.message}")
                
                // Check for network-related errors that can be retried
                if (e.message?.contains("stream was reset") == true || 
                    e.message?.contains("CANCEL") == true ||
                    e.message?.contains("connection") == true ||
                    e.message?.contains("timeout") == true) {
                    
                    retryCount++
                    lastException = e
                    
                    if (retryCount < maxRetries) {
                        println("Network error, retrying (attempt ${retryCount+1}/$maxRetries)")
                        // Add a delay before retrying
                        delay((1000 * retryCount).toLong()) // Exponential backoff
                        continue
                    } else {
                        throw Exception("Network error during translation after $maxRetries attempts. Please try again later.")
                    }
                }
                
                e.printStackTrace()
                throw Exception("Error with Gemini API model $modelName: ${e.message}")
            }
        }
        
        // If we've exhausted retries, throw the last exception
        throw lastException ?: Exception("Failed to communicate with Gemini API model $modelName after $maxRetries attempts")
    }
}


class DeepSeekWebViewTranslateEngine(httpClients: HttpClients, readerPreferences: ReaderPreferences) :
    WebscrapingTranslateEngine(httpClients, readerPreferences) {
    init{
        setAIService(AI_SERVICE.DEEPSEEK)
    }
    override val id: Long = 7L
    override val engineName: String = "DeepSeek WebView (No API Key)"
}
class GeminiTranslateEngine(httpClients: HttpClients, readerPreferences: ReaderPreferences) :
    WebscrapingTranslateEngine(httpClients, readerPreferences) {
    init{
        setAIService(WebscrapingTranslateEngine.AI_SERVICE.GEMINI)
    }

    override val id: Long = 8L
    override val engineName: String = "Google Gemini API"
}