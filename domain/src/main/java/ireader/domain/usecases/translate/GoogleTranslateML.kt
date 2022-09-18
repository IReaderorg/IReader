package ireader.domain.usecases.translate

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import ireader.domain.data.engines.TranslateEngine
import ireader.i18n.UiText

class GoogleTranslateML : TranslateEngine {
    override val id: Long = -1

    fun languages() = listOf(
        "af" to "Afrikaans ",
        "sq" to "Albanian ",
        "ar" to "Arabic ",
        "be" to "Belarusian ",
        "bn" to "Bengali",
        "bg" to "Bulgarian  ",
        "ca" to "Catalan ",
        "zh" to "Chinese",
        "co" to "Corsican ",
        "hr" to "Croatian ",
        "cs" to "Czech ",
        "da" to "Danish ",
        "nl" to "Dutch ",
        "en" to "English ",
        "eo" to "Esperanto ",
        "et" to "Estonian ",
        "tl" to "Filipino ",
        "fi" to "Finnish ",
        "fr" to "French ",
        "gl" to "Galician ",
        "ka" to "Georgian ",
        "de" to "German ",
        "el" to "Greek ",
        "gu" to "Gujarati ",
        "ht" to "Haitian Creole ",
        "iw" to "Hebrew ",
        "hi" to "Hindi ",
        "hu" to "Hungarian ",
        "`is`" to "Icelandic ",
        "id" to "Indonesian ",
        "ga" to "Irish ",
        "it" to "Italian ",
        "ja" to "Japanese ",
        "jw" to "Javanese ",
        "kn" to "Kannada ",
        "ko" to "Korean ",
        "lv" to "Latvian ",
        "lt" to "Lithuanian ",
        "mk" to "Macedonian ",
        "ms" to "Malay ",
        "mt" to "Maltese ",
        "mr" to "Marathi ",
        "no" to "Norwegian ",
        "fa" to "Persian ",
        "pl" to "Polish ",
        "pt" to "Portuguese ",
        "ro" to "Romanian ",
        "ru" to "Russian ",
        "sk" to "Slovak ",
        "sl" to "Slovenian ",
        "es" to "Spanish ",
        "sw" to "Swahili ",
        "sv" to "Swedish ",
        "ta" to "Tamil ",
        "te" to "Telugu ",
        "th" to "Thai ",
        "tr" to "Turkish ",
        "uk" to "Ukrainian ",
        "ur" to "Urdu ",
        "vi" to "Vietnamese ",
        "cy" to "Welsh ",
    )

    override val supportedLanguages: List<Pair<String, String>> = languages()


    override suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onSuccess: (List<String>) -> Unit,
        onError: (UiText) -> Unit
    ) {
        TranslateLanguage.HAITIAN_CREOLE
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