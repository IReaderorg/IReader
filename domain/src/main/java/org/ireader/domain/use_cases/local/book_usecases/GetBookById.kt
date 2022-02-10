package org.ireader.domain.use_cases.local.book_usecases

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.LocalBookRepository
import org.ireader.domain.utils.Resource

/**
 * return a book from id
 */
class GetBookById(private val localBookRepository: LocalBookRepository) {
    operator fun invoke(id: Int): Flow<Resource<Book>> {
        return localBookRepository.getBookById(id = id)
    }
}

class GetBookByIdDirectly(private val localBookRepository: LocalBookRepository) {
    operator fun invoke(id: Int): Flow<Book?> {
        return localBookRepository.getBookByIdDirectly(id = id)
    }
}


