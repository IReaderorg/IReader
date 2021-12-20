package ir.kazemcodes.infinity.domain.local_feature.domain.use_case.book

import ir.kazemcodes.infinity.domain.repository.Repository
import timber.log.Timber
import javax.inject.Inject

class DeleteAllLocalBooksUseCase @Inject constructor(
    private val repository: Repository
) {

    suspend operator fun invoke() {
        Timber.d("Timber: DeleteAllLocalBooksUseCase was Called")
        repository.localBookRepository.deleteAllBook()
        Timber.d("Timber: DeleteAllLocalBooksUseCase was Finished Successfully")
    }
}