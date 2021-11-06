package ir.kazemcodes.infinity.domain.use_case.local

import ir.kazemcodes.infinity.domain.model.book.BookEntity
import ir.kazemcodes.infinity.domain.repository.LocalRepository

class DeleteBookUseCase(
    private val repository: LocalRepository
) {

    suspend operator fun invoke(bookEntity : BookEntity) {
        repository.deleteBook(bookEntity)
    }
}