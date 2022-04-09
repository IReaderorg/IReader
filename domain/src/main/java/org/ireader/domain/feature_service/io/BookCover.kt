package org.ireader.domain.feature_service.io

import org.ireader.domain.models.entities.BaseBook
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.UpdateWithInfo

class BookCover(
    val id: Long,
    val sourceId: Long,
    val cover: String,
    val favorite: Boolean,
) {

    companion object {

        fun from(book: Book): BookCover {
            return BookCover(book.id, book.sourceId, book.cover, book.favorite)
        }

        fun from(book: BaseBook): BookCover {
            return BookCover(book.id, book.sourceId, book.cover, book.favorite)
        }

        fun from(history: HistoryWithRelations): BookCover {
            return BookCover(history.bookId, history.sourceId, history.cover, history.favorite)
        }

        fun from(update: UpdateWithInfo): BookCover {
            return BookCover(update.bookId, update.sourceId, update.cover, update.favorite)
        }
    }

}

data class HistoryWithRelations(
    val bookId: Long,
    val chapterId: Long,
    val readAt: Long,
    val bookTitle: String,
    val sourceId: Long,
    val cover: String,
    val favorite: Boolean,
    val chapterTitle: String,
    val chapterNumber: Int,
    val date: String,
)
