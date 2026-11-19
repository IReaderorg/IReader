

package ireader.domain.models.entities

import androidx.compose.runtime.Immutable
import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.Page
import kotlinx.serialization.Serializable

/**
 * @param number it's number of current chapter
 */
@Immutable
@Serializable
data class Chapter(
    val id: Long = 0,
    val bookId: Long,
    val key: String,
    val name: String,
    val read: Boolean = false,
    val bookmark: Boolean = false,
    val dateUpload: Long = 0,
    val dateFetch: Long = 0,
    val sourceOrder: Long = 0,
    val content: List<Page> = emptyList(),
    val number: Float = -1f,
    val translator: String = "",
    val lastPageRead: Long = 0,
    val type: Long = ChapterInfo.NOVEL
) {

    val isRecognizedNumber get() = number >= 0
    
    /**
     * Check if the chapter has no actual text content.
     * This properly handles placeholder content (empty Text objects) from light queries,
     * as well as the DOWNLOADED_CHAPTER_PLACEHOLDER marker used by chapterMapperLight.
     */
    fun isEmpty(): Boolean {
        if (content.isEmpty()) return true
        
        // Extract actual text content from Text pages
        val textContent = content.mapNotNull { page ->
            when (page) {
                is ireader.core.source.model.Text -> page.text.takeIf { 
                    it.isNotBlank() && !it.contains("PLACEHOLDER_DO_NOT_DISPLAY_THIS_TEXT_TO_USER")
                }
                else -> null
            }
        }
        
        return textContent.isEmpty()
    }
}

fun Chapter.toChapterInfo(): ChapterInfo {
    return ChapterInfo(
        key = this.key,
        scanlator = this.translator,
        name = this.name,
        dateUpload = this.dateUpload,
        number = this.number,
    )
}

fun ChapterInfo.toChapter(bookId: Long): Chapter {
    return Chapter(
        name = name,
        key = key,
        bookId = bookId,
        number = number.sanitizedChapterNumber(),
        dateUpload = dateUpload,
        translator = this.scanlator,
        type =  this.type,

    )
}

/** Sources can return NaN/Infinity; SQLite binds NaN as NULL and the chapter
 * table declares chapter_number NOT NULL, which would abort the whole update. */
fun Float.sanitizedChapterNumber(): Float = if (isNaN() || isInfinite()) -1f else this

/**
 * Convert source chapters while preserving the source's order.
 *
 * [Chapter.sourceOrder] is the only stable ordering key in the database. Leaving
 * it at its default makes every chapter tie, so the list falls back to row
 * insertion order - which changes when a chapter is re-added by an update or a
 * backup restore.
 */
fun List<ChapterInfo>.toChapters(bookId: Long): List<Chapter> =
    mapIndexed { index, info -> info.toChapter(bookId).copy(sourceOrder = index.toLong()) }

/**
 * Whether two entries refer to the same logical chapter.
 *
 * Source keys are not stable: a site can change a chapter URL, and backups can
 * be restored with different keys. Matching on the key alone then inserts a
 * second row for a chapter the user already has. A parsed chapter number plus a
 * matching name is used as a fallback identity in that case.
 */
fun Chapter.isSameChapter(other: Chapter): Boolean {
    if (bookId != other.bookId) return false
    if (key.isNotBlank() && key == other.key) return true
    if (!isRecognizedNumber || !other.isRecognizedNumber) {
        return !isRecognizedNumber && !other.isRecognizedNumber && sameChapterName(name, other.name)
    }
    return number == other.number && sameChapterName(name, other.name)
}

private fun sameChapterName(a: String, b: String): Boolean {
    if (a.equals(b, ignoreCase = true)) return true
    val first = a.trim().lowercase()
    val second = b.trim().lowercase()
    return first.startsWith(second) || second.startsWith(first)
}

/**
 * Check if the chapter is locked/premium.
 * 
 * A chapter is considered locked if its name contains:
 * - Lock emoji (🔒)
 * - "locked" text (case-insensitive)
 */
fun Chapter.isLockedChapter(): Boolean {
    if (name.isEmpty()) return false
    
    // Check for lock emoji
    if (name.contains("🔒")) return true
    
    // Check for "locked" but not when part of "unlocked"
    val lowerName = name.lowercase()
    val lockedIndex = lowerName.indexOf("locked")
    if (lockedIndex == -1) return false
    
    // Check if preceded by "un" to exclude "unlocked"
    if (lockedIndex >= 2 && lowerName.substring(lockedIndex - 2, lockedIndex) == "un") {
        return false
    }
    
    return true
}
