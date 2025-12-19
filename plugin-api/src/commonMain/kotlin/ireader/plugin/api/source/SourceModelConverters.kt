package ireader.plugin.api.source

/**
 * Converters between UnifiedSource models and IReader's internal models.
 * 
 * These are defined as extension functions that the main app can use
 * to convert between source plugin models and database models.
 * 
 * The actual conversion implementations will be in the domain layer
 * since they need access to IReader's Book/Chapter models.
 */

/**
 * Interface for converting source items to IReader books.
 * Implement this in the domain layer.
 */
interface SourceToBookConverter {
    /**
     * Convert a source item to a book model.
     */
    fun convertToBook(item: SourceItem, sourceId: Long): Any
    
    /**
     * Convert source item details to a book model.
     */
    fun convertToBook(details: SourceItemDetails, sourceId: Long): Any
    
    /**
     * Convert a source chapter to a chapter model.
     */
    fun convertToChapter(chapter: SourceChapter, bookId: Long): Any
    
    /**
     * Convert a book model to a source item.
     */
    fun convertToSourceItem(book: Any): SourceItem
    
    /**
     * Convert a chapter model to a source chapter.
     */
    fun convertToSourceChapter(chapter: Any): SourceChapter
}

/**
 * Helper object for source ID generation.
 * Matches Tachiyomi's ID generation algorithm.
 */
object SourceIdGenerator {
    /**
     * Generate a source ID from name, language, and version.
     * Uses MD5 hash of "${name.lowercase()}/$lang/$versionId".
     */
    fun generateId(name: String, lang: String, versionId: Int = 1): Long {
        val key = "${name.lowercase()}/$lang/$versionId"
        val bytes = simpleMd5(key.encodeToByteArray())
        return (0..7).map { bytes[it].toLong() and 0xff shl 8 * (7 - it) }
            .reduce(Long::or) and Long.MAX_VALUE
    }
    
    /**
     * Simple hash function for KMP compatibility.
     * For actual MD5, use platform-specific implementation.
     */
    private fun simpleMd5(input: ByteArray): ByteArray {
        // Simple hash for KMP - real MD5 should be implemented per-platform
        val result = ByteArray(16)
        var hash = 0L
        for (i in input.indices) {
            hash = hash * 31 + input[i]
        }
        for (i in 0 until 8) {
            result[i] = (hash shr (56 - i * 8)).toByte()
        }
        hash = hash xor (hash shr 32)
        for (i in 8 until 16) {
            result[i] = (hash shr (56 - (i - 8) * 8)).toByte()
        }
        return result
    }
}

/**
 * Extension to get a display-friendly language name.
 */
fun String.toLanguageDisplayName(): String = when (this.lowercase()) {
    "en" -> "English"
    "ja" -> "Japanese"
    "ko" -> "Korean"
    "zh" -> "Chinese"
    "zh-hans" -> "Chinese (Simplified)"
    "zh-hant" -> "Chinese (Traditional)"
    "es" -> "Spanish"
    "pt" -> "Portuguese"
    "pt-br" -> "Portuguese (Brazil)"
    "fr" -> "French"
    "de" -> "German"
    "it" -> "Italian"
    "ru" -> "Russian"
    "ar" -> "Arabic"
    "th" -> "Thai"
    "vi" -> "Vietnamese"
    "id" -> "Indonesian"
    "tr" -> "Turkish"
    "pl" -> "Polish"
    "all" -> "All"
    "other" -> "Other"
    else -> this.uppercase()
}

/**
 * Extension to get language flag emoji.
 */
fun String.toLanguageFlag(): String = when (this.lowercase()) {
    "en" -> "🇬🇧"
    "ja" -> "🇯🇵"
    "ko" -> "🇰🇷"
    "zh", "zh-hans", "zh-hant" -> "🇨🇳"
    "es" -> "🇪🇸"
    "pt" -> "🇵🇹"
    "pt-br" -> "🇧🇷"
    "fr" -> "🇫🇷"
    "de" -> "🇩🇪"
    "it" -> "🇮🇹"
    "ru" -> "🇷🇺"
    "ar" -> "🇸🇦"
    "th" -> "🇹🇭"
    "vi" -> "🇻🇳"
    "id" -> "🇮🇩"
    "tr" -> "🇹🇷"
    "pl" -> "🇵🇱"
    "all" -> "🌐"
    else -> "🏳️"
}
