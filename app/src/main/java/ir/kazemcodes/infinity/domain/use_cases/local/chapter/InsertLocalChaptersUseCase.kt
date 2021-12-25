package ir.kazemcodes.infinity.domain.use_cases.local.chapter

import ir.kazemcodes.infinity.domain.models.ChapterEntity
import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.domain.utils.InvalidBookException
import timber.log.Timber
import javax.inject.Inject

class InsertLocalChaptersUseCase @Inject constructor(
    private val repository: Repository,
) {

    @Throws(InvalidBookException::class)
    suspend operator fun invoke(chapterEntity: List<ChapterEntity>) {
        Timber.d("Timber: InsertLocalChaptersUseCase was Called")
        repository.localChapterRepository.insertChapters(chapterEntity = chapterEntity)
        Timber.d("Timber: InsertLocalChaptersUseCase was Finished Successfully")
    }

}

