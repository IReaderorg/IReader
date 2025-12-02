package ireader.domain.models

import ireader.domain.models.entities.BaseBook
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.HistoryWithRelations
import ireader.domain.models.entities.UpdatesWithRelations

/**
 * Data class representing book cover information for image loading.
 * Using data class ensures proper equality comparison, which is critical for
 * Compose's image caching - without it, different instances with the same values
 * would be treated as different keys, causing unnecessary image reloads.
 */
data class BookCover(
    val bookId: Long,
    val sourceId: Long,
    val cover: String?,
    val favorite: Boolean,
    val lastModified: Long = 0
) {

    companion object {

        fun from(book: Book): BookCover {
            return BookCover(book.id, book.sourceId, book.cover, book.favorite)
        }

        fun from(book: BaseBook): BookCover {
            return BookCover(book.id, book.sourceId, book.cover, book.favorite)
        }

        fun from(history: HistoryWithRelations): BookCover {
            return history.coverData
        }

        fun from(update: UpdatesWithRelations): BookCover {
            return update.coverData
        }
    }
}
