package ir.kazemcodes.infinity.core.domain.use_cases.local.insert_usecases

import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository

class InsertBooks(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke(books: List<Book>) {
        localBookRepository.insertBooks(books)
    }
}