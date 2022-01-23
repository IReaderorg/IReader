package ir.kazemcodes.infinity.core.domain.use_cases.local.book_usecases

import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.core.utils.Resource
import kotlinx.coroutines.flow.Flow

/**
 * return a book from id
 */
class GetBookById(private val localBookRepository: LocalBookRepository) {
    operator fun invoke(id: Int): Flow<Resource<Book>> {
        return localBookRepository.getBookById(id = id)
    }
}

