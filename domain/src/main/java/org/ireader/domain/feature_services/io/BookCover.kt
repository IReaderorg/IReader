package org.ireader.domain.feature_services.io

import org.ireader.domain.models.entities.Book

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

        fun from(history: HistoryWithRelations): BookCover {
            return BookCover(history.bookId, history.sourceId, history.cover, history.favorite)
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
    val date: String,
)
