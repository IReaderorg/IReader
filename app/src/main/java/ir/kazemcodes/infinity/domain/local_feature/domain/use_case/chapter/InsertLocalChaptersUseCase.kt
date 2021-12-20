package ir.kazemcodes.infinity.domain.local_feature.domain.use_case.chapter

import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.domain.local_feature.domain.model.ChapterEntity
import ir.kazemcodes.infinity.domain.local_feature.domain.util.InvalidBookException
import timber.log.Timber
import javax.inject.Inject
class InsertLocalChaptersUseCase @Inject constructor(
    private val repository: Repository
) {

    @Throws(InvalidBookException::class)
    suspend operator fun invoke(chapterEntity: List<ChapterEntity>) {
        Timber.d("Timber: InsertLocalChaptersUseCase was Called")
        repository.localChapterRepository.insertChapters(chapterEntity = chapterEntity)
        Timber.d("Timber: InsertLocalChaptersUseCase was Finished Successfully")
    }

}

