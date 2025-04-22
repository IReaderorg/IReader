package ireader.domain.usecases.translate

import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import ireader.domain.data.engines.TranslateEngine
import ireader.i18n.UiText
import ireader.i18n.resources.MR

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
            onError(UiText.MStringResource(MR.strings.no_text_to_translate))
            return
        }
        
        try {
            onProgress(0)
            val options =
                TranslatorOptions.Builder().setSourceLanguage(source).setTargetLanguage(target).build()
            val client = Translation.getClient(options)
            
            onProgress(20)
            client.downloadModelIfNeeded().onSuccessTask {
                onProgress(60)
                client.translate(texts.joinToString("####")).addOnSuccessListener { result ->
                    if (result != null && result.isNotEmpty()) {
                        onProgress(100)
                        onSuccess(result.split("####"))
                    } else {
                        onError(UiText.MStringResource(MR.strings.empty_response))
                    }
                }
            }.addOnFailureListener {
                onProgress(0)
                println("Google Translate ML error: ${it.message}")
                it.printStackTrace()
                onError(UiText.ExceptionString(it))
            }
        } catch (e: Exception) {
            onProgress(0)
            println("Google Translate ML error: ${e.message}")
            e.printStackTrace()
            onError(UiText.ExceptionString(e))
        }
    }
}