package ir.kazemcodes.infinity.core.domain.use_cases.local.book

import ir.kazemcodes.infinity.core.domain.repository.Repository
import timber.log.Timber
import javax.inject.Inject

class DeleteLocalBookUseCase @Inject constructor(
    private val repository: Repository,
) {

    suspend operator fun invoke(bookName: String) {
        Timber.d("Timber: DeleteLocalBookUseCase was Called")
        repository.localBookRepository.deleteBook(bookName)
        Timber.d("Timber: DeleteLocalBookUseCase was Finished Successfully")
    }
}