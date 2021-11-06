package ir.kazemcodes.infinity.domain.use_case.local

import ir.kazemcodes.infinity.domain.model.book.BookEntity
import ir.kazemcodes.infinity.domain.repository.LocalRepository
import ir.kazemcodes.infinity.domain.util.InvalidBookException

class AddBookUserCase(
    private val repository: LocalRepository
) {

    @Throws(InvalidBookException::class)
    suspend operator fun invoke(bookEntity: BookEntity) {
        if(bookEntity.name.isBlank()) {
            throw InvalidBookException("The Extension is out of data.")
        }

        repository.insertBook(bookEntity)
    }

}