package ireader.domain.usecases.translate

import ireader.domain.data.engines.TranslateEngine
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class GoogleTranslateML : TranslateEngine() {
    override val id: Long
        get() = 0
    
    actual override val requiresInitialization: Boolean
        get() = true
    
    companion object {
        /** Maximum paragraphs per chunk to avoid overwhelming ML Kit */
        const val MAX_CHUNK_SIZE = 5 // Reduced from 10 to improve accuracy
        
        /** Paragraph separator marker - using unique marker less likely to be translated */
        const val MARKER = "\n<<<PARA_SEP>>>\n"
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
            // Use reflection to check if ML Kit is available at runtime
            val translatorOptionsClass = Class.forName("com.google.mlkit.nl.translate.TranslatorOptions")
            val translationClass = Class.forName("com.google.mlkit.nl.translate.Translation")
            
            onProgress(0)
            
            // Build options using reflection
            val builderClass = Class.forName("com.google.mlkit.nl.translate.TranslatorOptions\$Builder")
            val builder = builderClass.getDeclaredConstructor().newInstance()
            
            val setSourceMethod = builderClass.getMethod("setSourceLanguage", String::class.java)
            val setTargetMethod = builderClass.getMethod("setTargetLanguage", String::class.java)
            val buildMethod = builderClass.getMethod("build")
            
            setSourceMethod.invoke(builder, sourceLanguage)
            setTargetMethod.invoke(builder, targetLanguage)
            val options = buildMethod.invoke(builder)
            
            // Get client
            val getClientMethod = translationClass.getMethod("getClient", translatorOptionsClass)
            val client = getClientMethod.invoke(null, options)
            
            onProgress(10)
            
            // Download model
            val downloadMethod = client.javaClass.getMethod("downloadModelIfNeeded")
            val downloadTask = downloadMethod.invoke(client)
            
            // Add success listener
            val addOnSuccessListenerMethod = downloadTask.javaClass.getMethod("addOnSuccessListener",
                Class.forName("com.google.android.gms.tasks.OnSuccessListener"))
            
            val successListener = java.lang.reflect.Proxy.newProxyInstance(
                javaClass.classLoader,
                arrayOf(Class.forName("com.google.android.gms.tasks.OnSuccessListener"))
            ) { _, _, _ ->
                onProgress(100)
                onSuccess("Language model downloaded successfully for $sourceLanguage → $targetLanguage")
                null
            }
            
            addOnSuccessListenerMethod.invoke(downloadTask, successListener)
            
            // Add failure listener
            val addOnFailureListenerMethod = downloadTask.javaClass.getMethod("addOnFailureListener",
                Class.forName("com.google.android.gms.tasks.OnFailureListener"))
            
            val failureListener = java.lang.reflect.Proxy.newProxyInstance(
                javaClass.classLoader,
                arrayOf(Class.forName("com.google.android.gms.tasks.OnFailureListener"))
            ) { _, _, args ->
                onProgress(0)
                val exception = args?.get(0) as? Exception
                println("Google ML Kit initialization error: ${exception?.message}")
                exception?.printStackTrace()
                onError(UiText.ExceptionString(exception ?: Exception("Failed to download language model")))
                null
            }
            
            addOnFailureListenerMethod.invoke(downloadTask, failureListener)
            
        } catch (e: ClassNotFoundException) {
            // ML Kit not available (F-Droid build)
            onProgress(0)
            onError(UiText.DynamicString("Google ML Kit is not available in this build. Please use the Play Store version."))
        } catch (e: Exception) {
            onProgress(0)
            println("Google ML Kit initialization error: ${e.message}")
            e.printStackTrace()
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
        // Validate inputs
        if (texts.isNullOrEmpty()) {
            onError(TranslationError.NoTextToTranslate.toUiText())
            return
        }
        
        try {
            onProgress(0)
            
            // Get translator client
            val client = getTranslatorClient(source, target)
            if (client == null) {
                onError(TranslationError.EngineNotAvailable("Google ML Kit").toUiText())
                return
            }
            
            onProgress(10)
            
            // Ensure model is downloaded first
            val modelReady = ensureModelDownloaded(client)
            if (!modelReady) {
                onError(TranslationError.LanguageModelNotAvailable(source, target).toUiText())
                return
            }
            
            onProgress(20)
            
            // Chunk texts like other engines do
            val chunks = texts.chunked(MAX_CHUNK_SIZE)
            val allResults = mutableListOf<String>()
            val totalChunks = chunks.size
            
            chunks.forEachIndexed { chunkIndex, chunk ->
                // Calculate progress: 20% for setup, 70% for translation, 10% for completion
                val chunkProgress = 20 + ((chunkIndex + 1) * 70 / totalChunks)
                onProgress(chunkProgress)
                
                var translatedChunk = translateChunk(client, chunk)
                
                // If chunked translation failed or returned wrong count, translate individually
                if (translatedChunk == null || translatedChunk.size != chunk.size) {
                    println("[GoogleML] Chunk translation failed or returned wrong count, translating individually")
                    translatedChunk = translateIndividually(client, chunk)
                }
                
                if (translatedChunk == null) {
                    onError(TranslationError.Unknown("Google ML Kit",
                        Exception("Translation failed for chunk ${chunkIndex + 1}")).toUiText())
                    return
                }
                allResults.addAll(translatedChunk)
            }
            
            // Ensure correct paragraph count
            val finalResults = if (allResults.size == texts.size) {
                allResults
            } else {
                adjustParagraphCount(allResults, texts)
            }
            
            onProgress(100)
            onSuccess(finalResults)
            
        } catch (e: ClassNotFoundException) {
            // ML Kit not available (F-Droid build)
            onProgress(0)
            onError(TranslationError.EngineNotAvailable("Google ML Kit").toUiText())
        } catch (e: Exception) {
            onProgress(0)
            println("Google Translate ML error: ${e.message}")
            e.printStackTrace()
            // Use TranslationError for user-friendly error messages
            val translationError = TranslationError.fromException(
                exception = e,
                engineName = "Google ML Kit",
                sourceLanguage = source,
                targetLanguage = target
            )
            onError(translationError.toUiText())
        }
    }
    
    /**
     * Get translator client using reflection
     */
    private fun getTranslatorClient(source: String, target: String): Any? {
        return try {
            val translatorOptionsClass = Class.forName("com.google.mlkit.nl.translate.TranslatorOptions")
            val translationClass = Class.forName("com.google.mlkit.nl.translate.Translation")
            
            val builderClass = Class.forName("com.google.mlkit.nl.translate.TranslatorOptions\$Builder")
            val builder = builderClass.getDeclaredConstructor().newInstance()
            
            val setSourceMethod = builderClass.getMethod("setSourceLanguage", String::class.java)
            val setTargetMethod = builderClass.getMethod("setTargetLanguage", String::class.java)
            val buildMethod = builderClass.getMethod("build")
            
            setSourceMethod.invoke(builder, source)
            setTargetMethod.invoke(builder, target)
            val options = buildMethod.invoke(builder)
            
            val getClientMethod = translationClass.getMethod("getClient", translatorOptionsClass)
            getClientMethod.invoke(null, options)
        } catch (e: ClassNotFoundException) {
            null
        } catch (e: Exception) {
            println("Failed to get translator client: ${e.message}")
            null
        }
    }
    
    /**
     * Ensure model is downloaded using suspendCoroutine to properly wait
     */
    private suspend fun ensureModelDownloaded(client: Any): Boolean = suspendCoroutine { continuation ->
        try {
            val downloadMethod = client.javaClass.getMethod("downloadModelIfNeeded")
            val downloadTask = downloadMethod.invoke(client)
            
            // Add success listener
            val addOnSuccessListenerMethod = downloadTask.javaClass.getMethod("addOnSuccessListener",
                Class.forName("com.google.android.gms.tasks.OnSuccessListener"))
            
            val successListener = java.lang.reflect.Proxy.newProxyInstance(
                javaClass.classLoader,
                arrayOf(Class.forName("com.google.android.gms.tasks.OnSuccessListener"))
            ) { _, _, _ ->
                continuation.resume(true)
                null
            }
            
            addOnSuccessListenerMethod.invoke(downloadTask, successListener)
            
            // Add failure listener
            val addOnFailureListenerMethod = downloadTask.javaClass.getMethod("addOnFailureListener",
                Class.forName("com.google.android.gms.tasks.OnFailureListener"))
            
            val failureListener = java.lang.reflect.Proxy.newProxyInstance(
                javaClass.classLoader,
                arrayOf(Class.forName("com.google.android.gms.tasks.OnFailureListener"))
            ) { _, _, args ->
                val exception = args?.get(0) as? Exception
                println("Model download failed: ${exception?.message}")
                continuation.resume(false)
                null
            }
            
            addOnFailureListenerMethod.invoke(downloadTask, failureListener)
            
        } catch (e: Exception) {
            println("Error downloading model: ${e.message}")
            continuation.resume(false)
        }
    }
    
    /**
     * Translate a chunk of texts using suspendCoroutine to properly wait for result
     */
    private suspend fun translateChunk(client: Any, chunk: List<String>): List<String>? = suspendCoroutine { continuation ->
        try {
            val combinedText = chunk.joinToString(MARKER)
            
            val translateMethod = client.javaClass.getMethod("translate", String::class.java)
            val translateTask = translateMethod.invoke(client, combinedText)
            
            // Add success listener
            val addOnSuccessListenerMethod = translateTask.javaClass.getMethod("addOnSuccessListener",
                Class.forName("com.google.android.gms.tasks.OnSuccessListener"))
            
            val successListener = java.lang.reflect.Proxy.newProxyInstance(
                javaClass.classLoader,
                arrayOf(Class.forName("com.google.android.gms.tasks.OnSuccessListener"))
            ) { _, _, args ->
                val result = args?.get(0) as? String
                if (result != null && result.isNotEmpty()) {
                    // Split by marker, handling cases where marker might be translated or modified
                    val splitResults = result.split(MARKER, "\n<<<", ">>>\n", "PARA_SEP")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() && !it.matches(Regex("[<>_]+")) }
                    
                    // If split didn't work well, try to match paragraph count
                    val finalResults = if (splitResults.size == chunk.size) {
                        splitResults
                    } else {
                        // Fallback: translate each paragraph individually
                        null
                    }
                    continuation.resume(finalResults)
                } else {
                    continuation.resume(null)
                }
                null
            }
            
            addOnSuccessListenerMethod.invoke(translateTask, successListener)
            
            // Add failure listener
            val addOnFailureListenerMethod = translateTask.javaClass.getMethod("addOnFailureListener",
                Class.forName("com.google.android.gms.tasks.OnFailureListener"))
            
            val failureListener = java.lang.reflect.Proxy.newProxyInstance(
                javaClass.classLoader,
                arrayOf(Class.forName("com.google.android.gms.tasks.OnFailureListener"))
            ) { _, _, args ->
                val exception = args?.get(0) as? Exception
                println("Translation chunk failed: ${exception?.message}")
                continuation.resume(null)
            }
            
            addOnFailureListenerMethod.invoke(translateTask, failureListener)
            
        } catch (e: Exception) {
            println("Error translating chunk: ${e.message}")
            continuation.resume(null)
        }
    }
    
    /**
     * Translate paragraphs individually (fallback when chunking fails)
     */
    private suspend fun translateIndividually(client: Any, texts: List<String>): List<String>? {
        return try {
            val results = mutableListOf<String>()
            
            for (text in texts) {
                if (text.isBlank()) {
                    results.add(text)
                    continue
                }
                
                val translated = suspendCoroutine<String?> { continuation ->
                    try {
                        val translateMethod = client.javaClass.getMethod("translate", String::class.java)
                        val translateTask = translateMethod.invoke(client, text)
                        
                        val addOnSuccessListenerMethod = translateTask.javaClass.getMethod("addOnSuccessListener",
                            Class.forName("com.google.android.gms.tasks.OnSuccessListener"))
                        
                        val successListener = java.lang.reflect.Proxy.newProxyInstance(
                            javaClass.classLoader,
                            arrayOf(Class.forName("com.google.android.gms.tasks.OnSuccessListener"))
                        ) { _, _, args ->
                            val result = args?.get(0) as? String
                            continuation.resume(result)
                            null
                        }
                        
                        addOnSuccessListenerMethod.invoke(translateTask, successListener)
                        
                        val addOnFailureListenerMethod = translateTask.javaClass.getMethod("addOnFailureListener",
                            Class.forName("com.google.android.gms.tasks.OnFailureListener"))
                        
                        val failureListener = java.lang.reflect.Proxy.newProxyInstance(
                            javaClass.classLoader,
                            arrayOf(Class.forName("com.google.android.gms.tasks.OnFailureListener"))
                        ) { _, _, _ ->
                            continuation.resume(null)
                            null
                        }
                        
                        addOnFailureListenerMethod.invoke(translateTask, failureListener)
                    } catch (e: Exception) {
                        continuation.resume(null)
                    }
                }
                
                results.add(translated ?: text)
            }
            
            results
        } catch (e: Exception) {
            println("Individual translation failed: ${e.message}")
            null
        }
    }
    
    /**
     * Adjust paragraph count to match input
     */
    private fun adjustParagraphCount(translatedParagraphs: List<String>, originalTexts: List<String>): List<String> {
        val result = translatedParagraphs.toMutableList()
        
        // If we have too few paragraphs, add original ones
        while (result.size < originalTexts.size) {
            result.add(originalTexts[result.size])
        }
        
        // If we have too many paragraphs, remove extras
        if (result.size > originalTexts.size) {
            result.subList(originalTexts.size, result.size).clear()
        }
        
        return result
    }
}
