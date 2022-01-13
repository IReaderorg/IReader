package ir.kazemcodes.infinity.domain.use_cases.local.chapter

import ir.kazemcodes.infinity.domain.models.remote.Chapter
import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.util.InvalidBookException
import timber.log.Timber
import javax.inject.Inject

class UpdateLocalChaptersContentUseCase @Inject constructor(
    private val repository: Repository,
) {

    @Throws(InvalidBookException::class)
    suspend operator fun invoke(chapters: List<Chapter>) =
        try {
            Timber.d("Timber: UpdateLocalChaptersContentUseCase was Called")
            repository.localChapterRepository.updateChapters(
                chapters.map { it.toChapterEntity() }
            )
            Timber.d("Timber: UpdateLocalChaptersContentUseCase was Finished Successfully")
        } catch (e: Exception) {
            Timber.e("Timber: UpdateLocalChaptersContentUseCase: " + e.localizedMessage)
        }

}


class UpdateAddToLibraryChaptersContentUseCase @Inject constructor(
    private val repository: Repository,
) {

    @Throws(InvalidBookException::class)
    suspend operator fun invoke(
        chapter: Chapter
    ) =
        try {
            Timber.d("Timber: UpdateLocalChaptersContentUseCase was Called")
            repository.localChapterRepository.updateAddToLibraryChapters(chapter.title,
                chapter.source,
                chapter.bookName?:""
            )
            Timber.d("Timber: UpdateLocalChaptersContentUseCase was Finished Successfully")
        } catch (e: Exception) {
            Timber.e("Timber: UpdateLocalChaptersContentUseCase: " + e.localizedMessage)
        }

}