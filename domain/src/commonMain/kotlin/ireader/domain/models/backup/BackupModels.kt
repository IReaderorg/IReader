package ireader.domain.models.backup

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.BookItem
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.Chapter
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * Complete backup data structure containing all user data
 */
@Serializable
data class BackupData(
    val novels: List<Book> = emptyList(),
    val chapters: List<Chapter> = emptyList(),
    val readingProgress: List<ReadingProgress> = emptyList(),
    val bookmarks: List<Bookmark> = emptyList(),
    // Note: categories excluded due to serialization issues - will be handled separately
    val settings: Map<String, String> = emptyMap(),
    val version: Int = CURRENT_VERSION
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

/**
 * Reading progress information for a book
 */
@Serializable
data class ReadingProgress(
    val bookId: Long,
    val chapterId: Long,
    val lastPageRead: Long,
    val lastReadTime: Long
)

/**
 * Bookmark information
 */
@Serializable
data class Bookmark(
    val bookId: Long,
    val chapterId: Long,
    val page: Long,
    val note: String = "",
    val timestamp: Long
)

/**
 * Information about a backup file
 */
@Serializable
data class BackupInfo(
    val id: String,
    val name: String,
    val timestamp: Long,
    val size: Long
)
