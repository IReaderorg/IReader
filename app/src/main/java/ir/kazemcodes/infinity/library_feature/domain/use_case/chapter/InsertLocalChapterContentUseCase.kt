package ir.kazemcodes.infinity.library_feature.domain.use_case.chapter

import ir.kazemcodes.infinity.base_feature.repository.Repository
import ir.kazemcodes.infinity.library_feature.domain.model.ChapterEntity
import ir.kazemcodes.infinity.library_feature.domain.util.InvalidBookException
import timber.log.Timber
import javax.inject.Inject

class InsertLocalChapterContentUseCase @Inject constructor(
    private val repository: Repository
) {

    @Throws(InvalidBookException::class)
    suspend operator fun invoke(chapterEntity: ChapterEntity) =
        try {

            repository.localChapterRepository.insertChapter(chapterEntity = chapterEntity)
        } catch (e: Exception) {
            Timber.e("InsertLocalChapterContentUseCase: " + e.localizedMessage)
        }

}