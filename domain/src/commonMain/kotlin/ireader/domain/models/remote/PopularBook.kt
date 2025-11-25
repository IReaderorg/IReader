package ireader.domain.models.remote

/**
 * Represents a popular book based on how many users are reading it
 */
data class PopularBook(
    val bookId: String,
    val title: String,
    val bookUrl: String,
    val sourceId: Long,
    val readerCount: Int,
    val lastRead: Long,
    val coverUrl: String? = null,
    val localBookId: Long? = null,
    val isInLibrary: Boolean = false
)
