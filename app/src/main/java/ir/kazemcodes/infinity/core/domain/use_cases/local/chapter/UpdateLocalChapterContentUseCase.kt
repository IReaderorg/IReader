package ir.kazemcodes.infinity.core.domain.use_cases.local.chapter

import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.repository.Repository
import timber.log.Timber
import javax.inject.Inject

class UpdateLocalChapterContentUseCase @Inject constructor(
    private val repository: Repository,
) {

    suspend operator fun invoke(chapter: Chapter) {
        try {
            Timber.d("Timber: UpdateLocalChapterContentUseCase was Called")
            repository.localChapterRepository.updateChapter(
                chapterEntity = chapter.toChapterEntity()
            )
            Timber.d("Timber: GetLocalBookByNameUseCase was Finished Successfully")
        } catch (e: Exception) {
            Timber.e("Timber: InsertLocalChapterContentUseCase: " + e.localizedMessage)
        }

    }
}