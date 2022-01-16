package ir.kazemcodes.infinity.core.domain.use_cases.local.chapter

import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.repository.Repository
import timber.log.Timber
import javax.inject.Inject

class InsertLocalChaptersUseCase @Inject constructor(
    private val repository: Repository,
) {

    suspend operator fun invoke(chapters: List<Chapter>, book : Book, inLibrary : Boolean, source : Source) {
        try {
            Timber.d("Timber: InsertLocalChaptersUseCase was Called")
            repository.localChapterRepository.insertChapters(chapterEntity = chapters.map { it.copy(bookName = book.bookName,source=source.name, inLibrary = inLibrary).toChapterEntity() })
            Timber.d("Timber: InsertLocalChaptersUseCase was Finished Successfully")
        }catch (e : Exception) {
            Timber.e("Timber: InsertLocalChaptersUseCase: ${e.localizedMessage}")
        }

    }

}
class SetLastReadChaptersUseCase @Inject constructor(
    private val repository: Repository,
) {


    suspend operator fun invoke(book : Book, chapterTitle : String, source : Source) {
        Timber.d("Timber: SetLastReadChaptersUseCase was Called")
        repository.localChapterRepository.setLastReadChapter(bookName = book.bookName,source=source.name, chapterTitle = chapterTitle)
        Timber.d("Timber: SetLastReadChaptersUseCase was Finished Successfully")
    }

}

