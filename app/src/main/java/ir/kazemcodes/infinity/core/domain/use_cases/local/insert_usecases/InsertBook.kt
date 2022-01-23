package ir.kazemcodes.infinity.core.domain.use_cases.local.insert_usecases

import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository

class InsertBook(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke(book: Book) {
        localBookRepository.insertBook(book)
    }
}