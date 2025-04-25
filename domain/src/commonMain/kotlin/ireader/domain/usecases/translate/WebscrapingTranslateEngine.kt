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
class WebscrapingTranslateEngine(
    private val client: HttpClients,
    val readerPreferences: ReaderPreferences
) : TranslateEngine() {

    override val id: Long = 6L
    override val engineName: String = "ChatGPT WebView (No API Key)"
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
    
    // Cookie storage key
    private val COOKIE_STORAGE_KEY = "chatgpt_cookies"
    
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
        readerPreferences.chatGptCookies().set(cookies)
    }
    
    // Function to get cookies
    fun getCookies(): String {
        return readerPreferences.chatGptCookies().get()
    }
    
    // Function to update the last raw message (called from platform code)
    fun updateLastRawMessage(message: String) {
        lastRawMessage = message
    }
    
    // Function to clear login data
    fun clearLoginData() {
        readerPreferences.chatGptCookies().set("")
        _loginState.value = LoginState.LOGGED_OUT
    }
    
    // Helper function to determine if we're logged in
    fun isLoggedIn(): Boolean {
        return _loginState.value == LoginState.LOGGED_IN && getCookies().isNotEmpty()
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
            
            // Process text in batches
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
            println("ChatGPT WebView translation error: ${e.message}")
            e.printStackTrace()
            onError(UiText.ExceptionString(e))
        }
    }
    
    // This function is a placeholder that will be replaced by actual platform implementation
    // that uses WebView to send a message to ChatGPT and retrieve the response
    private suspend fun sendMessageToChatGPT(message: String): String {
        // In real usage, the prompt will be set here and the result will be retrieved from the WebView
        // via the lastRawMessage property which is updated by the WebView JavaScript interface
        
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
        
        return response ?: throw Exception("No response from ChatGPT after $attempts seconds")
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
} 