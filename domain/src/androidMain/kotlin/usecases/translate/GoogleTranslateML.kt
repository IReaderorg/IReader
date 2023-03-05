package ireader.domain.usecases.translate

import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import ireader.domain.data.engines.TranslateEngine
import ireader.i18n.UiText

actual class GoogleTranslateML : TranslateEngine() {
    actual override suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onSuccess: (List<String>) -> Unit,
        onError: (UiText) -> Unit
    ) {
        val options =
            TranslatorOptions.Builder().setSourceLanguage(source).setTargetLanguage(target).build()
        val client = Translation.getClient(options)
        client.downloadModelIfNeeded().onSuccessTask {
            client.translate(texts.joinToString("####")).addOnSuccessListener { result ->
                onSuccess(result.split("####"))
            }
        }.addOnFailureListener {
            onError(UiText.ExceptionString(it))
        }
    }
}