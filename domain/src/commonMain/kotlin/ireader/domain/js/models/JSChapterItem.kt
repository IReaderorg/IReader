package ireader.domain.js.models

import ireader.core.source.model.ChapterInfo
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Chapter item returned from JavaScript plugin.
 * Represents a single chapter in a novel.
 */
data class JSChapterItem(
    val name: String,
    val path: String,
    val chapterNumber: Int? = null,
    val releaseTime: String? = null
) {
    /**
     * Converts this JavaScript chapter item to IReader's ChapterInfo domain model.
     * Parses the release time using multiple date formats.
     */
    fun toChapterInfo(): ChapterInfo {
        return ChapterInfo(
            key = path,
            name = name,
            dateUpload = parseDate(releaseTime),
            number = chapterNumber?.toFloat() ?: -1f,
            scanlator = "",
            type = ChapterInfo.NOVEL
        )
    }
    
    /**
     * Parses a date string using multiple common formats.
     * Returns 0 if parsing fails or date is null.
     */
    private fun parseDate(dateString: String?): Long {
        if (dateString.isNullOrBlank()) return 0L
        
        val formats = listOf(
            "MMM dd, yyyy",           // Jan 15, 2024
            "yyyy-MM-dd",             // 2024-01-15
            "yyyy-MM-dd'T'HH:mm:ss",  // ISO 8601 without timezone
            "yyyy-MM-dd'T'HH:mm:ss'Z'", // ISO 8601 with Z
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", // ISO 8601 with milliseconds
            "dd/MM/yyyy",             // 15/01/2024
            "MM/dd/yyyy"              // 01/15/2024
        )
        
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.ENGLISH)
                sdf.isLenient = false
                return sdf.parse(dateString)?.time ?: 0L
            } catch (e: Exception) {
                // Try next format
            }
        }
        
        // Try parsing as Unix timestamp (seconds)
        try {
            val timestamp = dateString.toLongOrNull()
            if (timestamp != null) {
                // If it's a reasonable timestamp (after year 2000 and before year 2100)
                if (timestamp > 946684800 && timestamp < 4102444800) {
                    return timestamp * 1000 // Convert to milliseconds
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        
        return 0L
    }
}
