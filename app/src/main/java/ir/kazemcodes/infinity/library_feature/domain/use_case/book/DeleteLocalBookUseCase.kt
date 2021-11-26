package ir.kazemcodes.infinity.library_feature.domain.use_case.book

import ir.kazemcodes.infinity.base_feature.repository.Repository
import ir.kazemcodes.infinity.library_feature.domain.model.BookEntity
import javax.inject.Inject

class DeleteLocalBookUseCase @Inject constructor(
    private val repository: Repository
) {

    suspend operator fun invoke(bookEntity : BookEntity) {
        bookEntity.bookId?.let { repository.localBookRepository.deleteBook(it) }
    }
}