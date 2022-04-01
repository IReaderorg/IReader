package org.ireader.domain.use_cases.local.book_usecases


import org.ireader.domain.models.SortType
import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.LocalBookRepository
import javax.inject.Inject


class FindAllInLibraryBooks @Inject constructor(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke(): List<Book> {
        return localBookRepository.findAllInLibraryBooks(sortType = SortType.LastRead,
            isAsc = false,
            unreadFilter = false)
    }
}