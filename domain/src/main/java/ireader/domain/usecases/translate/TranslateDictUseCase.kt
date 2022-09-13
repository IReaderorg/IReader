package ireader.domain.usecases.translate

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.delay
import ireader.domain.data.engines.TranslateEngine
import ireader.common.resources.UiText
import ireader.core.api.http.HttpClients
import java.net.URLEncoder

class TranslateDictUseCase(
    private val client: HttpClients
) : TranslateEngine {
    override val supportedLanguages: List<Pair<String, String>> = listOf(
       "auto " to "Automatic ",
        "af" to "Afrikaans ",
        "sq" to "Albanian ",
        "am" to "Amharic ",
        "ar" to "Arabic ",
        "hy" to "Armenian ",
        "az" to "Azerbaijani ",
        "eu" to "Basque ",
        "be" to "Belarusian ",
        "bn" to "Bengali ",
        "bs" to "Bosnian  ",
        "bg" to "Bulgarian  ",
        "ca" to "Catalan ",
        "ceb" to "Cebuano ",
        "ny" to "Chichewa ",
        "zh_cn" to "Chinese Simplified ",
        "zh_tw" to "Chinese Traditional ",
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
        "fy" to "Frisian ",
        "gl" to "Galician ",
        "ka" to "Georgian ",
        "de" to "German ",
        "el" to "Greek ",
        "gu" to "Gujarati ",
        "ht" to "Haitian Creole ",
        "ha" to "Hausa ",
        "haw" to "Hawaiian ",
        "iw" to "Hebrew ",
        "hi" to "Hindi ",
        "hmn" to "Hmong ",
        "hu" to "Hungarian ",
        "`is`" to "Icelandic ",
        "ig" to "Igbo ",
        "id" to "Indonesian ",
        "ga" to "Irish ",
        "it" to "Italian ",
        "ja" to "Japanese ",
        "jw" to "Javanese ",
        "kn" to "Kannada ",
        "kk" to "Kazakh ",
        "km" to "Khmer ",
        "ko" to "Korean ",
        "ku" to "Kurdish",
        "ky" to "Kyrgyz ",
        "lo" to "Lao ",
        "la" to "Latin ",
        "lv" to "Latvian ",
        "lt" to "Lithuanian ",
        "lb" to "Luxembourgish ",
        "mk" to "Macedonian ",
        "mg" to "Malagasy ",
        "ms" to "Malay ",
        "ml" to "Malayalam ",
        "mt" to "Maltese ",
        "mi" to "Maori ",
        "mr" to "Marathi ",
        "mn" to "Mongolian ",
        "my" to "Myanmar",
        "ne" to "Nepali ",
        "no" to "Norwegian ",
        "ps" to "Pashto ",
        "fa" to "Persian ",
        "pl" to "Polish ",
        "pt" to "Portuguese ",
        "ma" to "Punjabi ",
        "ro" to "Romanian ",
        "ru" to "Russian ",
        "sm" to "Samoan ",
        "gd" to "Scots Gaelic ",
        "sr" to "Serbian ",
        "st" to "Sesotho ",
        "sn" to "Shona ",
        "sd" to "Sindhi ",
        "si" to "Sinhala ",
        "sk" to "Slovak ",
        "sl" to "Slovenian ",
        "so" to "Somali ",
        "es" to "Spanish ",
        "su" to "Sundanese ",
        "sw" to "Swahili ",
        "sv" to "Swedish ",
        "tg" to "Tajik ",
        "ta" to "Tamil ",
        "te" to "Telugu ",
        "th" to "Thai ",
        "tr" to "Turkish ",
        "uk" to "Ukrainian ",
        "ur" to "Urdu ",
        "uz" to "Uzbek ",
        "vi" to "Vietnamese ",
        "cy" to "Welsh ",
        "xh" to "Xhosa ",
        "yi" to "Yiddish ",
        "yo" to "Yoruba ",
        "zu" to "Zulu "
    )

    override val id: Long
        get() = -2

    override suspend fun translate(
     texts: List<String>,
     source: String,
     target: String,
     onSuccess: (List<String>) -> Unit,
     onError: (UiText) -> Unit
    ) {
        val result = texts.joinToString("\n").chunked(1000).map { text ->
            val url = "https://t2.translatedict.com/1.php?p1=$source&p2=$target&p3=${URLEncoder.encode(text,"utf-8")}"
            delay(1000)
            client.default.get(urlString = url) {}.bodyAsText()
        }.joinToString().split("\n")
        onSuccess(result)
    }
}