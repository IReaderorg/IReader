package ir.kazemcodes.infinity.domain.use_cases.local.book

import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.util.InvalidBookException
import timber.log.Timber
import javax.inject.Inject

class InsertLocalBookUserCase @Inject constructor(
    private val repository: Repository,
) {

    @Throws(InvalidBookException::class)
    suspend operator fun invoke(book : Book) {
        try {
            Timber.d("Timber: InsertLocalBookUserCase was Called")
            repository.localBookRepository.insertBook(book.toBookEntity())
            Timber.d("Timber: InsertLocalBookUserCase was Finished Successfully")
        } catch (e: Exception) {
            Timber.e("invoke: " + e.localizedMessage)
        }
    }


}
class UpdateLocalBookUserCase @Inject constructor(
    private val repository: Repository,
) {

    @Throws(InvalidBookException::class)
    suspend operator fun invoke(book : Book) {
        try {
            Timber.d("Timber: InsertLocalBookUserCase was Called")
            repository.localBookRepository.updateBook(book.toBookEntity())
            Timber.d("Timber: InsertLocalBookUserCase was Finished Successfully")
        } catch (e: Exception) {
            Timber.e("invoke: " + e.localizedMessage)
        }
    }


}