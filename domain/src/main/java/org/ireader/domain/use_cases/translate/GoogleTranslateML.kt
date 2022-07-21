package org.ireader.domain.use_cases.translate

import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import org.ireader.common_data.TranslateEngine

class GoogleTranslateML : TranslateEngine {
    override val id: Long = -1

    override suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onSuccess: (List<String>) -> Unit
    ) {
        val options =
            TranslatorOptions.Builder().setSourceLanguage(source).setTargetLanguage(target).build()
        val client = Translation.getClient(options)
        client.downloadModelIfNeeded().onSuccessTask {
            client.translate(texts.joinToString("####")).addOnSuccessListener { result ->
                onSuccess(result.split("####"))
            }
        }
    }
}