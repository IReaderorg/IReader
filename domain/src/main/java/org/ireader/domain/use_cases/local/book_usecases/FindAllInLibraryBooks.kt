package org.ireader.domain.use_cases.local.book_usecases

import org.ireader.common_models.entities.Book
import org.ireader.common_models.library.LibrarySort
import javax.inject.Inject

class FindAllInLibraryBooks @Inject constructor(private val bookRepository: org.ireader.common_data.repository.BookRepository) {
    suspend operator fun invoke(): List<Book> {
        return bookRepository.findAllInLibraryBooks(
            sortType = LibrarySort.default,
            isAsc = false,
            unreadFilter = false
        )
    }
}
