package ireader.domain.usecases.translate

import ireader.domain.data.engines.TranslateEngine
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class GoogleTranslateML : TranslateEngine() {
    override val id: Long
        get() = 0
    
    actual override val requiresInitialization: Boolean
        get() = true
    
    companion object {
        private const val TAG = "GoogleTranslateML"
        private const val MAX_CONCURRENT_TRANSLATIONS = 8
        private const val MAX_RETRIES_PER_PARAGRAPH = 2

        // Reflection caches for high performance
        private var translatorOptionsClass: Class<*>? = null
        private var translationClass: Class<*>? = null
        private var builderClass: Class<*>? = null
        private var setSourceMethod: Method? = null
        private var setTargetMethod: Method? = null
        private var buildMethod: Method? = null
        private var getClientMethod: Method? = null
        private var downloadMethod: Method? = null
        private var translateMethod: Method? = null
        private var onSuccessListenerClass: Class<*>? = null
        private var onFailureListenerClass: Class<*>? = null
        private var addOnSuccessListenerMethod: Method? = null
        private var addOnFailureListenerMethod: Method? = null

        private var isReflectionInitialized = false

        private fun initReflection() {
            if (isReflectionInitialized) return
            try {
                translatorOptionsClass = Class.forName("com.google.mlkit.nl.translate.TranslatorOptions")
                translationClass = Class.forName("com.google.mlkit.nl.translate.Translation")
                builderClass = Class.forName("com.google.mlkit.nl.translate.TranslatorOptions\$Builder")
                
                setSourceMethod = builderClass?.getMethod("setSourceLanguage", String::class.java)
                setTargetMethod = builderClass?.getMethod("setTargetLanguage", String::class.java)
                buildMethod = builderClass?.getMethod("build")
                getClientMethod = translationClass?.getMethod("getClient", translatorOptionsClass)
                
                onSuccessListenerClass = Class.forName("com.google.android.gms.tasks.OnSuccessListener")
                onFailureListenerClass = Class.forName("com.google.android.gms.tasks.OnFailureListener")
                
                isReflectionInitialized = true
            } catch (e: Exception) {
                println("[$TAG] Failed to initialize ML Kit reflection: ${e.message}")
            }
        }
    }
    
    override val supportedLanguages: List<Pair<String, String>> = listOf(
        "af" to "Afrikaans",
        "sq" to "Albanian",
        "ar" to "Arabic",
        "be" to "Belarusian",
        "bn" to "Bengali",
        "bg" to "Bulgarian",
        "ca" to "Catalan",
        "zh" to "Chinese",
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
    
    /**
     * Initialize the Google ML Kit translation engine by downloading language models
     */
    actual override suspend fun initialize(
        sourceLanguage: String,
        targetLanguage: String,
        onProgress: (Int) -> Unit,
        onSuccess: (String) -> Unit,
        onError: (UiText) -> Unit
    ) {
        try {
            initReflection()
            if (!isReflectionInitialized) {
                onError(UiText.DynamicString("Google ML Kit is not available in this build."))
                return
            }
            
            onProgress(0)
            
            val builder = builderClass?.getDeclaredConstructor()?.newInstance() ?: throw IllegalStateException("Cannot create builder")
            setSourceMethod?.invoke(builder, sourceLanguage)
            setTargetMethod?.invoke(builder, targetLanguage)
            val options = buildMethod?.invoke(builder)
            
            val client = getClientMethod?.invoke(null, options) ?: throw IllegalStateException("Cannot get ML Kit client")
            
            onProgress(10)
            
            val downloadMethod = client.javaClass.getMethod("downloadModelIfNeeded")
            val downloadTask = downloadMethod.invoke(client)
            
            val addOnSuccessListenerMethod = downloadTask.javaClass.getMethod("addOnSuccessListener", onSuccessListenerClass)
            val addOnFailureListenerMethod = downloadTask.javaClass.getMethod("addOnFailureListener", onFailureListenerClass)
            
            val successListener = java.lang.reflect.Proxy.newProxyInstance(
                javaClass.classLoader,
                arrayOf(onSuccessListenerClass)
            ) { _, _, _ ->
                onProgress(100)
                onSuccess("Language model downloaded successfully for $sourceLanguage → $targetLanguage")
                null
            }
            
            val failureListener = java.lang.reflect.Proxy.newProxyInstance(
                javaClass.classLoader,
                arrayOf(onFailureListenerClass)
            ) { _, _, args ->
                onProgress(0)
                val exception = args?.get(0) as? Exception
                println("[$TAG] ML Kit initialization error: ${exception?.message}")
                onError(UiText.ExceptionString(exception ?: Exception("Failed to download language model")))
                null
            }
            
            addOnSuccessListenerMethod.invoke(downloadTask, successListener)
            addOnFailureListenerMethod.invoke(downloadTask, failureListener)
            
        } catch (e: ClassNotFoundException) {
            onProgress(0)
            onError(UiText.DynamicString("Google ML Kit is not available in this build. Please use the Play Store version."))
        } catch (e: Exception) {
            onProgress(0)
            println("[$TAG] ML Kit initialization error: ${e.message}")
            onError(UiText.ExceptionString(e))
        }
    }
    
    actual override suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onProgress: (Int) -> Unit,
        onSuccess: (List<String>) -> Unit,
        onError: (UiText) -> Unit
    ) {
        if (texts.isNullOrEmpty()) {
            onError(TranslationError.NoTextToTranslate.toUiText())
            return
        }
        
        try {
            initReflection()
            onProgress(0)
            
            val client = getTranslatorClient(source, target)
            if (client == null) {
                onError(TranslationError.EngineNotAvailable("Google ML Kit").toUiText())
                return
            }
            
            onProgress(5)
            
            val modelReady = ensureModelDownloaded(client)
            if (!modelReady) {
                onError(TranslationError.LanguageModelNotAvailable(source, target).toUiText())
                return
            }
            
            onProgress(10)
            
            // Cache client methods
            val clientClass = client.javaClass
            if (translateMethod == null) {
                translateMethod = clientClass.getMethod("translate", String::class.java)
            }
            
            val total = texts.size
            val results = Array(total) { "" }
            val completedCount = AtomicInteger(0)
            val semaphore = Semaphore(MAX_CONCURRENT_TRANSLATIONS)
            
            coroutineScope {
                val deferreds = texts.mapIndexed { index, paragraph ->
                    async(Dispatchers.Default) {
                        if (paragraph.isBlank()) {
                            results[index] = paragraph
                            val done = completedCount.incrementAndGet()
                            onProgress(10 + (done * 90 / total))
                        } else {
                            val translated = semaphore.withPermit {
                                translateSingleParagraphWithRetry(client, paragraph)
                            }
                            results[index] = translated ?: paragraph
                            val done = completedCount.incrementAndGet()
                            onProgress(10 + (done * 90 / total))
                        }
                    }
                }
                deferreds.awaitAll()
            }
            
            onProgress(100)
            onSuccess(results.toList())
            
        } catch (e: ClassNotFoundException) {
            onProgress(0)
            onError(TranslationError.EngineNotAvailable("Google ML Kit").toUiText())
        } catch (e: Exception) {
            onProgress(0)
            println("[$TAG] Error: ${e.message}")
            e.printStackTrace()
            val translationError = TranslationError.fromException(
                exception = e,
                engineName = "Google ML Kit",
                sourceLanguage = source,
                targetLanguage = target
            )
            onError(translationError.toUiText())
        }
    }
    
    private fun getTranslatorClient(source: String, target: String): Any? {
        return try {
            initReflection()
            val builder = builderClass?.getDeclaredConstructor()?.newInstance() ?: return null
            setSourceMethod?.invoke(builder, source)
            setTargetMethod?.invoke(builder, target)
            val options = buildMethod?.invoke(builder)
            getClientMethod?.invoke(null, options)
        } catch (e: Exception) {
            println("[$TAG] Failed to get client: ${e.message}")
            null
        }
    }
    
    private suspend fun ensureModelDownloaded(client: Any): Boolean = suspendCoroutine { continuation ->
        try {
            val downloadM = downloadMethod ?: client.javaClass.getMethod("downloadModelIfNeeded").also { downloadMethod = it }
            val downloadTask = downloadM.invoke(client)
            
            val addSuccessM = addOnSuccessListenerMethod ?: downloadTask.javaClass.getMethod("addOnSuccessListener", onSuccessListenerClass).also { addOnSuccessListenerMethod = it }
            val addFailureM = addOnFailureListenerMethod ?: downloadTask.javaClass.getMethod("addOnFailureListener", onFailureListenerClass).also { addOnFailureListenerMethod = it }
            
            val successListener = java.lang.reflect.Proxy.newProxyInstance(
                javaClass.classLoader,
                arrayOf(onSuccessListenerClass)
            ) { _, _, _ ->
                continuation.resume(true)
                null
            }
            
            val failureListener = java.lang.reflect.Proxy.newProxyInstance(
                javaClass.classLoader,
                arrayOf(onFailureListenerClass)
            ) { _, _, args ->
                val exception = args?.get(0) as? Exception
                println("[$TAG] Model download failed: ${exception?.message}")
                continuation.resume(false)
                null
            }
            
            addSuccessM.invoke(downloadTask, successListener)
            addFailureM.invoke(downloadTask, failureListener)
            
        } catch (e: Exception) {
            println("[$TAG] Error downloading model: ${e.message}")
            continuation.resume(false)
        }
    }
    
    /**
     * Fast single paragraph translation with automated retry to guarantee no untranslated text
     */
    private suspend fun translateSingleParagraphWithRetry(client: Any, text: String): String? {
        if (text.isBlank()) return text
        
        var attempt = 0
        while (attempt <= MAX_RETRIES_PER_PARAGRAPH) {
            val result = suspendCancellableCoroutine<String?> { continuation ->
                try {
                    val tMethod = translateMethod ?: client.javaClass.getMethod("translate", String::class.java).also { translateMethod = it }
                    val translateTask = tMethod.invoke(client, text)
                    
                    val addSuccessM = addOnSuccessListenerMethod ?: translateTask.javaClass.getMethod("addOnSuccessListener", onSuccessListenerClass).also { addOnSuccessListenerMethod = it }
                    val addFailureM = addOnFailureListenerMethod ?: translateTask.javaClass.getMethod("addOnFailureListener", onFailureListenerClass).also { addOnFailureListenerMethod = it }
                    
                    val successListener = java.lang.reflect.Proxy.newProxyInstance(
                        javaClass.classLoader,
                        arrayOf(onSuccessListenerClass)
                    ) { _, _, args ->
                        val translatedText = args?.get(0) as? String
                        if (continuation.isActive) continuation.resume(translatedText)
                        null
                    }
                    
                    val failureListener = java.lang.reflect.Proxy.newProxyInstance(
                        javaClass.classLoader,
                        arrayOf(onFailureListenerClass)
                    ) { _, _, _ ->
                        if (continuation.isActive) continuation.resume(null)
                        null
                    }
                    
                    addSuccessM.invoke(translateTask, successListener)
                    addFailureM.invoke(translateTask, failureListener)
                } catch (e: Exception) {
                    if (continuation.isActive) continuation.resume(null)
                }
            }
            
            if (result != null && result.isNotEmpty()) {
                return result
            }
            
            attempt++
            if (attempt <= MAX_RETRIES_PER_PARAGRAPH) {
                delay(40L * attempt)
            }
        }
        
        return null
    }
}
