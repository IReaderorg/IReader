package ireader.domain.usecases.translate
import ireader.domain.utils.extensions.ioDispatcher

import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
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
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
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
            onError(TranslationError.NoTextToTranslate.toUiText())
            return
        }
        
        try {
            _translationInProgress.value = true
            onProgress(10)
            
            // Check login state - provide specific error based on service
            if (!isLoggedIn()) {
                val authError = when (currentService) {
                    AI_SERVICE.CHATGPT -> TranslationError.AuthenticationRequired("ChatGPT WebView")
                    AI_SERVICE.DEEPSEEK -> TranslationError.AuthenticationRequired("DeepSeek WebView")
                    AI_SERVICE.GEMINI -> TranslationError.ApiKeyNotSet("Google Gemini")
                }
                onError(authError.toUiText())
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
            
            // For Gemini API, chunk texts to avoid token limits
            if (currentService == AI_SERVICE.GEMINI) {
                // OPTIMIZED: Dynamic chunk size based on text length
                // Shorter texts can be batched more aggressively to reduce API calls
                val avgTextLength = texts.map { it.length }.average()
                val maxChunkSize = when {
                    avgTextLength < 100 -> 40  // Short paragraphs: batch more
                    avgTextLength < 300 -> 25  // Medium paragraphs
                    else -> 15                  // Long paragraphs: smaller batches
                }
                val chunks = texts.chunked(maxChunkSize)
                val allResults = mutableListOf<String>()
                
                chunks.forEachIndexed { chunkIndex, chunk ->
                    val chunkProgress = 30 + (chunkIndex * 60 / chunks.size)
                    onProgress(chunkProgress)
                    
                    val combinedText = chunk.joinToString("\n---PARAGRAPH_BREAK---\n")
                    val prompt = buildPrompt(combinedText, sourceLang, targetLang, context)
                    
                    val translationResult = sendMessageToChatGPT(prompt)
                    val splitResults = splitResponse(translationResult, chunk.size)
                    allResults.addAll(splitResults)
                }
                
                onProgress(90)
                val translationResult = allResults.joinToString("\n---PARAGRAPH_BREAK---\n")
                
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
            
            withContext(ioDispatcher) {
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
            // Use TranslationError for user-friendly error messages
            val translationError = TranslationError.fromException(
                exception = e,
                engineName = engineName,
                sourceLanguage = source,
                targetLanguage = target
            )
            onError(translationError.toUiText())
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
        
        // OPTIMIZED: Ultra-minimal prompt for Gemini to reduce token usage
        // System instruction handles the translation context, so we only send the text
        return if (currentService == AI_SERVICE.GEMINI) {
            // Format: "SRC→TGT:\n<text>" - minimal tokens, system instruction does the rest
            "$sourceLang→$targetLang:\n$text"
        } else {
            """
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
        val systemInstruction: GeminiContent? = null, // System instruction for token optimization
        val generationConfig: GeminiGenerationConfig = GeminiGenerationConfig()
    )
    
    @Serializable
    private data class GeminiContent(
        val parts: List<GeminiPart>? = null,
        val role: String? = null // Added to handle "model" role in response
    )
    
    @Serializable
    private data class GeminiPart(
        val text: String? = null
    )
    
    @Serializable
    private data class GeminiGenerationConfig(
        val temperature: Float = 0.1f, // Lower = more deterministic, fewer retries
        val topK: Int = 40,
        val topP: Float = 0.95f,
        val maxOutputTokens: Int = 8192 // Reduced from 16384 to save tokens
    )
    
    // Data class for Gemini API response
    @Serializable
    private data class GeminiResponse(
        val candidates: List<GeminiCandidate>? = null
    )
    
    @Serializable
    private data class GeminiCandidate(
        val content: GeminiContent? = null,
        val finishReason: String? = null,
        val index: Int? = null,
        val citationMetadata: CitationMetadata? = null
    )
    
    @Serializable
    private data class CitationMetadata(
        val citationSources: List<CitationSource>? = null
    )
    
    @Serializable
    private data class CitationSource(
        val startIndex: Int? = null,
        val endIndex: Int? = null,
        val uri: String? = null,
        val license: String? = null
    )
    
    // Define available Gemini models
    companion object {
        // No default models - users must fetch from API
        private val DEFAULT_GEMINI_MODELS = emptyList<Pair<String, String>>()
        
        // Mutable list that can be updated from API
        private val _availableGeminiModels = MutableStateFlow(DEFAULT_GEMINI_MODELS)
        val availableGeminiModels: StateFlow<List<Pair<String, String>>> = _availableGeminiModels.asStateFlow()
        
        // List of available Gemini models (for backward compatibility)
        val AVAILABLE_GEMINI_MODELS: List<Pair<String, String>>
            get() = _availableGeminiModels.value
            
        // Update the available models list
        fun updateAvailableModels(models: List<Pair<String, String>>) {
            _availableGeminiModels.value = models
        }
        
        // CJK language codes for token estimation
        private val CJK_LANGUAGES = setOf("zh", "ja", "ko", "th", "vi")
        
        /**
         * Estimate token count for text.
         * Uses character-based approximation since tiktoken isn't available in KMP.
         * ~4 chars per token for Latin scripts, ~2-3 for CJK languages.
         */
        fun estimateTokens(text: String, targetLang: String = "en"): Int {
            val charsPerToken = if (targetLang in CJK_LANGUAGES) 2.5 else 4.0
            return (text.length / charsPerToken).toInt()
        }
        
        /**
         * Check if text is within safe token limits for Gemini.
         * Gemini has ~30k token limit, we use 8k as safe input limit.
         */
        fun isWithinTokenLimit(text: String, targetLang: String = "en", maxTokens: Int = 8000): Boolean {
            return estimateTokens(text, targetLang) <= maxTokens
        }
    }
    
    // Data classes for Gemini models API response
    @Serializable
    private data class GeminiModelsResponse(
        val models: List<GeminiModelInfo> = emptyList()
    )
    
    @Serializable
    private data class GeminiModelInfo(
        val name: String,
        val displayName: String? = null,
        val description: String? = null,
        val supportedGenerationMethods: List<String> = emptyList()
    )
    
    /**
     * Fetch available Gemini models from the API
     * This should be called manually by the user to refresh the model list
     */
    suspend fun fetchAvailableGeminiModels(apiKey: String): Result<List<Pair<String, String>>> {
        return try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey"
            
            val response = client.default.get(endpoint) {
                timeout {
                    requestTimeoutMillis = 30000 // 30 seconds
                    connectTimeoutMillis = 10000 // 10 seconds
                    socketTimeoutMillis = 30000 // 30 seconds
                }
            }
            
            if (response.status.value in 200..299) {
                // Manually parse JSON using kotlinx.serialization to avoid Gson LinkedTreeMap issue
                val responseText = response.bodyAsText()
                val json = Json { 
                    ignoreUnknownKeys = true
                    isLenient = true
                }
                val modelsResponse = json.decodeFromString<GeminiModelsResponse>(responseText)
                
                // Filter models that support generateContent
                val availableModels = modelsResponse.models
                    .filter { model ->
                        model.supportedGenerationMethods.contains("generateContent") &&
                        model.name.contains("gemini", ignoreCase = true)
                    }
                    .map { model ->
                        // Extract model ID from full name (e.g., "models/gemini-1.5-flash" -> "gemini-1.5-flash")
                        val modelId = model.name.substringAfterLast("/")
                        val displayName = model.displayName ?: modelId.replace("-", " ").split(" ")
                            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                        modelId to displayName
                    }
                
                if (availableModels.isNotEmpty()) {
                    // Update the companion object's model list
                    updateAvailableModels(availableModels)
                    Result.success(availableModels)
                } else {
                    Result.failure(Exception("No compatible models found"))
                }
            } else {
                val errorBody = response.bodyAsText()
                Result.failure(Exception("API error: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Function to send text to Gemini API and get response
    private suspend fun translateWithGeminiApi(prompt: String): String {
        val apiKey = getCookies()
        if (apiKey.isEmpty()) {
            throw Exception(Res.string.gemini_api_key_not_set.toString())
        }
        
        // Get user-selected model or use available models
        val userSelectedModel = readerPreferences.geminiModel().get()
        
        // Check if models have been loaded
        if (AVAILABLE_GEMINI_MODELS.isEmpty() && userSelectedModel.isBlank()) {
            throw Exception("No Gemini models available. Please refresh the model list in settings first.")
        }
        
        // Create a prioritized list starting with user selection if available
        val geminiModels = if (userSelectedModel.isNotBlank()) {
            // Start with user's preferred model, then fallback to others
            listOf(userSelectedModel) + AVAILABLE_GEMINI_MODELS.map { it.first }.filter { it != userSelectedModel }
        } else if (AVAILABLE_GEMINI_MODELS.isNotEmpty()) {
            // Use all available models in order
            AVAILABLE_GEMINI_MODELS.map { it.first }
        } else {
            throw Exception("No Gemini models configured. Please refresh the model list in settings.")
        }
        
        // Try each model until one succeeds or all fail
        var lastException: Exception? = null
        
        for (modelName in geminiModels) {
            try {
                return tryGeminiModel(apiKey, modelName, prompt)
            } catch (e: Exception) {
                lastException = e
                
                // Special error handling for model-specific issues
                if (e.message.toString().contains("doesn't have a free quota tier")) {
                    // This model requires payment, try the next one
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
            }
        }
        
        // If we've exhausted all models, throw the last exception
        throw lastException ?: Exception("All Gemini models failed or exceeded quota limits")
    }
    
    // Try a specific Gemini model with retry logic
    private suspend fun tryGeminiModel(apiKey: String, modelName: String, prompt: String): String {
        // Construct API endpoint in the exact format used in Google's examples
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
        
        // OPTIMIZED: Use systemInstruction for reusable context (reduces per-request tokens)
        // The system instruction is cached by Gemini, so it's more efficient than repeating in each prompt
        val requestBody = GeminiRequest(
            systemInstruction = GeminiContent(
                parts = listOf(
                    GeminiPart(text = "Translator. Output only translation. Keep ---PARAGRAPH_BREAK--- markers intact.")
                )
            ),
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
                val response = client.default.post(endpoint) {
                    contentType(io.ktor.http.ContentType.Application.Json)
                    setBody(requestBody)
                    timeout {
                        requestTimeoutMillis = 60000 // 60 seconds
                        connectTimeoutMillis = 15000 // 15 seconds
                        socketTimeoutMillis = 60000 // 60 seconds
                    }
                }
                
                if (response.status.value in 200..299) {
                    val responseText = response.bodyAsText()
                    
                    val geminiResponse = try {
                        val json = Json { 
                            ignoreUnknownKeys = true
                            isLenient = true
                        }
                        json.decodeFromString<GeminiResponse>(responseText)
                    } catch (e: Exception) {
                        throw Exception("Failed to parse Gemini API response: ${e.message}")
                    }
                    
                    // Add comprehensive null checks
                    if (geminiResponse.candidates.isNullOrEmpty()) {
                        throw Exception("Empty response from Gemini API")
                    }
                    
                    val firstCandidate = geminiResponse.candidates.firstOrNull()
                    if (firstCandidate == null) {
                        throw Exception("Invalid response structure from Gemini API")
                    }
                    
                    val content = firstCandidate.content
                    if (content == null) {
                        throw Exception("No content in Gemini API response")
                    }
                    
                    if (content.parts.isNullOrEmpty()) {
                        val finishReason = firstCandidate.finishReason
                        
                        if (finishReason == "MAX_TOKENS") {
                            // The model used all tokens on reasoning, no output generated
                            throw Exception("Translation failed: The text is too long for the model to process. Try translating smaller sections or use a different model.")
                        } else {
                            throw Exception("No content in Gemini API response. Finish reason: $finishReason")
                        }
                    }
                    
                    // Extract text from the first candidate's content
                    val textParts = content.parts.mapNotNull { it.text }
                    
                    if (textParts.isEmpty()) {
                        throw Exception("No text in Gemini API response parts")
                    }
                    
                    return textParts.joinToString("")
                } else {
                    val errorBody = response.bodyAsText()
                    
                    // Check for specific error codes
                    when (response.status.value) {
                        400 -> throw Exception("Invalid request: $errorBody")
                        401 -> throw Exception(Res.string.gemini_api_key_not_set.toString())
                        403 -> throw Exception("API key does not have permission")
                        404 -> throw Exception("Model $modelName not found or not available")
                        429 -> {
                            val errorBody = response.bodyAsText()
                            if (errorBody.contains("You exceeded your current quota") || errorBody.contains("quota exceeded")) {
                                throw Exception("You exceeded your current Gemini API quota. Please check your plan and billing details.")
                            } else {
                                throw Exception(Res.string.gemini_payment_required.toString())
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
                // Check for network-related errors that can be retried
                val isRetryableError = e.message?.contains("stream was reset") == true || 
                    e.message?.contains("CANCEL") == true ||
                    e.message?.contains("connection") == true ||
                    e.message?.contains("timeout") == true ||
                    e.message?.contains("Socket timeout") == true ||
                    e.message?.contains("SocketTimeoutException") == true
                
                if (isRetryableError) {
                    retryCount++
                    lastException = e
                    
                    if (retryCount < maxRetries) {
                        val delayMs = (2000 * retryCount).toLong() // Exponential backoff: 2s, 4s, 6s
                        delay(delayMs)
                        continue
                    } else {
                        throw Exception("Network timeout after $maxRetries attempts. The API may be slow or unavailable. Please try again later or select a different model.")
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
    
    // WebView engines have similar limits
    override val maxCharsPerRequest: Int = 6000
    
    // WebView needs longer delays to avoid detection
    override val rateLimitDelayMs: Long = 5000L
    
    override val isOffline: Boolean = false
}
class GeminiTranslateEngine(httpClients: HttpClients, readerPreferences: ReaderPreferences) :
    WebscrapingTranslateEngine(httpClients, readerPreferences) {
    init{
        setAIService(WebscrapingTranslateEngine.AI_SERVICE.GEMINI)
    }

    override val id: Long = 8L
    override val engineName: String = "Google Gemini API"
    override val requiresApiKey: Boolean = true
    
    // Gemini has a 30k token limit, but we use characters as approximation
    // ~4 chars per token, so ~8000 chars is safe for input + output
    override val maxCharsPerRequest: Int = 6000
    
    // Gemini free tier: 15 RPM (requests per minute) = 4 seconds between requests
    // We use 5 seconds to be safe
    override val rateLimitDelayMs: Long = 5000L
    
    override val isOffline: Boolean = false
}