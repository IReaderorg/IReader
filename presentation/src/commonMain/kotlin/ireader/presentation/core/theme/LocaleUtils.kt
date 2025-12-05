package ireader.presentation.core.theme

/**
 * Parses a language code into language and region components.
 * 
 * @param languageCode The language code (e.g., "en", "zh-CN", "zh_TW")
 * @return Pair of (language, region) where region may be empty
 */
fun parseLanguageCode(languageCode: String): Pair<String, String> {
    return when {
        languageCode.contains("-r") -> {
            // Format: zh-rCN
            val parts = languageCode.split("-r")
            parts[0] to parts.getOrElse(1) { "" }
        }
        languageCode.contains("-") -> {
            // Format: zh-CN
            val parts = languageCode.split("-")
            parts[0] to parts.getOrElse(1) { "" }
        }
        languageCode.contains("_") -> {
            // Format: zh_CN
            val parts = languageCode.split("_")
            parts[0] to parts.getOrElse(1) { "" }
        }
        else -> {
            languageCode to ""
        }
    }
}
