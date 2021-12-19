package ir.kazemcodes.infinity.local_feature.domain.use_case.chapter

import ir.kazemcodes.infinity.base_feature.repository.Repository
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