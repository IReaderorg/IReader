package ireader.presentation.ui.home.sources.extension

/* inline */
data class Language(val code: String) {

    fun toEmoji(): String? {
        val country = when (code) {
            // Common languages
            "ar" -> "SA" // Arabic
            "be" -> "BY" // Belarusian
            "bg" -> "BG" // Bulgarian
            "bn" -> "BD" // Bengali
            "ca" -> "ES" // Catalan
            "cs" -> "CZ" // Czech
            "da" -> "DK" // Danish
            "de" -> "DE" // German
            "el" -> "GR" // Greek
            "en" -> "GB" // English
            "eo" -> "EU" // Esperanto (using EU flag)
            "es" -> "ES" // Spanish
            "eu" -> "ES" // Basque
            "fa" -> "IR" // Persian
            "fi" -> "FI" // Finnish
            "fil" -> "PH" // Filipino
            "fr" -> "FR" // French
            "gl" -> "ES" // Galician
            "he" -> "IL" // Hebrew
            "hi" -> "IN" // Hindi
            "hr" -> "HR" // Croatian
            "hu" -> "HU" // Hungarian
            "id" -> "ID" // Indonesian
            "in" -> "ID" // Indonesian (alternative code)
            "it" -> "IT" // Italian
            "ja" -> "JP" // Japanese
            "jv" -> "ID" // Javanese
            "ka" -> "GE" // Georgian
            "kk" -> "KZ" // Kazakh
            "km" -> "KH" // Khmer
            "kn" -> "IN" // Kannada
            "ko" -> "KR" // Korean
            "lt" -> "LT" // Lithuanian
            "lv" -> "LV" // Latvian
            "ml" -> "IN" // Malayalam
            "mn" -> "MN" // Mongolian
            "mr" -> "IN" // Marathi
            "ms" -> "MY" // Malay
            "my" -> "MM" // Burmese
            "nb" -> "NO" // Norwegian Bokmål
            "ne" -> "NP" // Nepali
            "nl" -> "NL" // Dutch
            "nn" -> "NO" // Norwegian Nynorsk
            "no" -> "NO" // Norwegian
            "or" -> "IN" // Odia
            "pa" -> "IN" // Punjabi
            "pl" -> "PL" // Polish
            "pt" -> "BR" // Portuguese
            "pt-BR" -> "BR" // Portuguese (Brazil)
            "pt-PT" -> "PT" // Portuguese (Portugal)
            "ro" -> "RO" // Romanian
            "ru" -> "RU" // Russian
            "sa" -> "IN" // Sanskrit
            "si" -> "LK" // Sinhala
            "sk" -> "SK" // Slovak
            "sr" -> "RS" // Serbian
            "sv" -> "SE" // Swedish
            "ta" -> "IN" // Tamil
            "te" -> "IN" // Telugu
            "th" -> "TH" // Thai
            "tl" -> "PH" // Tagalog
            "tr" -> "TR" // Turkish
            "uk" -> "UA" // Ukrainian
            "ur" -> "PK" // Urdu
            "uz" -> "UZ" // Uzbek
            "vi" -> "VN" // Vietnamese
            "zh" -> "CN" // Chinese
            "zh-CN" -> "CN" // Chinese Simplified
            "zh-TW" -> "TW" // Chinese Traditional
            "zh-Hans" -> "CN" // Chinese Simplified
            "zh-Hant" -> "TW" // Chinese Traditional
            // Less common languages
            "aii" -> "IQ" // Assyrian Neo-Aramaic
            "am" -> "ET" // Amharic
            "ceb" -> "PH" // Cebuano
            "cv" -> "RU" // Chuvash
            "sah" -> "RU" // Sakha/Yakut
            "sc" -> "IT" // Sardinian
            "sdh" -> "IR" // Southern Kurdish
            "ti" -> "ER" // Tigrinya
            // Multi-language
            "multi" -> "UN" // United Nations flag for multi-language
            else -> null
        }
        return country?.let { toFlag(it) }
    }

    private fun toFlag(countryCode: String): String {
        return try {
            val firstLetter = countryCode[0].code - 0x41 + 0x1F1E6
            val secondLetter = countryCode[1].code - 0x41 + 0x1F1E6
            String.fromCodePoints(firstLetter, secondLetter)
        } catch (e: Throwable) {
            ""
        }
    }
}

fun String.Companion.fromCodePoints(vararg codePoints: Int): String {
    val builder = StringBuilder()
    for (codePoint in codePoints) {
        // For BMP characters (most common), just convert directly
        if (codePoint in 0..0xFFFF) {
            builder.append(codePoint.toChar())
        } else {
            // For supplementary characters, create surrogate pair
            val highSurrogate = ((codePoint - 0x10000) shr 10) + 0xD800
            val lowSurrogate = ((codePoint - 0x10000) and 0x3FF) + 0xDC00
            builder.append(highSurrogate.toChar())
            builder.append(lowSurrogate.toChar())
        }
    }
    return builder.toString()
}
