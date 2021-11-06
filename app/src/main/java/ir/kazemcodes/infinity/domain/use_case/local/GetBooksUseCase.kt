package ir.kazemcodes.infinity.domain.use_case.local

import ir.kazemcodes.infinity.domain.repository.LocalRepository

class GetBooksUseCase (
    private val repository: LocalRepository
) {

    suspend operator fun invoke() {
        repository.getBooks()
    }
}