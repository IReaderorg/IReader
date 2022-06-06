package org.ireader.domain.use_cases.local.delete_usecases.book

import org.ireader.common_models.entities.Book
import javax.inject.Inject

/**
 * Delete All Books That are paged in Explore Screen
 */
class DeleteAllExploreBook @Inject constructor(private val bookRepository: org.ireader.common_data.repository.BookRepository) {
    suspend operator fun invoke() {
        return bookRepository.deleteAllExploreBook()
    }
}

class DeleteBooks @Inject constructor(private val bookRepository: org.ireader.common_data.repository.BookRepository) {
    suspend operator fun invoke(books: List<Book>) {
        bookRepository.deleteBooks(books)
    }
}

