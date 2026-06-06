package ireader.domain.models.remote

import ireader.domain.models.entities.SourceGroup

/**
 * Represents a popular book based on how many users are reading it
 */
data class PopularBook(
    val bookId: String,
    val title: String,
    val bookUrl: String,
    val sourceId: Long,
    val sourceName: String = "",
    val sourceGroup: SourceGroup? = null,
    val readerCount: Int,
    val lastRead: Long,
    val coverUrl: String? = null,
    val description: String? = null,
    val localBookId: Long? = null,
    val isInLibrary: Boolean = false
)
