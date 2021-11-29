package ir.kazemcodes.infinity.library_feature.domain.use_case.chapter

import ir.kazemcodes.infinity.base_feature.repository.Repository
import ir.kazemcodes.infinity.library_feature.domain.model.ChapterEntity
import ir.kazemcodes.infinity.library_feature.domain.util.InvalidBookException
import javax.inject.Inject

class InsertLocalChaptersUseCase @Inject constructor(
    private val repository: Repository
) {

    @Throws(InvalidBookException::class)
    suspend operator fun invoke(chapterEntity: List<ChapterEntity>) =
        repository.localChapterRepository.insertChapters(chapterEntity = chapterEntity)

}