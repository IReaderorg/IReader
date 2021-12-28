package ir.kazemcodes.infinity.domain.use_cases.local.chapter

import ir.kazemcodes.infinity.domain.models.local.ChapterEntity
import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.domain.utils.InvalidBookException
import timber.log.Timber
import javax.inject.Inject

class UpdateLocalChaptersContentUseCase @Inject constructor(
    private val repository: Repository,
) {

    @Throws(InvalidBookException::class)
    suspend operator fun invoke(chapterEntities: List<ChapterEntity>) =
        try {
            Timber.d("Timber: UpdateLocalChaptersContentUseCase was Called")
            repository.localChapterRepository.updateChapters(
                chapterEntities
            )
            Timber.d("Timber: UpdateLocalChaptersContentUseCase was Finished Successfully")
        } catch (e: Exception) {
            Timber.e("Timber: UpdateLocalChaptersContentUseCase: " + e.localizedMessage)
        }

}