package ir.kazemcodes.infinity.domain.use_case.local

import ir.kazemcodes.infinity.domain.repository.LocalRepository

class DeleteAllBooksUseCase (
    private val repository: LocalRepository
) {

    suspend operator fun invoke() {
        repository.deleteAllBook()
    }
}