package ir.kazemcodes.infinity.core.domain.use_cases.local.book

import ir.kazemcodes.infinity.core.domain.repository.Repository
import timber.log.Timber
import javax.inject.Inject

class DeleteAllLocalBooksUseCase @Inject constructor(
    private val repository: Repository,
) {

    suspend operator fun invoke() {
        Timber.d("Timber: DeleteAllLocalBooksUseCase was Called")
        repository.localBookRepository.deleteAllBook()
        Timber.d("Timber: DeleteAllLocalBooksUseCase was Finished Successfully")
    }
}

class DeleteNotInLibraryLocalBooksUseCase @Inject constructor(
    private val repository: Repository,
) {

    suspend operator fun invoke() {
        Timber.d("Timber: DeleteNotInLibraryLocalBooksUseCase was Called")
        repository.localBookRepository.deleteNotInLibraryBooks()
        Timber.d("Timber: DeleteNotInLibraryLocalBooksUseCase was Finished Successfully")
    }
}