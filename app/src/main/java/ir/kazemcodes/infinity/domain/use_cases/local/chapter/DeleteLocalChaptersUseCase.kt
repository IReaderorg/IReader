package ir.kazemcodes.infinity.domain.use_cases.local.chapter

import ir.kazemcodes.infinity.domain.repository.Repository
import timber.log.Timber
import javax.inject.Inject

class DeleteLocalChaptersUseCase @Inject constructor(
    private val repository: Repository
) {

    suspend operator fun invoke(bookName : String) {
        Timber.d("Timber: GetLocalBookByNameUseCase was Called")
        repository.localChapterRepository.deleteChapters(bookName)
        Timber.d("Timber: GetLocalBookByNameUseCase was Finished Successfully")
    }
}