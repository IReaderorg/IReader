package ir.kazemcodes.infinity.core.domain.use_cases.local.book

import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.repository.Repository
import timber.log.Timber
import javax.inject.Inject

class InsertLocalBookUserCase @Inject constructor(
    private val repository: Repository,
) {

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