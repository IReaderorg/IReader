package org.ireader.domain.use_cases.local.book_usecases

import org.ireader.common_models.SortType
import org.ireader.common_models.entities.Book
import javax.inject.Inject

class FindAllInLibraryBooks @Inject constructor(private val localBookRepository: org.ireader.common_data.repository.LocalBookRepository) {
    suspend operator fun invoke(): List<Book> {
        return localBookRepository.findAllInLibraryBooks(
            sortType = SortType.LastRead,
            isAsc = false,
            unreadFilter = false
        )
    }
}
