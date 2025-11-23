package ireader.domain.usecases.translate

import ireader.domain.data.engines.TranslateEngine
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.*

actual class GoogleTranslateML : TranslateEngine() {
    override val id: Long
        get() = 0
    
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
            onError(UiText.MStringResource(Res.string.no_text_to_translate))
            return
        }
        
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
            
            setSourceMethod.invoke(builder, source)
            setTargetMethod.invoke(builder, target)
            val options = buildMethod.invoke(builder)
            
            // Get client
            val getClientMethod = translationClass.getMethod("getClient", translatorOptionsClass)
            val client = getClientMethod.invoke(null, options)
            
            onProgress(20)
            
            // Download model if needed
            val downloadMethod = client.javaClass.getMethod("downloadModelIfNeeded")
            val downloadTask = downloadMethod.invoke(client)
            
            // Add success listener using reflection
            val onSuccessTaskMethod = downloadTask.javaClass.getMethod("onSuccessTask", 
                Class.forName("com.google.android.gms.tasks.SuccessContinuation"))
            
            val successContinuation = java.lang.reflect.Proxy.newProxyInstance(
                javaClass.classLoader,
                arrayOf(Class.forName("com.google.android.gms.tasks.SuccessContinuation"))
            ) { _, method, _ ->
                if (method.name == "then") {
                    onProgress(60)
                    val translateMethod = client.javaClass.getMethod("translate", String::class.java)
                    val translateTask = translateMethod.invoke(client, texts.joinToString("####"))
                    
                    // Add success listener for translation
                    val addOnSuccessListenerMethod = translateTask.javaClass.getMethod("addOnSuccessListener",
                        Class.forName("com.google.android.gms.tasks.OnSuccessListener"))
                    
                    val successListener = java.lang.reflect.Proxy.newProxyInstance(
                        javaClass.classLoader,
                        arrayOf(Class.forName("com.google.android.gms.tasks.OnSuccessListener"))
                    ) { _, _, args ->
                        val result = args?.get(0) as? String
                        if (result != null && result.isNotEmpty()) {
                            onProgress(100)
                            onSuccess(result.split("####"))
                        } else {
                            onError(UiText.MStringResource(Res.string.empty_response))
                        }
                        null
                    }
                    
                    addOnSuccessListenerMethod.invoke(translateTask, successListener)
                    translateTask
                } else null
            }
            
            val taskWithTranslation = onSuccessTaskMethod.invoke(downloadTask, successContinuation)
            
            // Add failure listener
            val addOnFailureListenerMethod = taskWithTranslation.javaClass.getMethod("addOnFailureListener",
                Class.forName("com.google.android.gms.tasks.OnFailureListener"))
            
            val failureListener = java.lang.reflect.Proxy.newProxyInstance(
                javaClass.classLoader,
                arrayOf(Class.forName("com.google.android.gms.tasks.OnFailureListener"))
            ) { _, _, args ->
                onProgress(0)
                val exception = args?.get(0) as? Exception
                println("Google Translate ML error: ${exception?.message}")
                exception?.printStackTrace()
                onError(UiText.ExceptionString(exception ?: Exception("Unknown error")))
                null
            }
            
            addOnFailureListenerMethod.invoke(taskWithTranslation, failureListener)
            
        } catch (e: ClassNotFoundException) {
            // ML Kit not available (F-Droid build)
            onProgress(0)
            onError(UiText.DynamicString("Translation feature is not available in this build. Please use the Play Store version for ML Kit translation."))
        } catch (e: Exception) {
            onProgress(0)
            println("Google Translate ML error: ${e.message}")
            e.printStackTrace()
            onError(UiText.ExceptionString(e))
        }
    }
}
