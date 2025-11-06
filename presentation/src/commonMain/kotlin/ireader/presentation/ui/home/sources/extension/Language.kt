

package ireader.presentation.ui.home.sources.extension

/* inline */
data class Language(val code: String) {

    fun toEmoji(): String? {
        val country = when (code) {
            "ar" -> "SA" // Arabic
            "bg" -> "BG" // Bulgarian
            "bn" -> "BD" // Bengali
            "ca" -> "ES" // Catalan
            "cs" -> "CZ" // Czech
            "da" -> "DK" // Danish
            "de" -> "DE" // German
            "el" -> "GR" // Greek
            "en" -> "GB" // English
            "es" -> "ES" // Spanish
            "fa" -> "IR" // Persian
            "fi" -> "FI" // Finnish
            "fil" -> "PH" // Filipino
            "fr" -> "FR" // French
            "he" -> "IL" // Hebrew
            "hi" -> "IN" // Hindi
            "hr" -> "HR" // Croatian
            "hu" -> "HU" // Hungarian
            "id" -> "ID" // Indonesian
            "it" -> "IT" // Italian
            "ja" -> "JP" // Japanese
            "ko" -> "KR" // Korean
            "lt" -> "LT" // Lithuanian
            "ms" -> "MY" // Malay
            "nb" -> "NO" // Norwegian
            "nl" -> "NL" // Dutch
            "no" -> "NO" // Norwegian
            "pl" -> "PL" // Polish
            "pt" -> "BR" // Portuguese
            "pt-BR" -> "BR" // Portuguese (Brazil)
            "pt-PT" -> "PT" // Portuguese (Portugal)
            "ro" -> "RO" // Romanian
            "ru" -> "RU" // Russian
            "sk" -> "SK" // Slovak
            "sr" -> "RS" // Serbian
            "sv" -> "SE" // Swedish
            "th" -> "TH" // Thai
            "tl" -> "PH" // Tagalog
            "tr" -> "TR" // Turkish
            "uk" -> "UA" // Ukrainian
            "vi" -> "VN" // Vietnamese
            "zh" -> "CN" // Chinese
            "zh-Hans" -> "CN" // Chinese Simplified
            "zh-Hant" -> "TW" // Chinese Traditional
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
    var buffer = charArrayOf()
    for (codePoint in codePoints) {
        buffer += Character.toChars(codePoint)
    }
    return String(buffer)
}
