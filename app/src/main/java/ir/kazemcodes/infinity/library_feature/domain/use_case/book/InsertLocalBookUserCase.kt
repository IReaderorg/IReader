package ir.kazemcodes.infinity.library_feature.domain.use_case.book

import ir.kazemcodes.infinity.base_feature.repository.Repository
import ir.kazemcodes.infinity.library_feature.domain.model.BookEntity
import ir.kazemcodes.infinity.library_feature.domain.util.InvalidBookException
import timber.log.Timber
import javax.inject.Inject

class InsertLocalBookUserCase @Inject constructor(
    private val repository: Repository
) {

    @Throws(InvalidBookException::class)
    suspend operator fun invoke(bookEntity: BookEntity)  {
        try {
            repository.localBookRepository.insertBook(bookEntity = bookEntity)
        }catch (e : Exception) {
            Timber.e("invoke: " + e.localizedMessage)
        }
    }



}