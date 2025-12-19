package ireader.domain.models.entities

import kotlinx.serialization.Serializable

/**
 * Content filter pattern entity.
 * 
 * Stores regex patterns to remove unwanted text from chapter content.
 * Patterns can be:
 * - Global (bookId = null): Applied to all books
 * - Book-specific (bookId = someId): Applied only to that book
 * - Preset (isPreset = true): Built-in patterns that can't be deleted
 */
@Serializable
data class ContentFilter(
    val id: Long = 0,
    val bookId: Long? = null, // null for global patterns
    val name: String,
    val pattern: String,
    val description: String? = null,
    val enabled: Boolean = true,
    val isPreset: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
) {
    companion object {
        /**
         * Common preset patterns that users might want
         */
        val PRESETS = listOf(
            ContentFilter(
                name = "Navigation Hints",
                pattern = "Use arrow keys.*chapter|Press.*to.*chapter|Click.*next.*chapter",
                description = "Removes keyboard/mouse navigation hints",
                isPreset = true,
                createdAt = 0,
                updatedAt = 0
            ),
            ContentFilter(
                name = "Website Promotions",
                pattern = "Read more at.*|Visit.*for more.*|Support us at.*|Join our.*discord",
                description = "Removes website promotion text",
                isPreset = true,
                createdAt = 0,
                updatedAt = 0
            ),
            ContentFilter(
                name = "Translator Notes (Brackets)",
                pattern = "\\[TL:.*?\\]|\\[TN:.*?\\]|\\[Note:.*?\\]",
                description = "Removes translator notes in brackets",
                isPreset = true,
                createdAt = 0,
                updatedAt = 0
            ),
            ContentFilter(
                name = "Chapter Watermarks",
                pattern = "This chapter.*uploaded.*|Stolen from.*|If you.*reading this.*",
                description = "Removes anti-piracy watermarks",
                isPreset = true,
                createdAt = 0,
                updatedAt = 0
            )
        )
    }
}

/**
 * Export format for content filters
 */
@Serializable
data class ContentFilterExport(
    val filters: List<ContentFilter>,
    val exportedAt: Long
)
