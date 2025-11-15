package ireader.domain.preferences

/**
 * Extension properties for general preferences related to UI enhancements.
 * 
 * This file contains preference extensions for:
 * - Library settings (FAB usage, chapter sorting)
 * - Global search behavior
 * - Auto-download capabilities
 * - UI customization (haptic feedback, loading animations)
 * 
 * Note: The actual preference methods are defined in UiPreferences class.
 * This file contains the ChapterSort enum used by those preferences.
 */

/**
 * Enum representing chapter sort options.
 */
enum class ChapterSort {
    SOURCE_ORDER,
    CHAPTER_NUMBER,
    UPLOAD_DATE_ASC,
    UPLOAD_DATE_DESC;
    
    companion object {
        /**
         * Converts a string preference value to ChapterSort enum.
         * Returns SOURCE_ORDER if the string doesn't match any enum value.
         */
        fun fromString(value: String): ChapterSort {
            return try {
                valueOf(value)
            } catch (e: IllegalArgumentException) {
                SOURCE_ORDER
            }
        }
    }
    
    /**
     * Returns a human-readable display name for the sort option.
     */
    fun displayName(): String {
        return when (this) {
            SOURCE_ORDER -> "By Source Order"
            CHAPTER_NUMBER -> "By Chapter Number"
            UPLOAD_DATE_ASC -> "By Upload Date (Ascending)"
            UPLOAD_DATE_DESC -> "By Upload Date (Descending)"
        }
    }
}
