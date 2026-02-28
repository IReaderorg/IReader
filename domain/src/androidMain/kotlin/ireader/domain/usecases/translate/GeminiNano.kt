package ireader.domain.usecases.translate

import android.os.Build
import androidx.annotation.RequiresApi
import ireader.domain.data.engines.TranslateEngine
import ireader.i18n.UiText
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.component.KoinComponent
import kotlin.coroutines.resume

/**
 * Gemini Nano - Google's on-device AI translation (Android implementation)
 * 
 * Uses AICore API available on Android 14+ (Pixel 8+, Samsung S24+)
 * Provides best quality offline translation with full LLM capabilities
 * 
 * Requirements:
 * - Android 14+ (API 34+)
 * - Device with AICore support (Pixel 8+, Samsung S24+, etc.)
 * - Google Play Services
 */
actual class GeminiNano actual constructor() : TranslateEngine(), KoinComponent {

    override val id: Long = 12L
    override val engineName: String = "Gemini Nano (On-Device AI)"
    override val requiresInitialization: Boolean = true
    override val requiresApiKey: Boolean = false
    override val supportsAI: Boolean = true
    override val supportsContextAwareTranslation: Boolean = true
    override val supportsStylePreservation: Boolean = true
    
    private var aiSession: Any? = null
    
    override val supportedLanguages: List<Pair<String, String>> = listOf(
        "auto" to "Auto-detect",
        "en" to "English",
        "es" to "Spanish",
        "fr" to "French",
        "de" to "German",
        "it" to "Italian",
        "pt" to "Portuguese",
        "ru" to "Russian",
        "ja" to "Japanese",
        "ko" to "Korean",
        "zh" to "Chinese",
        "ar" to "Arabic",
        "hi" to "Hindi",
        "th" to "Thai",
        "vi" to "Vietnamese",
        "id" to "Indonesian",
        "tr" to "Turkish",
        "pl" to "Polish",
        "nl" to "Dutch",
        "sv" to "Swedish"
    )
    
    /**
     * Check if device supports Gemini Nano
     */
    fun isSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE // Android 14
    }
    
    override suspend fun initialize(
        sourceLanguage: String,
        targetLanguage: String,
        onProgress: (Int) -> Unit,
        onSuccess: (String) -> Unit,
        onError: (UiText) -> Unit
    ) {
        if (!isSupported()) {
            onError(UiText.DynamicString(
                "Gemini Nano requires Android 14 or higher. Your device is running Android ${Build.VERSION.SDK_INT}."
            ))
            return
        }
        
        try {
            onProgress(10)
            
            // Check if AICore is available using reflection
            val aiCoreAvailable = checkAICoreAvailability()
            
            if (!aiCoreAvailable) {
                onError(UiText.DynamicString(
                    "Gemini Nano is not available on this device. " +
                    "It requires a compatible device (Pixel 8+, Samsung S24+) with Google Play Services."
                ))
                return
            }
            
            onProgress(50)
            
            // Initialize AI session
            initializeAISession(onProgress, onSuccess, onError)
            
        } catch (e: Exception) {
            println("[GeminiNano] Initialization error: ${e.message}")
            e.printStackTrace()
            onError(UiText.DynamicString(
                "Failed to initialize Gemini Nano: ${e.message ?: "Unknown error"}"
            ))
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
        if (!isSupported()) {
            onError(UiText.DynamicString("Gemini Nano requires Android 14 or higher"))
            return
        }
        
        if (texts.isEmpty()) {
            onError(UiText.DynamicString("No text to translate"))
            return
        }
        
        try {
            onProgress(0)
            
            if (aiSession == null) {
                onError(UiText.DynamicString(
                    "Gemini Nano not initialized. Please initialize the engine first."
                ))
                return
            }
            
            val results = mutableListOf<String>()
            val total = texts.size
            
            texts.forEachIndexed { index, text ->
                if (text.isBlank()) {
                    results.add(text)
                } else {
                    val translated = translateWithAI(text, source, target)
                    results.add(translated ?: text)
                }
                
                val progress = ((index + 1) * 100) / total
                onProgress(progress)
            }
            
            onSuccess(results)
            
        } catch (e: Exception) {
            println("[GeminiNano] Translation error: ${e.message}")
            e.printStackTrace()
            onError(UiText.DynamicString(
                "Translation failed: ${e.message ?: "Unknown error"}"
            ))
        }
    }
    
    /**
     * Check if AICore is available on device
     */
    private fun checkAICoreAvailability(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                return false
            }
            
            // Try to load AICore classes using reflection
            val aiCoreClass = Class.forName("com.google.android.aicore.AiCoreClient")
            val availabilityMethod = aiCoreClass.getMethod("isAvailable")
            availabilityMethod.invoke(null) as? Boolean ?: false
            
        } catch (e: ClassNotFoundException) {
            println("[GeminiNano] AICore not found: ${e.message}")
            false
        } catch (e: Exception) {
            println("[GeminiNano] AICore check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Initialize AI session
     */
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private suspend fun initializeAISession(
        onProgress: (Int) -> Unit,
        onSuccess: (String) -> Unit,
        onError: (UiText) -> Unit
    ) = suspendCancellableCoroutine { continuation ->
        try {
            // Use reflection to initialize AICore session
            val aiCoreClass = Class.forName("com.google.android.aicore.AiCoreClient")
            val createMethod = aiCoreClass.getMethod("create")
            val client = createMethod.invoke(null)
            
            onProgress(75)
            
            // Get translation session
            val getSessionMethod = client.javaClass.getMethod("getTranslationSession")
            aiSession = getSessionMethod.invoke(client)
            
            onProgress(100)
            onSuccess("Gemini Nano initialized successfully")
            continuation.resume(Unit)
            
        } catch (e: Exception) {
            println("[GeminiNano] Session initialization failed: ${e.message}")
            e.printStackTrace()
            onError(UiText.DynamicString(
                "Failed to create AI session: ${e.message ?: "Unknown error"}"
            ))
            continuation.resume(Unit)
        }
    }
    
    /**
     * Translate text using AI session
     */
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private suspend fun translateWithAI(
        text: String,
        source: String,
        target: String
    ): String? = suspendCancellableCoroutine { continuation ->
        try {
            val session = aiSession ?: run {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }
            
            // Build translation prompt
            val prompt = buildTranslationPrompt(text, source, target)
            
            // Execute translation using reflection
            val translateMethod = session.javaClass.getMethod("translate", String::class.java)
            val resultTask = translateMethod.invoke(session, prompt)
            
            // Add success listener
            val addOnSuccessListenerMethod = resultTask.javaClass.getMethod(
                "addOnSuccessListener",
                Class.forName("com.google.android.gms.tasks.OnSuccessListener")
            )
            
            val successListener = java.lang.reflect.Proxy.newProxyInstance(
                javaClass.classLoader,
                arrayOf(Class.forName("com.google.android.gms.tasks.OnSuccessListener"))
            ) { _, _, args ->
                val result = args?.get(0) as? String
                continuation.resume(result)
                null
            }
            
            addOnSuccessListenerMethod.invoke(resultTask, successListener)
            
            // Add failure listener
            val addOnFailureListenerMethod = resultTask.javaClass.getMethod(
                "addOnFailureListener",
                Class.forName("com.google.android.gms.tasks.OnFailureListener")
            )
            
            val failureListener = java.lang.reflect.Proxy.newProxyInstance(
                javaClass.classLoader,
                arrayOf(Class.forName("com.google.android.gms.tasks.OnFailureListener"))
            ) { _, _, args ->
                val exception = args?.get(0) as? Exception
                println("[GeminiNano] Translation failed: ${exception?.message}")
                continuation.resume(null)
                null
            }
            
            addOnFailureListenerMethod.invoke(resultTask, failureListener)
            
        } catch (e: Exception) {
            println("[GeminiNano] Translation error: ${e.message}")
            continuation.resume(null)
        }
    }
    
    /**
     * Build translation prompt for AI
     */
    private fun buildTranslationPrompt(text: String, source: String, target: String): String {
        val sourceLang = supportedLanguages.find { it.first == source }?.second ?: source
        val targetLang = supportedLanguages.find { it.first == target }?.second ?: target
        
        return """Translate the following text from $sourceLang to $targetLang. 
Maintain the original tone, style, and formatting. 
Preserve character names and cultural references appropriately.

Text to translate:
$text

Translation:"""
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        try {
            aiSession?.let { session ->
                val closeMethod = session.javaClass.getMethod("close")
                closeMethod.invoke(session)
            }
            aiSession = null
        } catch (e: Exception) {
            println("[GeminiNano] Cleanup error: ${e.message}")
        }
    }
}
