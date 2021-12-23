package ir.kazemcodes.infinity.domain.use_cases.local.book

import ir.kazemcodes.infinity.domain.models.BookEntity
import ir.kazemcodes.infinity.domain.utils.InvalidBookException
import ir.kazemcodes.infinity.domain.repository.Repository
import timber.log.Timber
import javax.inject.Inject

class InsertLocalBookUserCase @Inject constructor(
    private val repository: Repository
) {

    @Throws(InvalidBookException::class)
    suspend operator fun invoke(bookEntity: BookEntity)  {
        try {
            Timber.d("Timber: InsertLocalBookUserCase was Called")
            repository.localBookRepository.insertBook(bookEntity = bookEntity)
            Timber.d("Timber: InsertLocalBookUserCase was Finished Successfully")
        }catch (e : Exception) {
            Timber.e("invoke: " + e.localizedMessage)
        }
    }



}