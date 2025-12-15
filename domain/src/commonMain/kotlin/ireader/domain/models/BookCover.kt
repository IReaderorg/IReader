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
 * 
 * The cover field prioritizes customCover over the default cover from source.
 * This ensures user-set custom covers are displayed and persisted across updates.
 */
data class BookCover(
    val bookId: Long,
    val sourceId: Long,
    val cover: String?,
    val favorite: Boolean,
    val lastModified: Long = 0,
    val hasCustomCover: Boolean = false
) {

    companion object {

        /**
         * Creates BookCover from Book, prioritizing customCover if set.
         * Custom cover is used when:
         * - customCover is not blank AND
         * - customCover is different from the default cover (indicating user set it)
         * 
         * The lastModified is set to book.lastUpdate to ensure cache invalidation
         * when the book is updated or custom cover is changed.
         */
        fun from(book: Book): BookCover {
            val useCustomCover = book.customCover.isNotBlank() && book.customCover != book.cover
            val effectiveCover = if (useCustomCover) book.customCover else book.cover
            return BookCover(
                bookId = book.id,
                sourceId = book.sourceId,
                cover = effectiveCover,
                favorite = book.favorite,
                lastModified = book.lastUpdate,
                hasCustomCover = useCustomCover
            )
        }

        /**
         * Creates BookCover from BaseBook, prioritizing customCover if set.
         */
        fun from(book: BaseBook): BookCover {
            val useCustomCover = book.customCover.isNotBlank() && book.customCover != book.cover
            val effectiveCover = if (useCustomCover) book.customCover else book.cover
            return BookCover(
                bookId = book.id,
                sourceId = book.sourceId,
                cover = effectiveCover,
                favorite = book.favorite,
                hasCustomCover = useCustomCover
            )
        }

        fun from(history: HistoryWithRelations): BookCover {
            return history.coverData
        }

        fun from(update: UpdatesWithRelations): BookCover {
            return update.coverData
        }

        /**
         * Creates BookCover from LibraryBook, prioritizing customCover if set.
         */
        fun from(book: ireader.domain.models.entities.LibraryBook): BookCover {
            val useCustomCover = book.customCover.isNotBlank() && book.customCover != book.cover
            val effectiveCover = if (useCustomCover) book.customCover else book.cover
            return BookCover(
                bookId = book.id,
                sourceId = book.sourceId,
                cover = effectiveCover,
                favorite = book.hasStarted,
                lastModified = book.lastUpdate,
                hasCustomCover = useCustomCover
            )
        }
    }
}
