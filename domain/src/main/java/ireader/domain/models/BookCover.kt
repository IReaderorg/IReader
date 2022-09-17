package ireader.domain.models

import ireader.common.models.entities.BaseBook
import ireader.common.models.entities.Book
import ireader.common.models.entities.HistoryWithRelations
import ireader.common.models.entities.UpdatesWithRelations

class BookCover(
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
