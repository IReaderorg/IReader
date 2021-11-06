package ir.kazemcodes.infinity.domain.use_case.local

import ir.kazemcodes.infinity.domain.repository.LocalRepository

class GetBookUseCase (
    private val repository: LocalRepository
) {

    suspend operator fun invoke(id : Int) {
        repository.getBookById(id)
    }
}