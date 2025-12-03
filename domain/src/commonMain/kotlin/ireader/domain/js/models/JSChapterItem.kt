package ireader.domain.js.models

import ireader.core.source.model.ChapterInfo
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlin.time.ExperimentalTime

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
     * Uses kotlinx-datetime for KMP compatibility.
     */
    private fun parseDate(dateString: String?): Long {
        if (dateString.isNullOrBlank()) return 0L
        
        // Try ISO 8601 formats first (most common in APIs)
        tryParseIso8601(dateString)?.let { return it }
        
        // Try yyyy-MM-dd format
        tryParseYearMonthDay(dateString)?.let { return it }
        
        // Try "MMM dd, yyyy" format (e.g., "Jan 15, 2024")
        tryParseMonthNameDayYear(dateString)?.let { return it }
        
        // Try dd/MM/yyyy or MM/dd/yyyy formats
        tryParseSlashFormat(dateString)?.let { return it }
        
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
    
    @OptIn(ExperimentalTime::class)
    private fun tryParseIso8601(dateString: String): Long? {
        return try {
            // Handle various ISO 8601 formats
            val cleanedString = dateString
                .replace("Z", "")
                .replace("z", "")
                .substringBefore(".")  // Remove milliseconds
            
            val dateTime = LocalDateTime.parse(cleanedString)
            dateTime.toInstant(TimeZone.UTC).toEpochMilliseconds()
        } catch (e: Exception) {
            null
        }
    }
    
    @OptIn(ExperimentalTime::class)
    private fun tryParseYearMonthDay(dateString: String): Long? {
        return try {
            val date = LocalDate.parse(dateString)
            date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
        } catch (e: Exception) {
            null
        }
    }
    
    @OptIn(ExperimentalTime::class)
    private fun tryParseMonthNameDayYear(dateString: String): Long? {
        val monthNames = mapOf(
            "jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4,
            "may" to 5, "jun" to 6, "jul" to 7, "aug" to 8,
            "sep" to 9, "oct" to 10, "nov" to 11, "dec" to 12
        )
        
        return try {
            // Pattern: "Jan 15, 2024" or "January 15, 2024"
            val parts = dateString.replace(",", "").split(" ").filter { it.isNotBlank() }
            if (parts.size != 3) return null
            
            val monthStr = parts[0].lowercase().take(3)
            val month = monthNames[monthStr] ?: return null
            val day = parts[1].toIntOrNull() ?: return null
            val year = parts[2].toIntOrNull() ?: return null
            
            val date = LocalDate(year, month, day)
            date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
        } catch (e: Exception) {
            null
        }
    }
    
    @OptIn(ExperimentalTime::class)
    private fun tryParseSlashFormat(dateString: String): Long? {
        return try {
            val parts = dateString.split("/")
            if (parts.size != 3) return null
            
            val first = parts[0].toIntOrNull() ?: return null
            val second = parts[1].toIntOrNull() ?: return null
            val third = parts[2].toIntOrNull() ?: return null
            
            // Try dd/MM/yyyy first (more common internationally)
            val (day, month, year) = if (first > 12) {
                Triple(first, second, third)
            } else if (second > 12) {
                Triple(second, first, third)
            } else {
                // Ambiguous, assume MM/dd/yyyy (US format)
                Triple(second, first, third)
            }
            
            val date = LocalDate(year, month, day)
            date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
        } catch (e: Exception) {
            null
        }
    }
}
