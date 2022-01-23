package ir.kazemcodes.infinity.core.domain.use_cases.local.delete_usecases.book

import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository

class DeleteBookById(private val localBookRepository: LocalBookRepository) {
    suspend operator fun invoke(id:Int) {
        return localBookRepository.deleteBookById(id)
    }
}