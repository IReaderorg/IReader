package ir.kazemcodes.infinity.library_feature.domain.use_case.book

import ir.kazemcodes.infinity.base_feature.repository.Repository
import javax.inject.Inject

class DeleteAllLocalBooksUseCase @Inject constructor(
    private val repository: Repository
) {

    suspend operator fun invoke() {
        repository.localBookRepository.deleteAllBook()
    }
}