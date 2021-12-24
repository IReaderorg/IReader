package ir.kazemcodes.infinity.domain.use_cases.local.chapter

import ir.kazemcodes.infinity.domain.models.Chapter
import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.domain.utils.InvalidBookException
import timber.log.Timber
import javax.inject.Inject

class UpdateLocalChapterContentUseCase @Inject constructor(
    private val repository: Repository
) {

    @Throws(InvalidBookException::class)
    suspend operator fun invoke(chapter : Chapter) =
        try {
            Timber.d("Timber: UpdateLocalChapterContentUseCase was Called")
            repository.localChapterRepository.updateChapter(
                readingContent = chapter.content ?: "",
                bookName = chapter.bookName ?: "",
                chapterTitle = chapter.title,
                haveBeenRead = chapter.haveBeenRead ?: false,
                lastRead = chapter.lastRead?:false
            )
            Timber.d("Timber: GetLocalBookByNameUseCase was Finished Successfully")
        } catch (e: Exception) {
            Timber.e("Timber: InsertLocalChapterContentUseCase: " + e.localizedMessage)
        }

}