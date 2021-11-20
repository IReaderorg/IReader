package ir.kazemcodes.infinity.library_feature.domain.use_case

import ir.kazemcodes.infinity.base_feature.repository.Repository
import ir.kazemcodes.infinity.library_feature.domain.model.BookEntity
import javax.inject.Inject

class DeleteLocalBookUseCase @Inject constructor(
    private val repository: Repository
) {

    suspend operator fun invoke(bookEntity : BookEntity) {
        repository.local.deleteBook(bookEntity)
    }
}