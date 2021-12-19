package ir.kazemcodes.infinity.local_feature.domain.use_case.chapter

import ir.kazemcodes.infinity.base_feature.repository.Repository
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter
import ir.kazemcodes.infinity.local_feature.domain.util.InvalidBookException
import timber.log.Timber
import javax.inject.Inject

class UpdateLocalChapterContentUseCase @Inject constructor(
    private val repository: Repository
) {

    @Throws(InvalidBookException::class)
    suspend operator fun invoke(chapter : Chapter) =
        try {
            Timber.d("Timber: UpdateLocalChapterContentUseCase was Called")
            repository.localChapterRepository.updateChapter(readingContent = chapter.content?:"" ,bookName = chapter.bookName ?:"" , chapterTitle = chapter.title)
            Timber.d("Timber: GetLocalBookByNameUseCase was Finished Successfully")
        } catch (e: Exception) {
            Timber.e("Timber: InsertLocalChapterContentUseCase: " + e.localizedMessage)
        }

}